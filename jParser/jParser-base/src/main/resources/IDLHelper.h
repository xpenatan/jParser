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
    virtual ~IDLArray() = default;
};

} // END IDL NAMESPACE

template<typename T>
class IDLArray : public IDL::IDLArray {
    private:
        int size = 0;
        bool isResizeEnabled;
        T* data = nullptr;

        void deleteData() {
            delete[] data;
            data = nullptr;
            size = 0;
        }

    public:
        IDLArray(int size, bool isResizeEnabled = true) {
            this->isResizeEnabled = true;
            resize(size);
            this->isResizeEnabled = isResizeEnabled;
        }
        ~IDLArray() override {
            clear();
            deleteData();
        }

        int getSize() override { return this->size; }

        void* getVoidData() override { return (void*)data; }

        void resize(int newSize) override {
            if (!isResizeEnabled || newSize == size) return;
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

        void setValue(T value) {
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
using IDLArrayBool = IDLArray<bool>;
using IDLArrayInt = IDLArray<int>;
using IDLArrayLong = IDLArray<long long>;
using IDLArrayFloat = IDLArray<float>;
using IDLArrayDouble = IDLArray<double>;
using IDLArrayByte = IDLArray<char>;

class IDLBool : public IDLArrayBool {
    public:
        IDLBool() : IDLArrayBool(1) {}
};

class IDLByte : public IDLArrayByte {
    public:
        IDLByte() : IDLArrayByte(1) {}
};

class IDLInt : public IDLArrayInt {
    public:
        IDLInt() : IDLArrayInt(1) {}
};

class IDLInt2 : public IDLArrayInt {
    public:
        IDLInt2() : IDLArrayInt(2, false) {}

        void set(int x, int y) {
            IDLArrayInt::setValue(0, x);
            IDLArrayInt::setValue(1, y);
        }

        int getX() { return IDLArrayInt::getValue(0); }
        int getY() { return IDLArrayInt::getValue(1); }
};

class IDLInt3 : public IDLArrayInt {
    public:
        IDLInt3() : IDLArrayInt(3, false) {}

        void set(int x, int y, int z) {
            IDLArrayInt::setValue(0, x);
            IDLArrayInt::setValue(1, y);
            IDLArrayInt::setValue(2, z);
        }

        int getX() { return IDLArrayInt::getValue(0); }
        int getY() { return IDLArrayInt::getValue(1); }
        int getZ() { return IDLArrayInt::getValue(2); }
};

class IDLInt4 : public IDLArrayInt {
    public:
        IDLInt4() : IDLArrayInt(4, false) {}

        void set(int x, int y, int z, int w) {
            IDLArrayInt::setValue(0, x);
            IDLArrayInt::setValue(1, y);
            IDLArrayInt::setValue(2, z);
            IDLArrayInt::setValue(3, w);
        }

        int getX() { return IDLArrayInt::getValue(0); }
        int getY() { return IDLArrayInt::getValue(1); }
        int getZ() { return IDLArrayInt::getValue(2); }
        int getW() { return IDLArrayInt::getValue(3); }
};

class IDLLong : public IDLArrayLong {
    public:
        IDLLong() : IDLArrayLong(1) {}
};

class IDLLong2 : public IDLArrayLong {
    public:
        IDLLong2() : IDLArrayLong(2, false) {}

        void set(long long x, long long y) {
            IDLArrayLong::setValue(0, x);
            IDLArrayLong::setValue(1, y);
        }

        long long getX() { return IDLArrayLong::getValue(0); }
        long long getY() { return IDLArrayLong::getValue(1); }
};

class IDLLong3 : public IDLArrayLong {
    public:
        IDLLong3() : IDLArrayLong(3, false) {}

        void set(long long x, long long y, long long z) {
            IDLArrayLong::setValue(0, x);
            IDLArrayLong::setValue(1, y);
            IDLArrayLong::setValue(2, z);
        }

        long long getX() { return IDLArrayLong::getValue(0); }
        long long getY() { return IDLArrayLong::getValue(1); }
        long long getZ() { return IDLArrayLong::getValue(2); }
};

class IDLLong4 : public IDLArrayLong {
    public:
        IDLLong4() : IDLArrayLong(4, false) {}

        void set(long long x, long long y, long long z, long long w) {
            IDLArrayLong::setValue(0, x);
            IDLArrayLong::setValue(1, y);
            IDLArrayLong::setValue(2, z);
            IDLArrayLong::setValue(3, w);
        }

        long long getX() { return IDLArrayLong::getValue(0); }
        long long getY() { return IDLArrayLong::getValue(1); }
        long long getZ() { return IDLArrayLong::getValue(2); }
        long long getW() { return IDLArrayLong::getValue(3); }
};

class IDLFloat : public IDLArrayFloat {
    public:
        IDLFloat() : IDLArrayFloat(1) {}
};

class IDLFloat2 : public IDLArrayFloat {
    public:
        IDLFloat2() : IDLArrayFloat(2, false) {}

        void set(float x, float y) {
            IDLArrayFloat::setValue(0, x);
            IDLArrayFloat::setValue(1, y);
        }

        float getX() { return IDLArrayFloat::getValue(0); }
        float getY() { return IDLArrayFloat::getValue(1); }
};

class IDLFloat3 : public IDLArrayFloat {
    public:
        IDLFloat3() : IDLArrayFloat(3, false) {}

        void set(float x, float y, float z) {
            IDLArrayFloat::setValue(0, x);
            IDLArrayFloat::setValue(1, y);
            IDLArrayFloat::setValue(2, z);
        }

        float getX() { return IDLArrayFloat::getValue(0); }
        float getY() { return IDLArrayFloat::getValue(1); }
        float getZ() { return IDLArrayFloat::getValue(2); }
};

class IDLFloat4 : public IDLArrayFloat {
    public:
        IDLFloat4() : IDLArrayFloat(4, false) {}

        void set(float x, float y, float z, float w) {
            IDLArrayFloat::setValue(0, x);
            IDLArrayFloat::setValue(1, y);
            IDLArrayFloat::setValue(2, z);
            IDLArrayFloat::setValue(3, w);
        }

        float getX() { return IDLArrayFloat::getValue(0); }
        float getY() { return IDLArrayFloat::getValue(1); }
        float getZ() { return IDLArrayFloat::getValue(2); }
        float getW() { return IDLArrayFloat::getValue(3); }
};

class IDLDouble : public IDLArrayDouble {
    public:
        IDLDouble() : IDLArrayDouble(1) {}
};

class IDLDouble2 : public IDLArrayDouble {
    public:
        IDLDouble2() : IDLArrayDouble(2, false) {}

        void set(double x, double y) {
            IDLArrayDouble::setValue(0, x);
            IDLArrayDouble::setValue(1, y);
        }

        double getX() { return IDLArrayDouble::getValue(0); }
        double getY() { return IDLArrayDouble::getValue(1); }
};

class IDLDouble3 : public IDLArrayDouble {
    public:
        IDLDouble3() : IDLArrayDouble(3, false) {}

        void set(double x, double y, double z) {
            IDLArrayDouble::setValue(0, x);
            IDLArrayDouble::setValue(1, y);
            IDLArrayDouble::setValue(2, z);
        }

        double getX() { return IDLArrayDouble::getValue(0); }
        double getY() { return IDLArrayDouble::getValue(1); }
        double getZ() { return IDLArrayDouble::getValue(2); }
};

class IDLDouble4 : public IDLArrayDouble {
    public:
        IDLDouble4() : IDLArrayDouble(4, false) {}

        void set(double x, double y, double z, double w) {
            IDLArrayDouble::setValue(0, x);
            IDLArrayDouble::setValue(1, y);
            IDLArrayDouble::setValue(2, z);
            IDLArrayDouble::setValue(3, w);
        }

        double getX() { return IDLArrayDouble::getValue(0); }
        double getY() { return IDLArrayDouble::getValue(1); }
        double getZ() { return IDLArrayDouble::getValue(2); }
        double getW() { return IDLArrayDouble::getValue(3); }
};

class IDLTemp {
    public:
        static IDLBool* Bool_1(bool value) {
            static IDLBool bool_temp1;
            bool_temp1.setValue(value);
            return &bool_temp1;
        }

        static IDLBool* Bool_2(bool value) {
            static IDLBool bool_temp2;
            bool_temp2.setValue(value);
            return &bool_temp2;
        }

        static IDLBool* Bool_3(bool value) {
            static IDLBool bool_temp3;
            bool_temp3.setValue(value);
            return &bool_temp3;
        }

        static IDLBool* Bool_4(bool value) {
            static IDLBool bool_temp4;
            bool_temp4.setValue(value);
            return &bool_temp4;
        }

        static IDLInt* Int_1(int value) {
            static IDLInt int_temp1;
            int_temp1.setValue(value);
            return &int_temp1;
        }

        static IDLInt* Int_2(int value) {
            static IDLInt int_temp2;
            int_temp2.setValue(value);
            return &int_temp2;
        }

        static IDLInt* Int_3(int value) {
            static IDLInt int_temp3;
            int_temp3.setValue(value);
            return &int_temp3;
        }

        static IDLInt* Int_4(int value) {
            static IDLInt int_temp4;
            int_temp4.setValue(value);
            return &int_temp4;
        }

        static IDLLong* Long_1(long long value) {
            static IDLLong long_temp1;
            long_temp1.setValue(value);
            return &long_temp1;
        }

        static IDLLong* Long_2(long long value) {
            static IDLLong long_temp2;
            long_temp2.setValue(value);
            return &long_temp2;
        }

        static IDLLong* Long_3(long long value) {
            static IDLLong long_temp3;
            long_temp3.setValue(value);
            return &long_temp3;
        }

        static IDLLong* Long_4(long long value) {
            static IDLLong long_temp4;
            long_temp4.setValue(value);
            return &long_temp4;
        }

        static IDLFloat* Float_1(float value) {
            static IDLFloat float_temp1;
            float_temp1.setValue(value);
            return &float_temp1;
        }

        static IDLFloat* Float_2(float value) {
            static IDLFloat float_temp2;
            float_temp2.setValue(value);
            return &float_temp2;
        }

        static IDLFloat* Float_3(float value) {
            static IDLFloat float_temp3;
            float_temp3.setValue(value);
            return &float_temp3;
        }

        static IDLFloat* Float_4(float value) {
            static IDLFloat float_temp4;
            float_temp4.setValue(value);
            return &float_temp4;
        }

        static IDLDouble* Double_1(double value) {
            static IDLDouble double_temp1;
            double_temp1.setValue(value);
            return &double_temp1;
        }

        static IDLDouble* Double_2(double value) {
            static IDLDouble double_temp2;
            double_temp2.setValue(value);
            return &double_temp2;
        }

        static IDLDouble* Double_3(double value) {
            static IDLDouble double_temp3;
            double_temp3.setValue(value);
            return &double_temp3;
        }

        static IDLDouble* Double_4(double value) {
            static IDLDouble double_temp4;
            double_temp4.setValue(value);
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