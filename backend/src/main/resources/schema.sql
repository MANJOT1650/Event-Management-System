-- 1. Tables and 3NF Normalization

CREATE TABLE Roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO Roles (role_name) VALUES ('ADMIN'), ('COORDINATOR'), ('PARTICIPANT');

CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL REFERENCES Roles(role_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE UserSettings (
    user_id INT PRIMARY KEY REFERENCES Users(user_id) ON DELETE CASCADE,
    theme VARCHAR(20) DEFAULT 'LIGHT',
    notifications_enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE Venues (
    venue_id SERIAL PRIMARY KEY,
    venue_name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    description TEXT
);

CREATE TABLE Categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE Events (
    event_id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INT NOT NULL REFERENCES Categories(category_id),
    venue_id INT NOT NULL REFERENCES Venues(venue_id),
    image_url VARCHAR(255),
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    current_registrations INT DEFAULT 0 CHECK (current_registrations >= 0 AND current_registrations <= capacity),
    status VARCHAR(20) DEFAULT 'LIVE' CHECK (status IN ('LIVE', 'PENDING_END', 'ENDED')),
    created_by INT NOT NULL REFERENCES Users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_time CHECK (start_time < end_time)
);

CREATE TABLE Registrations (
    registration_id SERIAL PRIMARY KEY,
    event_id INT NOT NULL REFERENCES Events(event_id) ON DELETE CASCADE,
    participant_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, participant_id) -- Prevent duplicate registrations
);

CREATE TABLE Waitlist (
    waitlist_id SERIAL PRIMARY KEY,
    event_id INT NOT NULL REFERENCES Events(event_id) ON DELETE CASCADE,
    participant_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, participant_id)
);

CREATE TABLE Attendance (
    attendance_id SERIAL PRIMARY KEY,
    event_id INT NOT NULL REFERENCES Events(event_id) ON DELETE CASCADE,
    participant_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    status VARCHAR(20) CHECK (status IN ('PRESENT', 'ABSENT')),
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, participant_id)
);

CREATE TABLE Notifications (
    notification_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE AuditLogs (
    log_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES Users(user_id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Note: EventHistory is a concept often better served by a VIEW, as it combines Registrations and Events.
-- We will create a view for it instead of a physical table to prevent data redundancy.


-- ==========================================
-- 2. Indexing
-- ==========================================

CREATE INDEX idx_events_title ON Events(title);
CREATE INDEX idx_events_date ON Events(event_date);
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_registrations_participant ON Registrations(participant_id);


-- ==========================================
-- 3. Triggers and Trigger Functions
-- ==========================================

-- Trigger: Increase currentRegistrations after registration
CREATE OR REPLACE FUNCTION after_registration_insert()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Events SET current_registrations = current_registrations + 1 WHERE event_id = NEW.event_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_registration_insert
AFTER INSERT ON Registrations
FOR EACH ROW EXECUTE FUNCTION after_registration_insert();

-- Trigger: Decrease currentRegistrations after cancellation
CREATE OR REPLACE FUNCTION after_registration_delete()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Events SET current_registrations = current_registrations - 1 WHERE event_id = OLD.event_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_registration_delete
AFTER DELETE ON Registrations
FOR EACH ROW EXECUTE FUNCTION after_registration_delete();

-- Audit Triggers (Example for Events)
CREATE OR REPLACE FUNCTION audit_events()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO AuditLogs (user_id, action, table_name, record_id) VALUES (NEW.created_by, 'CREATE_EVENT', 'Events', NEW.event_id);
    ELSIF TG_OP = 'UPDATE' THEN
        -- Basic assumption: updating user is not strictly tracked here unless we pass it, but let's log the event update
        INSERT INTO AuditLogs (user_id, action, table_name, record_id) VALUES (NEW.created_by, 'UPDATE_EVENT', 'Events', NEW.event_id);
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO AuditLogs (user_id, action, table_name, record_id) VALUES (OLD.created_by, 'DELETE_EVENT', 'Events', OLD.event_id);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_events
AFTER INSERT OR UPDATE OR DELETE ON Events
FOR EACH ROW EXECUTE FUNCTION audit_events();


-- ==========================================
-- 4. Stored Procedures
-- ==========================================

-- Procedure: registerParticipant()
CREATE OR REPLACE PROCEDURE registerParticipant(p_event_id INT, p_participant_id INT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_capacity INT;
    v_current_registrations INT;
BEGIN
    SELECT capacity, current_registrations INTO v_capacity, v_current_registrations 
    FROM Events WHERE event_id = p_event_id FOR UPDATE; -- Lock row

    IF v_current_registrations < v_capacity THEN
        -- Register directly
        INSERT INTO Registrations (event_id, participant_id) VALUES (p_event_id, p_participant_id);
        INSERT INTO Notifications (user_id, message, type) VALUES (p_participant_id, 'Successfully registered for event ' || p_event_id, 'REGISTRATION_SUCCESS');
    ELSE
        -- Add to waitlist
        INSERT INTO Waitlist (event_id, participant_id) VALUES (p_event_id, p_participant_id);
        INSERT INTO Notifications (user_id, message, type) VALUES (p_participant_id, 'Added to waitlist for event ' || p_event_id, 'WAITLIST_JOIN');
    END IF;
END;
$$;

-- Procedure: cancelRegistration() and promote waitlist
CREATE OR REPLACE PROCEDURE cancelRegistration(p_event_id INT, p_participant_id INT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_waitlist_user_id INT;
BEGIN
    -- Delete registration
    DELETE FROM Registrations WHERE event_id = p_event_id AND participant_id = p_participant_id;
    INSERT INTO Notifications (user_id, message, type) VALUES (p_participant_id, 'Registration cancelled for event ' || p_event_id, 'CANCELLATION');

    -- Promote from waitlist if someone is waiting
    SELECT participant_id INTO v_waitlist_user_id 
    FROM Waitlist 
    WHERE event_id = p_event_id 
    ORDER BY joined_at ASC LIMIT 1;

    IF v_waitlist_user_id IS NOT NULL THEN
        -- Remove from waitlist
        DELETE FROM Waitlist WHERE event_id = p_event_id AND participant_id = v_waitlist_user_id;
        -- Register
        INSERT INTO Registrations (event_id, participant_id) VALUES (p_event_id, v_waitlist_user_id);
        -- Notify
        INSERT INTO Notifications (user_id, message, type) VALUES (v_waitlist_user_id, 'Promoted from waitlist for event ' || p_event_id, 'WAITLIST_PROMOTION');
    END IF;
END;
$$;

-- Procedure: endEvent()
CREATE OR REPLACE PROCEDURE endEvent(p_event_id INT)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE Events SET status = 'ENDED' WHERE event_id = p_event_id;
    INSERT INTO AuditLogs (action, table_name, record_id) VALUES ('END_EVENT', 'Events', p_event_id);
END;
$$;


-- ==========================================
-- 5. Views
-- ==========================================

CREATE OR REPLACE VIEW LiveEvents_View AS
SELECT e.event_id, e.title, e.description, e.event_date, e.start_time, e.end_time, e.capacity, e.current_registrations, 
       c.category_name, v.venue_name, v.location
FROM Events e
JOIN Categories c ON e.category_id = c.category_id
JOIN Venues v ON e.venue_id = v.venue_id
WHERE e.status = 'LIVE';

CREATE OR REPLACE VIEW ParticipantHistory_View AS
SELECT r.participant_id, e.event_id, e.title, e.event_date, e.status as event_status, 
       r.registered_at, a.status as attendance_status
FROM Registrations r
JOIN Events e ON r.event_id = e.event_id
LEFT JOIN Attendance a ON r.event_id = a.event_id AND r.participant_id = a.participant_id;

CREATE OR REPLACE VIEW EventStatistics_View AS
SELECT e.event_id, e.title, e.capacity, e.current_registrations,
       (e.capacity - e.current_registrations) AS available_seats,
       ROUND((e.current_registrations::DECIMAL / e.capacity) * 100, 2) AS occupancy_percentage
FROM Events e;
