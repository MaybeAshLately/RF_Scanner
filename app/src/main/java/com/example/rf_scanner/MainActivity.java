package com.example.rf_scanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private Button getListButton;
    private Button turnOffAlarmButton;
    private Button changeButton;
    private EditText changeEditText;

    private ListView listView;
    private ArrayAdapter<String> adapter;


    private int criticalLevel=10;
    private Vector<String> nodeAddresses;
    private Vector<String> nodeNames;
    private Vector<String> stringToDisplay;

    private Communication communication;

    private DataTransfer dataTransfer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dataTransfer=DataTransfer.getInstance();

        if(checkPermissions()==false) lackOfSomePermission();
        else
        {
            getListButton= findViewById(R.id.get_list);
            turnOffAlarmButton= findViewById(R.id.off_alarm);
            changeButton= findViewById(R.id.change_critical);
            changeEditText= findViewById(R.id.critical_level_edit_text);

            nodeAddresses= new Vector<>();
            nodeNames = new Vector<>();
            stringToDisplay = new Vector<>();
            setUpDataIfInMemory();

            for(int i=0;i<nodeAddresses.size();i++)
            {
                stringToDisplay.add(nodeAddresses.elementAt(i)+": "+nodeNames.elementAt(i));
            }

            listView=findViewById(R.id.list);
            adapter = new ArrayAdapter<>(this, R.layout.list_row, stringToDisplay);
            listView.setAdapter(adapter);

            communication=new Communication(this);

            turnOffAlarmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    communication.sendEndAlarmMessage();
                }
            });

            changeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String str=changeEditText.getText().toString();
                    int newValue=Integer.valueOf(str);
                    if((newValue>=0)&&(newValue<=255)) communication.sendChangeMessage(newValue);
                }
            });

            getListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getListButton.setBackgroundColor(getResources().getColor(R.color.gray));
                    getListButton.setText("Downloading...");
                    getListButton.setEnabled(false);


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getListButton.setText("Get/update node list");
                                    getListButton.setBackgroundColor(getResources().getColor(R.color.green));
                                    getListButton.setEnabled(true);

                                    if(communication.sendGetListMessage()==true)
                                    {
                                        nodeAddresses.clear();
                                        nodeNames.clear();
                                        stringToDisplay.clear();
                                        int[] buffer;
                                        buffer=communication.getIncomingData();
                                        for(int i=7;i<buffer.length;i++)
                                        {
                                            nodeAddresses.add(String.valueOf(buffer[i]));
                                            nodeNames.add("Name"+String.valueOf(buffer[i]));
                                        }
                                        for(int i=0;i<nodeAddresses.size();i++)
                                        {
                                            stringToDisplay.add(nodeAddresses.elementAt(i)+": "+nodeNames.elementAt(i));
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }).start();


                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dataTransfer.currentNodeAddress=nodeAddresses.elementAt(position);
                    dataTransfer.currentNodeName=nodeNames.elementAt(position);
                    dataTransfer.currentPosition=position;
                    dataTransfer.communication=communication;
                    Intent intent = new Intent(MainActivity.this, LastMeas.class);
                    startActivityForResult(intent,1);
                }
            });
        }
    }

    private boolean checkPermissions()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) return false;
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) return false;
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false;
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return false;
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) return false;

        return true;
    }

    private void lackOfSomePermission()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Lack of some permissions")
                .setMessage("Without permissions app will not be working correctly.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    /* Data format in List.txt
    Line | data
    1    | critical level
    2    | nodeOneAddress;nodeOneName
    ...  | ...
    N    | nodeNAddress;nodeNName
     */
    private void setUpDataIfInMemory()
    {
        String fileName="List.txt";
        Vector<String> fileContent;
        fileContent=new Vector<>();

        File file = getFileStreamPath(fileName);
        if(file.exists()==false)
        {
            try (FileOutputStream fileOutputStream = openFileOutput(fileName, this.MODE_PRIVATE)) {}
            catch (IOException e) {e.printStackTrace();}
        }
        else
        {
            try(FileInputStream fileInputStream = this.openFileInput(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
                String bufferLine;
                while ((bufferLine = reader.readLine()) != null) fileContent.add(bufferLine);
            } catch (IOException e) { e.printStackTrace(); }

            criticalLevel=Integer.parseInt(fileContent.elementAt(0));
            for(int i=1;i<fileContent.size();i++)
            {
                String[] data=fileContent.elementAt(i).split(";");
                nodeAddresses.add(data[0]);
                nodeNames.add(data[1]);
            }
        }
    }


    private void showInfo()
    {
        setContentView(R.layout.info);
        Button goBack;
        goBack=findViewById(R.id.go_back);

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_main);
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();

        try (FileOutputStream fileOutputStream = openFileOutput("List.txt", this.MODE_PRIVATE)) {
            fileOutputStream.write(String.valueOf(criticalLevel).getBytes());
            fileOutputStream.write("\n".getBytes());
            for(int i=0;i<nodeAddresses.size();i++)
            {
                fileOutputStream.write((nodeAddresses.elementAt(i)+";"+nodeNames.elementAt(i)).getBytes());
                fileOutputStream.write("\n".getBytes());
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(dataTransfer.nameChanged)
        {
            nodeNames.set(dataTransfer.currentPosition,dataTransfer.newName);
            stringToDisplay.set(dataTransfer.currentPosition,dataTransfer.currentNodeAddress+":"+dataTransfer.newName);
            dataTransfer.nameChanged=false;
        }
    }

}