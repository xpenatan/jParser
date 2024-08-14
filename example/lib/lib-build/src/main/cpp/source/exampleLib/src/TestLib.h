#pragma once

#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <iostream>

using namespace std;

class TestObjectClass {
    private:

    public:
        int intValue01;
        float floatValue01;
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
        const int readOnlyIntValue01;
        int intValue01;
        float floatValue01;
        double doubleValue01;
        bool boolValue01;
        string strValue01;
        void* voidPointer01;
        TestObjectClass valueObject;
        TestObjectClass* pointerObject;
        TestObjectClass* nullPointerObject;

        TestAttributeClass(): readOnlyIntValue01(7) {
            pointerObject = new TestObjectClass();
            nullPointerObject = NULL;
        };
        ~TestAttributeClass() {
            delete pointerObject;
        };
};

class TestStaticAttributeClass {
    private:

    public:
        inline static int staticIntValue01;
        inline static const int staticConstIntValue01 = 20;
        inline static float staticFloatValue01;
        inline static double staticDoubleValue01;
        inline static bool staticBoolValue01;
};

class TestConstructorClass {
    private:

    public:
        int intValue01;
        int intValue02;
        float floatValue01;
        float floatValue02;
        bool boolValue01;
        string strValue01;

        TestConstructorClass(int intValue01) {
            this->intValue01 = intValue01;
        };
        TestConstructorClass(float floatValue01, const char* strValue01) {
            this->floatValue01 = floatValue01;
            this->strValue01 = strValue01;
        };
        TestConstructorClass(int intValue01, int intValue02, float floatValue01, float floatValue02, bool boolValue01 = true) {
            this->intValue01 = intValue01;
            this->intValue02 = intValue02;
            this->floatValue01 = floatValue01;
            this->floatValue02 = floatValue02;
            this->boolValue01 = boolValue01;
        };
        TestConstructorClass(int intValue01, int* intArraySize2, float* floatArraySize2) {
            this->intValue01 = intValue01;
            intArraySize2[0] = 1;
            intArraySize2[1] = 2;
            floatArraySize2[0] = 3.1;
            floatArraySize2[1] = 4.2;
        };
};

class TestMethodClass {
    private:
        int intValue01;
        int intValue02;
        float floatValue01;
        float floatValue02;
        bool boolValue01;
        string strValue01;
        const TestObjectClass* pointerObject01;
        TestObjectClass* pointerObject02;
        TestObjectClass refObject01;
        TestObjectClass refObject02;
    public:
        TestMethodClass() {
            pointerObject01 = NULL;
            pointerObject02 = NULL;
        };
        void setMethod01(int intValue01) {
            this->intValue01 = intValue01;
        };
        void setMethod02(float floatValue01, bool boolValue01) {
            this->floatValue01 = floatValue01;
            this->boolValue01 = boolValue01;
        };
        void setMethod03(int intValue01, int intValue02, float floatValue01, float floatValue02, bool boolValue01 = true) {
            this->intValue01 = intValue01;
            this->intValue02 = intValue02;
            this->floatValue01 = floatValue01;
            this->floatValue02 = floatValue02;
            this->boolValue01 = boolValue01;
        };
        void setMethod04(int intValue01, int* intArraySize2, float* floatArraySize2) {
            this->intValue01 = intValue01;
            intArraySize2[0] = 1;
            intArraySize2[1] = 2;
            floatArraySize2[0] = 3.1;
            floatArraySize2[1] = 4.2;
        };
        void setMethod05(const char* strValue01) {
            this->strValue01 = strValue01;
        };
        void setMethod06(const TestObjectClass* pointerObject01, TestObjectClass* pointerObject02, const TestObjectClass& refObject01, TestObjectClass& refObject02) {
            this->pointerObject01 = pointerObject01;
            this->pointerObject02 = pointerObject02;
            this->refObject01 = refObject01;
            this->refObject02 = refObject02;
        };

        int getIntValue01() { return intValue01; };
        int getIntValue02() { return intValue02; };
        float getFloatValue01() { return floatValue01; };
        float getFloatValue02() { return floatValue02; };
        bool getBoolValue01() { return boolValue01; };
        const char* getStrValue01() { return strValue01.c_str(); };
        const TestObjectClass* getPointerObject01() { return pointerObject01; };
        TestObjectClass* getPointerObject02() { return pointerObject02; };
        const TestObjectClass& getRefObject01() { return refObject01; };
        TestObjectClass& getRefObject02() { return refObject02; };
        TestObjectClass getValueObject() { return refObject02; };
};

class TestStaticMethodClass {
    private:
        inline static int intValue01;
        inline static int intValue02;
        inline static float floatValue01;
        inline static float floatValue02;
        inline static bool boolValue01;
        inline static string strValue01;
        inline static const TestObjectClass* pointerObject01 = NULL;
        inline static TestObjectClass* pointerObject02 = NULL;
        inline static TestObjectClass refObject01;
        inline static TestObjectClass refObject02;
    public:
        static void setMethod01(int intValue01) {
            TestStaticMethodClass::intValue01 = intValue01;
        };
        static void setMethod02(float floatValue01, bool boolValue01) {
            TestStaticMethodClass::floatValue01 = floatValue01;
            TestStaticMethodClass::boolValue01 = boolValue01;
        };
        static void setMethod03(int intValue01, int intValue02, float floatValue01, float floatValue02, bool boolValue01 = true) {
            TestStaticMethodClass::intValue01 = intValue01;
            TestStaticMethodClass::intValue02 = intValue02;
            TestStaticMethodClass::floatValue01 = floatValue01;
            TestStaticMethodClass::floatValue02 = floatValue02;
            TestStaticMethodClass::boolValue01 = boolValue01;
        };
        static void setMethod04(int intValue01, int* intArraySize2, float* floatArraySize2) {
            TestStaticMethodClass::intValue01 = intValue01;
            intArraySize2[0] = 1;
            intArraySize2[1] = 2;
            floatArraySize2[0] = 3.1;
            floatArraySize2[1] = 4.2;
        };
        static void setMethod05(const char* strValue01) {
            TestStaticMethodClass::strValue01 = strValue01;
        };
        static void setMethod06(const TestObjectClass* pointerObject01, TestObjectClass* pointerObject02, const TestObjectClass& refObject01, TestObjectClass& refObject02) {
            TestStaticMethodClass::pointerObject01 = pointerObject01;
            TestStaticMethodClass::pointerObject02 = pointerObject02;
            TestStaticMethodClass::refObject01 = refObject01;
            TestStaticMethodClass::refObject02 = refObject02;
        };

        static int getIntValue01() { return TestStaticMethodClass::intValue01; };
        static int getIntValue02() { return TestStaticMethodClass::intValue02; };
        static float getFloatValue01() { return TestStaticMethodClass::floatValue01; };
        static float getFloatValue02() { return TestStaticMethodClass::floatValue02; };
        static bool getBoolValue01() { return TestStaticMethodClass::boolValue01; };
        static const char* getStrValue01() { return TestStaticMethodClass::strValue01.c_str(); };
        static const TestObjectClass* getPointerObject01() { return TestStaticMethodClass::pointerObject01; };
        static TestObjectClass* getPointerObject02() { return TestStaticMethodClass::pointerObject02; };
        static const TestObjectClass& getRefObject01() { return TestStaticMethodClass::refObject01; };
        static TestObjectClass& getRefObject02() { return TestStaticMethodClass::refObject02; };
        static TestObjectClass getValueObject() { return TestStaticMethodClass::refObject02; };
};

class CallbackClass
{
public:
    virtual ~CallbackClass() {}

    virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const = 0;
    virtual int onIntCallback(int intValue01, int intValue02) const = 0;
    virtual float onFloatCallback(float floatValue01, float Value02) const = 0;
    virtual bool onBoolCallback(bool boolValue01) const = 0;
    virtual void onStringCallback(const char* strValue01) const = 0;
};

class DefaultCallbackClass : public CallbackClass
{
public:
    DefaultCallbackClass() {
    };

    virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const {
        refData.intValue01 = 7;
        refData.floatValue01 = 7.77;
        pointerData->intValue01 = 10;
        pointerData->floatValue01 = 10.77;
    }
    virtual int onIntCallback(int intValue01, int intValue02) const {
        return intValue01 - intValue02;
    }
    virtual float onFloatCallback(float floatValue01, float floatValue02) const {
        return floatValue01 + floatValue02;
    }
    virtual bool onBoolCallback(bool boolValue01) const {
        return !boolValue01;
    }
    virtual void onStringCallback(const char* strValue01) const {
        cout << "strValue01: " << strValue01 << endl;
    }
};

class TestCallbackClass {
    private:
    public:
        int intValue01;
        int intValue02;
        float floatValue01;
        float floatValue02;
        bool boolValue01;
        string strValue01;
        TestObjectClass valueObject;
        TestObjectClass* pointerObject;

        void callVoidCallback(CallbackClass* callback) {
            callback->onVoidCallback(valueObject, pointerObject);
        };
        int callIntCallback(CallbackClass* callback) {
            return callback->onIntCallback(intValue01, intValue02);
        };
        int callFloatCallback(CallbackClass* callback) {
            return callback->onFloatCallback(floatValue01, floatValue02);
        };
        bool callBoolCallback(CallbackClass* callback) {
            return callback->onBoolCallback(boolValue01);
        };
        void callStringCallback(CallbackClass* callback) {
            callback->onStringCallback(strValue01.c_str());
        };
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