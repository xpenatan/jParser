#pragma once

#include "ParentClass.h"
#include "subpackage/ParamData.h"
#include "ReturnClass.h"
#include "InterfaceClass.h"
#include <stdio.h>
#include <stdlib.h>
#include <string>

enum EnumLib : int {
    DEFAULT = 10,
    FIRST,
    SECOND,
};

enum EnumTwoLib {
    EnumTwoLib_THIRD = 15,
    EnumTwoLib_FOURTH
};

namespace EnumNamespace {
  enum EnumInNamespace {
    e_namespace_val = 78
  };
};

class NormalClass : public ParentClass
{

private:
    ParamData data;

public:
    enum EnumWithinClass {
        e_val = 34
    };

    enum class EnumClassWithinClass {
        testEnum = 35
    };

    int hiddenInt = 1;

    ReturnClass valueReturnClass;
    ReturnClass * pointerReturnClass;
    ReturnClass * nullPointerReturnClass;

    inline static int hiddenInt_static = 0;
    inline static ReturnClass valueReturnClass_static;
    inline static ReturnClass * pointerReturnClass_static = new ReturnClass();
    inline static ReturnClass * nullPointerReturnClass_static = NULL;

    NormalClass();
    NormalClass(EnumNamespace::EnumInNamespace);
    NormalClass(int c, ParamData & refParamData);
    NormalClass(ParamData * pointerParamData, ParamData & refParamData, ParamData valueParamData);

    int getVersion();
    void setString(char* text);
    static int subIntValue(int a, int b, int subValue = 0);
    int addIntValue(int a, int b);

    static ReturnClass getStaticReturnValueClass(ParamData * paramData);
    ReturnClass getReturnValueClass(ParamData * paramData);
    ReturnClass getReturnValueObject();

    ReturnClass & getReturnRefClass();
    ReturnClass * getReturnPointerClass();
    ReturnClass * getReturnNullPointerClass();
    void refParam(ParamData & refParamOne, ParamData & refParamTwo, ParamData & refParamThree);
    void pointerParam(ParamData * pointerParamData);
    void valueParam(ParamData valueParamData);
    void addIntArrayItems(int* array, int valueToAdd);
    void callInterface(InterfaceClass & obj);
    int enumParam(EnumLib value);
    void enumVoidParam(EnumLib value);
    EnumLib enumReturn(int value);
    void enumTwoVoidParam(EnumTwoLib value);
    bool printText(int dummyParam, const char* text);
    void setArray(float* array);

    void setString(std::string& text);
    std::string& getString();
    std::string getStringValue();
    void setVoidParam(void* param);
    void* getVoidParam();
};
