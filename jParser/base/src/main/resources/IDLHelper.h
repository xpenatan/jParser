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
        IDLArray(int size) : size(0), data(nullptr) { resize(size); }
        ~IDLArray() {
            clear(true);
            deleteData();
        }
        void resize(int newSize, bool deletePointers = true) {
            if(newSize > 0 && size != newSize) {
                T* newData = new T[newSize];
                clear(deletePointers);
                deleteData();
                data = newData; // Update the data pointer
                size = newSize; // Update the size
            }
        }
        void clear(bool deletePointers = false) {
            if (!data || size == 0) {
                return;
            }

            if constexpr (std::is_pointer<T>::value) {
                // Handle pointer types (e.g., T = std::string*)
                if (deletePointers) {
                    // Delete pointed-to objects
                    for (int i = 0; i < size; ++i) {
                        delete data[i]; // Safe if data[i] is nullptr
                        data[i] = nullptr;
                    }
                } else {
                    // Set pointers to nullptr without deleting
                    std::fill(data, data + size, nullptr);
                }
            } else {
                // Handle non-pointer types (primitives or objects)
                static_assert(std::is_default_constructible<T>::value,
                              "T must be default constructible for clear()");
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