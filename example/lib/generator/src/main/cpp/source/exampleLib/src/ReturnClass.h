#ifndef RETURNCLASS_H
#define RETURNCLASS_H

class ReturnClass
{
public:
    float value = 0;
    ReturnClass& operator=(const ReturnClass& other);
};

#endif  //RETURNCLASS_H
