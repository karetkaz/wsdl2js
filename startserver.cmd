@echo off

REM ~ change working directory to this files parent
cd %~dp0

if not exist bin\nul mkdir bin

if not exist "bin/wsdl2js.jar" (
echo compiling server ...
javac -sourcepath src -d bin src/server/*.java
jar cvf bin/wsdl2js.jar -C bin/ .
)

echo
echo launching server ...
java -cp bin/wsdl2js.jar server.WSApplication
