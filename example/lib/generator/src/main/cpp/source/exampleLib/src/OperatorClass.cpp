#include "OperatorClass.h"

OperatorClass& OperatorClass::operator=(const OperatorClass& other) {
    this->value = other.value;
    return *this;
}

float OperatorClass::operator+(OperatorClass toAdd) {
    return value + toAdd.value;
}