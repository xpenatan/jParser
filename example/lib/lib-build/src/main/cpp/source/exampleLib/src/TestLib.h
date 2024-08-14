#pragma once

#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <iostream>

using namespace std;

class TestObjectClass {
    private:

    public:
        int intValue;
        float floatValue;
};

class TestAttributeArrayClass {
    private:

    public:
        int intArray[3];
        float floatArray[3];
        char byteArray[3];
        bool boolArray[3];
        TestObjectClass valueObjectArray[3];
        TestObjectClass* pointerObjectArray[3];
};

class TestAttributeClass {
    private:

    public:
        const int readOnlyIntValue;
        int intValue;
        float floatValue;
        double doubleValue;
        bool boolValue;
        void* voidPointer;
        TestObjectClass valueObject;
        TestObjectClass* pointerObject;
        TestObjectClass* nullPointerObject;

        TestAttributeClass(): readOnlyIntValue(7) {
            pointerObject = new TestObjectClass();
            nullPointerObject = NULL;
        };
        ~TestAttributeClass() {
            delete pointerObject;
        };
};

class TestConstructorClass {
    private:

    public:
        int intParam;
        float floatParam;
        bool boolParam;

        TestConstructorClass(int intParam) {
            this->intParam = intParam;
        };
        TestConstructorClass(float floatParam, bool boolParam) {
            this->floatParam = floatParam;
            this->boolParam = boolParam;
        };
        TestConstructorClass(int intParam, int intParam2, float floatParam, float floatParam2, bool boolParam = true) {
            this->intParam = intParam;
            this->floatParam = floatParam;
            this->boolParam = boolParam;
        };
        TestConstructorClass(int intParam, int* intArray, float* floatArray) {
            this->intParam = intParam;
        };
};