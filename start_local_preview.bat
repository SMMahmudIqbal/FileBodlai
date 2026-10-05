@echo off
title File Bodlai Web - Local Preview
cd /d "%~dp0"

echo =======================================================
echo    Starting File Bodlai Web locally...
echo =======================================================
echo.
start http://localhost:5173
call npm run dev
pause
