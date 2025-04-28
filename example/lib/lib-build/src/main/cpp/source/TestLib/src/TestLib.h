#pragma once

#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <iostream>

using namespace std;

namespace TestNamespace {
    class TestNamespaceClass {
        private:
        public:
            int intValue01;

            void setMethod01Value(int intValue01) {
                this->intValue01 = intValue01;
            }
            int getMethod01Value() {
                return intValue01;
            }
    };
};

class TestEnumClass {
    private:

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
        TestEnumLib enumValue;

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
        TestEnumLib enumValue;

        TestConstructorClass(int intValue01) {
            this->intValue01 = intValue01;
        };
        TestConstructorClass(float floatValue01, int intValue01) {
            this->floatValue01 = floatValue01;
            this->intValue01 = intValue01;
        };
        TestConstructorClass(int intValue01, int intValue02, float floatValue01, float floatValue02, TestEnumLib enumValue, bool boolValue01 = true) {
            this->intValue01 = intValue01;
            this->intValue02 = intValue02;
            this->floatValue01 = floatValue01;
            this->floatValue02 = floatValue02;
            this->boolValue01 = boolValue01;
            this->enumValue = enumValue;
        };
        TestConstructorClass(int intValue01, int* intArraySize2, float* floatArraySize2) {
            this->intValue01 = intValue01;
            intArraySize2[0] = 1;
            intArraySize2[1] = 2;
            floatArraySize2[0] = 3.1;
            floatArraySize2[1] = 4.2;
        };
};

class TestStringConstructorClass {
    private:

    public:
        int intValue01;
        int floatValue01;
        string strValue01;

        TestStringConstructorClass(const char* strValue01) {
            this->strValue01 = strValue01;
        };
        TestStringConstructorClass(const char* strValue01, int intValue01) {
            this->strValue01 = strValue01;
            this->intValue01 = intValue01;
        };
        TestStringConstructorClass(const char* strValue01, float floatValue01, int intValue01) {
            this->strValue01 = strValue01;
            this->floatValue01 = floatValue01;
            this->intValue01 = intValue01;
        };
};

class TestMethodClass {
    private:
        int intValue01;
        int intValue02;
        float floatValue01;
        float floatValue02;
        bool boolValue01;
        long long longLongValue01;
        string strValue01;
        const TestObjectClass* pointerObject01;
        TestObjectClass* pointerObject02;
        TestObjectClass** pointerObjectArray;
        TestObjectClass refObject01;
        TestObjectClass refObject02;
        TestEnumLib enumValue;
    public:
        TestMethodClass() {
            pointerObject01 = NULL;
            pointerObject02 = NULL;
        };
        TestMethodClass(const char* strValue01) {
            cout << "strValue01 before: " << strValue01 << endl;
            this->strValue01 = strValue01;
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
        void setMethod07(TestObjectClass** pointerObjectArray) {
            this->pointerObjectArray = pointerObjectArray;
//            this->pointerObjectArray = &pointerObjectArray[0];
//            this->pointerObjectArray = &(*array[0]);

            TestObjectClass* obj1 = this->pointerObjectArray[0];
            TestObjectClass* obj2 = this->pointerObjectArray[1];
            obj1->intValue01 = 20;
            obj1->floatValue01 = 10.4;
            obj2->intValue01 = 40;
            obj2->floatValue01 = 30.8;
        };
        void setMethod08(long long longLongValue01) {
            this->longLongValue01 = longLongValue01;
        };
        void setMethod09(TestEnumLib enumValue) {
            this->enumValue = enumValue;
        };

        int getIntValue01() { return intValue01; };
        int getIntValue02() { return intValue02; };
        float getFloatValue01() { return floatValue01; };
        float getFloatValue02() { return floatValue02; };
        bool getBoolValue01() { return boolValue01; };
        const char* getStrValue01() { return strValue01.c_str(); };
        const string& getRefStrValue01() { return strValue01; };
        const TestObjectClass* getPointerObject01() { return pointerObject01; };
        TestObjectClass* getPointerObject02() { return pointerObject02; };
        const TestObjectClass& getRefObject01() { return refObject01; };
        TestObjectClass& getRefObject02() { return refObject02; };
        TestObjectClass getValueObject() { return refObject02; };
        long long getLongLongValue01() { return longLongValue01; };
        TestEnumLib getEnumValue() { return enumValue; };
};

class TestOperatorClass {
private:
    float value;

public:
    // Constructor
    TestOperatorClass() { value = 0; }
    TestOperatorClass(float value) { this->value = value; }

    // Arithmetic Operators
    TestOperatorClass operator+(const TestOperatorClass& other) const { return TestOperatorClass(value + other.value); }
    TestOperatorClass operator-(const TestOperatorClass& other) const { return TestOperatorClass(value - other.value); }
    TestOperatorClass operator*(const TestOperatorClass& other) const { return TestOperatorClass(value * other.value); }
    TestOperatorClass operator/(const TestOperatorClass& other) const {
        if (other.value != 0) return TestOperatorClass(value / other.value);
        cout << "Error: Division by zero!" << endl;
        return *this;
    }
    TestOperatorClass operator-() const { return TestOperatorClass(-value); } // Unary minus

    // Compound Assignment Operators
    TestOperatorClass& operator+=(const TestOperatorClass& other) { value += other.value; return *this; }
    TestOperatorClass& operator-=(const TestOperatorClass& other) { value -= other.value; return *this; }
    TestOperatorClass& operator*=(const TestOperatorClass& other) { value *= other.value; return *this; }
    TestOperatorClass& operator/=(const TestOperatorClass& other) {
        if (other.value != 0) value /= other.value;
        else cout << "Error: Division by zero!" << endl;
        return *this;
    }

    // Comparison Operators
    bool operator==(const TestOperatorClass& other) const { return value == other.value; }
    bool operator!=(const TestOperatorClass& other) const { return value != other.value; }
    bool operator<(const TestOperatorClass& other) const { return value < other.value; }
    bool operator>(const TestOperatorClass& other) const { return value > other.value; }
    bool operator<=(const TestOperatorClass& other) const { return value <= other.value; }
    bool operator>=(const TestOperatorClass& other) const { return value >= other.value; }

    // Increment and Decrement Operators
    TestOperatorClass& operator++() { value += 1.0; return *this; } // Prefix increment
    TestOperatorClass operator++(int) { TestOperatorClass temp = *this; value += 1.0; return temp; } // Postfix increment
    TestOperatorClass& operator--() { value -= 1.0; return *this; } // Prefix decrement
    TestOperatorClass operator--(int) { TestOperatorClass temp = *this; value -= 1.0; return temp; } // Postfix decrement

    // Bitwise Operators (optional, but included for completeness with float caveats)
    TestOperatorClass operator&(const TestOperatorClass& other) const { return TestOperatorClass(static_cast<float>(static_cast<int>(value) & static_cast<int>(other.value))); }
    TestOperatorClass operator|(const TestOperatorClass& other) const { return TestOperatorClass(static_cast<float>(static_cast<int>(value) | static_cast<int>(other.value))); }
    TestOperatorClass operator^(const TestOperatorClass& other) const { return TestOperatorClass(static_cast<float>(static_cast<int>(value) ^ static_cast<int>(other.value))); }
    TestOperatorClass operator~() const { return TestOperatorClass(static_cast<float>(~static_cast<int>(value))); }
    TestOperatorClass operator<<(const TestOperatorClass& other) const { return TestOperatorClass(static_cast<float>(static_cast<int>(value) << static_cast<int>(other.value))); }
    TestOperatorClass operator>>(const TestOperatorClass& other) const { return TestOperatorClass(static_cast<float>(static_cast<int>(value) >> static_cast<int>(other.value))); }

    // Compound Bitwise Assignment Operators
    TestOperatorClass& operator&=(const TestOperatorClass& other) { value = static_cast<float>(static_cast<int>(value) & static_cast<int>(other.value)); return *this; }
    TestOperatorClass& operator|=(const TestOperatorClass& other) { value = static_cast<float>(static_cast<int>(value) | static_cast<int>(other.value)); return *this; }
    TestOperatorClass& operator^=(const TestOperatorClass& other) { value = static_cast<float>(static_cast<int>(value) ^ static_cast<int>(other.value)); return *this; }
    TestOperatorClass& operator<<=(const TestOperatorClass& other) { value = static_cast<float>(static_cast<int>(value) << static_cast<int>(other.value)); return *this; }
    TestOperatorClass& operator>>=(const TestOperatorClass& other) { value = static_cast<float>(static_cast<int>(value) >> static_cast<int>(other.value)); return *this; }

    // Accessor
    float getValue() const { return value; }
    void setValue(float val) { this->value = val; }
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
    virtual void onStdStringCallback(std::string* data) const = 0;
    virtual unsigned int onUnsignedIntCallback(unsigned int unsignedInt) = 0;
    virtual unsigned short onUnsignedShortCallback(unsigned short unsignedShort) const = 0;
    virtual void onAnyCallback_1(void * anyPtr) const = 0;
    virtual void onAnyCallback_2(void * anyPtr, int value) const = 0;
    virtual void onLongLongValue(unsigned long long longLongValue) const = 0;

    int addInt(int a, int b)
    {
        return a + b;
    }
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
    }
    virtual void onStdStringCallback(std::string* data) const {
    }
    virtual unsigned int onUnsignedIntCallback(unsigned int unsignedInt) {
        return 10;
    }
    virtual unsigned short onUnsignedShortCallback(unsigned short unsignedShort) const {
        return 20;
    }
    virtual void onAnyCallback_1(void * anyPtr) const {
    }
    virtual void onAnyCallback_2(void * anyPtr, int value) const {
    }
    virtual void onLongLongValue(unsigned long long longLongValue) const {
    }
};

class CallbackClassManual
{
public:
    virtual ~CallbackClassManual() {}

    virtual void onVoidCallback(TestObjectClass& refData, TestObjectClass* pointerData) const = 0;
    virtual int onIntCallback(int intValue01, int intValue02) const = 0;
    virtual float onFloatCallback(float floatValue01, float Value02) const = 0;
    virtual bool onBoolCallback(bool boolValue01) const = 0;
    virtual void onStringCallback(const char* strValue01) const = 0;

    int addInt(int a, int b)
    {
        return a + b;
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
        float callFloatCallback(CallbackClass* callback) {
            return callback->onFloatCallback(floatValue01, floatValue02);
        };
        bool callBoolCallback(CallbackClass* callback) {
            return callback->onBoolCallback(boolValue01);
        };
        void callStringCallback(CallbackClass* callback) {
            const char* text = strValue01.c_str();
            callback->onStringCallback(text);
        };
        void callIDLStringCallback(CallbackClass* callback) {
            callback->onStdStringCallback(&strValue01);
        };
        unsigned int callUnsignedIntCallback(CallbackClass* callback) {
            unsigned int value = 13;
            return callback->onUnsignedIntCallback(value);
        };
        unsigned short callUnsignedShortCallback(CallbackClass* callback) {
            unsigned short value = 12;
            return callback->onUnsignedShortCallback(value);
        };

        void callManualVoidCallback(CallbackClassManual* callback) {
            callback->onVoidCallback(valueObject, pointerObject);
        };
        int callManualIntCallback(CallbackClassManual* callback) {
            return callback->onIntCallback(intValue01, intValue02);
        };
        float callManualFloatCallback(CallbackClassManual* callback) {
            return callback->onFloatCallback(floatValue01, floatValue02);
        };
        bool callManualBoolCallback(CallbackClassManual* callback) {
            return callback->onBoolCallback(boolValue01);
        };
        void callManualStringCallback(CallbackClassManual* callback) {
            const char* text = strValue01.c_str();
            callback->onStringCallback(text);
        };
};


class CallbackExceptionManual
{
public:
    virtual void callJava() const = 0;
};

class TestExceptionManual {
    private:
    public:

        int setDataToNullPointer() {
            TestObjectClass* objectClass = NULL;
            objectClass->intValue01 = 10;
            return 10;
        };

        void callJavaMethod(CallbackExceptionManual* callback) {
            callback->callJava();
        };
};