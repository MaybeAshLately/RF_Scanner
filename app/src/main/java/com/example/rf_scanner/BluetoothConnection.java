package com.example.rf_scanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnection {

    static private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    static private InputStream inputStream;
    static private OutputStream outputStream;
    private Context context;

    private String[] deviceAddress;
    private int deviceCounter = 0;

    public BluetoothConnection(Context ctx) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        context = ctx;
        deviceAddress = new String[5];
    }

    static public boolean isEnabled() {
        if ((bluetoothAdapter == null) || (!bluetoothAdapter.isEnabled())) return false;
        return true;
    }

    public boolean enableBluetooth() {
        if (isEnabled() == true) return true;
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        ((Activity) context).startActivityForResult(enableBtIntent, 0);

        return isEnabled();
    }

    private void getDeviceMac() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();

                if (deviceName.equals("RF_SCANNER_SERVER  ")) {
                    deviceAddress[deviceCounter] = deviceHardwareAddress;
                    deviceCounter++;
                }
            }
        }
    }


    private boolean tryConnect(BluetoothDevice device, UUID uuid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean connect() {
        getDeviceMac();
        for (int i = 0; i < deviceCounter; i++) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress[i]);
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            if (tryConnect(device, uuid) == true) return true;
        }

        return false;
    }


    public void disconnect() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    static public int[] readData() {
        byte[] buffer = new byte[1024];
        int[] receivedData = null;

        try {
            if (inputStream.available() > 0) {
                int totalBytesRead = 0;
                while (inputStream.available() > 0) {
                    int numBytes = inputStream.read(buffer, totalBytesRead, buffer.length - totalBytesRead);
                    totalBytesRead += numBytes;
                    Thread.sleep(1000);
                }


                int size;

                if(buffer[0]>=0) size=buffer[0];
                else size=buffer[0]+256;

                receivedData = new int[size];
                for (int i = 0; i < Math.min(size, totalBytesRead); i++) {
                    if(buffer[i]>=0)receivedData[i]=buffer[i];
                    else receivedData[i] = buffer[i] + 256; // Conversion from byte to int in range 0-255
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return receivedData;
    }

    static public void sendData(byte[] buffer) {
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
