#pragma once

#include <string>

#ifdef NO_DEBUG_TRACE
#define DEBUG_TRACE(expression)
#else
#define DEBUG_TRACE(expression) ::timetrace::Trace((expression), #expression, __FUNCTION__, __FILE__, __LINE__);
#endif

namespace timetrace {
    template <typename T>
    void Trace(
                const T& v,
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber);
}


