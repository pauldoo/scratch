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
    const int maximumIterations = 500;

    // Bhuddabrot
#if 0
    const double sampleMinA = 0.0;
    const double sampleMinB = 0.0;
    const double sampleMinC = -2.5;
    const double sampleMinD = -1.5;
    const double sampleMaxA = 0.0;
    const double sampleMaxB = 0.0;
    const double sampleMaxC = 1.5;
    const double sampleMaxD = 1.5;
#endif

    // Juliabrot (?)
#if 0
    const double sampleMinA = -2.0;
    const double sampleMinB = -2.0;
    const double sampleMinC = -0.4;
    const double sampleMinD = 0.6;
    const double sampleMaxA = 2.0;
    const double sampleMaxB = 2.0;
    const double sampleMaxC = -0.4;
    const double sampleMaxD = 0.6;
#endif

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

    class Exception : public std::exception
    {
    private:
        const std::string m_err;

    public:
        Exception(const std::string& err) : m_err(err)
        {}

        virtual ~Exception() throw()
        {}

        virtual const char* what() const throw()
        {
            return m_err.c_str();
        }
    };

    template<typename T> const T fromString(const std::string& string)
    {
        std::istringstream buf(string);
        T result;
        buf >> result;
        if (!buf) {
            throw Exception("Error parsing: " + string);
        }
        return result;
    }
}

int main(int argc, char** argv) {
    try {
        if (argc != 9) {
            throw Exception("Exactly 8 arguments needed.");
        }
        const double sampleMinA = fromString<double>(argv[1]);
        const double sampleMinB = fromString<double>(argv[2]);
        const double sampleMinC = fromString<double>(argv[3]);
        const double sampleMinD = fromString<double>(argv[4]);
        const double sampleMaxA = fromString<double>(argv[5]);
        const double sampleMaxB = fromString<double>(argv[6]);
        const double sampleMaxC = fromString<double>(argv[7]);
        const double sampleMaxD = fromString<double>(argv[8]);

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
                const double chunkMinA = sampleMinA + ((sampleMaxA - sampleMinA) * (w + 0)) / sampleWidth;;
                const double chunkMinB = sampleMinB + ((sampleMaxB - sampleMinB) * (h + 0)) / sampleHeight;;
                const double chunkMinC = sampleMinC + ((sampleMaxC - sampleMinC) * (w + 0)) / sampleWidth;
                const double chunkMinD = sampleMinD + ((sampleMaxD - sampleMinD) * (h + 0)) / sampleHeight;
                const double chunkMaxA = sampleMinA + ((sampleMaxA - sampleMinA) * std::min((w + chunkWidth), sampleWidth)) / sampleWidth;
                const double chunkMaxB = sampleMinB + ((sampleMaxB - sampleMinB) * std::min((h + chunkHeight), sampleHeight)) / sampleHeight;
                const double chunkMaxC = sampleMinC + ((sampleMaxC - sampleMinC) * std::min((w + chunkWidth), sampleWidth)) / sampleWidth;
                const double chunkMaxD = sampleMinD + ((sampleMaxD - sampleMinD) * std::min((h + chunkHeight), sampleHeight)) / sampleHeight;

                cl::Kernel buddhaKernel(program, "Buddhabrot");
                int argIndex = 0;
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMinA));
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMinB));
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMinC));
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMinD));
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMaxA));
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMaxB));
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMaxC));
                buddhaKernel.setArg(argIndex++, static_cast<float>(chunkMaxD));
                buddhaKernel.setArg(argIndex++, imageWidth);
                buddhaKernel.setArg(argIndex++, imageHeight);
                buddhaKernel.setArg(argIndex++, static_cast<float>(imageMinX));
                buddhaKernel.setArg(argIndex++, static_cast<float>(imageMinY));
                buddhaKernel.setArg(argIndex++, static_cast<float>(imageMaxX));
                buddhaKernel.setArg(argIndex++, static_cast<float>(imageMaxY));
                buddhaKernel.setArg(argIndex++, maximumIterations);
                buddhaKernel.setArg(argIndex++, outputBuffer);

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
