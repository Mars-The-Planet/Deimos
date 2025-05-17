@echo off
setlocal enabledelayedexpansion

set JAVA_VER=
for /f "tokens=1,2 delims==" %%a in (gradle.properties) do (
    if /i "%%a"=="java_version" set JAVA_VER=%%b
)

set JAVA_VER=%JAVA_VER: =%

if "%JAVA_VER%"=="21" (
    set "JAVA_HOME=%Java_21%"
) else if "%JAVA_VER%"=="17" (
    set "JAVA_HOME=%Java_17%"
) else (
    echo Unsupported java_version '%JAVA_VER%' in gradle.properties. Must be 17 or 21.
    pause
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java %JAVA_VER% from %JAVA_HOME%

set "TARGET_DIR=%BuildsOutput%"

if not exist "%TARGET_DIR%" (
    mkdir "%TARGET_DIR%"
)

for %%L in (forge neoforge fabric) do (
    echo Building %%L...
    call gradlew :%%L:build

    for %%F in (%%L\build\libs\*.jar) do (
        echo %%~nxF | findstr /i /v "sources" | findstr /i /v "javadoc" | findstr /i /v "dev" >nul
        if !errorlevel! == 0 (
            echo Moving %%F to %TARGET_DIR%\%%~nxF
            move /Y "%%F" "%TARGET_DIR%\%%~nxF" >nul
        )
    )

    echo Cleaning %%L\build\libs...
    rmdir /s /q %%L\build\libs
)

echo Build complete. Jars are in %TARGET_DIR%.