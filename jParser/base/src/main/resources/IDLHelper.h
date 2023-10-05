#pragma once

#include <stddef.h>     // NULL
#include <stdint.h>     // intptr_t

class BoolArray {
    private:
        int size;
    public:
        bool * data;
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
        intptr_t getPointer() { return (intptr_t)data; }
        int getSize() { return size; }
};

class IntArray {
    private:
        int size;
    public:
        int * data;
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
        intptr_t getPointer() { return (intptr_t)data; }
        int getSize() { return size; }
};

class FloatArray {
    private:
        int size;
    public:
        float * data;
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
        intptr_t getPointer() { return (intptr_t)data; }
        int getSize() { return size; }
};

class DoubleArray {
    private:
        int size;
    public:
        double * data;
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
        intptr_t getPointer() { return (intptr_t)data; }
        int getSize() { return size; }
};

class ByteArray {
    private:
        int size;
    public:
        char * data;
        ByteArray(int size) { data = NULL; resize(size); }
        ~ByteArray() { if(data != NULL) { deleteData(); } }
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
        intptr_t getPointer() { return (intptr_t)data; }
        int getSize() { return size; }
};