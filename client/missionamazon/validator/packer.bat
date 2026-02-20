@SET /A ERROR=0

C:\Progra~1\Java\jre1.8.0_60\bin\java.exe -jar ..\..\_tools\yuicompressor-2.4.7.jar --type js --charset UTF-8 -o ..\lobby\validator.js .\validator.js
C:\Progra~1\Java\jre1.8.0_60\bin\java.exe -jar ..\..\_tools\yuicompressor-2.4.7.jar --type js --charset UTF-8 -o ..\game\validator.js .\validator.js
@IF %ERRORLEVEL% GEQ 1 GOTO YUICompressorError

@GOTO eof

:YUICompressorError
@SET ERROR=%ERRORLEVEL%
@ECHO YUICompressor returned error code %ERROR%
@GOTO eof

:eof

@IF NOT "%1"=="batchmode" (
	pause
)
@EXIT /B %ERROR%