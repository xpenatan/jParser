#pragma once

#if defined(_MSC_VER) && _MSC_VER <= 1500 // MSVC 2008 or earlier
#include <stddef.h>     // intptr_t
#else
#include <stdint.h>     // intptr_t
#endif

class BoolArray {
    public:
        bool * data;
        int size;

        BoolArray(int size) { data = NULL; resize(size); }
        ~BoolArray() { if(data != NULL) { deleteData(); } }
        void resize(int newSize) {
            if(this->data != NULL) {
                deleteData();
            }
            bool * newData = new bool[newSize];
            this->data = newData;
            size = newSize;
            clear();
        }
        void clear() {
            for(int i = 0; i < size; i++) {
                data[i] = 0;
            }
        }
        void deleteData() { delete data; }
        bool getValue(int index) { return data[index]; }
        void setValue(int index, bool value) { data[index] = value; }
        void* getPointer() { return (void*)data; }
};

class IntArray {
    public:
        int * data;
        int size;

        IntArray(int size) { data = NULL; resize(size); }
        ~IntArray() { if(data != NULL) { deleteData(); } }
        void resize(int newSize) {
            if(this->data != NULL) {
                deleteData();
            }
            int * newData = new int[newSize];
            this->data = newData;
            size = newSize;
            clear();
        }
        void clear() {
            for(int i = 0; i < size; i++) {
                data[i] = 0;
            }
        }
        void deleteData() { delete data; }
        int getValue(int index) { return data[index]; }
        void setValue(int index, int value) { data[index] = value; }
        void* getPointer() { return (void*)data; }
};

class FloatArray {
    public:
        float * data;
        int size;

        FloatArray(int size) { data = NULL; resize(size); }
        ~FloatArray() { if(data != NULL) { deleteData(); } }
        void resize(int newSize) {
            if(this->data != NULL) {
                deleteData();
            }
            float * newData = new float[newSize];
            this->data = newData;
            size = newSize;
            clear();
        }
        void clear() {
            for(int i = 0; i < size; i++) {
                data[i] = 0;
            }
        }
        void deleteData() { delete data; }
        float getValue(int index) { return data[index]; }
        void setValue(int index, float value) { data[index] = value; }
        void* getPointer() { return (void*)data; }
};

class DoubleArray {
    public:
        double * data;
        int size;

        DoubleArray(int size) { data = NULL; resize(size); }
        ~DoubleArray() { if(data != NULL) { deleteData(); } }
        void resize(int newSize) {
            if(this->data != NULL) {
                deleteData();
            }
            double * newData = new double[newSize];
            this->data = newData;
            size = newSize;
            clear();
        }
        void clear() {
            for(int i = 0; i < size; i++) {
                data[i] = 0;
            }
        }
        void deleteData() { delete data; }
        double getValue(int index) { return data[index]; }
        void setValue(int index, double value) { data[index] = value; }
        void* getPointer() { return (void*)data; }
};

class CharArray {
    public:
        char * data;
        int size;

        CharArray(int size) { data = NULL; resize(size); }
        ~CharArray() { if(data != NULL) { deleteData(); } }
        void resize(int newSize) {
            if(this->data != NULL) {
                deleteData();
            }
            char * newData = new char[newSize];
            this->data = newData;
            size = newSize;
            clear();
        }
        void clear() {
            for(int i = 0; i < size; i++) {
                data[i] = 0;
            }
        }
        void deleteData() { delete data; }
        char getValue(int index) { return data[index]; }
        void setValue(int index, char value) { data[index] = value; }
        void* getPointer() { return (void*)data; }
};