package com.example.rf_scanner;

public class DataTransfer {
    private DataTransfer(){}

    private static DataTransfer instance;
    public static DataTransfer getInstance() {
        if (instance == null) { instance = new DataTransfer();}
        return instance;
    }

    public String currentNodeAddress;
    public String currentNodeName;
    public int currentPosition;

    public boolean nameChanged;
    public String newName;

    public Communication communication;
    int lastMeas[];
    String formattedDateTime;

}
