#pragma once

class LibAInstanceClass {
    private:

    public:
        int intValue;
};

class LibAClass {
    private:

    public:

        LibAClass() {

        };
        ~LibAClass() {

        };

        int addInt(LibAInstanceClass* obj1, LibAInstanceClass* obj2) {
            return obj1->intValue + obj2->intValue;
        }
};