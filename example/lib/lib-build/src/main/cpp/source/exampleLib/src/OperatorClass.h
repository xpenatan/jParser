#pragma once

class OperatorClass
{
public:
    float value = 0;
    OperatorClass& operator=(const OperatorClass& other);
    float operator+(OperatorClass toAdd);
};