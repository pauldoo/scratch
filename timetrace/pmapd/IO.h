#pragma once

#include "PhotonMapFwd.h"

#include <cstdio>
#include <string>

namespace timetrace {

    PhotonMap openPhotonMap(const std::string& filename);

    Request readRequest(FILE* const in);

}
