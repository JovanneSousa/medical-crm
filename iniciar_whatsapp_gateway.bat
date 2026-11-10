@echo off
echo =========================================================
echo   OmniMed Care - Gateway WhatsApp (Evolution API)
echo =========================================================
echo.
echo [1/2] Iniciando containers Docker (Evolution API + Postgres)...
docker-compose up -d

echo.
echo [2/2] Verificando status dos containers...
docker-compose ps

echo.
echo =========================================================
echo Gateway WhatsApp ativo na porta: http://localhost:8081
echo Backend OmniMed integrado:       http://localhost:8080
echo.
echo Abra o OmniMed na aba "Simulador / Bot Demo" e clique em:
echo "Conectar / Ler QR Code" para vincular o WhatsApp!
echo =========================================================
pause
