#pragma once

#ifdef LIBA_EXPORTS
#define LIBA_API __declspec(dllexport)
#else
#define LIBA_API __declspec(dllimport)
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