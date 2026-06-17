@echo off
setlocal
set ANDROID_HOME=D:\app-code\Android\Sdk
set JAVA_HOME=C:\Java\jdk17

echo ========================================
echo  Fukuoka Airport Board - APK Builder
echo ========================================
echo.
echo [1/3] Building web assets...
cd /d "%~dp0"
call npx vite build
if %errorlevel% neq 0 (
    echo ERROR: Vite build failed!
    pause
    exit /b 1
)
echo.
echo [2/3] Syncing Capacitor...
call npx cap sync android
if %errorlevel% neq 0 (
    echo ERROR: Capacitor sync failed!
    pause
    exit /b 1
)
echo.
echo [3/3] Building debug APK with Gradle...
cd /d "%~dp0android"
call gradlew.bat assembleDebug
echo.
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ========================================
    echo  SUCCESS! APK location:
    echo  app\build\outputs\apk\debug\app-debug.apk
    echo ========================================
    copy "app\build\outputs\apk\debug\app-debug.apk" "%~dp0FukuokaAirportBoard.apk" >nul
    echo  Also copied to: FukuokaAirportBoard.apk
) else (
    echo ========================================
    echo  BUILD FAILED. Check errors above.
    echo ========================================
)
echo.
pause
