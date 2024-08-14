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
        string strParam;

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
        float intParam2;
        float floatParam;
        float floatParam2;
        bool boolParam;
        string strParam;

        TestConstructorClass(int intParam) {
            this->intParam = intParam;
        };
        TestConstructorClass(float floatParam, const char* strParam) {
            this->floatParam = floatParam;
            this->boolParam = boolParam;
            this->strParam = strParam;
        };
        TestConstructorClass(int intParam, int intParam2, float floatParam, float floatParam2, bool boolParam = true) {
            this->intParam = intParam;
            this->intParam2 = intParam2;
            this->floatParam = floatParam;
            this->floatParam2 = floatParam2;
            this->boolParam = boolParam;
        };
        TestConstructorClass(int intParam, int* intArraySize2, float* floatArraySize2) {
            this->intParam = intParam;
            intArraySize2[0] = 1;
            intArraySize2[1] = 2;
            floatArraySize2[0] = 3.1;
            floatArraySize2[1] = 4.2;
        };
};

class TestMethodClass {
    private:
        int intParam;
        int intParam2;
        float floatParam;
        float floatParam2;
        bool boolParam;
        string strParam;
        const TestObjectClass* pointerObject01;
        TestObjectClass* pointerObject02;
        TestObjectClass refObject01;
        TestObjectClass refObject02;
    public:
        TestMethodClass() {
            pointerObject01 = NULL;
            pointerObject02 = NULL;
        };
        void setMethod01(int intParam) {
            this->intParam = intParam;
        };
        void setMethod02(float floatParam, bool boolParam) {
            this->floatParam = floatParam;
            this->boolParam = boolParam;
        };
        void setMethod03(int intParam, int intParam2, float floatParam, float floatParam2, bool boolParam = true) {
            this->intParam = intParam;
            this->intParam2 = intParam2;
            this->floatParam = floatParam;
            this->floatParam2 = floatParam2;
            this->boolParam = boolParam;
        };
        void setMethod04(int intParam, int* intArraySize2, float* floatArraySize2) {
            this->intParam = intParam;
            intArraySize2[0] = 1;
            intArraySize2[1] = 2;
            floatArraySize2[0] = 3.1;
            floatArraySize2[1] = 4.2;
        };
        void setMethod05(const char* strParam) {
            this->strParam = strParam;
        };
        void setMethod06(const TestObjectClass* pointerObject01, TestObjectClass* pointerObject02, const TestObjectClass& refObject01, TestObjectClass& refObject02) {
            this->pointerObject01 = pointerObject01;
            this->pointerObject02 = pointerObject02;
            this->refObject01 = refObject01;
            this->refObject02 = refObject02;
        };

        int getIntParam() { return intParam; };
        int getIntParam2() { return intParam2; };
        float getFloatParam() { return floatParam; };
        float getFloatParam2() { return floatParam2; };
        bool getBoolParam() { return boolParam; };
        const char* getStrParam() { return strParam.c_str(); };
        const TestObjectClass* getPointerObject01() { return pointerObject01; };
        TestObjectClass* getPointerObject02() { return pointerObject02; };
        const TestObjectClass& getRefObject01() { return refObject01; };
        TestObjectClass& getRefObject02() { return refObject02; };
        TestObjectClass getValueObject() { return refObject02; };
};

class TestEnumClass {
    private:
        ParamData data;

    public:
        enum TestEnumWithinClass {
            e_val = 34
        };

        enum class TestEnumClassWithinClass {
            testEnum = 35
        };
};

namespace TestEnumNamespace {
    enum TestEnumInNamespace {
        e_namespace_val = 78
    };
};

enum TestEnumLib : int {
    TEST_DEFAULT = 7,
    TEST_FIRST,
    TEST_SECOND
};