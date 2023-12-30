#pragma once

#include <string>
#include <stddef.h>     // NULL
#include <stdint.h>     // intptr_t

typedef std::string IDLString;

class IDLBoolArray {
    private:
        int size;
    public:
        bool * data;
        IDLBoolArray(int size) { data = NULL; resize(size); }
        ~IDLBoolArray() { if(data != NULL) { deleteData(); } }
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

class IDLIntArray {
    private:
        int size;
    public:
        int * data;
        IDLIntArray(int size) { data = NULL; resize(size); }
        ~IDLIntArray() { if(data != NULL) { deleteData(); } }
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

class IDLFloatArray {
    private:
        int size;
    public:
        float * data;
        IDLFloatArray(int size) { data = NULL; resize(size); }
        ~IDLFloatArray() { if(data != NULL) { deleteData(); } }
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

class IDLDoubleArray {
    private:
        int size;
    public:
        double * data;
        IDLDoubleArray(int size) { data = NULL; resize(size); }
        ~IDLDoubleArray() { if(data != NULL) { deleteData(); } }
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

class IDLByteArray {
    private:
        int size;
    public:
        char * data;
        IDLByteArray(int size) { data = NULL; resize(size); }
        ~IDLByteArray() { if(data != NULL) { deleteData(); } }
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