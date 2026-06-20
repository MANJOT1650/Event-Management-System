package com.ems.services;

import com.ems.repositories.RegistrationRepository;

public class RegistrationService {
    private RegistrationRepository registrationRepository;

    public RegistrationService() {
        this.registrationRepository = new RegistrationRepository();
    }

    public boolean registerParticipant(int eventId, int participantId) {
        return registrationRepository.registerParticipant(eventId, participantId);
    }

    public boolean cancelRegistration(int eventId, int participantId) {
        return registrationRepository.cancelRegistration(eventId, participantId);
    }
}
