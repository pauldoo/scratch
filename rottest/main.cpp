#include <sys/time.h>
#include <cstdint>
#include <iostream>
#include <iomanip>

namespace {
    const int64_t ANSWER = 1176470588235294;
    const int64_t MIN = ANSWER - 1000000000;
    const int64_t MAX = ANSWER + 100;
    const int64_t HIGHEST_DIGIT = 1000000000000000;

    const double now() {
        timeval tv;
        gettimeofday(&tv, NULL);
        return tv.tv_sec + 1e-6 * tv.tv_usec;
    }

    const int64_t rot(const int64_t val) {
        return (val % HIGHEST_DIGIT) * 10 + (val / HIGHEST_DIGIT);
    }

    const bool test(const int64_t val) {
        return rot(val) * 2 == 3 * val;
    }

    const bool v1() {
        bool found = false;
        for (int64_t i = MIN; i < MAX; i++) {
            found = found || test(i);
        }
        return found;
    }

    const bool v2() {
        bool found = false;
        for (int64_t i = MIN; i < MAX; i+=4) {
            found = found || test(i);
            found = found || test(i+1);
            found = found || test(i+2);
            found = found || test(i+3);
        }
        return found;
    }

    const bool v3() {
        bool found = false;
        for (int64_t i = MIN; i < MAX; i+=16) {
            found = found || test(i);
            found = found || test(i+1);
            found = found || test(i+2);
            found = found || test(i+3);
            found = found || test(i+4);
            found = found || test(i+5);
            found = found || test(i+6);
            found = found || test(i+7);
            found = found || test(i+8);
            found = found || test(i+9);
            found = found || test(i+10);
            found = found || test(i+11);
            found = found || test(i+12);
            found = found || test(i+13);
            found = found || test(i+14);
            found = found || test(i+15);
        }
        return found;
    }

    typedef const bool (*TestFunction)(void);

    void test( TestFunction func ) {
        const double startTime = now();
        const bool found = func();
        const double endTime = now();

        std::cout << (1e-6) * (MAX-MIN) / (endTime - startTime) << "M tests/sec\n";
        std::cout << "Found the value: " << found << "\n";
    }

    void go() {
        test(&v1);
        test(&v2);
        test(&v3);
    }
}


int main(void) {
    go();
    return 0;
}

