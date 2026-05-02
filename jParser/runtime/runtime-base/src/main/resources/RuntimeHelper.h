#pragma once

#include <string>
//#include <vector>
//#include <stddef.h>     // NULL
//#include <stdint.h>     // intptr_t
#include <algorithm>
#include <assert.h>

namespace Native
{

class NativeArray {
public:
    virtual void clear() = 0;
    virtual int getSize() = 0;
    virtual void resize(int newSize) = 0;
    virtual void* getVoidData() = 0;
    virtual bool ownsDataAddress() = 0;
    virtual void setData(void* newData, int size) = 0;
    virtual ~NativeArray() = default;
};

} // END Native NAMESPACE

template<typename T>
class NativeArray : public Native::NativeArray {
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
        NativeArray(int size, bool isResizeEnabled = true) {
            this->isResizeEnabled = true;
            resize(size);
            this->isResizeEnabled = isResizeEnabled;
        }
        NativeArray(bool ownsData) {
            this->ownsData = ownsData;
            resize(0);
        }
        ~NativeArray() override {
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

        void copy(NativeArray<T>& src, int srcPos, int destPos, int length) {
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

using NativeString = std::string;
using NativeBoolArray = NativeArray<bool>;
using NativeIntArray = NativeArray<int>;
using NativeLongArray = NativeArray<long long>;
using NativeFloatArray = NativeArray<float>;
using NativeDoubleArray = NativeArray<double>;
using NativeByteArray = NativeArray<char>;

class NativeBool : public NativeBoolArray {
    public:
        NativeBool();
        NativeBool(bool ownsData);
};

class NativeByte : public NativeByteArray {
    public:
        NativeByte();
        NativeByte(bool ownsData);
};

class NativeInt : public NativeIntArray {
    public:
        NativeInt();
        NativeInt(bool ownsData);
};

class NativeInt2 : public NativeIntArray {
    public:
        NativeInt2();
        void set(int x, int y);
        int getX();
        int getY();
};

class NativeInt3 : public NativeIntArray {
    public:
        NativeInt3();
        void set(int x, int y, int z);
        int getX();
        int getY();
        int getZ();
};

class NativeInt4 : public NativeIntArray {
    public:
        NativeInt4();
        void set(int x, int y, int z, int w);
        int getX();
        int getY();
        int getZ();
        int getW();
};

class NativeLong : public NativeLongArray {
    public:
        NativeLong();
        NativeLong(bool ownsData);
};

class NativeLong2 : public NativeLongArray {
    public:
        NativeLong2();
        void set(long long x, long long y);
        long long getX();
        long long getY();
};

class NativeLong3 : public NativeLongArray {
    public:
        NativeLong3();
        void set(long long x, long long y, long long z);
        long long getX();
        long long getY();
        long long getZ();
};

class NativeLong4 : public NativeLongArray {
    public:
        NativeLong4();
        void set(long long x, long long y, long long z, long long w);
        long long getX();
        long long getY();
        long long getZ();
        long long getW();
};

class NativeFloat : public NativeFloatArray {
    public:
        NativeFloat();
        NativeFloat(bool ownsData);
};

class NativeFloat2 : public NativeFloatArray {
    public:
        NativeFloat2();
        void set(float x, float y);
        float getX();
        float getY();
};

class NativeFloat3 : public NativeFloatArray {
    public:
        NativeFloat3();
        void set(float x, float y, float z);
        float getX();
        float getY();
        float getZ();
};

class NativeFloat4 : public NativeFloatArray {
    public:
        NativeFloat4();
        void set(float x, float y, float z, float w);
        float getX();
        float getY();
        float getZ();
        float getW();
};

class NativeDouble : public NativeDoubleArray {
    public:
        NativeDouble();
        NativeDouble(bool ownsData);
};

class NativeDouble2 : public NativeDoubleArray {
    public:
        NativeDouble2();
        void set(double x, double y);
        double getX();
        double getY();
};

class NativeDouble3 : public NativeDoubleArray {
    public:
        NativeDouble3();
        void set(double x, double y, double z);
        double getX();
        double getY();
        double getZ();
};

class NativeDouble4 : public NativeDoubleArray {
    public:
        NativeDouble4();
        void set(double x, double y, double z, double w);
        double getX();
        double getY();
        double getZ();
        double getW();
};

class NativeTemp {
    public:
        static NativeByteArray* ByteArray_1(void* dataAddress, int size);
        static NativeBoolArray* BoolArray_1(void* dataAddress, int size);
        static NativeIntArray* IntArray_1(void* dataAddress, int size);
        static NativeLongArray* LongArray_1(void* dataAddress, int size);
        static NativeFloatArray* FloatArray_1(void* dataAddress, int size);
        static NativeDoubleArray* DoubleArray_1(void* dataAddress, int size);

        static NativeByte* Byte_1(void* dataAddress);
        static NativeBool* Bool_1(void* dataAddress);
        static NativeInt* Int_1(void* dataAddress);
        static NativeLong* Long_1(void* dataAddress);
        static NativeFloat* Float_1(void* dataAddress);
        static NativeDouble* Double_1(void* dataAddress);

        static NativeBool* Bool_1(bool value);
        static NativeBool* Bool_2(bool value);
        static NativeBool* Bool_3(bool value);
        static NativeBool* Bool_4(bool value);

        static NativeInt* Int_1(int value);
        static NativeInt* Int_2(int value);
        static NativeInt* Int_3(int value);
        static NativeInt* Int_4(int value);

        static NativeLong* Long_1(long long value);
        static NativeLong* Long_2(long long value);
        static NativeLong* Long_3(long long value);
        static NativeLong* Long_4(long long value);

        static NativeFloat* Float_1(float value);
        static NativeFloat* Float_2(float value);
        static NativeFloat* Float_3(float value);
        static NativeFloat* Float_4(float value);

        static NativeDouble* Double_1(double value);
        static NativeDouble* Double_2(double value);
        static NativeDouble* Double_3(double value);
        static NativeDouble* Double_4(double value);

        static NativeInt2* Int2_1(int x, int y);
        static NativeInt2* Int2_2(int x, int y);

        static NativeInt3* Int3_1(int x, int y, int z);
        static NativeInt3* Int3_2(int x, int y, int z);

        static NativeInt4* Int4_1(int x, int y, int z, int w);
        static NativeInt4* Int4_2(int x, int y, int z, int w);

        static NativeLong2* Long2_1(long long x, long long y);
        static NativeLong2* Long2_2(long long x, long long y);

        static NativeLong3* Long3_1(long long x, long long y, long long z);
        static NativeLong3* Long3_2(long long x, long long y, long long z);

        static NativeLong4* Long4_1(long long x, long long y, long long z, long long w);
        static NativeLong4* Long4_2(long long x, long long y, long long z, long long w);

        static NativeFloat2* Float2_1(float x, float y);
        static NativeFloat2* Float2_2(float x, float y);

        static NativeFloat3* Float3_1(float x, float y, float z);
        static NativeFloat3* Float3_2(float x, float y, float z);

        static NativeFloat4* Float4_1(float x, float y, float z, float w);
        static NativeFloat4* Float4_2(float x, float y, float z, float w);

        static NativeDouble2* Double2_1(double x, double y);
        static NativeDouble2* Double2_2(double x, double y);

        static NativeDouble3* Double3_1(double x, double y, double z);
        static NativeDouble3* Double3_2(double x, double y, double z);

        static NativeDouble4* Double4_1(double x, double y, double z, double w);
        static NativeDouble4* Double4_2(double x, double y, double z, double w);
};