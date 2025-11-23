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
};