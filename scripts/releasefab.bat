@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

SET PROJECT_HOME_PATH=%~dp0
SET PROJECT_HOME=%PROJECT_HOME_PATH:~0,-1%
SET JRE_PATH=%PROJECT_HOME%\build\runtime\build_jre\windows\bin
SET PATH=%JRE_PATH%;%PATH%
SET RELEASEFAB_HOMEPATH=%PROJECT_HOME%\build\build_process\build_process_sw_releasefab_bin\bin
SET RELEASEFAB_LIBS=%PROJECT_HOME%\build\build_process\build_process_sw_releasefab_bin\libs
SET RELEASEFAB_PLUGINS=%PROJECT_HOME%\build\build_process\build_process_sw_releasefab_bin\plugins
SET SWT_64BIT_WINDOWS=%PROJECT_HOME%\build\build_process\build_process_sw_releasefab_bin\libs\win


IF "%BUILD_NUMBER%" == "" (
   FOR /F "delims=" %%F IN ('wmic path WIN32_UserAccount where "Name="%USERNAME%"" get fullname ^| findstr /v FullName') DO (
      SET RETURN_VALUE=%%F
      REM remove trailing spaces and CR
      for /f "tokens=* delims= " %%a in ("!RETURN_VALUE!") do set FULL_USER_NAME=%%a
      for /l %%a in (1,1,100) do if "!FULL_USER_NAME:~-1!"==" " set FULL_USER_NAME=!FULL_USER_NAME:~0,-1!
      echo "local ReleaseFab run for user !FULL_USER_NAME!"
      goto :endfor
   )
   ) ELSE (
   echo "Jenkins ReleaseFab run on Jenkins"
)
:endfor
java -Dlogback.configurationFile=./logback.xml --module-path %RELEASEFAB_HOMEPATH%;%RELEASEFAB_LIBS%;%RELEASEFAB_PLUGINS%;%SWT_64BIT_WINDOWS% --module=releasefab.application/de.comlet.releasefab.Main source=%PROJECT_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9

endlocal

