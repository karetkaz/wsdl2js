#!/bin/sh

#~ change working directory to this files parent
cd "$(dirname "$(readlink -f "$0")")"

if [ ! -e "out" ]
then
	mkdir "out"
fi

if [ ! -e "wsdl2js.jar" ]
then
	echo compiling server ...
	javac -sourcepath src -d out src/server/*.java
	jar cvf "wsdl2js.jar" -C out/ .
fi

echo
echo launching server ...
java -cp "libs/sceye-fi.jar" -cp "wsdl2js.jar" server.WSApplication
