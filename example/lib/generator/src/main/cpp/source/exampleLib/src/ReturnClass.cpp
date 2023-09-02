#include "ReturnClass.h"

ReturnClass& ReturnClass::operator=(const ReturnClass& other) {
    this->value = other.value;
    return *this;
}