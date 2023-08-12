#include "ParentClass.h"

ParentClass::ParentClass()
{
    int a = 1;
    int b = 2;
    int c = a + b;
    c++;
}

float ParentClass::addFloatValue(float a, float b)
{
    return (a + b) * hiddenParentInt;
//    return (a + b);
}

bool ParentClass::invertBoolean(bool value)
{
    return !(bool)(value * hiddenParentInt);
//    return !value;
}