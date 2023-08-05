#include "ParentClass.h"
#include "subpackage/ParamClass.h"
#include "ReturnClass.h"
#include <stdio.h>
#include <stdlib.h>

#ifndef NORMALCLASS_H
#define NORMALCLASS_H

class NormalClass : ParentClass
{
public:
    int hiddenInt = 1;

    ReturnClass valueReturnClass;
    ReturnClass * pointerReturnClass;
    ReturnClass * nullPointerReturnClass;

    NormalClass();
    NormalClass(int c, ParamClass & refParamClass);
    NormalClass(ParamClass * pointerParamClass, ParamClass & refParamClass, ParamClass valueParamClass);

    void setString(char* text);
    static int subIntValue(int a, int b, int subValue = 0);
    int addIntValue(int a, int b);

    static ReturnClass getStaticReturnValueClass(ParamClass * paramClass);
    ReturnClass getReturnValueClass(ParamClass * paramClass);
    ReturnClass getReturnValueObject();

    ReturnClass & getReturnRefClass();
    ReturnClass * getReturnPointerClass();
    ReturnClass * getReturnNullPointerClass();
    void refParam(ParamClass & refParamOne, ParamClass & refParamTwo, ParamClass & refParamThree);
    void pointerParam(ParamClass * pointerParamClass);
    void valueParam(ParamClass valueParamClass);
    void addIntArrayItems(int* array, int valueToAdd);
};

#endif  //NORMALCLASS_H
