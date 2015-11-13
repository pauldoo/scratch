#pragma once

#include <string>

#define ASSERT(expression) ::timetrace::Assert((expression), #expression, __FUNCTION__, __FILE__, __LINE__);

namespace timetrace {
    void Assert(
                const bool v,
                const std::string& expression,
                const std::string& function,
                const std::string& file,
                const int lineNumber);
}

