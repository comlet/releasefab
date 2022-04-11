@ECHO OFF
setlocal

IF "%~1" == "" (
   SET EXPORTFILE=export_docbook
) ELSE (
   REM USE FULL PATH
   For %%I in (%1) do (
      SET EXPORTFILE=%%~nI
      SET EXPORTFOLDER=%%~dpI
   )
)

SET JRE_PATH=.\build\runtime\build_jre\windows\bin
SET PATH=%JRE_PATH%;%PATH%

SET BATCH_SCRIPT_HOME=%~dp0
SET BATCH_SCRIPT_HOME=%BATCH_SCRIPT_HOME:~0,-1%
SET TOOLSROOT=%BATCH_SCRIPT_HOME%\build\build_process\utilities\build_process_docbook
SET EXPORTFOLDER=%EXPORTFOLDER:~0,-1%

%TOOLSROOT%\xsltproc -o %EXPORTFOLDER%\%EXPORTFILE%.fo %TOOLSROOT%\xsl\fo\docbook.xsl %EXPORTFOLDER%\%EXPORTFILE%.xml 

call %TOOLSROOT%\fop\fop -fo %EXPORTFOLDER%\%EXPORTFILE%.fo -pdf %EXPORTFOLDER%\%EXPORTFILE%.pdf

DEL %EXPORTFOLDER%\%EXPORTFILE%.fo

endlocal
