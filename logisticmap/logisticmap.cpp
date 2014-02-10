#include <vector>
#include <cmath>
#include <iostream>

namespace {
    template<typename T> class Image;
    
    const int finalWidth = 2560;
    const int finalHeight = 1440;
    const int supersample = 10;
    const int iterations = finalHeight * supersample * 100;
    const int discardCount = iterations / 10;
    const double gammaCorrection = 1.8;
    const double exposure = 0.001;
    typedef float PixelType;
    typedef Image<PixelType> ImageType;
    
    
    void check(const bool value) {
        if (!value) {
            abort();
        }
    }
    
    template<typename T>
    const T clamp(const T x, const T min, const T max)
    {
        return std::max(min, std::min(x, max));
    }
    
    template<typename T>
    class Image {
    public:
        const int width;
        const int height;
        typedef std::vector<T> DataType;

    private:
        DataType data;
        
    public:
        
        Image(const int width, const int height) :
          width(width),
          height(height),
          data(width * height)
        {
        }
        
        DataType& allPixels() {
            return data;
        }
        
        float& at(const int x, const int y) {
            return data.at(y + x * height);
        }

        const float at(const int x, const int y) const {
            return data.at(y + x * height);
        }
        
        const float clampedAt(const int x, const int y) const {
            return at(clamp(x, 0, width-1), clamp(y, 0, height-1));
        }
        
        const DataType& allPixels() const {
            return data;
        }
    };
    
    const double rescale(const double v, const double inMin, const double inMax, const double outMin, const double outMax) {
        return ((v - inMin) / (inMax - inMin)) * (outMax - outMin) + outMin;
    }
    
    void renderSlice(const int ix, const double r, ImageType* const output) {
        double z = 0.5;
        for (int i = 0; i < iterations; i++) {
            z = r * z * (1.0 - z);
            if (i >= discardCount) {
                const int iy = floorl(rescale(z, 0.0, 1.0, output->height, 0.0));
                
                if (iy >= 0 && iy < output->height) {
                    output->at(ix, iy) += 1.0;
                }
            }
        }
    }
    
    const double gaussian(const double x, const double s) {
        return exp(-(x*x) / (2.0*s*s)) / (s * sqrt(2.0 * M_PI));
    }
    
    const std::vector<double> createKernel(const double s)
    {
        const int size = ceill(3.0 * s);
        std::vector<double> result;
        for (int i = -size; i <= size; i++) {
            result.push_back(gaussian(i, s));
        }
        return result;
    }
    
    void downscale(const ImageType& input, ImageType* const output, const double s, const int dx, const int dy)
    {
        const std::vector<double> kernel = createKernel(s);
        const int size = kernel.size() / 2;
        
        for (int x = 0; x < output->width; x++) {
            const int cx = floorl(rescale(x + 0.5, 0, output->width, 0, input.width));
            for (int y = 0; y < output->height; y++) {
                const int cy = floorl(rescale(y + 0.5, 0, output->height, 0, input.height));

                double c = 0.0;
                for (int i = -size; i <= size; i++) {
                    c += kernel[i+size] * input.clampedAt(cx + i*dx, cy + i*dy);
                }
                output->at(x, y) = c;
            }
        }
    }
    
    void applyGamma(ImageType* const image, const double gamma)
    {
        for (ImageType::DataType::iterator i = image->allPixels().begin();
             i != image->allPixels().end();
             ++i) {
            *i = pow(*i, gamma);
        }
    }
    
    void smoothDownscale(ImageType* const input, ImageType* const output)
    {
        check(input->width == output->width * supersample);
        check(input->height == output->height * supersample);
        
        applyGamma(input, gammaCorrection);
        
        ImageType temp(input->width, output->height);
        downscale(*input, &temp, supersample * 0.5, 0, 1);
        downscale(temp, output, supersample * 0.5, 1, 0);

        applyGamma(output, 1.0 / gammaCorrection);
    }
    
    const double getPercentile(const ImageType& image, const double p)
    {
        std::vector<PixelType> allPixels = image.allPixels();
        std::sort(allPixels.begin(), allPixels.end());
        return allPixels.at(floorl(allPixels.size() * p));
    }
    
    
    void write(const ImageType& image, std::ostream& out)
    {
        out << "P2\n";
        out << image.width << " " << image.height << "\n";
        out << 65535 << "\n";
        
        const double max = getPercentile(image, 0.999);
        std::clog << "P 99.9: " << max << "\n";
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                out << clamp<int>(floorl(rescale(image.at(x, y), 0, max, 0, 65536)), 0, 65535) << " ";
            }
            out << "\n";
        }
    }
    
    void expose(ImageType* const image)
    {
        for (ImageType::DataType::iterator i = image->allPixels().begin();
             i != image->allPixels().end();
             ++i) {
            //*i = pow(*i, 0.2);
            *i = 1.0 - exp(-(*i) * exposure);
        }
    }
    
    int go() {
        ImageType downscaled(finalWidth, finalHeight);
        {
            ImageType intermediate(finalWidth * supersample, finalHeight * supersample);
        
            for (int ix = 0; ix < intermediate.width; ix++) {
                const  double r = rescale(ix + 0.5, 0, intermediate.width, 2.5, 4.0);
                renderSlice(ix, r, &intermediate);
            }
            expose(&intermediate);
            smoothDownscale(&intermediate, &downscaled);
        }
        
        write(downscaled, std::cout);
        
        return 0;
    }
}

int main() {
    return go();
}
