#pragma once

#ifdef LIB_USER_CONFIG
#include LIB_USER_CONFIG
#endif

#ifndef LIBA_API
#define LIBA_API
#endif

class LibAData {
public:
    int intValue;
};

extern LIBA_API LibAData* GData;

class LibA {
    private:

    public:

        LibA() {

        };
        ~LibA() {

        };

        int addInt(LibAData* obj1, LibAData* obj2) {
            return obj1->intValue + obj2->intValue;
        }

        int addIntPtr(int* value1, int* value2) {
            return *value1 + *value2;
        }

        static void setGlobalData(LibAData* data);
        static LibAData* getGlobalData();
};