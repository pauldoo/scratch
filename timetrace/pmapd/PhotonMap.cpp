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
                DEBUG_ASSERT(node != NULL);
                DEBUG_ASSERT(minDistance >= 0.0f);
                DEBUG_ASSERT(treeSize >= 1);
                DEBUG_ASSERT(min(mins, maxs) == mins);
                DEBUG_ASSERT(max(mins, maxs) == maxs);
                DEBUG_ASSERT(min(mins, node->photon.position) == mins);
                DEBUG_ASSERT(max(maxs, node->photon.position) == maxs);
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
        DEBUG_TRACE(request.target);
        DEBUG_TRACE(request.interestingHemisphere);

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
            DEBUG_TRACE(queue.size());
            DEBUG_ASSERT(queue.empty() == false);
            const NodeWithKnownBoundsAndMinDistance top = queue.top();
            queue.pop();
            DEBUG_TRACE(top.treeSize);
            DEBUG_TRACE(top.node - photonMap.begin);

            if (top.treeSize == 1) {
                const int index = top.node - photonMap.begin;
                DEBUG_TRACE("writing back photon")
                DEBUG_TRACE(index);
                DEBUG_TRACE(top.node->photon.position);
                size_t numWritten = fwrite(&index, sizeof(index), 1, out);
                ASSERT(numWritten == 1);
                writeCount++;
            } else {
                if (dotProduct(request.interestingHemisphere, top.node->photon.direction) > 0.0f) {
                    DEBUG_TRACE("adding leaf");
                    queue.push( NodeWithKnownBoundsAndMinDistance( //
                        top.node->photon.position, //
                        top.node->photon.position, //
                        top.node, //
                        distanceSquared(top.node->photon.position, request.target), //
                        1) );
                    DEBUG_TRACE("leaf added");
                } else {
                    DEBUG_TRACE("excluding wrong hemisphere");
                    DEBUG_TRACE(top.node->photon.direction);
                }

                const int splitIndex = top.node->splitAxis - 1;
                DEBUG_ASSERT(0 <= splitIndex && splitIndex <= 3);

                const int countOnLeft = top.treeSize / 2;
                if (countOnLeft >= 1) {
                    DEBUG_TRACE("adding left");
                    DEBUG_TRACE(countOnLeft);
                    const KDTreeNode* const rootLeft = top.node - (top.treeSize / 2) + (countOnLeft / 2);
                    DEBUG_TRACE(rootLeft - photonMap.begin);
                    DEBUG_ASSERT(photonMap.begin <= rootLeft && rootLeft < (photonMap.begin + photonMap.count));

                    if (countOnLeft == 1) {
                        queue.push( NodeWithKnownBoundsAndMinDistance( //
                            rootLeft->photon.position, //
                            rootLeft->photon.position, //
                            rootLeft, //
                            distanceSquared(rootLeft->photon.position, request.target), //
                            1) );
                    } else {
                        const Vector4 minsLeft = top.mins;
                        Vector4 maxsLeft = top.maxs;
                        DEBUG_ASSERT(maxsLeft.data[splitIndex] >= top.node->photon.position.data[splitIndex]);
                        maxsLeft.data[splitIndex] = top.node->photon.position.data[splitIndex];

                        queue.push( NodeWithKnownBoundsAndMinDistance( //
                            minsLeft, //
                            maxsLeft, //
                            rootLeft, //
                            distanceSquaredToBoundingBox( minsLeft, maxsLeft, request.target ), //
                            countOnLeft ) );
                    }
                }

                const int countOnRight = top.treeSize - countOnLeft - 1;
                if (countOnRight >= 1) {
                    DEBUG_TRACE("adding right");
                    const KDTreeNode* const rootRight = top.node + 1 + (countOnRight / 2);
                    DEBUG_TRACE(rootRight - photonMap.begin);
                    DEBUG_ASSERT(photonMap.begin <= rootRight && rootRight < (photonMap.begin + photonMap.count));

                    if (countOnRight == 1) {
                        queue.push( NodeWithKnownBoundsAndMinDistance( //
                            rootRight->photon.position, //
                            rootRight->photon.position, //
                            rootRight, //
                            distanceSquared(rootRight->photon.position, request.target), //
                            1) );
                    } else {

                        Vector4 minsRight = top.mins;
                        DEBUG_ASSERT(minsRight.data[splitIndex] <= top.node->photon.position.data[splitIndex]);
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
        }

        fflush(out);
    }


}

