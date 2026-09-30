@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the input commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait at the end of the script
@REM MAVEN_OPTS - parameters to pass to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@ECHO OFF
@REM set %ENABLE_DELAYED_EXPANSION% to on if you need to test this script
SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

IF "%MAVEN_BATCH_ECHO%"=="on" ECHO ON

IF NOT "%MAVEN_SKIP_RC%"=="" GOTO skipRc
IF EXIST "%USERPROFILE%\mavenrc_pre.bat" CALL "%USERPROFILE%\mavenrc_pre.bat"
IF EXIST "%USERPROFILE%\.mavenrc" CALL "%USERPROFILE%\.mavenrc"
:skipRc

IF NOT "%MAVEN_BATCH_PAUSE%"=="" GOTO skipPause
SET MAVEN_BATCH_PAUSE=off
:skipPause

SET "ERROR_CODE=0"

@REM set local project root
SET "MAVEN_PROJECTBASEDIR=%~dp0"
IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

SET "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

IF NOT EXIST "%WRAPPER_JAR%" (
    ECHO Downloading maven-wrapper.jar...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webClient = New-Object System.Net.WebClient; $webClient.DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar', '%WRAPPER_JAR%')"
)

IF NOT EXIST "%JAVA_HOME%" (
    FOR /F "tokens=*" %%I IN ('where java.exe 2^>nul') DO (
        SET "JAVA_EXE=%%I"
        GOTO foundJava
    )
) ELSE (
    SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

:foundJava
IF NOT EXIST "%JAVA_EXE%" (
    ECHO Error: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    EXIT /B 1
)

"%JAVA_EXE%" %MAVEN_OPTS% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
SET "ERROR_CODE=%ERRORLEVEL%"

IF "%MAVEN_BATCH_PAUSE%"=="on" PAUSE

EXIT /B %ERROR_CODE%
