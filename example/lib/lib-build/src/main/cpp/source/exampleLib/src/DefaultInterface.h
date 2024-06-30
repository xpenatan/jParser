#pragma once

#include "InterfaceClass.h"

class DefaultInterface : public InterfaceClass
{
public:
    virtual void onParamCall(ParamData& data) const {
        data.intData = 99;
        data.floatData = 99.9;
    }
};
