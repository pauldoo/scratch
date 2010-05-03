#include <exception>
#include <iostream>
#include <fstream>
#include <sstream>
#include <sys/time.h>

#define __CL_ENABLE_EXCEPTIONS

#include "cl.hpp"

namespace {
    const int chunkWidth = 2000;
    const int chunkHeight = 2000;
    const int sampleWidth = 16000;
    const int sampleHeight = 12000;
    const double sampleMinX = -2.5;
    const double sampleMinY = -1.5;
    const double sampleMaxX = 1.5;
    const double sampleMaxY = 1.5;
    const int maximumIterations = 20;

    const int imageWidth = 3200;
    const int imageHeight = 2400;
    const double imageMinX = -2.5;
    const double imageMinY = -1.5;
    const double imageMaxX = 1.5;
    const double imageMaxY = 1.5;

    const std::string ReadFileIntoString(const std::string& filename)
    {
        std::ostringstream buf;
        std::ifstream file(filename.c_str());
        buf << file.rdbuf();
        file.close();
        return buf.str();
    }

    const double Time()
    {
        timeval tp;
        gettimeofday(&tp, NULL);
        return tp.tv_sec + tp.tv_usec * 1e-6;
    }
}

int main(void) {
    try {
        const std::string kernelSource = ReadFileIntoString("kernel.cl");

        cl::Context context(CL_DEVICE_TYPE_GPU);
        std::vector<cl::Device> devices = context.getInfo<CL_CONTEXT_DEVICES>();
        cl::Program::Sources sources;
        sources.push_back(std::make_pair(kernelSource.c_str(), kernelSource.size()));

        cl::Program program(context, sources);
        try {
            program.build(devices);
        } catch (const cl::Error& ex) {
            if (ex.err() == CL_BUILD_PROGRAM_FAILURE) {
                std::wcerr
                    << L"Build error:\n"
                    << program.getBuildInfo<CL_PROGRAM_BUILD_LOG>(devices.front()).c_str() << "\n";
            }
            throw;
        }

        cl::CommandQueue queue(context, devices.front());

        const double startTime = Time();

        const size_t outputBufferSize = imageWidth * imageHeight * sizeof(int);
        cl::Buffer outputBuffer(context, CL_MEM_READ_WRITE, outputBufferSize);

        const std::vector<cl::Event> memsetEvent(1,
            cl::Kernel(program, "Memzero").bind(
                    queue,
                    cl::NDRange(1000),
                    cl::NDRange()
                )(
                    imageWidth * imageHeight,
                    outputBuffer
                ));

        std::vector<cl::Event> computeEvents;
        for (int h = 0; h < sampleHeight; h += chunkHeight) {
            for (int w = 0; w < sampleWidth; w += chunkWidth) {
                const double chunkMinX = sampleMinX + ((sampleMaxX - sampleMinX) * (w + 0)) / sampleWidth;
                const double chunkMinY = sampleMinY + ((sampleMaxY - sampleMinY) * (h + 0)) / sampleHeight;
                const double chunkMaxX = sampleMinX + ((sampleMaxX - sampleMinX) * std::min((w + chunkWidth), sampleWidth)) / sampleWidth;
                const double chunkMaxY = sampleMinY + ((sampleMaxY - sampleMinY) * std::min((h + chunkHeight), sampleHeight)) / sampleHeight;
                computeEvents.push_back(cl::Kernel(program, "Buddhabrot").bind(
                        queue,
                        cl::NDRange(chunkWidth, chunkHeight),
                        cl::NDRange(20, 20)
                    )(
                        static_cast<float>(chunkMinX),
                        static_cast<float>(chunkMinY),
                        static_cast<float>(chunkMaxX),
                        static_cast<float>(chunkMaxY),
                        imageWidth,
                        imageHeight,
                        static_cast<float>(imageMinX),
                        static_cast<float>(imageMinY),
                        static_cast<float>(imageMaxX),
                        static_cast<float>(imageMaxY),
                        maximumIterations,
                        outputBuffer,
                        &memsetEvent
                    ));
            }
        }

        std::vector<int> resultBuffer(imageWidth * imageHeight);
        queue.enqueueReadBuffer(
            outputBuffer,
            true,
            0,
            outputBufferSize,
            &(resultBuffer.front()),
            static_cast<const std::vector<cl::Event>*>(&computeEvents));

        const double endTime = Time();

        std::wclog << L"OpenCL time: " << (endTime - startTime) << L"s\n";

        const int maxLevel = *std::max_element(resultBuffer.begin(), resultBuffer.end());
        std::wcout
            << L"P2\n"
            << L"# buddhabrot\n"
            << imageWidth << L" " << imageHeight << "\n"
            << maxLevel << "\n";

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                std::wcout << resultBuffer.at(y * imageWidth + x) << L" ";
            }
            std::wcout << L"\n";
        }

        return EXIT_SUCCESS;
    } catch (const cl::Error& ex) {
        std::wcerr << L"ERROR: " << ex.what() << L"(" << ex.err() << L")" << std::endl;
    } catch (const std::exception& ex) {
        std::wcerr << L"ERROR: " << ex.what() << std::endl;
    }
    return EXIT_FAILURE;
}
