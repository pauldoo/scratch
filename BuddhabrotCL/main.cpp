#include <exception>
#include <iostream>
#include <fstream>
#include <sstream>

#define __CL_ENABLE_EXCEPTIONS

#include "cl.hpp"

namespace {
    const int sampleWidth = 512;
    const int sampleHeight = 512;
    const double sampleMinX = -2.0;
    const double sampleMinY = -2.0;
    const double sampleMaxX = 2.0;
    const double sampleMaxY = 2.0;
    const int maximumIterations = 20;

    const int imageWidth = 320;
    const int imageHeight = 240;
    const double imageMinX = -2.0;
    const double imageMinY = -1.5;
    const double imageMaxX = 2.0;
    const double imageMaxY = 1.5;

    const std::string ReadFileIntoString(const std::string& filename)
    {
        std::ostringstream buf;
        std::ifstream file(filename.c_str());
        buf << file.rdbuf();
        file.close();
        return buf.str();
    }
}

int main(void) {
    try {
        const std::string kernelSource = ReadFileIntoString("kernel.cl");

        cl::Context context(CL_DEVICE_TYPE_CPU);
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

        cl::Kernel kernel(program, "Buddhabrot");

        cl::CommandQueue queue(context, devices.front());

        const size_t outputBufferSize = imageWidth * imageHeight * 4;
        cl::Buffer outputBuffer(context, CL_MEM_READ_WRITE, outputBufferSize);

        cl::KernelFunctor functor = kernel.bind(
            queue,
            cl::NDRange(sampleWidth, sampleHeight),
            cl::NDRange());

        cl::Event kernelEvent = functor(
            static_cast<float>(sampleMinX),
            static_cast<float>(sampleMinY),
            static_cast<float>(sampleMaxX),
            static_cast<float>(sampleMaxY),
            imageWidth,
            imageHeight,
            static_cast<float>(imageMinX),
            static_cast<float>(imageMinY),
            static_cast<float>(imageMaxX),
            static_cast<float>(imageMaxY),
            maximumIterations,
            outputBuffer);

        std::vector<int> resultBuffer(imageWidth * imageHeight);
        std::vector<cl::Event> events;
        events.push_back(kernelEvent);
        queue.enqueueReadBuffer(
            outputBuffer,
            true,
            0,
            outputBufferSize,
            &(resultBuffer.front()),
            &events);

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
