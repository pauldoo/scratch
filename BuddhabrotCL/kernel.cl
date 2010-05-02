#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics

__kernel void Buddhabrot(
    float sampleMinX,
    float sampleMinY,
    float sampleMaxX,
    float sampleMaxY,
    int imageWidth,
    int imageHeight,
    float imageMinX,
    float imageMinY,
    float imageMaxX,
    float imageMaxY,
    int maximumIterations,
    __global int* outputImage)
{
    const float cR = sampleMinX + get_global_id(0) * (sampleMaxX - sampleMinX) / (get_global_size(0) - 1);
    const float cI = sampleMinY + get_global_id(1) * (sampleMaxY - sampleMinY) / (get_global_size(1) - 1);

    bool escaped = false;
    {
        float zR = 0.0;
        float zI = 0.0;
        for (int i = 0; i < maximumIterations; i++) {
            if (zR * zR + zI * zI >= 4.0) {
                escaped = true;
                break;
            }
            float tzR = zR * zR - zI * zI + cR;
            float tzI = zR * zI + zR * zI + cI;
            zR = tzR;
            zI = tzI;
        }
    }

    if (escaped) {
        float zR = 0.0;
        float zI = 0.0;
        for (int i = 0; i < maximumIterations; i++) {
            float tzR = zR * zR - zI * zI + cR;
            float tzI = zR * zI + zR * zI + cI;
            zR = tzR;
            zI = tzI;

            int x = (zR - imageMinX) * (imageWidth - 1) / (imageMaxX - imageMinX);
            int y = (zI - imageMinY) * (imageHeight - 1) / (imageMaxY - imageMinY);
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

