#include "NormalClass.h"
#include <iostream>

NormalClass::NormalClass()
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
}

NormalClass::NormalClass(int c, ParamData & refParamData)
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
}

NormalClass::NormalClass(ParamData * pointerParamData, ParamData & refParamData, ParamData valueParamData)
{
    nullPointerReturnClass = 0;
    pointerReturnClass = new ReturnClass();
}

int NormalClass::getVersion()
{
    return 7;
}

void NormalClass::setString(char* text)
{

}

int NormalClass::subIntValue(int a, int b, int subValue)
{
    return (a - b) - subValue;
}

int NormalClass::addIntValue(int a, int b)
{
    return (a + b) * hiddenInt * hiddenParentInt;
}

ReturnClass NormalClass::getStaticReturnValueClass(ParamData * paramData)
{
    ReturnClass temp;
    return temp;
}

ReturnClass NormalClass::getReturnValueClass(ParamData * paramData)
{
    return valueReturnClass;
}

ReturnClass NormalClass::getReturnValueObject()
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

void NormalClass::refParam(ParamData & refParamOne, ParamData & refParamTwo, ParamData & refParamThree)
{
}

void NormalClass::pointerParam(ParamData * pointerParamData)
{
}

void NormalClass::valueParam(ParamData valueParamData)
{
}

void NormalClass::addIntArrayItems(int* array, int valueToAdd)
{
    int value = array[0];
    array[0] = value + valueToAdd;
}

void NormalClass::callInterface(InterfaceClass & obj)
{
    obj.onParamCall(data);
}

void NormalClass::enumParam(EnumLib value)
{
}