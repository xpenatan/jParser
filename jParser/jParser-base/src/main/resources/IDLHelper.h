#pragma once

#include <string>
#include <vector>
#include <stddef.h>     // NULL
#include <stdint.h>     // intptr_t
#include <algorithm>
#include <assert.h>

namespace IDL
{

class IDLPointer {
public:
    virtual void* getVoidData() = 0;
    virtual ~IDLPointer() = default;
};

class IDLArray : public virtual IDLPointer {
public:
    virtual void clear() = 0;
    virtual int getSize() = 0;
    virtual void resize(int newSize) = 0;
    virtual ~IDLArray() = default;
};

} // END IDL NAMESPACE

template<typename T>
class IDLPointer : public IDL::IDLPointer {
    private:
        T* data = nullptr;

        void deleteData() {
            delete data;
            data = nullptr;
        }

    public:
        IDLPointer() : data(new T()) {}
        virtual ~IDLPointer() { deleteData(); }
        void* getVoidData() override { return (void*)data; }
        T* getData() { return data; }
        void setValue(T value) { *data = value; }
        T getValue() { return *data; }
};

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
        IDLArray(int size, bool isResizeEnabled = true) : isResizeEnabled(isResizeEnabled) { resize(size); }
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

using IDLBool = IDLPointer<bool>;
using IDLInt = IDLPointer<int>;
using IDLLong = IDLPointer<long long>;
using IDLFloat = IDLPointer<float>;
using IDLDouble = IDLPointer<double>;
using IDLByte = IDLPointer<char>;

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

        static IDLIntArray* Int2_1(int x, int y) {
            static IDLIntArray intArray2_temp1(2, false);
            intArray2_temp1.setValue(0, x);
            intArray2_temp1.setValue(1, y);
            return &intArray2_temp1;
        }

        static IDLIntArray* Int2_2(int x, int y) {
            static IDLIntArray intArray2_temp2(2, false);
            intArray2_temp2.setValue(0, x);
            intArray2_temp2.setValue(1, y);
            return &intArray2_temp2;
        }

        static IDLIntArray* Int3_1(int x, int y, int z) {
            static IDLIntArray intArray3_temp1(3, false);
            intArray3_temp1.setValue(0, x);
            intArray3_temp1.setValue(1, y);
            intArray3_temp1.setValue(2, z);
            return &intArray3_temp1;
        }

        static IDLIntArray* Int3_2(int x, int y, int z) {
            static IDLIntArray intArray3_temp2(3, false);
            intArray3_temp2.setValue(0, x);
            intArray3_temp2.setValue(1, y);
            intArray3_temp2.setValue(2, z);
            return &intArray3_temp2;
        }

        static IDLIntArray* Int4_1(int x, int y, int z, int w) {
            static IDLIntArray intArray4_temp1(4, false);
            intArray4_temp1.setValue(0, x);
            intArray4_temp1.setValue(1, y);
            intArray4_temp1.setValue(2, z);
            intArray4_temp1.setValue(3, w);
            return &intArray4_temp1;
        }

        static IDLIntArray* Int4_2(int x, int y, int z, int w) {
            static IDLIntArray intArray4_temp2(4, false);
            intArray4_temp2.setValue(0, x);
            intArray4_temp2.setValue(1, y);
            intArray4_temp2.setValue(2, z);
            intArray4_temp2.setValue(3, w);
            return &intArray4_temp2;
        }

        static IDLLongArray* Long2_1(long long x, long long y) {
            static IDLLongArray longArray2_temp1(2, false);
            longArray2_temp1.setValue(0, x);
            longArray2_temp1.setValue(1, y);
            return &longArray2_temp1;
        }

        static IDLLongArray* Long2_2(long long x, long long y) {
            static IDLLongArray longArray2_temp2(2, false);
            longArray2_temp2.setValue(0, x);
            longArray2_temp2.setValue(1, y);
            return &longArray2_temp2;
        }

        static IDLLongArray* Long3_1(long long x, long long y, long long z) {
            static IDLLongArray longArray3_temp1(3, false);
            longArray3_temp1.setValue(0, x);
            longArray3_temp1.setValue(1, y);
            longArray3_temp1.setValue(2, z);
            return &longArray3_temp1;
        }

        static IDLLongArray* Long3_2(long long x, long long y, long long z) {
            static IDLLongArray longArray3_temp2(3, false);
            longArray3_temp2.setValue(0, x);
            longArray3_temp2.setValue(1, y);
            longArray3_temp2.setValue(2, z);
            return &longArray3_temp2;
        }

        static IDLLongArray* Long4_1(long long x, long long y, long long z, long long w) {
            static IDLLongArray longArray4_temp1(4, false);
            longArray4_temp1.setValue(0, x);
            longArray4_temp1.setValue(1, y);
            longArray4_temp1.setValue(2, z);
            longArray4_temp1.setValue(3, w);
            return &longArray4_temp1;
        }

        static IDLLongArray* Long4_2(long long x, long long y, long long z, long long w) {
            static IDLLongArray longArray4_temp2(4, false);
            longArray4_temp2.setValue(0, x);
            longArray4_temp2.setValue(1, y);
            longArray4_temp2.setValue(2, z);
            longArray4_temp2.setValue(3, w);
            return &longArray4_temp2;
        }

        static IDLFloatArray* Float2_1(float x, float y) {
            static IDLFloatArray floatArray2_temp1(2, false);
            floatArray2_temp1.setValue(0, x);
            floatArray2_temp1.setValue(1, y);
            return &floatArray2_temp1;
        }

        static IDLFloatArray* Float2_2(float x, float y) {
            static IDLFloatArray floatArray2_temp2(2, false);
            floatArray2_temp2.setValue(0, x);
            floatArray2_temp2.setValue(1, y);
            return &floatArray2_temp2;
        }

        static IDLFloatArray* Float3_1(float x, float y, float z) {
            static IDLFloatArray floatArray3_temp1(3, false);
            floatArray3_temp1.setValue(0, x);
            floatArray3_temp1.setValue(1, y);
            floatArray3_temp1.setValue(2, z);
            return &floatArray3_temp1;
        }

        static IDLFloatArray* Float3_2(float x, float y, float z) {
            static IDLFloatArray floatArray3_temp2(3, false);
            floatArray3_temp2.setValue(0, x);
            floatArray3_temp2.setValue(1, y);
            floatArray3_temp2.setValue(2, z);
            return &floatArray3_temp2;
        }

        static IDLFloatArray* Float4_1(float x, float y, float z, float w) {
            static IDLFloatArray floatArray4_temp1(4, false);
            floatArray4_temp1.setValue(0, x);
            floatArray4_temp1.setValue(1, y);
            floatArray4_temp1.setValue(2, z);
            floatArray4_temp1.setValue(3, w);
            return &floatArray4_temp1;
        }

        static IDLFloatArray* Float4_2(float x, float y, float z, float w) {
            IDLFloatArray floatArray4_temp2(4, false);
            floatArray4_temp2.setValue(0, x);
            floatArray4_temp2.setValue(1, y);
            floatArray4_temp2.setValue(2, z);
            floatArray4_temp2.setValue(3, w);
            return &floatArray4_temp2;
        }

        static IDLDoubleArray* Double2_1(double x, double y) {
            static IDLDoubleArray doubleArray2_temp1(2, false);
            doubleArray2_temp1.setValue(0, x);
            doubleArray2_temp1.setValue(1, y);
            return &doubleArray2_temp1;
        }

        static IDLDoubleArray* Double2_2(double x, double y) {
            static IDLDoubleArray doubleArray2_temp2(2, false);
            doubleArray2_temp2.setValue(0, x);
            doubleArray2_temp2.setValue(1, y);
            return &doubleArray2_temp2;
        }

        static IDLDoubleArray* Double3_1(double x, double y, double z) {
            static IDLDoubleArray doubleArray3_temp1(3, false);
            doubleArray3_temp1.setValue(0, x);
            doubleArray3_temp1.setValue(1, y);
            doubleArray3_temp1.setValue(2, z);
            return &doubleArray3_temp1;
        }

        static IDLDoubleArray* Double3_2(double x, double y, double z) {
            static IDLDoubleArray doubleArray3_temp2(3, false);
            doubleArray3_temp2.setValue(0, x);
            doubleArray3_temp2.setValue(1, y);
            doubleArray3_temp2.setValue(2, z);
            return &doubleArray3_temp2;
        }

        static IDLDoubleArray* Double4_1(double x, double y, double z, double w) {
            static IDLDoubleArray doubleArray4_temp1(4, false);
            doubleArray4_temp1.setValue(0, x);
            doubleArray4_temp1.setValue(1, y);
            doubleArray4_temp1.setValue(2, z);
            doubleArray4_temp1.setValue(3, w);
            return &doubleArray4_temp1;
        }

        static IDLDoubleArray* Double4_2(double x, double y, double z, double w) {
            static IDLDoubleArray doubleArray4_temp2(4, false);
            doubleArray4_temp2.setValue(0, x);
            doubleArray4_temp2.setValue(1, y);
            doubleArray4_temp2.setValue(2, z);
            doubleArray4_temp2.setValue(3, w);
            return &doubleArray4_temp2;
        }
};