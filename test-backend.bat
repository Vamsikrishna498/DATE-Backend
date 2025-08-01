@echo off
echo Testing backend connection...
timeout /t 5 /nobreak >nul
curl -X GET http://localhost:8081/api/super-admin/health
if %errorlevel% equ 0 (
    echo Backend is running successfully!
) else (
    echo Backend is not responding. Checking if process is running...
    netstat -an | findstr :8081
)
pause 