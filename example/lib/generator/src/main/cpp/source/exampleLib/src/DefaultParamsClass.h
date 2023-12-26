#pragma once

#include "ParentClass.h"
#include "subpackage/ParamData.h"
#include "ReturnClass.h"
#include "InterfaceClass.h"
#include "OperatorClass.h"

class DefaultParamsClass
{


public:
    DefaultParamsClass(int a, int b, float c = 0, float d = 0);

    void defaultMethodParams(int a, int b, float c = 0, float d = 0);
};
