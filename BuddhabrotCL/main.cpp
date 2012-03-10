#include <cmath>
#include <exception>
#include <fstream>
#include <iostream>
#include <sstream>

#include <sys/time.h>

#define __CL_ENABLE_EXCEPTIONS

#include "cl.hpp"

namespace {
    const int chunkWidth = 500;
    const int chunkHeight = 500;
    const int sampleWidth = 12800;
    const int sampleHeight = 9600;
    const double sampleMinX = -2.5;
    const double sampleMinY = -1.5;
    const double sampleMaxX = 1.5;
    const double sampleMaxY = 1.5;
    const int maximumIterations = 500;

    const int imageWidth = 320;
    const int imageHeight = 240;
    const double imageMinX = -0.8;
    const double imageMinY = -1.125;
    const double imageMaxX = 0.2;
    const double imageMaxY = -0.375;

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

    const int Percentile(const std::vector<int>& vec, const double fraction)
    {
        std::vector<int> vecCopy = vec;
        const int targetIndex = round((vecCopy.size() - 1) * fraction);
        std::vector<int>::iterator targetIterator = vecCopy.begin() + targetIndex;
        std::nth_element(vecCopy.begin(), targetIterator, vecCopy.end());
        return *targetIterator;
    }

    const int Clamp(const int x, const int min, const int max)
    {
        return std::min(std::max(x, min), max);
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


        std::vector<cl::Event> memsetEvent(1);
        cl::Kernel memsetKernel(program, "Memzero");
        memsetKernel.setArg(0, imageWidth * imageHeight);
        memsetKernel.setArg(1, outputBuffer);
        queue.enqueueNDRangeKernel(
            memsetKernel,
            cl::NDRange(),
            cl::NDRange(1000),
            cl::NDRange(),
            NULL,
            &(memsetEvent.back()));

        std::vector<cl::Event> computeEvents;
        for (int h = 0; h < sampleHeight; h += chunkHeight) {
            for (int w = 0; w < sampleWidth; w += chunkWidth) {
                const double chunkMinX = sampleMinX + ((sampleMaxX - sampleMinX) * (w + 0)) / sampleWidth;
                const double chunkMinY = sampleMinY + ((sampleMaxY - sampleMinY) * (h + 0)) / sampleHeight;
                const double chunkMaxX = sampleMinX + ((sampleMaxX - sampleMinX) * std::min((w + chunkWidth), sampleWidth)) / sampleWidth;
                const double chunkMaxY = sampleMinY + ((sampleMaxY - sampleMinY) * std::min((h + chunkHeight), sampleHeight)) / sampleHeight;

                cl::Kernel buddhaKernel(program, "Buddhabrot");
                buddhaKernel.setArg(0, static_cast<float>(chunkMinX));
                buddhaKernel.setArg(1, static_cast<float>(chunkMinY));
                buddhaKernel.setArg(2, static_cast<float>(chunkMaxX));
                buddhaKernel.setArg(3, static_cast<float>(chunkMaxY));
                buddhaKernel.setArg(4, imageWidth);
                buddhaKernel.setArg(5, imageHeight);
                buddhaKernel.setArg(6, static_cast<float>(imageMinX));
                buddhaKernel.setArg(7, static_cast<float>(imageMinY));
                buddhaKernel.setArg(8, static_cast<float>(imageMaxX));
                buddhaKernel.setArg(9, static_cast<float>(imageMaxY));
                buddhaKernel.setArg(10, maximumIterations);
                buddhaKernel.setArg(11, outputBuffer);

                computeEvents.push_back(cl::Event());
                queue.enqueueNDRangeKernel(
                    buddhaKernel,
                    cl::NDRange(),
                    cl::NDRange(chunkWidth, chunkHeight),
                    cl::NDRange(),
                    &memsetEvent,
                    &(computeEvents.back()));

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

        const int minimumValue = Percentile(resultBuffer, 0.001);
        const int maximumValue = Percentile(resultBuffer, 0.999);

        const int maxImageLevel = (maximumValue - minimumValue);
        std::wcout
            << L"P2\n"
            << L"# buddhabrot\n"
            << imageWidth << L" " << imageHeight << "\n"
            << maxImageLevel << "\n";

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                const int outputValue = Clamp(resultBuffer.at(y * imageWidth + x), minimumValue, maximumValue) - minimumValue;
                std::wcout << outputValue << L" ";
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
