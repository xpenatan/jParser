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

class TestStaticAttributeClass {
    private:

    public:
        inline static int staticIntValue;
        inline static const int staticConstIntValue = 20;
        inline static float staticFloatValue;
        inline static double staticDoubleValue;
        inline static bool staticBoolValue;
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

class TestStaticMethodClass {
    private:
        inline static int intParam;
        inline static int intParam2;
        inline static float floatParam;
        inline static float floatParam2;
        inline static bool boolParam;
        inline static string strParam;
        inline static const TestObjectClass* pointerObject01 = NULL;
        inline static TestObjectClass* pointerObject02 = NULL;
        inline static TestObjectClass refObject01;
        inline static TestObjectClass refObject02;
    public:
        static void setMethod01(int intParam) {
            TestStaticMethodClass::intParam = intParam;
        };
        static void setMethod02(float floatParam, bool boolParam) {
            TestStaticMethodClass::floatParam = floatParam;
            TestStaticMethodClass::boolParam = boolParam;
        };
        static void setMethod03(int intParam, int intParam2, float floatParam, float floatParam2, bool boolParam = true) {
            TestStaticMethodClass::intParam = intParam;
            TestStaticMethodClass::intParam2 = intParam2;
            TestStaticMethodClass::floatParam = floatParam;
            TestStaticMethodClass::floatParam2 = floatParam2;
            TestStaticMethodClass::boolParam = boolParam;
        };
        static void setMethod04(int intParam, int* intArraySize2, float* floatArraySize2) {
            TestStaticMethodClass::intParam = intParam;
            intArraySize2[0] = 1;
            intArraySize2[1] = 2;
            floatArraySize2[0] = 3.1;
            floatArraySize2[1] = 4.2;
        };
        static void setMethod05(const char* strParam) {
            TestStaticMethodClass::strParam = strParam;
        };
        static void setMethod06(const TestObjectClass* pointerObject01, TestObjectClass* pointerObject02, const TestObjectClass& refObject01, TestObjectClass& refObject02) {
            TestStaticMethodClass::pointerObject01 = pointerObject01;
            TestStaticMethodClass::pointerObject02 = pointerObject02;
            TestStaticMethodClass::refObject01 = refObject01;
            TestStaticMethodClass::refObject02 = refObject02;
        };

        static int getIntParam() { return TestStaticMethodClass::intParam; };
        static int getIntParam2() { return TestStaticMethodClass::intParam2; };
        static float getFloatParam() { return TestStaticMethodClass::floatParam; };
        static float getFloatParam2() { return TestStaticMethodClass::floatParam2; };
        static bool getBoolParam() { return TestStaticMethodClass::boolParam; };
        static const char* getStrParam() { return TestStaticMethodClass::strParam.c_str(); };
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
    virtual int onIntCallback(int intParam01, int intParam02) const = 0;
    virtual float onFloatCallback(float floatParam01, float Param02) const = 0;
    virtual bool onBoolCallback(bool boolParam) const = 0;
    virtual void onStringCallback(const char* strParam) const = 0;
};

class DefaultCallbackClass : public CallbackClass
{
public:
    DefaultCallbackClass() {
    };

    virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const {
        refData.intValue = 7;
        refData.floatValue = 7.77;
        pointerData->intValue = 10;
        pointerData->floatValue = 10.77;
    }
    virtual int onIntCallback(int intParam01, int intParam02) const {
        return intParam01 - intParam02;
    }
    virtual float onFloatCallback(float floatParam01, float floatParam02) const {
        return floatParam01 + floatParam02;
    }
    virtual bool onBoolCallback(bool boolParam) const {
        return !boolParam;
    }
    virtual void onStringCallback(const char* strParam) const {
        cout << "strParam: " << strParam << endl;
    }
};

class TestCallbackClass {
    private:
    public:
        int intParam01;
        int intParam02;
        float floatParam01;
        float floatParam02;
        bool boolParam;
        string strParam;
        TestObjectClass valueObject;
        TestObjectClass* pointerObject;

        void callVoidCallback(CallbackClass* callback) {
            callback->onVoidCallback(valueObject, pointerObject);
        };
        int callIntCallback(CallbackClass* callback) {
            return callback->onIntCallback(intParam01, intParam02);
        };
        int callFloatCallback(CallbackClass* callback) {
            return callback->onFloatCallback(floatParam01, floatParam02);
        };
        bool callBoolCallback(CallbackClass* callback) {
            return callback->onBoolCallback(boolParam);
        };
        void callStringCallback(CallbackClass* callback) {
            callback->onStringCallback(strParam.c_str());
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