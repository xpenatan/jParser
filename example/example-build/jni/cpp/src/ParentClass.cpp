#include "ParentClass.h"

float ParentClass::addFloatValue(float a, float b)
{
    return (a + b) * hiddenParentInt;
}

bool ParentClass::invertBoolean(bool value)
{
    return !(bool)(value * hiddenParentInt);
}