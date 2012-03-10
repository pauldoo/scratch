#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics

__kernel void Memzero(
    int numberOfElements,
    __global int* output)
{
    const int min = (numberOfElements * (get_global_id(0) + 0)) / get_global_size(0);
    const int max = (numberOfElements * (get_global_id(0) + 1)) / get_global_size(0);
    for (int i = min; i < max; i++) {
        output[i] = 0;
    }
}

__kernel void Buddhabrot(
    float sampleMinA,
    float sampleMinB,
    float sampleMinC,
    float sampleMinD,
    float sampleMaxA,
    float sampleMaxB,
    float sampleMaxC,
    float sampleMaxD,
    int imageWidth,
    int imageHeight,
    float imageMinX,
    float imageMinY,
    float imageMaxX,
    float imageMaxY,
    int maximumIterations,
    __global int* outputImage)
{
    const float sx = (get_global_id(0) + 0.5) / get_global_size(0);
    const float sy = (get_global_id(1) + 0.5) / get_global_size(1);
    const float a = sampleMinA + sx * (sampleMaxA - sampleMinA);
    const float b = sampleMinB + sy * (sampleMaxB - sampleMinB);
    const float c = sampleMinC + sx * (sampleMaxC - sampleMinC);
    const float d = sampleMinD + sy * (sampleMaxD - sampleMinD);

    bool escaped = false;
    {
        float zR = a;
        float zI = b;
        for (int i = 0; i < maximumIterations; i++) {
            if (zR * zR + zI * zI >= 4.0) {
                escaped = true;
                break;
            }
            float tzR = zR * zR - zI * zI + c;
            float tzI = zR * zI + zR * zI + d;
            zR = tzR;
            zI = tzI;
        }
    }

    if (escaped) {
        float zR = a;
        float zI = b;
        for (int i = 0; i < maximumIterations; i++) {
            if (zR * zR + zI * zI >= 4.0) {
                break;
            }
            float tzR = zR * zR - zI * zI + c;
            float tzI = zR * zI + zR * zI + d;
            zR = tzR;
            zI = tzI;

            int x = floor(((zR - imageMinX) * imageWidth) / (imageMaxX - imageMinX));
            int y = floor(((zI - imageMinY) * imageHeight) / (imageMaxY - imageMinY));
            if (x >= 0 && x < imageWidth &&
                y >= 0 && y < imageHeight) {
                __global int* address =
                    outputImage +
                    y * imageWidth +
                    x;

                atom_inc(address);
            }
        }
    }
}

