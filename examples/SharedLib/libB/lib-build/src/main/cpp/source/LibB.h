#pragma once

#include "LibA.h"
#include <iostream>

#define LIBB_API __declspec(dllimport)

extern LIBB_API LibAData* GData;

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
            // Access the shared GData pointer
            LibAData* data = GData;
            std::cout << "GData pointer: " << data << std::endl;
            return data;
        }
};