package com.example.rf_scanner;

public class DataTransfer {
    private DataTransfer(){}

    private static DataTransfer instance;
    public static DataTransfer getInstance() {
        if (instance == null) { instance = new DataTransfer();}
        return instance;
    }
}
