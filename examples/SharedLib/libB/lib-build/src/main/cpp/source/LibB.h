#pragma once

#include "LibA.h"
#include <iostream>

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

        static LibAData* getGlobalData() {
            LibAData* data = LibA::getGlobalData();
            std::cout << "GData pointer: " << data << std::endl;
            return data;
        }
};