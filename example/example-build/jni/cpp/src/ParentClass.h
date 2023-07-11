#ifndef PARENTCLASS_H
#define PARENTCLASS_H

class ParentClass
{
protected:
    int hiddenParentInt = 1;
public:
    float addFloatValue(float a, float b);
    bool invertBoolean(bool value);
};

#endif  //PARENTCLASS_H
