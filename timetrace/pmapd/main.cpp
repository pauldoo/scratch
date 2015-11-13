#include "Assert.h"
#include "IO.h"
#include "PhotonMap.h"


#include <iostream>
#include <cstdio>


namespace timetrace {

    void dump(const KDTreeNode* begin, int count, int depth)
    {
        if (count >= 1) {
            const int index = count / 2;
            const KDTreeNode* node = begin + index;

            std::clog << std::string(depth, ' ') << count << std::endl;
            std::clog << std::string(depth, ' ') << node->photon.position << std::endl;
            //std::clog << std::string(depth, ' ') << node->photon.bounceCount << std::endl;
            std::clog << std::string(depth, ' ') << node->splitAxis << std::endl;
            ASSERT((node->splitAxis == 0) == (count == 1));

            if (count >= 2) {
                int countOnLeft = index;
                int countOnRight = count - countOnLeft - 1;
                dump(begin, countOnLeft, depth + 1);
                dump(node + 1, countOnRight, depth + 1);
            }
        }
    }

    int pmapd_main(int argc, char** argv) {

        std::clog << sizeof(Vector4) << std::endl;
        std::clog << sizeof(Photon) << std::endl;
        std::clog << sizeof(KDTreeNode) << std::endl;
        ASSERT(argc == 2);

        const std::string filename = argv[1];

        const PhotonMap photonMap = openPhotonMap(filename);
        std::clog << "Opened photon map with " << photonMap.count << " photons." << std::endl;

        std::clog << photonMap.mins << std::endl;
        std::clog << photonMap.maxs << std::endl;
        dump(photonMap.begin, photonMap.count, 0);

        while (true) {
            Request req = readRequest(stdin);

            findClosestTo(stdout, photonMap, req);
        }

        return EXIT_SUCCESS;
    }
}


int main(int argc, char** argv) {
    std::ios::sync_with_stdio(false);
    return timetrace::pmapd_main(argc, argv);
}
