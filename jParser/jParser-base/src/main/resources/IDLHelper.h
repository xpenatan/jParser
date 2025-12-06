#pragma once

#include <string>
//#include <vector>
//#include <stddef.h>     // NULL
//#include <stdint.h>     // intptr_t
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
using IDLBoolArray = IDLArray<bool>;
using IDLIntArray = IDLArray<int>;
using IDLLongArray = IDLArray<long long>;
using IDLFloatArray = IDLArray<float>;
using IDLDoubleArray = IDLArray<double>;
using IDLByteArray = IDLArray<char>;

class IDLBool : public IDLBoolArray {
    public:
        IDLBool();
        IDLBool(bool ownsData);
};

class IDLByte : public IDLByteArray {
    public:
        IDLByte();
        IDLByte(bool ownsData);
};

class IDLInt : public IDLIntArray {
    public:
        IDLInt();
        IDLInt(bool ownsData);
};

class IDLInt2 : public IDLIntArray {
    public:
        IDLInt2();
        void set(int x, int y);
        int getX();
        int getY();
};

class IDLInt3 : public IDLIntArray {
    public:
        IDLInt3();
        void set(int x, int y, int z);
        int getX();
        int getY();
        int getZ();
};

class IDLInt4 : public IDLIntArray {
    public:
        IDLInt4();
        void set(int x, int y, int z, int w);
        int getX();
        int getY();
        int getZ();
        int getW();
};

class IDLLong : public IDLLongArray {
    public:
        IDLLong();
        IDLLong(bool ownsData);
};

class IDLLong2 : public IDLLongArray {
    public:
        IDLLong2();
        void set(long long x, long long y);
        long long getX();
        long long getY();
};

class IDLLong3 : public IDLLongArray {
    public:
        IDLLong3();
        void set(long long x, long long y, long long z);
        long long getX();
        long long getY();
        long long getZ();
};

class IDLLong4 : public IDLLongArray {
    public:
        IDLLong4();
        void set(long long x, long long y, long long z, long long w);
        long long getX();
        long long getY();
        long long getZ();
        long long getW();
};

class IDLFloat : public IDLFloatArray {
    public:
        IDLFloat();
        IDLFloat(bool ownsData);
};

class IDLFloat2 : public IDLFloatArray {
    public:
        IDLFloat2();
        void set(float x, float y);
        float getX();
        float getY();
};

class IDLFloat3 : public IDLFloatArray {
    public:
        IDLFloat3();
        void set(float x, float y, float z);
        float getX();
        float getY();
        float getZ();
};

class IDLFloat4 : public IDLFloatArray {
    public:
        IDLFloat4();
        void set(float x, float y, float z, float w);
        float getX();
        float getY();
        float getZ();
        float getW();
};

class IDLDouble : public IDLDoubleArray {
    public:
        IDLDouble();
        IDLDouble(bool ownsData);
};

class IDLDouble2 : public IDLDoubleArray {
    public:
        IDLDouble2();
        void set(double x, double y);
        double getX();
        double getY();
};

class IDLDouble3 : public IDLDoubleArray {
    public:
        IDLDouble3();
        void set(double x, double y, double z);
        double getX();
        double getY();
        double getZ();
};

class IDLDouble4 : public IDLDoubleArray {
    public:
        IDLDouble4();
        void set(double x, double y, double z, double w);
        double getX();
        double getY();
        double getZ();
        double getW();
};

class IDLTemp {
    public:
        static IDLByteArray* ByteArray_1(void* dataAddress, int size);
        static IDLBoolArray* BoolArray_1(void* dataAddress, int size);
        static IDLIntArray* IntArray_1(void* dataAddress, int size);
        static IDLLongArray* LongArray_1(void* dataAddress, int size);
        static IDLFloatArray* FloatArray_1(void* dataAddress, int size);
        static IDLDoubleArray* DoubleArray_1(void* dataAddress, int size);

        static IDLByte* Byte_1(void* dataAddress);
        static IDLBool* Bool_1(void* dataAddress);
        static IDLInt* Int_1(void* dataAddress);
        static IDLLong* Long_1(void* dataAddress);
        static IDLFloat* Float_1(void* dataAddress);
        static IDLDouble* Double_1(void* dataAddress);

        static IDLBool* Bool_1(bool value);
        static IDLBool* Bool_2(bool value);
        static IDLBool* Bool_3(bool value);
        static IDLBool* Bool_4(bool value);

        static IDLInt* Int_1(int value);
        static IDLInt* Int_2(int value);
        static IDLInt* Int_3(int value);
        static IDLInt* Int_4(int value);

        static IDLLong* Long_1(long long value);
        static IDLLong* Long_2(long long value);
        static IDLLong* Long_3(long long value);
        static IDLLong* Long_4(long long value);

        static IDLFloat* Float_1(float value);
        static IDLFloat* Float_2(float value);
        static IDLFloat* Float_3(float value);
        static IDLFloat* Float_4(float value);

        static IDLDouble* Double_1(double value);
        static IDLDouble* Double_2(double value);
        static IDLDouble* Double_3(double value);
        static IDLDouble* Double_4(double value);

        static IDLInt2* Int2_1(int x, int y);
        static IDLInt2* Int2_2(int x, int y);

        static IDLInt3* Int3_1(int x, int y, int z);
        static IDLInt3* Int3_2(int x, int y, int z);

        static IDLInt4* Int4_1(int x, int y, int z, int w);
        static IDLInt4* Int4_2(int x, int y, int z, int w);

        static IDLLong2* Long2_1(long long x, long long y);
        static IDLLong2* Long2_2(long long x, long long y);

        static IDLLong3* Long3_1(long long x, long long y, long long z);
        static IDLLong3* Long3_2(long long x, long long y, long long z);

        static IDLLong4* Long4_1(long long x, long long y, long long z, long long w);
        static IDLLong4* Long4_2(long long x, long long y, long long z, long long w);

        static IDLFloat2* Float2_1(float x, float y);
        static IDLFloat2* Float2_2(float x, float y);

        static IDLFloat3* Float3_1(float x, float y, float z);
        static IDLFloat3* Float3_2(float x, float y, float z);

        static IDLFloat4* Float4_1(float x, float y, float z, float w);
        static IDLFloat4* Float4_2(float x, float y, float z, float w);

        static IDLDouble2* Double2_1(double x, double y);
        static IDLDouble2* Double2_2(double x, double y);

        static IDLDouble3* Double3_1(double x, double y, double z);
        static IDLDouble3* Double3_2(double x, double y, double z);

        static IDLDouble4* Double4_1(double x, double y, double z, double w);
        static IDLDouble4* Double4_2(double x, double y, double z, double w);
};