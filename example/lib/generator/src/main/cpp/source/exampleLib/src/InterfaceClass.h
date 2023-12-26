#pragma once

#include "subpackage/ParamData.h"

class InterfaceClass
{
public:
    virtual ~InterfaceClass() {}

    virtual void onParamCall(ParamData& data) const = 0;
};
