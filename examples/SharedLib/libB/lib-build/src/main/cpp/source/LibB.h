#pragma once

#include "LibA.h"

class LibBClass {
    private:

    public:

        LibBClass() {

        };
        ~LibBClass() {

        };

        LibAInstanceClass* createInstance(int value) {
            LibAInstanceClass* instance = new LibAInstanceClass();
            instance->intValue = value;
            return instance;
        }
};