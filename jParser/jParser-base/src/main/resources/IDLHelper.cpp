#include "IDLHelper.h"

// Implementations for derived classes

IDLBool::IDLBool() : IDLBoolArray(1, false) {}
IDLBool::IDLBool(bool ownsData) : IDLBoolArray(ownsData) {}

IDLByte::IDLByte() : IDLByteArray(1, false) {}
IDLByte::IDLByte(bool ownsData) : IDLByteArray(ownsData) {}

IDLInt::IDLInt() : IDLIntArray(1, false) {}
IDLInt::IDLInt(bool ownsData) : IDLIntArray(ownsData) {}

IDLInt2::IDLInt2() : IDLIntArray(2, false) {}
void IDLInt2::set(int x, int y) {
    IDLIntArray::setValue(0, x);
    IDLIntArray::setValue(1, y);
}
int IDLInt2::getX() { return IDLIntArray::getValue(0); }
int IDLInt2::getY() { return IDLIntArray::getValue(1); }

IDLInt3::IDLInt3() : IDLIntArray(3, false) {}
void IDLInt3::set(int x, int y, int z) {
    IDLIntArray::setValue(0, x);
    IDLIntArray::setValue(1, y);
    IDLIntArray::setValue(2, z);
}
int IDLInt3::getX() { return IDLIntArray::getValue(0); }
int IDLInt3::getY() { return IDLIntArray::getValue(1); }
int IDLInt3::getZ() { return IDLIntArray::getValue(2); }

IDLInt4::IDLInt4() : IDLIntArray(4, false) {}
void IDLInt4::set(int x, int y, int z, int w) {
    IDLIntArray::setValue(0, x);
    IDLIntArray::setValue(1, y);
    IDLIntArray::setValue(2, z);
    IDLIntArray::setValue(3, w);
}
int IDLInt4::getX() { return IDLIntArray::getValue(0); }
int IDLInt4::getY() { return IDLIntArray::getValue(1); }
int IDLInt4::getZ() { return IDLIntArray::getValue(2); }
int IDLInt4::getW() { return IDLIntArray::getValue(3); }

IDLLong::IDLLong() : IDLLongArray(1, false) {}
IDLLong::IDLLong(bool ownsData) : IDLLongArray(ownsData) {}

IDLLong2::IDLLong2() : IDLLongArray(2, false) {}
void IDLLong2::set(long long x, long long y) {
    IDLLongArray::setValue(0, x);
    IDLLongArray::setValue(1, y);
}
long long IDLLong2::getX() { return IDLLongArray::getValue(0); }
long long IDLLong2::getY() { return IDLLongArray::getValue(1); }

IDLLong3::IDLLong3() : IDLLongArray(3, false) {}
void IDLLong3::set(long long x, long long y, long long z) {
    IDLLongArray::setValue(0, x);
    IDLLongArray::setValue(1, y);
    IDLLongArray::setValue(2, z);
}
long long IDLLong3::getX() { return IDLLongArray::getValue(0); }
long long IDLLong3::getY() { return IDLLongArray::getValue(1); }
long long IDLLong3::getZ() { return IDLLongArray::getValue(2); }

IDLLong4::IDLLong4() : IDLLongArray(4, false) {}
void IDLLong4::set(long long x, long long y, long long z, long long w) {
    IDLLongArray::setValue(0, x);
    IDLLongArray::setValue(1, y);
    IDLLongArray::setValue(2, z);
    IDLLongArray::setValue(3, w);
}
long long IDLLong4::getX() { return IDLLongArray::getValue(0); }
long long IDLLong4::getY() { return IDLLongArray::getValue(1); }
long long IDLLong4::getZ() { return IDLLongArray::getValue(2); }
long long IDLLong4::getW() { return IDLLongArray::getValue(3); }

IDLFloat::IDLFloat() : IDLFloatArray(1, false) {}
IDLFloat::IDLFloat(bool ownsData) : IDLFloatArray(ownsData) {}

IDLFloat2::IDLFloat2() : IDLFloatArray(2, false) {}
void IDLFloat2::set(float x, float y) {
    IDLFloatArray::setValue(0, x);
    IDLFloatArray::setValue(1, y);
}
float IDLFloat2::getX() { return IDLFloatArray::getValue(0); }
float IDLFloat2::getY() { return IDLFloatArray::getValue(1); }

IDLFloat3::IDLFloat3() : IDLFloatArray(3, false) {}
void IDLFloat3::set(float x, float y, float z) {
    IDLFloatArray::setValue(0, x);
    IDLFloatArray::setValue(1, y);
    IDLFloatArray::setValue(2, z);
}
float IDLFloat3::getX() { return IDLFloatArray::getValue(0); }
float IDLFloat3::getY() { return IDLFloatArray::getValue(1); }
float IDLFloat3::getZ() { return IDLFloatArray::getValue(2); }

IDLFloat4::IDLFloat4() : IDLFloatArray(4, false) {}
void IDLFloat4::set(float x, float y, float z, float w) {
    IDLFloatArray::setValue(0, x);
    IDLFloatArray::setValue(1, y);
    IDLFloatArray::setValue(2, z);
    IDLFloatArray::setValue(3, w);
}
float IDLFloat4::getX() { return IDLFloatArray::getValue(0); }
float IDLFloat4::getY() { return IDLFloatArray::getValue(1); }
float IDLFloat4::getZ() { return IDLFloatArray::getValue(2); }
float IDLFloat4::getW() { return IDLFloatArray::getValue(3); }

IDLDouble::IDLDouble() : IDLDoubleArray(1, false) {}
IDLDouble::IDLDouble(bool ownsData) : IDLDoubleArray(ownsData) {}

IDLDouble2::IDLDouble2() : IDLDoubleArray(2, false) {}
void IDLDouble2::set(double x, double y) {
    IDLDoubleArray::setValue(0, x);
    IDLDoubleArray::setValue(1, y);
}
double IDLDouble2::getX() { return IDLDoubleArray::getValue(0); }
double IDLDouble2::getY() { return IDLDoubleArray::getValue(1); }

IDLDouble3::IDLDouble3() : IDLDoubleArray(3, false) {}
void IDLDouble3::set(double x, double y, double z) {
    IDLDoubleArray::setValue(0, x);
    IDLDoubleArray::setValue(1, y);
    IDLDoubleArray::setValue(2, z);
}
double IDLDouble3::getX() { return IDLDoubleArray::getValue(0); }
double IDLDouble3::getY() { return IDLDoubleArray::getValue(1); }
double IDLDouble3::getZ() { return IDLDoubleArray::getValue(2); }

IDLDouble4::IDLDouble4() : IDLDoubleArray(4, false) {}
void IDLDouble4::set(double x, double y, double z, double w) {
    IDLDoubleArray::setValue(0, x);
    IDLDoubleArray::setValue(1, y);
    IDLDoubleArray::setValue(2, z);
    IDLDoubleArray::setValue(3, w);
}
double IDLDouble4::getX() { return IDLDoubleArray::getValue(0); }
double IDLDouble4::getY() { return IDLDoubleArray::getValue(1); }
double IDLDouble4::getZ() { return IDLDoubleArray::getValue(2); }
double IDLDouble4::getW() { return IDLDoubleArray::getValue(3); }

// IDLTemp static methods
IDLByteArray* IDLTemp::ByteArray_1(void* dataAddress, int size) {
    static IDLByteArray byteArray_temp(false);
    byteArray_temp.setData(dataAddress, size);
    return &byteArray_temp;
}
IDLBoolArray* IDLTemp::BoolArray_1(void* dataAddress, int size) {
    static IDLBoolArray boolArray_temp(false);
    boolArray_temp.setData(dataAddress, size);
    return &boolArray_temp;
}
IDLIntArray* IDLTemp::IntArray_1(void* dataAddress, int size) {
    static IDLIntArray intArray_temp(false);
    intArray_temp.setData(dataAddress, size);
    return &intArray_temp;
}
IDLLongArray* IDLTemp::LongArray_1(void* dataAddress, int size) {
    static IDLLongArray longArray_temp(false);
    longArray_temp.setData(dataAddress, size);
    return &longArray_temp;
}
IDLFloatArray* IDLTemp::FloatArray_1(void* dataAddress, int size) {
    static IDLFloatArray floatArray_temp(false);
    floatArray_temp.setData(dataAddress, size);
    return &floatArray_temp;
}
IDLDoubleArray* IDLTemp::DoubleArray_1(void* dataAddress, int size) {
    static IDLDoubleArray doubleArray_temp(false);
    doubleArray_temp.setData(dataAddress, size);
    return &doubleArray_temp;
}

IDLByte* IDLTemp::Byte_1(void* dataAddress) {
    static IDLByte byte_temp(false);
    byte_temp.setData(dataAddress, 1);
    return &byte_temp;
}
IDLBool* IDLTemp::Bool_1(void* dataAddress) {
    static IDLBool bool_temp(false);
    bool_temp.setData(dataAddress, 1);
    return &bool_temp;
}
IDLInt* IDLTemp::Int_1(void* dataAddress) {
    static IDLInt int_temp(false);
    int_temp.setData(dataAddress, 4);
    return &int_temp;
}
IDLLong* IDLTemp::Long_1(void* dataAddress) {
    static IDLLong long_temp(false);
    long_temp.setData(dataAddress, 8);
    return &long_temp;
}
IDLFloat* IDLTemp::Float_1(void* dataAddress) {
    static IDLFloat float_temp(false);
    float_temp.setData(dataAddress, 4);
    return &float_temp;
}
IDLDouble* IDLTemp::Double_1(void* dataAddress) {
    static IDLDouble double_temp(false);
    double_temp.setData(dataAddress, 8);
    return &double_temp;
}

IDLBool* IDLTemp::Bool_1(bool value) {
    static IDLBool bool_temp1;
    bool_temp1.set(value);
    return &bool_temp1;
}

IDLBool* IDLTemp::Bool_2(bool value) {
    static IDLBool bool_temp2;
    bool_temp2.set(value);
    return &bool_temp2;
}

IDLBool* IDLTemp::Bool_3(bool value) {
    static IDLBool bool_temp3;
    bool_temp3.set(value);
    return &bool_temp3;
}

IDLBool* IDLTemp::Bool_4(bool value) {
    static IDLBool bool_temp4;
    bool_temp4.set(value);
    return &bool_temp4;
}

IDLInt* IDLTemp::Int_1(int value) {
    static IDLInt int_temp1;
    int_temp1.set(value);
    return &int_temp1;
}

IDLInt* IDLTemp::Int_2(int value) {
    static IDLInt int_temp2;
    int_temp2.set(value);
    return &int_temp2;
}

IDLInt* IDLTemp::Int_3(int value) {
    static IDLInt int_temp3;
    int_temp3.set(value);
    return &int_temp3;
}

IDLInt* IDLTemp::Int_4(int value) {
    static IDLInt int_temp4;
    int_temp4.set(value);
    return &int_temp4;
}

IDLLong* IDLTemp::Long_1(long long value) {
    static IDLLong long_temp1;
    long_temp1.set(value);
    return &long_temp1;
}

IDLLong* IDLTemp::Long_2(long long value) {
    static IDLLong long_temp2;
    long_temp2.set(value);
    return &long_temp2;
}

IDLLong* IDLTemp::Long_3(long long value) {
    static IDLLong long_temp3;
    long_temp3.set(value);
    return &long_temp3;
}

IDLLong* IDLTemp::Long_4(long long value) {
    static IDLLong long_temp4;
    long_temp4.set(value);
    return &long_temp4;
}

IDLFloat* IDLTemp::Float_1(float value) {
    static IDLFloat float_temp1;
    float_temp1.set(value);
    return &float_temp1;
}

IDLFloat* IDLTemp::Float_2(float value) {
    static IDLFloat float_temp2;
    float_temp2.set(value);
    return &float_temp2;
}

IDLFloat* IDLTemp::Float_3(float value) {
    static IDLFloat float_temp3;
    float_temp3.set(value);
    return &float_temp3;
}

IDLFloat* IDLTemp::Float_4(float value) {
    static IDLFloat float_temp4;
    float_temp4.set(value);
    return &float_temp4;
}

IDLDouble* IDLTemp::Double_1(double value) {
    static IDLDouble double_temp1;
    double_temp1.set(value);
    return &double_temp1;
}

IDLDouble* IDLTemp::Double_2(double value) {
    static IDLDouble double_temp2;
    double_temp2.set(value);
    return &double_temp2;
}

IDLDouble* IDLTemp::Double_3(double value) {
    static IDLDouble double_temp3;
    double_temp3.set(value);
    return &double_temp3;
}

IDLDouble* IDLTemp::Double_4(double value) {
    static IDLDouble double_temp4;
    double_temp4.set(value);
    return &double_temp4;
}

IDLInt2* IDLTemp::Int2_1(int x, int y) {
    static IDLInt2 intArray2_temp1;
    intArray2_temp1.set(x, y);
    return &intArray2_temp1;
}

IDLInt2* IDLTemp::Int2_2(int x, int y) {
    static IDLInt2 intArray2_temp2;
    intArray2_temp2.set(x, y);
    return &intArray2_temp2;
}

IDLInt3* IDLTemp::Int3_1(int x, int y, int z) {
    static IDLInt3 intArray3_temp1;
    intArray3_temp1.set(x, y, z);
    return &intArray3_temp1;
}

IDLInt3* IDLTemp::Int3_2(int x, int y, int z) {
    static IDLInt3 intArray3_temp2;
    intArray3_temp2.set(x, y, z);
    return &intArray3_temp2;
}

IDLInt4* IDLTemp::Int4_1(int x, int y, int z, int w) {
    static IDLInt4 intArray4_temp1;
    intArray4_temp1.set(x, y, z, w);
    return &intArray4_temp1;
}

IDLInt4* IDLTemp::Int4_2(int x, int y, int z, int w) {
    static IDLInt4 intArray4_temp2;
    intArray4_temp2.set(x, y, z, w);
    return &intArray4_temp2;
}

IDLLong2* IDLTemp::Long2_1(long long x, long long y) {
    static IDLLong2 longArray2_temp1;
    longArray2_temp1.set(x, y);
    return &longArray2_temp1;
}

IDLLong2* IDLTemp::Long2_2(long long x, long long y) {
    static IDLLong2 longArray2_temp2;
    longArray2_temp2.set(x, y);
    return &longArray2_temp2;
}

IDLLong3* IDLTemp::Long3_1(long long x, long long y, long long z) {
    static IDLLong3 longArray3_temp1;
    longArray3_temp1.set(x, y, z);
    return &longArray3_temp1;
}

IDLLong3* IDLTemp::Long3_2(long long x, long long y, long long z) {
    static IDLLong3 longArray3_temp2;
    longArray3_temp2.set(x, y, z);
    return &longArray3_temp2;
}

IDLLong4* IDLTemp::Long4_1(long long x, long long y, long long z, long long w) {
    static IDLLong4 longArray4_temp1;
    longArray4_temp1.set(x, y, z, w);
    return &longArray4_temp1;
}

IDLLong4* IDLTemp::Long4_2(long long x, long long y, long long z, long long w) {
    static IDLLong4 longArray4_temp2;
    longArray4_temp2.set(x, y, z, w);
    return &longArray4_temp2;
}

IDLFloat2* IDLTemp::Float2_1(float x, float y) {
    static IDLFloat2 floatArray2_temp1;
    floatArray2_temp1.set(x, y);
    return &floatArray2_temp1;
}

IDLFloat2* IDLTemp::Float2_2(float x, float y) {
    static IDLFloat2 floatArray2_temp2;
    floatArray2_temp2.set(x, y);
    return &floatArray2_temp2;
}

IDLFloat3* IDLTemp::Float3_1(float x, float y, float z) {
    static IDLFloat3 floatArray3_temp1;
    floatArray3_temp1.set(x, y, z);
    return &floatArray3_temp1;
}

IDLFloat3* IDLTemp::Float3_2(float x, float y, float z) {
    static IDLFloat3 floatArray3_temp2;
    floatArray3_temp2.set(x, y, z);
    return &floatArray3_temp2;
}

IDLFloat4* IDLTemp::Float4_1(float x, float y, float z, float w) {
    static IDLFloat4 floatArray4_temp1;
    floatArray4_temp1.set(x, y, z, w);
    return &floatArray4_temp1;
}

IDLFloat4* IDLTemp::Float4_2(float x, float y, float z, float w) {
    static IDLFloat4 floatArray4_temp2;
    floatArray4_temp2.set(x, y, z, w);
    return &floatArray4_temp2;
}

IDLDouble2* IDLTemp::Double2_1(double x, double y) {
    static IDLDouble2 doubleArray2_temp1;
    doubleArray2_temp1.set(x, y);
    return &doubleArray2_temp1;
}

IDLDouble2* IDLTemp::Double2_2(double x, double y) {
    static IDLDouble2 doubleArray2_temp2;
    doubleArray2_temp2.set(x, y);
    return &doubleArray2_temp2;
}

IDLDouble3* IDLTemp::Double3_1(double x, double y, double z) {
    static IDLDouble3 doubleArray3_temp1;
    doubleArray3_temp1.set(x, y, z);
    return &doubleArray3_temp1;
}

IDLDouble3* IDLTemp::Double3_2(double x, double y, double z) {
    static IDLDouble3 doubleArray3_temp2;
    doubleArray3_temp2.set(x, y, z);
    return &doubleArray3_temp2;
}

IDLDouble4* IDLTemp::Double4_1(double x, double y, double z, double w) {
    static IDLDouble4 doubleArray4_temp1;
    doubleArray4_temp1.set(x, y, z, w);
    return &doubleArray4_temp1;
}

IDLDouble4* IDLTemp::Double4_2(double x, double y, double z, double w) {
    static IDLDouble4 doubleArray4_temp2;
    doubleArray4_temp2.set(x, y, z, w);
    return &doubleArray4_temp2;
}
