@echo off
setlocal
cd /d "%~dp0"
java -jar CampusICT.jar
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%