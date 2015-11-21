#pragma once

#include "Trace.h"

#include <iostream>

namespace timetrace {
    template <typename T>
    void Trace(
                const T& v,
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber)
    {
        std::clog << function << " (" << file << ", line" << lineNumber << "):" << std::endl;
        std::clog << "  " << expression << " = " << v << std::endl;
    }
}

