#pragma once

#include <string>
#include <vector>
#include <stddef.h>     // NULL
#include <stdint.h>     // intptr_t
#include <algorithm>
#include <assert.h>

namespace IDL
{

class IDLArray {
public:
    virtual void clear() = 0;
    virtual int getSize() = 0;
    virtual void resize(int newSize) = 0;
    virtual void* getVoidData() = 0;
    virtual bool ownsDataAddress() = 0;
    virtual void setData(void* newData, int size) = 0;
    virtual ~IDLArray() = default;
};

} // END IDL NAMESPACE

template<typename T>
class IDLArray : public IDL::IDLArray {
    private:
        bool ownsData = true;
        int size = 0;
        bool isResizeEnabled;
        T* data = nullptr;

        void deleteData() {
            if(ownsData && data != nullptr) {
                delete[] data;
            }
            data = nullptr;
            size = 0;
        }

    public:
        IDLArray(int size, bool isResizeEnabled = true) {
            this->isResizeEnabled = true;
            resize(size);
            this->isResizeEnabled = isResizeEnabled;
        }
        IDLArray(bool ownsData) {
            this->ownsData = ownsData;
            resize(0);
        }
        ~IDLArray() override {
            if(ownsData) {
                clear();
            }
            deleteData();
        }

        bool ownsDataAddress() override {
            return this->ownsData;
        }

        void setData(void* newData, int newSize) override {
            if(!ownsData) {
                size = newSize;
                data = static_cast<T*>(newData);
            }
        }

        int getSize() override { return this->size; }

        void* getVoidData() override { return (void*)data; }

        void resize(int newSize) override {
            if (!ownsData || !isResizeEnabled || newSize == size) return;
            if (newSize < 0) newSize = 0;

            if (newSize == 0) {
                clear();
                deleteData();
                return;
            }

            T* newData = new T[newSize]();
            int copySize = std::min(size, newSize);
            if (data && newData && copySize > 0) {
                std::copy(data, data + copySize, newData);
            }

            deleteData();
            data = newData;
            size = newSize;
        }

        void clear() override {
            if (!data || size == 0) return;
            if constexpr (std::is_pointer<T>::value) {
                std::fill(data, data + size, nullptr);
            } else {
                static_assert(std::is_default_constructible<T>::value, "T must be default constructible for clear()");
                std::fill(data, data + size, T());
            }
        }

        void copy(IDLArray<T>& src, int srcPos, int destPos, int length) {
            assert(srcPos >= 0 && destPos >= 0 && length >= 0 && srcPos + length <= src.size && destPos + length <= this->size);
            std::copy(src.data + srcPos, src.data + srcPos + length, this->data + destPos);
        }

        T getValue() {
            return *this->data;
        }

        void set(T value) {
            *this->data = value;
        }

        T getValue(int index) {
            assert(index >= 0 && index < this->size);
            return this->data[index];
        }

        T& getValueRef(int index) {
            assert(index >= 0 && index < this->size);
            return this->data[index];
        }

        T* getValuePtr(int index) {
            assert(index >= 0 && index < this->size);
            return &(this->data[index]);
        }

        void setValue(int index, T value) {
            assert(index >= 0 && index < this->size);
            this->data[index] = value;
        }

        void setValueRef(int index, T& value) {
            assert(index >= 0 && index < this->size);
            this->data[index] = value;
        }

        void setValuePtr(int index, T* value) {
            assert(index >= 0 && index < this->size);
            this->data[index] = *value;
        }
};

using IDLString = std::string;
using IDLStringView = std::string_view;
using IDLBoolArray = IDLArray<bool>;
using IDLIntArray = IDLArray<int>;
using IDLLongArray = IDLArray<long long>;
using IDLFloatArray = IDLArray<float>;
using IDLDoubleArray = IDLArray<double>;
using IDLByteArray = IDLArray<char>;

class IDLBool : public IDLBoolArray {
    public:
        IDLBool() : IDLBoolArray(1, false) {}
        IDLBool(bool ownsData) : IDLBoolArray(ownsData) {}
};

class IDLByte : public IDLByteArray {
    public:
        IDLByte() : IDLByteArray(1, false) {}
        IDLByte(bool ownsData) : IDLByteArray(ownsData) {}
};

class IDLInt : public IDLIntArray {
    public:
        IDLInt() : IDLIntArray(1, false) {}
        IDLInt(bool ownsData) : IDLIntArray(ownsData) {}
};

class IDLInt2 : public IDLIntArray {
    public:
        IDLInt2() : IDLIntArray(2, false) {}

        void set(int x, int y) {
            IDLIntArray::setValue(0, x);
            IDLIntArray::setValue(1, y);
        }

        int getX() { return IDLIntArray::getValue(0); }
        int getY() { return IDLIntArray::getValue(1); }
};

class IDLInt3 : public IDLIntArray {
    public:
        IDLInt3() : IDLIntArray(3, false) {}

        void set(int x, int y, int z) {
            IDLIntArray::setValue(0, x);
            IDLIntArray::setValue(1, y);
            IDLIntArray::setValue(2, z);
        }

        int getX() { return IDLIntArray::getValue(0); }
        int getY() { return IDLIntArray::getValue(1); }
        int getZ() { return IDLIntArray::getValue(2); }
};

class IDLInt4 : public IDLIntArray {
    public:
        IDLInt4() : IDLIntArray(4, false) {}

        void set(int x, int y, int z, int w) {
            IDLIntArray::setValue(0, x);
            IDLIntArray::setValue(1, y);
            IDLIntArray::setValue(2, z);
            IDLIntArray::setValue(3, w);
        }

        int getX() { return IDLIntArray::getValue(0); }
        int getY() { return IDLIntArray::getValue(1); }
        int getZ() { return IDLIntArray::getValue(2); }
        int getW() { return IDLIntArray::getValue(3); }
};

class IDLLong : public IDLLongArray {
    public:
        IDLLong() : IDLLongArray(1, false) {}
        IDLLong(bool ownsData) : IDLLongArray(ownsData) {}
};

class IDLLong2 : public IDLLongArray {
    public:
        IDLLong2() : IDLLongArray(2, false) {}

        void set(long long x, long long y) {
            IDLLongArray::setValue(0, x);
            IDLLongArray::setValue(1, y);
        }

        long long getX() { return IDLLongArray::getValue(0); }
        long long getY() { return IDLLongArray::getValue(1); }
};

class IDLLong3 : public IDLLongArray {
    public:
        IDLLong3() : IDLLongArray(3, false) {}

        void set(long long x, long long y, long long z) {
            IDLLongArray::setValue(0, x);
            IDLLongArray::setValue(1, y);
            IDLLongArray::setValue(2, z);
        }

        long long getX() { return IDLLongArray::getValue(0); }
        long long getY() { return IDLLongArray::getValue(1); }
        long long getZ() { return IDLLongArray::getValue(2); }
};

class IDLLong4 : public IDLLongArray {
    public:
        IDLLong4() : IDLLongArray(4, false) {}

        void set(long long x, long long y, long long z, long long w) {
            IDLLongArray::setValue(0, x);
            IDLLongArray::setValue(1, y);
            IDLLongArray::setValue(2, z);
            IDLLongArray::setValue(3, w);
        }

        long long getX() { return IDLLongArray::getValue(0); }
        long long getY() { return IDLLongArray::getValue(1); }
        long long getZ() { return IDLLongArray::getValue(2); }
        long long getW() { return IDLLongArray::getValue(3); }
};

class IDLFloat : public IDLFloatArray {
    public:
        IDLFloat() : IDLFloatArray(1, false) {}
        IDLFloat(bool ownsData) : IDLFloatArray(ownsData) {}
};

class IDLFloat2 : public IDLFloatArray {
    public:
        IDLFloat2() : IDLFloatArray(2, false) {}

        void set(float x, float y) {
            IDLFloatArray::setValue(0, x);
            IDLFloatArray::setValue(1, y);
        }

        float getX() { return IDLFloatArray::getValue(0); }
        float getY() { return IDLFloatArray::getValue(1); }
};

class IDLFloat3 : public IDLFloatArray {
    public:
        IDLFloat3() : IDLFloatArray(3, false) {}

        void set(float x, float y, float z) {
            IDLFloatArray::setValue(0, x);
            IDLFloatArray::setValue(1, y);
            IDLFloatArray::setValue(2, z);
        }

        float getX() { return IDLFloatArray::getValue(0); }
        float getY() { return IDLFloatArray::getValue(1); }
        float getZ() { return IDLFloatArray::getValue(2); }
};

class IDLFloat4 : public IDLFloatArray {
    public:
        IDLFloat4() : IDLFloatArray(4, false) {}

        void set(float x, float y, float z, float w) {
            IDLFloatArray::setValue(0, x);
            IDLFloatArray::setValue(1, y);
            IDLFloatArray::setValue(2, z);
            IDLFloatArray::setValue(3, w);
        }

        float getX() { return IDLFloatArray::getValue(0); }
        float getY() { return IDLFloatArray::getValue(1); }
        float getZ() { return IDLFloatArray::getValue(2); }
        float getW() { return IDLFloatArray::getValue(3); }
};

class IDLDouble : public IDLDoubleArray {
    public:
        IDLDouble() : IDLDoubleArray(1, false) {}
        IDLDouble(bool ownsData) : IDLDoubleArray(ownsData) {}
};

class IDLDouble2 : public IDLDoubleArray {
    public:
        IDLDouble2() : IDLDoubleArray(2, false) {}

        void set(double x, double y) {
            IDLDoubleArray::setValue(0, x);
            IDLDoubleArray::setValue(1, y);
        }

        double getX() { return IDLDoubleArray::getValue(0); }
        double getY() { return IDLDoubleArray::getValue(1); }
};

class IDLDouble3 : public IDLDoubleArray {
    public:
        IDLDouble3() : IDLDoubleArray(3, false) {}

        void set(double x, double y, double z) {
            IDLDoubleArray::setValue(0, x);
            IDLDoubleArray::setValue(1, y);
            IDLDoubleArray::setValue(2, z);
        }

        double getX() { return IDLDoubleArray::getValue(0); }
        double getY() { return IDLDoubleArray::getValue(1); }
        double getZ() { return IDLDoubleArray::getValue(2); }
};

class IDLDouble4 : public IDLDoubleArray {
    public:
        IDLDouble4() : IDLDoubleArray(4, false) {}

        void set(double x, double y, double z, double w) {
            IDLDoubleArray::setValue(0, x);
            IDLDoubleArray::setValue(1, y);
            IDLDoubleArray::setValue(2, z);
            IDLDoubleArray::setValue(3, w);
        }

        double getX() { return IDLDoubleArray::getValue(0); }
        double getY() { return IDLDoubleArray::getValue(1); }
        double getZ() { return IDLDoubleArray::getValue(2); }
        double getW() { return IDLDoubleArray::getValue(3); }
};

class IDLTemp {
    public:
        static IDLByteArray* ByteArray_1(void* dataAddress, int size) {
            static IDLByteArray byteArray_temp(false);
            byteArray_temp.setData(dataAddress, size);
            return &byteArray_temp;
        }
        static IDLBoolArray* BoolArray_1(void* dataAddress, int size) {
            static IDLBoolArray boolArray_temp(false);
            boolArray_temp.setData(dataAddress, size);
            return &boolArray_temp;
        }
        static IDLIntArray* IntArray_1(void* dataAddress, int size) {
            static IDLIntArray intArray_temp(false);
            intArray_temp.setData(dataAddress, size);
            return &intArray_temp;
        }
        static IDLLongArray* LongArray_1(void* dataAddress, int size) {
            static IDLLongArray longArray_temp(false);
            longArray_temp.setData(dataAddress, size);
            return &longArray_temp;
        }
        static IDLFloatArray* FloatArray_1(void* dataAddress, int size) {
            static IDLFloatArray floatArray_temp(false);
            floatArray_temp.setData(dataAddress, size);
            return &floatArray_temp;
        }
        static IDLDoubleArray* DoubleArray_1(void* dataAddress, int size) {
            static IDLDoubleArray doubleArray_temp(false);
            doubleArray_temp.setData(dataAddress, size);
            return &doubleArray_temp;
        }

        static IDLByte* Byte_1(void* dataAddress) {
            static IDLByte byte_temp(false);
            byte_temp.setData(dataAddress, 1);
            return &byte_temp;
        }
        static IDLBool* Bool_1(void* dataAddress) {
            static IDLBool bool_temp(false);
            bool_temp.setData(dataAddress, 1);
            return &bool_temp;
        }
        static IDLInt* Int_1(void* dataAddress) {
            static IDLInt int_temp(false);
            int_temp.setData(dataAddress, 4);
            return &int_temp;
        }
        static IDLLong* Long_1(void* dataAddress) {
            static IDLLong long_temp(false);
            long_temp.setData(dataAddress, 8);
            return &long_temp;
        }
        static IDLFloat* Float_1(void* dataAddress) {
            static IDLFloat float_temp(false);
            float_temp.setData(dataAddress, 4);
            return &float_temp;
        }
        static IDLDouble* Double_1(void* dataAddress) {
            static IDLDouble double_temp(false);
            double_temp.setData(dataAddress, 8);
            return &double_temp;
        }

        static IDLBool* Bool_1(bool value) {
            static IDLBool bool_temp1;
            bool_temp1.set(value);
            return &bool_temp1;
        }

        static IDLBool* Bool_2(bool value) {
            static IDLBool bool_temp2;
            bool_temp2.set(value);
            return &bool_temp2;
        }

        static IDLBool* Bool_3(bool value) {
            static IDLBool bool_temp3;
            bool_temp3.set(value);
            return &bool_temp3;
        }

        static IDLBool* Bool_4(bool value) {
            static IDLBool bool_temp4;
            bool_temp4.set(value);
            return &bool_temp4;
        }

        static IDLInt* Int_1(int value) {
            static IDLInt int_temp1;
            int_temp1.set(value);
            return &int_temp1;
        }

        static IDLInt* Int_2(int value) {
            static IDLInt int_temp2;
            int_temp2.set(value);
            return &int_temp2;
        }

        static IDLInt* Int_3(int value) {
            static IDLInt int_temp3;
            int_temp3.set(value);
            return &int_temp3;
        }

        static IDLInt* Int_4(int value) {
            static IDLInt int_temp4;
            int_temp4.set(value);
            return &int_temp4;
        }

        static IDLLong* Long_1(long long value) {
            static IDLLong long_temp1;
            long_temp1.set(value);
            return &long_temp1;
        }

        static IDLLong* Long_2(long long value) {
            static IDLLong long_temp2;
            long_temp2.set(value);
            return &long_temp2;
        }

        static IDLLong* Long_3(long long value) {
            static IDLLong long_temp3;
            long_temp3.set(value);
            return &long_temp3;
        }

        static IDLLong* Long_4(long long value) {
            static IDLLong long_temp4;
            long_temp4.set(value);
            return &long_temp4;
        }

        static IDLFloat* Float_1(float value) {
            static IDLFloat float_temp1;
            float_temp1.set(value);
            return &float_temp1;
        }

        static IDLFloat* Float_2(float value) {
            static IDLFloat float_temp2;
            float_temp2.set(value);
            return &float_temp2;
        }

        static IDLFloat* Float_3(float value) {
            static IDLFloat float_temp3;
            float_temp3.set(value);
            return &float_temp3;
        }

        static IDLFloat* Float_4(float value) {
            static IDLFloat float_temp4;
            float_temp4.set(value);
            return &float_temp4;
        }

        static IDLDouble* Double_1(double value) {
            static IDLDouble double_temp1;
            double_temp1.set(value);
            return &double_temp1;
        }

        static IDLDouble* Double_2(double value) {
            static IDLDouble double_temp2;
            double_temp2.set(value);
            return &double_temp2;
        }

        static IDLDouble* Double_3(double value) {
            static IDLDouble double_temp3;
            double_temp3.set(value);
            return &double_temp3;
        }

        static IDLDouble* Double_4(double value) {
            static IDLDouble double_temp4;
            double_temp4.set(value);
            return &double_temp4;
        }

        static IDLInt2* Int2_1(int x, int y) {
            static IDLInt2 intArray2_temp1;
            intArray2_temp1.set(x, y);
            return &intArray2_temp1;
        }

        static IDLInt2* Int2_2(int x, int y) {
            static IDLInt2 intArray2_temp2;
            intArray2_temp2.set(x, y);
            return &intArray2_temp2;
        }

        static IDLInt3* Int3_1(int x, int y, int z) {
            static IDLInt3 intArray3_temp1;
            intArray3_temp1.set(x, y, z);
            return &intArray3_temp1;
        }

        static IDLInt3* Int3_2(int x, int y, int z) {
            static IDLInt3 intArray3_temp2;
            intArray3_temp2.set(x, y, z);
            return &intArray3_temp2;
        }

        static IDLInt4* Int4_1(int x, int y, int z, int w) {
            static IDLInt4 intArray4_temp1;
            intArray4_temp1.set(x, y, z, w);
            return &intArray4_temp1;
        }

        static IDLInt4* Int4_2(int x, int y, int z, int w) {
            static IDLInt4 intArray4_temp2;
            intArray4_temp2.set(x, y, z, w);
            return &intArray4_temp2;
        }

        static IDLLong2* Long2_1(long long x, long long y) {
            static IDLLong2 longArray2_temp1;
            longArray2_temp1.set(x, y);
            return &longArray2_temp1;
        }

        static IDLLong2* Long2_2(long long x, long long y) {
            static IDLLong2 longArray2_temp2;
            longArray2_temp2.set(x, y);
            return &longArray2_temp2;
        }

        static IDLLong3* Long3_1(long long x, long long y, long long z) {
            static IDLLong3 longArray3_temp1;
            longArray3_temp1.set(x, y, z);
            return &longArray3_temp1;
        }

        static IDLLong3* Long3_2(long long x, long long y, long long z) {
            static IDLLong3 longArray3_temp2;
            longArray3_temp2.set(x, y, z);
            return &longArray3_temp2;
        }

        static IDLLong4* Long4_1(long long x, long long y, long long z, long long w) {
            static IDLLong4 longArray4_temp1;
            longArray4_temp1.set(x, y, z, w);
            return &longArray4_temp1;
        }

        static IDLLong4* Long4_2(long long x, long long y, long long z, long long w) {
            static IDLLong4 longArray4_temp2;
            longArray4_temp2.set(x, y, z, w);
            return &longArray4_temp2;
        }

        static IDLFloat2* Float2_1(float x, float y) {
            static IDLFloat2 floatArray2_temp1;
            floatArray2_temp1.set(x, y);
            return &floatArray2_temp1;
        }

        static IDLFloat2* Float2_2(float x, float y) {
            static IDLFloat2 floatArray2_temp2;
            floatArray2_temp2.set(x, y);
            return &floatArray2_temp2;
        }

        static IDLFloat3* Float3_1(float x, float y, float z) {
            static IDLFloat3 floatArray3_temp1;
            floatArray3_temp1.set(x, y, z);
            return &floatArray3_temp1;
        }

        static IDLFloat3* Float3_2(float x, float y, float z) {
            static IDLFloat3 floatArray3_temp2;
            floatArray3_temp2.set(x, y, z);
            return &floatArray3_temp2;
        }

        static IDLFloat4* Float4_1(float x, float y, float z, float w) {
            static IDLFloat4 floatArray4_temp1;
            floatArray4_temp1.set(x, y, z, w);
            return &floatArray4_temp1;
        }

        static IDLFloat4* Float4_2(float x, float y, float z, float w) {
            static IDLFloat4 floatArray4_temp2;
            floatArray4_temp2.set(x, y, z, w);
            return &floatArray4_temp2;
        }

        static IDLDouble2* Double2_1(double x, double y) {
            static IDLDouble2 doubleArray2_temp1;
            doubleArray2_temp1.set(x, y);
            return &doubleArray2_temp1;
        }

        static IDLDouble2* Double2_2(double x, double y) {
            static IDLDouble2 doubleArray2_temp2;
            doubleArray2_temp2.set(x, y);
            return &doubleArray2_temp2;
        }

        static IDLDouble3* Double3_1(double x, double y, double z) {
            static IDLDouble3 doubleArray3_temp1;
            doubleArray3_temp1.set(x, y, z);
            return &doubleArray3_temp1;
        }

        static IDLDouble3* Double3_2(double x, double y, double z) {
            static IDLDouble3 doubleArray3_temp2;
            doubleArray3_temp2.set(x, y, z);
            return &doubleArray3_temp2;
        }

        static IDLDouble4* Double4_1(double x, double y, double z, double w) {
            static IDLDouble4 doubleArray4_temp1;
            doubleArray4_temp1.set(x, y, z, w);
            return &doubleArray4_temp1;
        }

        static IDLDouble4* Double4_2(double x, double y, double z, double w) {
            static IDLDouble4 doubleArray4_temp2;
            doubleArray4_temp2.set(x, y, z, w);
            return &doubleArray4_temp2;
        }
};