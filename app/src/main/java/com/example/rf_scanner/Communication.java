package com.example.rf_scanner;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.widget.Toast;

public class Communication {

    private BluetoothConnection bluetoothConnection;
    private Context context;
    private DataTransfer dataTransfer;

    Communication(Context ctx)
    {
        context=ctx;
        bluetoothConnection=new BluetoothConnection(context);
        dataTransfer=DataTransfer.getInstance();
    }

    private void checkIfBluetoothOnAndIfNotAsked()
    {
        if(BluetoothConnection.isEnabled()==false) bluetoothConnection.enableBluetooth();
    }

    public void sendEndAlarmMessage()
    {
        Toast toast = Toast.makeText(context, "Sending...", Toast.LENGTH_LONG);
        toast.show();
        checkIfBluetoothOnAndIfNotAsked();
        generateOutgoingMessage(255,0,0);
        send();
        String resultInfo;
        if(dataCame)
        {
            resultInfo="Alarm turned off.";
        }
        else
        {
            resultInfo="Error of connection.";
        }
        Toast toast1 = Toast.makeText(context, resultInfo, Toast.LENGTH_LONG);
        toast1.show();
    }

    public void sendChangeMessage(int newValue)
    {
        Toast toast = Toast.makeText(context, "Sending...", Toast.LENGTH_LONG);
        toast.show();
        checkIfBluetoothOnAndIfNotAsked();
        generateOutgoingMessage(12,0,newValue);
        send();

        String resultInfo;
        if(dataCame)
        {
            resultInfo="Critical level changed.";
        }
        else
        {
            resultInfo="Error of connection.";
        }
        Toast toast1 = Toast.makeText(context, resultInfo, Toast.LENGTH_LONG);
        toast1.show();

    }

    public boolean sendGetListMessage()
    {
        Toast toast = Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG);
        toast.show();

        checkIfBluetoothOnAndIfNotAsked();
        generateOutgoingMessage(0,0,0);
        send();

        String resultInfo;
        if(dataCame)
        {
            resultInfo="Data acquired.";
        }
        else
        {
            resultInfo="Error of connection.";
        }
        Toast toast1 = Toast.makeText(context, resultInfo, Toast.LENGTH_LONG);
        toast1.show();

        if(dataCame) return true;
        return false;
    }

    public boolean sendGetLastDataRequest()
    {
        Toast toast = Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG);
        toast.show();

        checkIfBluetoothOnAndIfNotAsked();
        generateOutgoingMessage(8,Integer.parseInt(dataTransfer.currentNodeAddress),0);
        send();

        String resultInfo;
        if(dataCame)
        {
            resultInfo="Data acquired.";
        }
        else
        {
            resultInfo="Error of connection.";
        }
        Toast toast1 = Toast.makeText(context, resultInfo, Toast.LENGTH_LONG);
        toast1.show();

        if(dataCame) return true;
        return false;
    }

    public int[] getIncomingData()
    {
        return incomingData;
    }

    private byte[] outgoingMessage;
    private void generateOutgoingMessage(int type,int address, int data)
    {
        //size
        if(type==16)
        {
            outgoingMessage = new byte[9];
            outgoingMessage[0]=(byte)9;
        }
        else if(type==12)
        {
            outgoingMessage = new byte[8];
            outgoingMessage[0]=(byte)8;
        }
        else
        {
            outgoingMessage = new byte[7];
            outgoingMessage[0]=(byte)7;
        }

        long currentTime= System.currentTimeMillis()/1000;
        outgoingMessage[1]= (byte)((currentTime >> 24) & 0xFF);
        outgoingMessage[2]= (byte)((currentTime >> 16) & 0xFF);
        outgoingMessage[3]= (byte)((currentTime >> 8) & 0xFF);
        outgoingMessage[4]= (byte)(currentTime & 0xFF);

        outgoingMessage[5]=(byte)type;
        outgoingMessage[6]=(byte)address;

        if(type==16)
        {
            ///TODO
        }
        else if(type==12)
        {
            outgoingMessage[7]=(byte)data;
        }

    }


    private void send()
    {
        boolean isConnected= bluetoothConnection.connect();
        if(isConnected)
        {
            sendMessage();
            bluetoothConnection.disconnect();
        }
    }

    private int[] incomingData;
    private boolean dataCame;
    private void sendMessage()
    {
        final int[][] data = {null};
        final int[] counter = {0};
        boolean finish=false;
        dataCame=false;
        while(finish==false)
        {
            data[0] = BluetoothConnection.readData();
            if (data[0] == null)
            {
                BluetoothConnection.sendData(outgoingMessage);
            }
            else
            {
                dataCame=true;
                incomingData=data[0];
                finish=true;
                return;
            }

            if(counter[0]==10)
            {
                dataCame=false;
                finish=true;
                return;
            }
            counter[0]++;
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
