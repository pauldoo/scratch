/*
    Copyright (c) 2012 Paul Richards <paul.richards@gmail.com>

    Permission to use, copy, modify, and/or distribute this software for any
    purpose with or without fee is hereby granted, provided that the above
    copyright notice and this permission notice appear in all copies.

    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

#include <CoreServices/CoreServices.h>
#include <iostream>

namespace {
    void timerCallback(CFRunLoopTimerRef, void *)
    {
        //std::cerr << "PING\n";
        UpdateSystemActivity(OverallAct);
    }

    void* doSystemActivityGuff(void*)
    {
        // Copied from: http://developer.apple.com/library/mac/#qa/qa1160/_index.html
        CFRunLoopTimerContext context = { 0, NULL, NULL, NULL, NULL };
        CFRunLoopTimerRef timer = CFRunLoopTimerCreate(NULL, CFAbsoluteTimeGetCurrent(), 30, 0, 0, timerCallback, &context);
        if (timer != NULL); {
            CFRunLoopAddTimer(CFRunLoopGetCurrent(), timer, kCFRunLoopCommonModes);
        }

        CFRunLoopRun();
        std::cerr << "CFRunLoopRun() returned, but wasn't expected to.\n";
        return 0;
    }
}

int main (const int argc,  char * const * const argv)
{
    const pid_t child = fork();
    if (child == 0) {
        // We are the child.
        execvp(argv[1], &(argv[1]));
        std::cerr << "Failed to launch child process.\n";
        exit(EXIT_FAILURE);
    } else {
        // We are the parent.
        pthread_t background_thread;
        if (pthread_create(&background_thread, NULL, doSystemActivityGuff, NULL) != 0) {
            std::cerr << "Failed to create thread to keep system awake.\n";
            exit(EXIT_FAILURE);
        }

        int child_status;
        waitpid(child, &child_status, 0);
        exit(WEXITSTATUS(child_status));
    }

    return 0; // Unreachable
}

