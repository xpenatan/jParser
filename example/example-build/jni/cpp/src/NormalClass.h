#include "ParentClass.h"

#ifndef NORMALCLASS_H
#define NORMALCLASS_H

class NormalClass : public ParentClass
{
    int hiddenInt = 1;
public:
    NormalClass();
    int addIntValue(int a, int b);
};

#endif  //NORMALCLASS_H
