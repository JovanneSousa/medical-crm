@echo off
echo =========================================================
echo    OmniMed Care - Plataforma Omnichannel de Atendimento
echo =========================================================
echo.
echo [1/2] Iniciando Backend Spring Boot (Java 21)...
start "OmniMed - Backend" cmd /k "cd backend && mvn spring-boot:run"

echo [2/2] Iniciando Frontend React (Vite)...
start "OmniMed - Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo Tudo pronto!
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo H2 Console: http://localhost:8080/h2-console
echo =========================================================
pause
