#pragma once

#include <string>

#ifdef NO_DEBUG_ASSERT
#define DEBUG_ASSERT(expression)
#else
#define DEBUG_ASSERT(expression) ::timetrace::Assert((expression), #expression, __FUNCTION__, __FILE__, __LINE__);
#endif

#define ASSERT(expression) ::timetrace::Assert((expression), #expression, __FUNCTION__, __FILE__, __LINE__);


namespace timetrace {
    void Assert(
                const bool v,
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber);
}

