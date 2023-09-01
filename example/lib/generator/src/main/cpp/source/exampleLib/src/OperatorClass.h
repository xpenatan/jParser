#ifndef OPERATORCLASS_H
#define OPERATORCLASS_H

class OperatorClass
{
public:
    float value = 0;
    OperatorClass& operator=(const OperatorClass& other);
    float operator+(OperatorClass toAdd);
};

#endif  //OPERATORCLASS_H
