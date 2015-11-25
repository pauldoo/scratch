#include "IO.h"

#include "PhotonMap.h"
#include "Assert.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/mman.h>


namespace timetrace {

    PhotonMap openPhotonMap(const std::string& filename)
    {
        const int fd = open(filename.c_str(), O_RDONLY);
        ASSERT(fd != -1);

        struct stat statData = {0};
        const int statResult = fstat(fd, &statData);
        ASSERT(statResult == 0);

        void* const result = mmap(NULL, statData.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
        ASSERT(result != MAP_FAILED);

        ASSERT(statData.st_size % sizeof(KDTreeNode) == 2 * sizeof(Vector4));
        int count = statData.st_size / sizeof(KDTreeNode);

        ASSERT(0 == madvise(result, statData.st_size, MADV_WILLNEED));
        ASSERT(0 == madvise(result, statData.st_size, MADV_HUGEPAGE));
        

        return PhotonMap(
          count, //
          static_cast<const Vector4*>(result)[0], //
          static_cast<const Vector4*>(result)[1], //
          static_cast<const KDTreeNode*>(static_cast<const void*>(static_cast<const Vector4*>(result)+2)));
    }

    Request readRequest(FILE* const in)
    {
        Request result;
        ASSERT(sizeof(result) == 48); // Assert is primarily here as a regression test
        size_t itemsRead = fread(&result, 36, 1, in);
        ASSERT(itemsRead == 1);
        return result;
    }

}

