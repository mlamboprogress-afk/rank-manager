@echo off
setlocal

set "PROJECT_ROOT=%~dp0"
set "JAVA_HOME=%PROJECT_ROOT%libraries\java\jdk\jdk-27.0.1.1"
set "MAVEN_HOME=%PROJECT_ROOT%libraries\maven\apache-maven-3.10.1"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JDK not found at %JAVA_HOME%
    exit /b 1
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [ERROR] Maven not found at %MAVEN_HOME%
    exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%