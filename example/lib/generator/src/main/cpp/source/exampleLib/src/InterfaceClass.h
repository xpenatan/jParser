#include "subpackage/ParamData.h"

#ifndef INTERFACECLASS_H
#define INTERFACECLASS_H

class InterfaceClass
{
public:
    virtual ~InterfaceClass() {}

    virtual void onParamCall(ParamData& data) const = 0;
};

#endif  //INTERFACECLASS_H
