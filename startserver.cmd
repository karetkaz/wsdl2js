@echo off

REM ~ change working directory to this files parent
cd %~dp0

if not exist out\nul mkdir out
IF ERRORLEVEL 1 goto ERROR

if not exist "wsdl2js.jar" (

	echo compiling server ...

	javac -sourcepath src -d out src/server/*.java
	IF ERRORLEVEL 1 goto ERROR

	jar cvf wsdl2js.jar -C out/ .
	IF ERRORLEVEL 1 goto ERROR
)

echo
echo launching server ...

java -cp wsdl2js.jar server.WSApplication
IF ERRORLEVEL 1 goto ERROR

goto EXIT

:ERROR
pause
:EXIT
