#include "NormalClass.h"

NormalClass::NormalClass()
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
    int a = 1;
    int b = 2;
    int c = a + b;
    c++;
}

NormalClass::NormalClass(int c, ParamClass & refParamClass)
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
    int a = 1;
    int b = 2;
    int d = a + b + c;
    d++;
}

NormalClass::NormalClass(ParamClass * pointerParamClass, ParamClass & refParamClass, ParamClass valueParamClass)
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
    int a = 1;
    int b = 2;
    int d = a + b;
    d++;
}

int NormalClass::subIntValue(int a, int b, int subValue)
{
    return (a - b) - subValue;
}

int NormalClass::addIntValue(int a, int b)
{
    return (a + b) * hiddenInt * hiddenParentInt;
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