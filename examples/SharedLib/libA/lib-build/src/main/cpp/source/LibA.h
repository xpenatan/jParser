#pragma once

class LibAData {
    private:

    public:
        int intValue;
};

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
};