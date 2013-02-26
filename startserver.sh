#!/bin/sh

#~ change working directory to this files parent
cd "$(dirname "$(readlink -f "$0")")"

if [ ! -e "bin" ]
then
	mkdir "bin"
fi

if [ ! -e "bin/wsdl2js.jar" ]
then
	echo compiling server ...
	javac -sourcepath src -d bin src/server/*.java
	jar cvf "bin/wsdl2js.jar" -C bin/ .
fi

echo
echo launching server ...
java -cp "libs/sceye-fi.jar" -cp "bin/wsdl2js.jar" server.WSApplication
