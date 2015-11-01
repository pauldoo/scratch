#include <iostream>
#include <string>

#include "Assert.h"
#include "IO.h"
#include "PhotonMap.h"

#define ASSERT(expression) ::timetrace::Assert((expression), #expression, __FUNCTION__, __FILE__, __LINE__);

namespace timetrace {
    
    void sendHelloMessage()
    {
        
    }
    
    PhotonMap openPhotonMap(const std::string& filename)
    {
        return PhotonMap();
    }
    
    int pmapd_main(int argc, char** argv) {
        
        std::clog << sizeof(Vector4) << std::endl;
        std::clog << sizeof(Photon) << std::endl;
        std::clog << sizeof(KDTreeNode) << std::endl;
        ASSERT(argc == 2);
        
        sendHelloMessage();
        
        const std::string filename = argv[1];
        
        const PhotonMap photonMap = openPhotonMap(filename);
        
        
        return EXIT_SUCCESS;
    }
}


int main(int argc, char** argv) {
    std::ios::sync_with_stdio(false);
    return timetrace::pmapd_main(argc, argv);
}
