#!/bin/sh

export PORT=5000
exec java -server -cp target/notepad-0.0.1-SNAPSHOT.jar:"target/dependency/*" HelloWorld
