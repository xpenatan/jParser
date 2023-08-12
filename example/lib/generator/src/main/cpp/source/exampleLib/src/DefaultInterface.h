#include "InterfaceClass.h"

#ifndef DEFAULTINTERFACE_H
#define DEFAULTINTERFACE_H

class DefaultInterface : public InterfaceClass
{
public:
    virtual void onParamCall(ParamData& data) const {
        data.intData = 99;
        data.floatData = 99.9;
    }
};

#endif  //DEFAULTINTERFACE_H
