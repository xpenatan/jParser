#include "NormalClass.h"
#include <iostream>

NormalClass::NormalClass()
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
    int a = 1;
    int b = 2;
    int c = a + b;
    c++;
    hiddenInt = 2;
}

NormalClass::NormalClass(int c, ParamClass & refParamClass)
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
    int a = 1;
    int b = 2;
    int d = a + b + c;
    d++;
    hiddenInt = 2;
}

NormalClass::NormalClass(ParamClass * pointerParamClass, ParamClass & refParamClass, ParamClass valueParamClass)
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
    int a = 1;
    int b = 2;
    int d = a + b;
    d++;
    hiddenInt = 2;
}

int NormalClass::subIntValue(int a, int b, int subValue)
{
    return (a - b) - subValue;
}

int NormalClass::addIntValue(int a, int b)
{
    std::cout << "hiddenInt: " << hiddenInt << std::endl;
    std::cout << "hiddenParentInt: " << hiddenParentInt << std::endl;
    hiddenInt = 20;
    std::cout << "hiddenInt: " << hiddenInt << std::endl;
//    return (a + b) * hiddenInt * hiddenParentInt;
    return (a + b);
}

ReturnClass NormalClass::getStaticReturnValueClass(ParamClass * paramClass)
{
    ReturnClass temp;
    return temp;
}

ReturnClass NormalClass::getReturnValueClass(ParamClass * paramClass)
{
    return valueReturnClass;
}

ReturnClass & NormalClass::getReturnRefClass()
{
    return valueReturnClass;
}

ReturnClass * NormalClass::getReturnPointerClass()
{
    return pointerReturnClass;
}

ReturnClass * NormalClass::getReturnNullPointerClass()
{
    return nullPointerReturnClass;
}

void NormalClass::refParam(ParamClass & refParamClass)
{
}

void NormalClass::pointerParam(ParamClass * pointerParamClass)
{
}

void NormalClass::valueParam(ParamClass valueParamClass)
{
}

void NormalClass::addIntArrayItems(int* array, int valueToAdd)
{
    int value = array[0];
    array[0] = value + valueToAdd;
}