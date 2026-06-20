@echo off
echo ====================================================
echo Starting Event Management System (Backend + Frontend)
echo ====================================================

:: Navigate to backend folder
cd backend

:: Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Maven is not installed or not added to your PATH!
    echo Please install Maven to run the Java backend.
    pause
    exit /b
)

:: Compile and run the backend
echo Compiling and starting the server via Maven...
echo The Frontend will be served automatically at http://localhost:7070/index.html
echo.

:: Open the browser after a short delay (ping localhost for ~3 seconds as a hacky wait)
start /b cmd /c "ping localhost -n 4 >nul && start http://localhost:7070/index.html"

:: Run Maven
mvn clean compile exec:java

pause
