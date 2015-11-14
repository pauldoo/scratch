#include "PhotonMap.h"

#include <queue>

namespace timetrace {


    namespace {
        struct NodeWithKnownBoundsAndMinDistance
        {
            NodeWithKnownBoundsAndMinDistance( //
                const Vector4& mins, //
                const Vector4& maxs, //
                const KDTreeNode* const node, //
                const float minDistance
                ) : //
                mins(mins), //
                maxs(maxs), //
                node(node), //
                minDistance(minDistance)
            {
            }

            Vector4 mins;
            Vector4 maxs;
            const KDTreeNode* node;
            float minDistance;
        };

        bool operator < (const NodeWithKnownBoundsAndMinDistance& lhs, const NodeWithKnownBoundsAndMinDistance& rhs)
        {
            return lhs.minDistance > rhs.minDistance;
        }


    }

    void findClosestTo(FILE* const out, const PhotonMap& photonMap, const Request& request)
    {
        std::priority_queue<NodeWithKnownBoundsAndMinDistance> queue;

        queue.push( NodeWithKnownBoundsAndMinDistance( //
          photonMap.mins, //
          photonMap.maxs, //
          photonMap.begin + (photonMap.count / 2), //
          minDistanceToBoundingBox( photonMap.mins, photonMap.maxs, request.target ) ) );


        // todo..

    }


}

