#include <iostream>
#include <cmath>
#include <complex>
#include <cstdlib>
#include <utility>
#include <vector>
#include <limits>

namespace {
    const std::vector<std::pair<double, double> > createApproximateCurve();

    const int width = 1536*2;
    const int height = width;
    const int maxColorValue = 255;
    const double scale = 1.2;
    const double minX = -1.5 * scale;
    const double minY = -1.7 * scale;
    const double maxX = 1.5 * scale;
    const double maxY = 1.3 * scale;
    const double centerX = 0.0;
    const double centerY = 0.3;
    const std::complex<double> juliaC = std::complex<double>(1.0, 0.3);
    const double angleMultiplier = 8.0;
    const double anglePhase = 0.0 * M_PI;
    const double distanceMultiplier = 15.0;
    const double escapeDistance = 50.0;
    const int maxIterations = 1000;
    const double brightness = 0.03;
    const double heartFactorA = 0.07;
    const int distanceEstimateSamples = 2000;
    
    const std::pair<double, double> heart5(const double t)
    {
        return std::make_pair(
            heartFactorA * (12*sin(t) - 4*sin(3*t)),
            heartFactorA * (13*cos(t) - 5*cos(2*t) - 2*cos(3*t) - cos(4*t)));
    }
    
    const std::vector<std::pair<double, double> > createApproximateCurve()
    {
        std::vector<std::pair<double, double> > result;
        for (int i = 0; i < distanceEstimateSamples; i++) {
            const double a = ((i + 0.5) / distanceEstimateSamples) * M_PI * 2.0;
            const std::pair<double, double> p = heart5(a);
            result.push_back(p);
        }
        return result;
    }
    
    const double distanceSquared(
        const std::pair<double, double>& a,
        const std::pair<double, double>& b)
    {
        const double dx = a.first - b.first;
        const double dy = a.second - b.second;
        return dx*dx + dy*dy;
    }
    
    const double distanceToLineSegment(
        const std::pair<double, double>& begin,
        const std::pair<double, double>& end,
        const std::pair<double, double>& v)
    {
        const double d1s = distanceSquared(begin, v);
        const double d2s = distanceSquared(end, v);                
        const double d3s = distanceSquared(begin, end);
        if (d2s >= (d1s + d3s)) {
            return sqrt(d1s);
        } else if (d1s >= (d2s + d3s)) {
            return sqrt(d2s);
        } else {
            const double a = sqrt(d1s);
            const double b = sqrt(d2s);
            const double c = sqrt(d3s);
            const double s = (a + b + c) / 2.0;
            const double area = sqrt(std::max(0.0, s * (s - a) * (s - b) * (s - c)));
            const double distance = 2 * area / c;
            return distance;
        }
    }
    
    const double distanceToCurve(
        const std::vector<std::pair<double, double> >& approximateCurve, 
        const std::pair<double, double>& v)
    {
        double minDistance = std::numeric_limits<double>::max();
        for (int i = 0; i < static_cast<int>(approximateCurve.size()); i++) {
            const double distance = distanceToLineSegment(
                approximateCurve.at(i),
                approximateCurve.at((i + 1) % approximateCurve.size()),
                v);

            minDistance = std::min(distance, minDistance);                
        }
        return minDistance;
    }
    
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
            
        const std::vector<std::pair<double, double> > approximateCurve = createApproximateCurve();
            
        for (int iy = 0; iy < height; iy++) {
            for (int ix = 0; ix < width; ix++) {
                const double x = ((ix + 0.5) / width) * (maxX - minX) + minX;
                const double y = (1.0 - ((iy + 0.5) / height)) * (maxY - minY) + minY;
                const double angle = atan2(y - centerY, x - centerX);
                const double distance = distanceToCurve(approximateCurve, std::make_pair(x, y));
                
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
            std::clog << "\r" << (((iy + 1) * 100) / height);
            std::clog.flush();
        }
        std::clog << std::endl;
    }
}

int main(void)
{
    go(std::cout);
    return EXIT_SUCCESS;
}


