@echo off

REM ~ change working directory to this files parent
cd %~dp0

if not exist bin\nul mkdir bin
IF ERRORLEVEL 1 goto ERROR

rem if not exist "bin/wsdl2js.jar" (

echo compiling server ...

javac -sourcepath src -d bin src/server/*.java
IF ERRORLEVEL 1 goto ERROR

jar cvf bin/wsdl2js.jar -C bin/ .
IF ERRORLEVEL 1 goto ERROR
rem )

echo
echo launching server ...

java -cp bin/wsdl2js.jar server.WSApplication
IF ERRORLEVEL 1 goto ERROR

goto EXIT

:ERROR
pause
:EXIT
