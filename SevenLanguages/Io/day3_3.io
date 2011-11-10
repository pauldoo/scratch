#!/usr/bin/env io

# Seven languages in seven weeks, Io, day3, Q3


Map atPutNumber := method(
    self atPut(
        call evalArgAt(0) asMutable removePrefix("\"") removeSuffix("\""),
        call evalArgAt(1))
)

OperatorTable addAssignOperator(":", "atPutNumber")
curlyBrackets := method(
    r := Map clone
    call message arguments foreach(arg, r doMessage(arg))
    r
)

Builder := Object clone

Builder forward := method(
    children := call message arguments clone

    result := ""

    attributes := Map clone
    t := doMessage(children at(0))
    if (t type == "Map",
        attributes = t
        children removeFirst)

    result := result .. ("  " repeated(depth)) .. "<" .. (call message name)
    attributes keys foreach(k,
        result := result .. " " .. k .. "=" .. "'" .. (attributes at(k)) .. "'")
    result := result .. ">\n"

    depth = depth + 1
    children foreach(
        arg,
        content := self doMessage(arg)
        result := result .. content .. "\n"
    )
    depth = depth - 1

    result := result .. ("  " repeated(depth)) .. "</" .. (call message name) .. ">"
    )

Builder depth := 0

doRelativeFile("builder.txt") println


