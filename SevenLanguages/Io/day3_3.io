#!/usr/bin/env io

# Seven languages in seven weeks, Io, day3, Q3

OperatorTable addAssignOperator(":", "atPutNumber")
curlyBrackets := method(
    "PING" println
    call message arguments println
    r := Map clone
    call message arguments foreach(arg,
        r doMessage(arg))
    r)

Map atPutNumber := method(
    "PONG" println
    self atPut(
        call evalArgAt(0) asMutable removePrefix("\"") removeSuffix("\""),
        call evalArgAt(1)))

{ "a" : "b" } println


Builder := Object clone

Builder forward := method(
    children := call message arguments clone
    attributes := Map clone
    if (children at(0) type == "Map",
        attributes = children removeFirst)
    attributes println
    write ("  " repeated(depth))
    writeln("<", call message name, ">")
    depth = depth + 1
    children foreach(
        arg,
        content := self doMessage(arg);
        if (content type == "Sequence",
            write ("  " repeated(depth))
            writeln(content)))
    depth = depth - 1
    write ("  " repeated(depth))
    writeln("</", call message name, ">"))
    
Builder depth := 0
    
Builder ul(
    li("Io"),
    li("Lua"),
    li("JavaScript"))

