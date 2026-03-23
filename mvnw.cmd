@echo off
REM Maven Wrapper Windows — utilise le Maven bundlé avec IntelliJ IDEA
setlocal

REM Maven 3.9.9 inclus dans IntelliJ (déjà présent sur cette machine)
set "MVN_BIN=%APPDATA%\JetBrains\IntelliJIdea2025.1\plugins\maven\lib\maven3\bin\mvn.cmd"

REM Java 17 requis par Quarkus 3.15
set "JAVA_HOME=C:\Java\rh-openjdk-17.0.7.0.7-1"

if not exist "%MVN_BIN%" (
    echo [ERREUR] Maven IntelliJ introuvable : %MVN_BIN%
    echo Verifie que IntelliJ IDEA 2025.1 est installe.
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERREUR] Java 17 introuvable : %JAVA_HOME%
    exit /b 1
)

echo [mvnw] Java 17 : %JAVA_HOME%
echo [mvnw] Maven   : %MVN_BIN%
call "%MVN_BIN%" %*
