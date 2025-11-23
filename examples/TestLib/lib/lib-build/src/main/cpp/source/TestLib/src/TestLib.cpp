#include "TestLib.h"


// This is required to support older c++ versions. inline is only supported in c++17 and later

int TestStaticAttributeClass::staticIntValue01 = 0;
float TestStaticAttributeClass::staticFloatValue01 = 0.0f;
double TestStaticAttributeClass::staticDoubleValue01 = 0.0;
bool TestStaticAttributeClass::staticBoolValue01 = false;

int TestStaticMethodClass::intValue01 = 0;
int TestStaticMethodClass::intValue02 = 0;
float TestStaticMethodClass::floatValue01 = 0;
float TestStaticMethodClass::floatValue02 = 0;
bool TestStaticMethodClass::boolValue01 = false;
string TestStaticMethodClass::strValue01 = "";
const TestObjectClass* TestStaticMethodClass::pointerObject01 = nullptr;
TestObjectClass* TestStaticMethodClass::pointerObject02 = NULL;
TestObjectClass TestStaticMethodClass::refObject01;
TestObjectClass TestStaticMethodClass::refObject02;