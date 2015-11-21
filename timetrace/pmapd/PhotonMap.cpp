#include "PhotonMap.h"
#include "Assert.h"

#include "Assert.h"
#include "TraceImp.h"

#include <queue>

namespace timetrace {


    namespace {
        struct NodeWithKnownBoundsAndMinDistance
        {
            NodeWithKnownBoundsAndMinDistance( //
                const Vector4& mins, //
                const Vector4& maxs, //
                const KDTreeNode* const node, //
                const float minDistance,
                const int treeSize
                ) : //
                mins(mins), //
                maxs(maxs), //
                node(node), //
                minDistance(minDistance), //
                treeSize(treeSize)
            {
                ASSERT(node != NULL);
                ASSERT(minDistance >= 0.0f);
                ASSERT(treeSize >= 1);
            }

            Vector4 mins;
            Vector4 maxs;
            const KDTreeNode* node;
            float minDistance;
            int treeSize;
        };

        struct Comparator {
            bool operator() (const NodeWithKnownBoundsAndMinDistance& lhs, const NodeWithKnownBoundsAndMinDistance& rhs)
            {
                return lhs.minDistance > rhs.minDistance;
            }
        };

    }

    void findClosestTo(FILE* const out, const PhotonMap& photonMap, const Request& request)
    {
        std::priority_queue< //
            NodeWithKnownBoundsAndMinDistance, //
            std::priority_queue<NodeWithKnownBoundsAndMinDistance>::container_type, //
            Comparator
            > queue;

        queue.push( NodeWithKnownBoundsAndMinDistance( //
          photonMap.mins, //
          photonMap.maxs, //
          photonMap.begin + (photonMap.count / 2), //
          distanceSquaredToBoundingBox( photonMap.mins, photonMap.maxs, request.target ), //
          photonMap.count ) );

        int writeCount = 0;
        while (writeCount < request.count) {
            TRACE(queue.size());
            ASSERT(queue.empty() == false);
            const NodeWithKnownBoundsAndMinDistance top = queue.top();
            queue.pop();
            TRACE(top.treeSize);
            TRACE(top.node - photonMap.begin);

            if (top.treeSize == 1) {
                const int index = top.node - photonMap.begin;
                TRACE(index);
                size_t numWritten = fwrite(&index, sizeof(index), 1, out);
                ASSERT(numWritten == 1);
                writeCount++;
            } else {
                if (dotProduct(request.interestingHemisphere, top.node->photon.direction) > 0.0f) {
                    TRACE("adding leaf");
                    queue.push( NodeWithKnownBoundsAndMinDistance( //
                        top.node->photon.position, //
                        top.node->photon.position, //
                        top.node, //
                        distanceSquared(top.node->photon.position, request.target), //
                        1) );
                    TRACE("leaf added");
                }

                const int splitIndex = top.node->splitAxis - 1;
                ASSERT(0 <= splitIndex && splitIndex <= 3);

                const int countOnLeft = top.treeSize / 2;
                if (countOnLeft >= 1) {
                    TRACE("adding left");
                    TRACE(countOnLeft);
                    const KDTreeNode* rootLeft = top.node - (top.treeSize / 2) + (countOnLeft / 2);
                    TRACE(rootLeft - photonMap.begin);
                    ASSERT(photonMap.begin <= rootLeft && rootLeft < (photonMap.begin + photonMap.count));

                    const Vector4 minsLeft = top.mins;
                    Vector4 maxsLeft = top.maxs;
                    maxsLeft.data[splitIndex] = top.node->photon.position.data[splitIndex];

                    queue.push( NodeWithKnownBoundsAndMinDistance( //
                        minsLeft, //
                        maxsLeft, //
                        rootLeft, //
                        distanceSquaredToBoundingBox( minsLeft, maxsLeft, request.target ), //
                        countOnLeft ) );
                }

                const int countOnRight = top.treeSize - countOnLeft - 1;
                if (countOnRight >= 1) {
                    TRACE("adding right");
                    const KDTreeNode* rootRight = top.node + 1 + (countOnRight / 2);
                    TRACE(rootRight - photonMap.begin);
                    ASSERT(photonMap.begin <= rootRight && rootRight < (photonMap.begin + photonMap.count));

                    Vector4 minsRight = top.mins;
                    minsRight.data[splitIndex] =
                    top.node->photon.position.data[splitIndex];
                    const Vector4 maxsRight = top.maxs;

                    queue.push( NodeWithKnownBoundsAndMinDistance( //
                        minsRight, //
                        maxsRight, //
                        rootRight, //
                        distanceSquaredToBoundingBox( minsRight, maxsRight, request.target ), //
                        countOnRight) );

                }

            }
        }

        fflush(out);
    }


}

