@echo off
echo ========================================
echo       PAC-MAN - JavaFX Game
echo ========================================
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo Warning: JAVA_HOME is not set. Using system Java.
    set JAVA_CMD=java
    set JAVAC_CMD=javac
) else (
    set JAVA_CMD="%JAVA_HOME%\bin\java"
    set JAVAC_CMD="%JAVA_HOME%\bin\javac"
)

REM Path to JavaFX
set PATH_TO_FX=C:\Nouveau dossier (2)\Nouveau dossier (3)\openjfx-17.0.17_windows-x64_bin-sdk\javafx-sdk-17.0.17\lib

echo Using JavaFX from: %PATH_TO_FX%
echo.

REM Create output directory
if not exist "out" mkdir out

echo Compiling...
%JAVAC_CMD% --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.graphics -d out src\module-info.java src\pacman\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Starting PAC-MAN...
echo.

REM Run the game
%JAVA_CMD% --module-path "%PATH_TO_FX%;out" --add-modules javafx.controls,javafx.graphics -m pacman/pacman.Main

pause
