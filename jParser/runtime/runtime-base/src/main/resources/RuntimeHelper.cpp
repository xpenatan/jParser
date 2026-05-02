#include "RuntimeHelper.h"

NativeBool::NativeBool() : NativeBoolArray(1, false) {}
NativeBool::NativeBool(bool ownsData) : NativeBoolArray(ownsData) {}

NativeByte::NativeByte() : NativeByteArray(1, false) {}
NativeByte::NativeByte(bool ownsData) : NativeByteArray(ownsData) {}

NativeInt::NativeInt() : NativeIntArray(1, false) {}
NativeInt::NativeInt(bool ownsData) : NativeIntArray(ownsData) {}

NativeInt2::NativeInt2() : NativeIntArray(2, false) {}
void NativeInt2::set(int x, int y) {
    NativeIntArray::setValue(0, x);
    NativeIntArray::setValue(1, y);
}
int NativeInt2::getX() { return NativeIntArray::getValue(0); }
int NativeInt2::getY() { return NativeIntArray::getValue(1); }

NativeInt3::NativeInt3() : NativeIntArray(3, false) {}
void NativeInt3::set(int x, int y, int z) {
    NativeIntArray::setValue(0, x);
    NativeIntArray::setValue(1, y);
    NativeIntArray::setValue(2, z);
}
int NativeInt3::getX() { return NativeIntArray::getValue(0); }
int NativeInt3::getY() { return NativeIntArray::getValue(1); }
int NativeInt3::getZ() { return NativeIntArray::getValue(2); }

NativeInt4::NativeInt4() : NativeIntArray(4, false) {}
void NativeInt4::set(int x, int y, int z, int w) {
    NativeIntArray::setValue(0, x);
    NativeIntArray::setValue(1, y);
    NativeIntArray::setValue(2, z);
    NativeIntArray::setValue(3, w);
}
int NativeInt4::getX() { return NativeIntArray::getValue(0); }
int NativeInt4::getY() { return NativeIntArray::getValue(1); }
int NativeInt4::getZ() { return NativeIntArray::getValue(2); }
int NativeInt4::getW() { return NativeIntArray::getValue(3); }

NativeLong::NativeLong() : NativeLongArray(1, false) {}
NativeLong::NativeLong(bool ownsData) : NativeLongArray(ownsData) {}

NativeLong2::NativeLong2() : NativeLongArray(2, false) {}
void NativeLong2::set(long long x, long long y) {
    NativeLongArray::setValue(0, x);
    NativeLongArray::setValue(1, y);
}
long long NativeLong2::getX() { return NativeLongArray::getValue(0); }
long long NativeLong2::getY() { return NativeLongArray::getValue(1); }

NativeLong3::NativeLong3() : NativeLongArray(3, false) {}
void NativeLong3::set(long long x, long long y, long long z) {
    NativeLongArray::setValue(0, x);
    NativeLongArray::setValue(1, y);
    NativeLongArray::setValue(2, z);
}
long long NativeLong3::getX() { return NativeLongArray::getValue(0); }
long long NativeLong3::getY() { return NativeLongArray::getValue(1); }
long long NativeLong3::getZ() { return NativeLongArray::getValue(2); }

NativeLong4::NativeLong4() : NativeLongArray(4, false) {}
void NativeLong4::set(long long x, long long y, long long z, long long w) {
    NativeLongArray::setValue(0, x);
    NativeLongArray::setValue(1, y);
    NativeLongArray::setValue(2, z);
    NativeLongArray::setValue(3, w);
}
long long NativeLong4::getX() { return NativeLongArray::getValue(0); }
long long NativeLong4::getY() { return NativeLongArray::getValue(1); }
long long NativeLong4::getZ() { return NativeLongArray::getValue(2); }
long long NativeLong4::getW() { return NativeLongArray::getValue(3); }

NativeFloat::NativeFloat() : NativeFloatArray(1, false) {}
NativeFloat::NativeFloat(bool ownsData) : NativeFloatArray(ownsData) {}

NativeFloat2::NativeFloat2() : NativeFloatArray(2, false) {}
void NativeFloat2::set(float x, float y) {
    NativeFloatArray::setValue(0, x);
    NativeFloatArray::setValue(1, y);
}
float NativeFloat2::getX() { return NativeFloatArray::getValue(0); }
float NativeFloat2::getY() { return NativeFloatArray::getValue(1); }

NativeFloat3::NativeFloat3() : NativeFloatArray(3, false) {}
void NativeFloat3::set(float x, float y, float z) {
    NativeFloatArray::setValue(0, x);
    NativeFloatArray::setValue(1, y);
    NativeFloatArray::setValue(2, z);
}
float NativeFloat3::getX() { return NativeFloatArray::getValue(0); }
float NativeFloat3::getY() { return NativeFloatArray::getValue(1); }
float NativeFloat3::getZ() { return NativeFloatArray::getValue(2); }

NativeFloat4::NativeFloat4() : NativeFloatArray(4, false) {}
void NativeFloat4::set(float x, float y, float z, float w) {
    NativeFloatArray::setValue(0, x);
    NativeFloatArray::setValue(1, y);
    NativeFloatArray::setValue(2, z);
    NativeFloatArray::setValue(3, w);
}
float NativeFloat4::getX() { return NativeFloatArray::getValue(0); }
float NativeFloat4::getY() { return NativeFloatArray::getValue(1); }
float NativeFloat4::getZ() { return NativeFloatArray::getValue(2); }
float NativeFloat4::getW() { return NativeFloatArray::getValue(3); }

NativeDouble::NativeDouble() : NativeDoubleArray(1, false) {}
NativeDouble::NativeDouble(bool ownsData) : NativeDoubleArray(ownsData) {}

NativeDouble2::NativeDouble2() : NativeDoubleArray(2, false) {}
void NativeDouble2::set(double x, double y) {
    NativeDoubleArray::setValue(0, x);
    NativeDoubleArray::setValue(1, y);
}
double NativeDouble2::getX() { return NativeDoubleArray::getValue(0); }
double NativeDouble2::getY() { return NativeDoubleArray::getValue(1); }

NativeDouble3::NativeDouble3() : NativeDoubleArray(3, false) {}
void NativeDouble3::set(double x, double y, double z) {
    NativeDoubleArray::setValue(0, x);
    NativeDoubleArray::setValue(1, y);
    NativeDoubleArray::setValue(2, z);
}
double NativeDouble3::getX() { return NativeDoubleArray::getValue(0); }
double NativeDouble3::getY() { return NativeDoubleArray::getValue(1); }
double NativeDouble3::getZ() { return NativeDoubleArray::getValue(2); }

NativeDouble4::NativeDouble4() : NativeDoubleArray(4, false) {}
void NativeDouble4::set(double x, double y, double z, double w) {
    NativeDoubleArray::setValue(0, x);
    NativeDoubleArray::setValue(1, y);
    NativeDoubleArray::setValue(2, z);
    NativeDoubleArray::setValue(3, w);
}
double NativeDouble4::getX() { return NativeDoubleArray::getValue(0); }
double NativeDouble4::getY() { return NativeDoubleArray::getValue(1); }
double NativeDouble4::getZ() { return NativeDoubleArray::getValue(2); }
double NativeDouble4::getW() { return NativeDoubleArray::getValue(3); }

// NativeTemp static methods
NativeByteArray* NativeTemp::ByteArray_1(void* dataAddress, int size) {
    static NativeByteArray byteArray_temp(false);
    byteArray_temp.setData(dataAddress, size);
    return &byteArray_temp;
}
NativeBoolArray* NativeTemp::BoolArray_1(void* dataAddress, int size) {
    static NativeBoolArray boolArray_temp(false);
    boolArray_temp.setData(dataAddress, size);
    return &boolArray_temp;
}
NativeIntArray* NativeTemp::IntArray_1(void* dataAddress, int size) {
    static NativeIntArray intArray_temp(false);
    intArray_temp.setData(dataAddress, size);
    return &intArray_temp;
}
NativeLongArray* NativeTemp::LongArray_1(void* dataAddress, int size) {
    static NativeLongArray longArray_temp(false);
    longArray_temp.setData(dataAddress, size);
    return &longArray_temp;
}
NativeFloatArray* NativeTemp::FloatArray_1(void* dataAddress, int size) {
    static NativeFloatArray floatArray_temp(false);
    floatArray_temp.setData(dataAddress, size);
    return &floatArray_temp;
}
NativeDoubleArray* NativeTemp::DoubleArray_1(void* dataAddress, int size) {
    static NativeDoubleArray doubleArray_temp(false);
    doubleArray_temp.setData(dataAddress, size);
    return &doubleArray_temp;
}

NativeByte* NativeTemp::Byte_1(void* dataAddress) {
    static NativeByte byte_temp(false);
    byte_temp.setData(dataAddress, 1);
    return &byte_temp;
}
NativeBool* NativeTemp::Bool_1(void* dataAddress) {
    static NativeBool bool_temp(false);
    bool_temp.setData(dataAddress, 1);
    return &bool_temp;
}
NativeInt* NativeTemp::Int_1(void* dataAddress) {
    static NativeInt int_temp(false);
    int_temp.setData(dataAddress, 4);
    return &int_temp;
}
NativeLong* NativeTemp::Long_1(void* dataAddress) {
    static NativeLong long_temp(false);
    long_temp.setData(dataAddress, 8);
    return &long_temp;
}
NativeFloat* NativeTemp::Float_1(void* dataAddress) {
    static NativeFloat float_temp(false);
    float_temp.setData(dataAddress, 4);
    return &float_temp;
}
NativeDouble* NativeTemp::Double_1(void* dataAddress) {
    static NativeDouble double_temp(false);
    double_temp.setData(dataAddress, 8);
    return &double_temp;
}

NativeBool* NativeTemp::Bool_1(bool value) {
    static NativeBool bool_temp1;
    bool_temp1.set(value);
    return &bool_temp1;
}

NativeBool* NativeTemp::Bool_2(bool value) {
    static NativeBool bool_temp2;
    bool_temp2.set(value);
    return &bool_temp2;
}

NativeBool* NativeTemp::Bool_3(bool value) {
    static NativeBool bool_temp3;
    bool_temp3.set(value);
    return &bool_temp3;
}

NativeBool* NativeTemp::Bool_4(bool value) {
    static NativeBool bool_temp4;
    bool_temp4.set(value);
    return &bool_temp4;
}

NativeInt* NativeTemp::Int_1(int value) {
    static NativeInt int_temp1;
    int_temp1.set(value);
    return &int_temp1;
}

NativeInt* NativeTemp::Int_2(int value) {
    static NativeInt int_temp2;
    int_temp2.set(value);
    return &int_temp2;
}

NativeInt* NativeTemp::Int_3(int value) {
    static NativeInt int_temp3;
    int_temp3.set(value);
    return &int_temp3;
}

NativeInt* NativeTemp::Int_4(int value) {
    static NativeInt int_temp4;
    int_temp4.set(value);
    return &int_temp4;
}

NativeLong* NativeTemp::Long_1(long long value) {
    static NativeLong long_temp1;
    long_temp1.set(value);
    return &long_temp1;
}

NativeLong* NativeTemp::Long_2(long long value) {
    static NativeLong long_temp2;
    long_temp2.set(value);
    return &long_temp2;
}

NativeLong* NativeTemp::Long_3(long long value) {
    static NativeLong long_temp3;
    long_temp3.set(value);
    return &long_temp3;
}

NativeLong* NativeTemp::Long_4(long long value) {
    static NativeLong long_temp4;
    long_temp4.set(value);
    return &long_temp4;
}

NativeFloat* NativeTemp::Float_1(float value) {
    static NativeFloat float_temp1;
    float_temp1.set(value);
    return &float_temp1;
}

NativeFloat* NativeTemp::Float_2(float value) {
    static NativeFloat float_temp2;
    float_temp2.set(value);
    return &float_temp2;
}

NativeFloat* NativeTemp::Float_3(float value) {
    static NativeFloat float_temp3;
    float_temp3.set(value);
    return &float_temp3;
}

NativeFloat* NativeTemp::Float_4(float value) {
    static NativeFloat float_temp4;
    float_temp4.set(value);
    return &float_temp4;
}

NativeDouble* NativeTemp::Double_1(double value) {
    static NativeDouble double_temp1;
    double_temp1.set(value);
    return &double_temp1;
}

NativeDouble* NativeTemp::Double_2(double value) {
    static NativeDouble double_temp2;
    double_temp2.set(value);
    return &double_temp2;
}

NativeDouble* NativeTemp::Double_3(double value) {
    static NativeDouble double_temp3;
    double_temp3.set(value);
    return &double_temp3;
}

NativeDouble* NativeTemp::Double_4(double value) {
    static NativeDouble double_temp4;
    double_temp4.set(value);
    return &double_temp4;
}

NativeInt2* NativeTemp::Int2_1(int x, int y) {
    static NativeInt2 intArray2_temp1;
    intArray2_temp1.set(x, y);
    return &intArray2_temp1;
}

NativeInt2* NativeTemp::Int2_2(int x, int y) {
    static NativeInt2 intArray2_temp2;
    intArray2_temp2.set(x, y);
    return &intArray2_temp2;
}

NativeInt3* NativeTemp::Int3_1(int x, int y, int z) {
    static NativeInt3 intArray3_temp1;
    intArray3_temp1.set(x, y, z);
    return &intArray3_temp1;
}

NativeInt3* NativeTemp::Int3_2(int x, int y, int z) {
    static NativeInt3 intArray3_temp2;
    intArray3_temp2.set(x, y, z);
    return &intArray3_temp2;
}

NativeInt4* NativeTemp::Int4_1(int x, int y, int z, int w) {
    static NativeInt4 intArray4_temp1;
    intArray4_temp1.set(x, y, z, w);
    return &intArray4_temp1;
}

NativeInt4* NativeTemp::Int4_2(int x, int y, int z, int w) {
    static NativeInt4 intArray4_temp2;
    intArray4_temp2.set(x, y, z, w);
    return &intArray4_temp2;
}

NativeLong2* NativeTemp::Long2_1(long long x, long long y) {
    static NativeLong2 longArray2_temp1;
    longArray2_temp1.set(x, y);
    return &longArray2_temp1;
}

NativeLong2* NativeTemp::Long2_2(long long x, long long y) {
    static NativeLong2 longArray2_temp2;
    longArray2_temp2.set(x, y);
    return &longArray2_temp2;
}

NativeLong3* NativeTemp::Long3_1(long long x, long long y, long long z) {
    static NativeLong3 longArray3_temp1;
    longArray3_temp1.set(x, y, z);
    return &longArray3_temp1;
}

NativeLong3* NativeTemp::Long3_2(long long x, long long y, long long z) {
    static NativeLong3 longArray3_temp2;
    longArray3_temp2.set(x, y, z);
    return &longArray3_temp2;
}

NativeLong4* NativeTemp::Long4_1(long long x, long long y, long long z, long long w) {
    static NativeLong4 longArray4_temp1;
    longArray4_temp1.set(x, y, z, w);
    return &longArray4_temp1;
}

NativeLong4* NativeTemp::Long4_2(long long x, long long y, long long z, long long w) {
    static NativeLong4 longArray4_temp2;
    longArray4_temp2.set(x, y, z, w);
    return &longArray4_temp2;
}

NativeFloat2* NativeTemp::Float2_1(float x, float y) {
    static NativeFloat2 floatArray2_temp1;
    floatArray2_temp1.set(x, y);
    return &floatArray2_temp1;
}

NativeFloat2* NativeTemp::Float2_2(float x, float y) {
    static NativeFloat2 floatArray2_temp2;
    floatArray2_temp2.set(x, y);
    return &floatArray2_temp2;
}

NativeFloat3* NativeTemp::Float3_1(float x, float y, float z) {
    static NativeFloat3 floatArray3_temp1;
    floatArray3_temp1.set(x, y, z);
    return &floatArray3_temp1;
}

NativeFloat3* NativeTemp::Float3_2(float x, float y, float z) {
    static NativeFloat3 floatArray3_temp2;
    floatArray3_temp2.set(x, y, z);
    return &floatArray3_temp2;
}

NativeFloat4* NativeTemp::Float4_1(float x, float y, float z, float w) {
    static NativeFloat4 floatArray4_temp1;
    floatArray4_temp1.set(x, y, z, w);
    return &floatArray4_temp1;
}

NativeFloat4* NativeTemp::Float4_2(float x, float y, float z, float w) {
    static NativeFloat4 floatArray4_temp2;
    floatArray4_temp2.set(x, y, z, w);
    return &floatArray4_temp2;
}

NativeDouble2* NativeTemp::Double2_1(double x, double y) {
    static NativeDouble2 doubleArray2_temp1;
    doubleArray2_temp1.set(x, y);
    return &doubleArray2_temp1;
}

NativeDouble2* NativeTemp::Double2_2(double x, double y) {
    static NativeDouble2 doubleArray2_temp2;
    doubleArray2_temp2.set(x, y);
    return &doubleArray2_temp2;
}

NativeDouble3* NativeTemp::Double3_1(double x, double y, double z) {
    static NativeDouble3 doubleArray3_temp1;
    doubleArray3_temp1.set(x, y, z);
    return &doubleArray3_temp1;
}

NativeDouble3* NativeTemp::Double3_2(double x, double y, double z) {
    static NativeDouble3 doubleArray3_temp2;
    doubleArray3_temp2.set(x, y, z);
    return &doubleArray3_temp2;
}

NativeDouble4* NativeTemp::Double4_1(double x, double y, double z, double w) {
    static NativeDouble4 doubleArray4_temp1;
    doubleArray4_temp1.set(x, y, z, w);
    return &doubleArray4_temp1;
}

NativeDouble4* NativeTemp::Double4_2(double x, double y, double z, double w) {
    static NativeDouble4 doubleArray4_temp2;
    doubleArray4_temp2.set(x, y, z, w);
    return &doubleArray4_temp2;
}
