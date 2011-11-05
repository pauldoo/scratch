#!/usr/bin/env io

# Seven languages in seven weeks, Io, day3, Q1

Builder := Object clone

Builder forward := method(
    write ("  " repeated(depth))
    writeln("<", call message name, ">")
    depth = depth + 1
    call message arguments foreach(
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

