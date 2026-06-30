@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem TNYX / Tio-hub debug installer for Android phone and Wear OS.
rem Run from repository root. Android Studio does not need to be open.

set "ROOT_DIR=%~dp0"
set "APPS_DIR=%ROOT_DIR%apps"
set "GRADLEW=%APPS_DIR%\gradlew.bat"
set "DEFAULT_JBR=C:\Program Files\Android\Android Studio\jbr"
set "DEFAULT_ADB_DIR=%LOCALAPPDATA%\Android\Sdk\platform-tools"

if exist "%DEFAULT_ADB_DIR%\adb.exe" (
    set "PATH=%DEFAULT_ADB_DIR%;%PATH%"
)

if not exist "%GRADLEW%" (
    echo.
    echo ERROR: Gradle wrapper not found at:
    echo %GRADLEW%
    echo.
    echo Run this script from the Tio-hub repository root.
    exit /b 1
)

if not defined JAVA_HOME (
    if exist "%DEFAULT_JBR%\bin\java.exe" (
        set "JAVA_HOME=%DEFAULT_JBR%"
    )
) else (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        if exist "%DEFAULT_JBR%\bin\java.exe" (
            echo JAVA_HOME is invalid. Using Android Studio bundled JBR.
            set "JAVA_HOME=%DEFAULT_JBR%"
        )
    )
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo.
    echo ERROR: Java not found.
    echo Install JDK 21 or Android Studio, then set JAVA_HOME.
    echo Expected fallback:
    echo %DEFAULT_JBR%
    exit /b 1
)

call :print_header
call :print_devices

if "%~1"=="" goto menu
set "CHOICE=%~1"
goto route_choice

:menu
echo.
echo Select install target:
echo   1. Mobile app only      ^(:app:installDebug^)
echo   2. Watch app only       ^(:wear:installDebug^)
echo   3. Mobile + Watch       ^(:app:installDebug then :wear:installDebug^)
echo   4. Build only           ^(:app:assembleDebug + :wear:assembleDebug^)
echo   5. Clean mobile install ^(uninstall com.tnyx, then install^)
echo   0. Exit
echo.
set /p "CHOICE=Enter choice: "

:route_choice
if /I "%CHOICE%"=="mobile" set "CHOICE=1"
if /I "%CHOICE%"=="phone" set "CHOICE=1"
if /I "%CHOICE%"=="watch" set "CHOICE=2"
if /I "%CHOICE%"=="wear" set "CHOICE=2"
if /I "%CHOICE%"=="both" set "CHOICE=3"
if /I "%CHOICE%"=="all" set "CHOICE=3"
if /I "%CHOICE%"=="build" set "CHOICE=4"
if /I "%CHOICE%"=="clean-mobile" set "CHOICE=5"

if "%CHOICE%"=="1" (
    call :install_mobile
    exit /b !ERRORLEVEL!
)

if "%CHOICE%"=="2" (
    call :install_watch
    exit /b !ERRORLEVEL!
)

if "%CHOICE%"=="3" (
    call :install_mobile
    if errorlevel 1 exit /b 1
    call :install_watch
    exit /b !ERRORLEVEL!
)

if "%CHOICE%"=="4" (
    call :gradle :app:assembleDebug :wear:assembleDebug
    exit /b !ERRORLEVEL!
)

if "%CHOICE%"=="5" (
    call :clean_install_mobile
    exit /b !ERRORLEVEL!
)

if "%CHOICE%"=="0" exit /b 0

echo.
echo Invalid choice: %CHOICE%
exit /b 1

:install_mobile
echo.
echo === Installing mobile app ===
call :ask_serial "Phone/mobile device serial (blank = Gradle default)"
call :gradle :app:installDebug
exit /b !ERRORLEVEL!

:clean_install_mobile
echo.
echo === Clean installing mobile app ===
call :ask_serial "Phone/mobile device serial (blank = Gradle default)"
echo.
echo WARNING: This will uninstall existing package com.tnyx and delete its local app data.
set /p "CONFIRM=Type UNINSTALL to continue: "
if not "%CONFIRM%"=="UNINSTALL" (
    echo Clean install cancelled.
    exit /b 1
)
call :adb uninstall com.tnyx
call :gradle :app:installDebug
exit /b !ERRORLEVEL!

:install_watch
echo.
echo === Installing Wear OS app ===
call :ask_serial "Watch device serial (blank = Gradle default)"
call :gradle :wear:installDebug
exit /b !ERRORLEVEL!

:ask_serial
echo.
if defined ANDROID_SERIAL (
    echo Current ANDROID_SERIAL=!ANDROID_SERIAL!
)
set /p "ANDROID_SERIAL=%~1: "
if defined ANDROID_SERIAL (
    echo Using ANDROID_SERIAL=!ANDROID_SERIAL!
) else (
    echo Using Gradle/ADB default device.
)
exit /b 0

:adb
where adb >nul 2>nul
if errorlevel 1 (
    echo adb not found. Install Android SDK platform-tools or add adb to PATH.
    exit /b 1
)
echo.
echo Running: adb %*
adb %*
exit /b !ERRORLEVEL!

:gradle
pushd "%APPS_DIR%" >nul
echo.
echo Running: %GRADLEW% %*
call "%GRADLEW%" %*
set "RESULT=%ERRORLEVEL%"
popd >nul
if not "%RESULT%"=="0" (
    echo.
    echo Gradle command failed with exit code %RESULT%.
)
exit /b %RESULT%

:print_header
echo.
echo ==============================================
echo  TNYX Debug Installer
echo ==============================================
echo Repo: %ROOT_DIR%
echo Java: %JAVA_HOME%
echo.
echo Usage:
echo   INSTALL_APPS.bat
echo   INSTALL_APPS.bat mobile
echo   INSTALL_APPS.bat watch
echo   INSTALL_APPS.bat both
echo   INSTALL_APPS.bat build
echo   INSTALL_APPS.bat clean-mobile
exit /b 0

:print_devices
echo.
echo Connected ADB devices:
where adb >nul 2>nul
if errorlevel 1 (
    echo adb not found in PATH. Gradle may still find SDK adb if Android SDK is configured.
    exit /b 0
)
adb devices
exit /b 0
