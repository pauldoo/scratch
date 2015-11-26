#include "Time.h"

#include "Assert.h"

#include <ctime>

namespace timetrace {
    long getNanos(void)
    {
#if defined(__linux__)
        timespec tp;
        int result = clock_gettime(CLOCK_MONOTONIC, &tp);
        ASSERT(result == 0);
        return tp.tv_sec * 1000000000 + tp.tv_nsec;
#else
        clock_t time = clock();
        return (time * 1000000000) / CLOCKS_PER_SEC;
#endif
    }
}

