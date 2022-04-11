@echo off
set DIR="%~dp0"
set PROJECT_HOME=%DIR:~0,-2%"
REM If necessary, adjust "RELEASEFAB_HOME" to your custom directory within the project
set RELEASEFAB_HOME=build_tools\releasefab
set JAVA_EXEC="%DIR:"=%%RELEASEFAB_HOME%\bin\java"



pushd %DIR% & %JAVA_EXEC% %CDS_JVM_OPTS% "-Dlogback.configurationFile=%~dp0/%RELEASEFAB_HOME%/logback.xml" -p "%~dp0/%RELEASEFAB_HOME%/app" -m releasefab.application/de.comlet.releasefab.Main source=%PROJECT_HOME% generalsettings=%~dp0/%RELEASEFAB_HOME% %* & popd