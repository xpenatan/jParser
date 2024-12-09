#pragma once

#include <string>
#include <vector>
#include <stddef.h>     // NULL
#include <stdint.h>     // intptr_t
#include <algorithm>
#include <stdexcept>

template<typename T>
class IDLArray {
    private:
        int size;
        T* data;

        void deleteData() { delete[] data; data = nullptr; }
    public:
        IDLArray(int size) : size(0), data(nullptr) { resize(size); }
        ~IDLArray() { deleteData(); }
        void resize(int newSize) {
            if(newSize > 0 && size != newSize) {
                T* newData = new T[newSize];
                if (data != nullptr) {
                    deleteData(); // Delete the old array
                }
                data = newData; // Update the data pointer
                size = newSize; // Update the size
                clear();
            }
        }
        void clear() {
            std::fill(data, data + size, T());
        }
        void copy(IDLArray<T>& src, int srcPos, int destPos, int length) {
            if (srcPos < 0 || destPos < 0 || length < 0 || srcPos + length > src.size || destPos + length > size) {
                throw std::out_of_range("Invalid copy range");
            }
            std::copy(src.data + srcPos, src.data + srcPos + length, data + destPos);
        }
        T getValue(int index) {
            if (index < 0 || index >= size) {
                throw std::out_of_range("Index out of range");
            }
            return data[index];
         }
        void setValue(int index, T value) {
            if (index < 0 || index >= size) {
                throw std::out_of_range("Index out of range");
            }
            data[index] = value;
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