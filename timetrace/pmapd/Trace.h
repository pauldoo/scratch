#pragma once

#include <string>

//#define TRACE(expression)

#define TRACE(expression) ::timetrace::Trace((expression), #expression, __FUNCTION__, __FILE__, __LINE__);

namespace timetrace {
    template <typename T>
    void Trace(
                const T& v,
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber);
}


