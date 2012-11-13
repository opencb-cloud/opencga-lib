#!/bin/bash

OPTIONS=" -Xms128m -Xmx2048m -Djava.net.preferIPv4Stack=true "

CLASSPATH=""


shell_path=`dirname "$0"`;
absolute=`echo $shell_path | grep "^/"`;

if [ -z $absolute ]
then
        GCSA_HOME="`pwd`/$shell_path"
else
        GCSA_HOME="$shell_path"
fi


if [ -z "$GCSA_HOME" ]
then
	echo "You must define the enviroment variable: GCSA_HOME"
	exit 1
fi

if [ $# -eq 0 ]
then
	echo "Arguments expected."
	exit 1
fi

for i in $GCSA_HOME/libs/*; do
	CLASSPATH=$CLASSPATH:$i
done;

java $OPTIONS -classpath $CLASSPATH org.bioinfo.gcsa.lib.cli.GcsaMain $@ --home $GCSA_HOME
