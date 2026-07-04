#include "LibA.h"

LibAData* GData = nullptr;

void LibA::setGlobalData(LibAData* data) {
    GData = data;
}

LibAData* LibA::getGlobalData() {
    return GData;
}