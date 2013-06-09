#!/bin/sh

export PORT=5000
java -server -cp target/classes:"target/dependency/*" HelloWorld
