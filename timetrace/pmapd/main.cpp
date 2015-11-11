#include <iostream>
#include <string>
#include <queue>

#include "Assert.h"
#include "IO.h"
#include "PhotonMap.h"



namespace timetrace {
    
#pragma pack(push, 4)    
    struct Request {
        Vector4 target;
        int count;
        Vector4 interestingHemisphere;
    };
#pragma pack(pop)

    Request readRequest(const int inFd)
    {
        Request result;
        ssize_t bytesRead = read(inFd, &result, sizeof(Request));
        ASSERT(bytesRead == sizeof(Request);
        return result;
    }
    
    
    struct NodeWithKnownBoundsAndMinDistance
    {
        const KDTreeNode* const node;
        const Vector4 mins;
        const Vector4 maxs;
        const double minDistance;
    };
    
    bool operator < (const NodeWithKnownBoundsAndMinDistance& lhs, const NodeWithKnownBoundsAndMinDistance& rhs)
    {
        return lhs.minDistance > rhs.minDistance;
    }
    
    double minDistance(const Vector4& mins, const Vector4& maxs, const Vector4& target)
    {
        Vector4 closestPoint = min(maxs(max(mins, target)));
        return magnitudeSquared(sub(target, closestPoint));
    }
    
    void findClosestTo(const int outFd, const PhotonMap& photonMap, const Request& request)
    {
        std::queue<NodeWithKnownBoundsAndMinDistance> queue;
        queue.push(NodeWithKnownBoundsAndMinDistance( //
          photonMap.begin, //
          photonMap.mins, //
          photonMap.maxs, //
          minDistance( photonMap.mins, photonMap.maxs, request.target ) );
          
          
        // todo..
        
    }
        
    void dump(const KDTreeNode* node, int count, int depth)
    {
        if (count >= 1) {
          std::clog << std::string(depth, ' ') << count << std::endl;
          std::clog << std::string(depth, ' ') << node->photon.position << std::endl;
          std::clog << std::string(depth, ' ') << node->photon.bounceCount << std::endl;
          std::clog << std::string(depth, ' ') << node->splitAxis << std::endl;
          ASSERT((node->splitAxis == 0) == (count == 1));
        }        
        
        if (count >= 2) {
          int index = count / 2;
          int countOnLeft = index;
          int countOnRight = count - countOnLeft - 1;
          dump(node + 1, countOnLeft, depth + 1);
          dump(node + 1 + countOnLeft, countOnRight, depth + 1);
        }
    }
        
    int pmapd_main(int argc, char** argv) {
        
        std::clog << sizeof(Vector4) << std::endl;
        std::clog << sizeof(Photon) << std::endl;
        std::clog << sizeof(KDTreeNode) << std::endl;
        ASSERT(argc == 2);
        
        sendHelloMessage();
        
        const std::string filename = argv[1];
        
        const PhotonMap photonMap = openPhotonMap(filename);
        std::clog << "Opened photon map with " << photonMap.count << " photons." << std::endl;
        
        std::clog << photonMap.mins << std::endl;
        std::clog << photonMap.maxs << std::endl;
        dump(photonMap.begin, photonMap.count, 0);
        
        while (true) {
            Request req = readRequest(STDIN_FILENO);
            
            findClosestTo(STDOUT_FILENO, photonMap, req);
        }
        
        return EXIT_SUCCESS;
    }
}


int main(int argc, char** argv) {
    std::ios::sync_with_stdio(false);
    return timetrace::pmapd_main(argc, argv);
}
