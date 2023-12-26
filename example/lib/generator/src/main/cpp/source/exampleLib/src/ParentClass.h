#pragma once

class ParentClass
{
protected:
    int hiddenParentInt = 1;
public:
    ParentClass();
    float addFloatValue(float a, float b);
    bool invertBoolean(bool value);
};
