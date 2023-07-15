#include "ParentClass.h"
#include "ParamClass.h"
#include "ReturnClass.h"

#ifndef NORMALCLASS_H
#define NORMALCLASS_H

class NormalClass : public ParentClass
{
    int hiddenInt = 1;

    ReturnClass valueReturnClass;
    ReturnClass * pointerReturnClass;
    ReturnClass * nullPointerReturnClass;

public:
    NormalClass();
    NormalClass(int c, ParamClass & refParamClass);
    NormalClass(ParamClass * pointerParamClass, ParamClass & refParamClass, ParamClass valueParamClass);

    static int subIntValue(int a, int b, int subValue = 0);
    int addIntValue(int a, int b);

    static ReturnClass getStaticReturnValueClass(ParamClass * paramClass);
    ReturnClass getReturnValueClass(ParamClass * paramClass);

    ReturnClass & getReturnRefClass();
    ReturnClass * getReturnPointerClass();
    ReturnClass * getReturnNullPointerClass();
    void refParam(ParamClass & refParamClass);
    void pointerParam(ParamClass * pointerParamClass);
    void valueParam(ParamClass valueParamClass);
};

#endif  //NORMALCLASS_H
