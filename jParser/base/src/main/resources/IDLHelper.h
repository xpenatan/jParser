#pragma once

#include <string>
#include <vector>
#include <stddef.h>     // NULL
#include <stdint.h>     // intptr_t

template<typename T>
class IDLArray {
    private:
        int size;
        T* data;

        void deleteData() { delete[] data; data = NULL; }
    public:
        IDLArray(int size) { data = NULL; resize(size); }
        ~IDLArray() { if(data != NULL) { deleteData(); } }
        void resize(int newSize) {
            if(this->data != NULL) {
                deleteData();
            }
            T * newData = new T[newSize];
            this->data = newData;
            size = newSize;
            clear();
        }
        void clear() {
            for(int i = 0; i < size; i++) {
                data[i] = 0;
            }
        }
        void copy(IDLArray<T>& src, int srcPos, int destPos, int length) {
            T* dest = data;
            int srcP = srcPos;
            int destP = destPos;
            int count = 0;
            while(count < length) {
                T srcByte = src.getValue(srcP);
                srcP++;
                dest[destP] = srcByte;
                destP++;
                count++;
            }
        }
        T getValue(int index) { return data[index]; }
        void setValue(int index, T value) { data[index] = value; }
        int getSize() { return size; }
        intptr_t getPointer() { return (intptr_t)data; }
};

typedef std::string IDLString;
typedef std::string_view IDLStringView;
typedef IDLArray<bool> IDLBoolArray;
typedef IDLArray<int> IDLIntArray;
typedef IDLArray<float> IDLFloatArray;
typedef IDLArray<double> IDLDoubleArray;
typedef IDLArray<char> IDLByteArray;