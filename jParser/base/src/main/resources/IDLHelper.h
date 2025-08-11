#pragma once

#include <string>
#include <vector>
#include <stddef.h>     // NULL
#include <stdint.h>     // intptr_t
#include <algorithm>
#include <assert.h>

template<typename T>
class IDLArray {
    private:
        int size;
        T* data;

        void deleteData() { delete[] data; data = nullptr; }
    public:
        IDLArray() : size(0), data(nullptr) {}
        IDLArray(int size) : size(0), data(nullptr) { resize(size); }
        ~IDLArray() {
            clear();
            deleteData();
        }

        IDLArray(const IDLArray& other) : size(0), data(nullptr) {
            resize(other.size);
            std::copy(other.data, other.data + size, data);
        }

        IDLArray& operator=(const IDLArray& other) {
            if (this == &other) return *this;
            resize(other.size);
            std::copy(other.data, other.data + size, data);
            return *this;
        }

        IDLArray(IDLArray&& other) noexcept : size(other.size), data(other.data) {
            other.size = 0;
            other.data = nullptr;
        }

        IDLArray& operator=(IDLArray&& other) noexcept {
            if (this == &other) return *this;
            clear();
            deleteData();
            data = other.data;
            size = other.size;
            other.data = nullptr;
            other.size = 0;
            return *this;
        }

        void resize(int newSize) {
            if (newSize < 0) newSize = 0;
            if (newSize == size) return;

            if (newSize == 0) {
                clear();
                deleteData();
                data = nullptr;
                size = 0;
                return;
            }

            T* newData = new T[newSize]();
            int copySize = std::min(size, newSize);
            std::copy(data, data + copySize, newData);

            deleteData();
            data = newData;
            size = newSize;
        }

        void clear() {
            if (!data || size == 0) {
                return;
            }

            if constexpr (std::is_pointer<T>::value) {
                std::fill(data, data + size, nullptr);
            } else {
                static_assert(std::is_default_constructible<T>::value, "T must be default constructible for clear()");
                std::fill(data, data + size, T());
            }
        }

        void copy(IDLArray<T>& src, int srcPos, int destPos, int length) {
            assert(!(srcPos < 0 || destPos < 0 || length < 0 || srcPos + length > src.size || destPos + length > size));
            std::copy(src.data + srcPos, src.data + srcPos + length, data + destPos);
        }

        T getValue(int index) {
            assert(!(index < 0 || index >= size));
            return data[index];
        }

        T& getValueRef(int index) {
            assert(!(index < 0 || index >= size));
            return data[index];
        }

        T* getValuePtr(int index) {
            assert(!(index < 0 || index >= size));
            return &data[index];
        }

        void setValue(int index, T value) {
            assert(!(index < 0 || index >= size));
            data[index] = value;
        }

        void setValueRef(int index, T& value) {
            assert(!(index < 0 || index >= size));
            data[index] = value;
        }

        void setValuePtr(int index, T* value) {
            assert(!(index < 0 || index >= size));
            data[index] = *value;
        }

        int getSize() { return size; }
        void* getPointer() { return (void*)data; }
        T* getData() { return data; }
};

typedef std::string IDLString;
typedef std::string_view IDLStringView;
typedef IDLArray<bool> IDLBoolArray;
typedef IDLArray<int> IDLIntArray;
typedef IDLArray<long long> IDLLongArray;
typedef IDLArray<float> IDLFloatArray;
typedef IDLArray<double> IDLDoubleArray;
typedef IDLArray<char> IDLByteArray;