#include "Time.h"

#include "Assert.h"

#include <ctime>

namespace timetrace {
    long getNanos(void)
    {
        timespec tp;
        int result = clock_gettime(CLOCK_MONOTONIC, &tp);
        ASSERT(result == 0);
        return tp.tv_sec * 1000000000 + tp.tv_nsec;
    }
}

