#pragma once

#include "LibA.h"

class LibB {
    private:

    public:
        LibB() { };
        ~LibB() { };

        LibAData* createInstance(int value) {
            LibAData* instance = new LibAData();
            instance->intValue = value;
            return instance;
        }

        int addIntPtr(int* value1, int* value2) {
            return *value1 + *value2;
        }
};