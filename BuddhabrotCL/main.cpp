#include <exception>
#include <iostream>
#include <fstream>
#include <sstream>

#define __CL_ENABLE_EXCEPTIONS

#include "cl.hpp"

namespace {
    const int width = 160;
    const int height = 160;

    const double minX = -2.0;
    const double minY = -2.0;
    const double maxX = 2.0;
    const double maxY = 2.0;
    const int maximumIterations = 20;

    const int levels = 256;
    
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
        
        std::wcout
            << L"P2\n"
            << L"# buddhabrot\n"
            << width << L" " << height << "\n"
            << (levels - 1) << "\n";
        
        cl::Context context(CL_DEVICE_TYPE_CPU);
        std::vector<cl::Device> devices = context.getInfo<CL_CONTEXT_DEVICES>();
        cl::Program::Sources sources;
        sources.push_back(std::make_pair(kernelSource.c_str(), kernelSource.size()));
        
        cl::Program program(context, sources);
        program.build(devices);
        
        cl::Kernel kernel(program, "Buddhabrot");
        
        cl::CommandQueue queue(context, devices.front());
        
        const size_t outputBufferSize = width * height * 4;
        cl::Buffer outputBuffer(context, CL_MEM_READ_WRITE, outputBufferSize);
        
        cl::KernelFunctor functor = kernel.bind(
            queue,
            cl::NDRange(width, height),
            cl::NDRange(16, 16));
        
        cl::Event event = functor(
            static_cast<float>(minX),
            static_cast<float>(minY),
            static_cast<float>(maxX),
            static_cast<float>(maxY),
            maximumIterations,
            outputBuffer);
        event.wait();
        
    } catch (const cl::Error& ex) {
        std::wcerr << L"ERROR: " << ex.what() << L"(" << ex.err() << L")" << std::endl;
    } catch (const std::exception& ex) {
        std::wcerr << L"ERROR: " << ex.what() << std::endl;
    }
}
