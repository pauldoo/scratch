#pragma once

#include <string>

#define likely(x) __builtin_expect (!!(x), 1)

#ifdef NO_DEBUG_ASSERT
#define DEBUG_ASSERT(expression)
#else
#define DEBUG_ASSERT(expression) ::timetrace::Assert((expression), #expression, __FUNCTION__, __FILE__, __LINE__);
#endif

#define ASSERT(expression) ((void) ((likely(expression)) ? ((void)0) : ::timetrace::AssertFailed(#expression, __FUNCTION__, __FILE__, __LINE__) ))


namespace timetrace {
    void Assert(
                const bool v,
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber);

    void AssertFailed(
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber);
}

