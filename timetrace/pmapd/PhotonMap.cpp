#include "PhotonMap.h"

#include <queue>

namespace timetrace {


    namespace {
        struct NodeWithKnownBoundsAndMinDistance
        {
            NodeWithKnownBoundsAndMinDistance( //
                const KDTreeNode* const node, //
                const Vector4& mins, //
                const Vector4& maxs, //
                const float minDistance
                ) : //
                node(node), //
                mins(mins), //
                maxs(maxs), //
                minDistance(minDistance)
            {
            }

            const KDTreeNode* node;
            Vector4 mins;
            Vector4 maxs;
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
          photonMap.begin, //
          photonMap.mins, //
          photonMap.maxs, //
          minDistanceToBoundingBox( photonMap.mins, photonMap.maxs, request.target ) ) );


        // todo..

    }


}

