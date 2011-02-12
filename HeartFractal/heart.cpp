#include <iostream>
#include <cmath>
#include <complex>
#include <cstdlib>

namespace {
    const int width = 512;
    const int height = 512;
    const int maxColorValue = 255;
    const double minX = -1.5;
    const double minY = -1.5;
    const double maxX = 1.5;
    const double maxY = 1.5;
    const double centerX = 0.0;
    const double centerY = 0.25;
    const std::complex<double> juliaC = std::complex<double>(1.0, 0.5);
    const double angleMultiplier = 10.0;
    const double anglePhase = 1.0 * M_PI;
    const double distanceMultiplier = 30.0;
    const double escapeDistance = 50.0;
    const int maxIterations = 1000;
    const double brightness = 0.05;
    
    void writePixel(std::ostream& out, int r, int g, int b)
    {
        out << r << " " << g << " " << b << "  ";
    }
    
    template<typename T> const T clamp(const T min, const T x, const T max)
    {
        return std::min(max, std::max(min, x));
    }
    
    void go(std::ostream& out)
    {
        out
            << "P3" << std::endl
            << width << " " << height << std::endl
            << maxColorValue << std::endl;
            
        for (int iy = 0; iy < height; iy++) {
            for (int ix = 0; ix < width; ix++) {
                const double x = ((ix + 0.5) / width) * (maxX - minX) + minX;
                const double y = (1.0 - ((iy + 0.5) / height)) * (maxY - minY) + minY;
                const double angle = atan2(y - centerY, x - centerX);
                const double distance = pow(x * x + y * y - 1.0, 3) - (x*x * pow(y,3));
                
                const std::complex<double> z0 = std::complex<double>(
                    angle * angleMultiplier + anglePhase, 
                    distance * distanceMultiplier);
                    
                std::complex<double> z = z0;
                int count = 0;
                while (count < maxIterations && abs(z) < escapeDistance) {
                    count++;
                    z = juliaC * sin(z); 
                }
                
            
                const double factor = 1.0 - exp(-count * brightness);    
            
                writePixel(
                    out, 
                    clamp<int>(0, maxColorValue, maxColorValue), 
                    clamp<int>(0, static_cast<int>((1.0 - factor) * maxColorValue), maxColorValue),
                    clamp<int>(0, static_cast<int>((1.0 - factor) * maxColorValue), maxColorValue));
            }
            out << std::endl;
        }
    }
}

int main(void)
{
    go(std::cout);
    return EXIT_SUCCESS;
}


