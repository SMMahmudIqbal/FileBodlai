@echo off
title 1-Click Deploy File Bodlai to Vercel
cd /d "%~dp0"

echo =======================================================
echo    1-CLICK DEPLOY: File Bodlai Web to Vercel
echo =======================================================
echo.
echo 1. If you are not logged into Vercel, a browser will open.
echo    Click "Continue with GitHub" to approve.
echo 2. Vercel will automatically build and publish your site!
echo.
call npx vercel --yes --prod
echo.
if %ERRORLEVEL% equ 0 (
    echo =======================================================
    echo  SUCCESS! Your website is live worldwide on Vercel!
    echo =======================================================
) else (
    echo =======================================================
    echo  If it prompted for login, finish logging in in your
    echo  browser and run this file once more.
    echo =======================================================
)
echo.
pause
