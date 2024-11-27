package com.example.rf_scanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class LastMeas extends AppCompatActivity {
    private DataTransfer dataTransfer;
    private Button goBackButton;
    private Button changeNameButton;
    private Button refreshButton;
    private Button historicButton;
    private TextView nameText;
    private TextView addressText;
    private TextView timeTextView;

    private String name;
    private String address;
    private int[] incomingDataBuffer;

    private String formattedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.last_meas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dataTransfer = DataTransfer.getInstance();
        goBackButton=findViewById(R.id.go_back);
        changeNameButton=findViewById(R.id.change_name);
        refreshButton=findViewById(R.id.refresh);
        historicButton=findViewById(R.id.historic_data);
        nameText=findViewById(R.id.name);
        addressText=findViewById(R.id.address);
        timeTextView=findViewById(R.id.time);

        address=dataTransfer.currentNodeAddress;
        name=dataTransfer.currentNodeName;

        nameText.setText(name);
        addressText.setText(address);

        getDataFromFileIfExist();
        showTable();

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LastMeas.this, EditName.class);
                startActivityForResult(intent,2);
            }
        });

        historicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataTransfer.lastMeas=incomingDataBuffer;
                dataTransfer.formattedDateTime=formattedDateTime;
                Intent intent = new Intent(LastMeas.this, HistMeas.class);
                startActivityForResult(intent,3);

            }
        });


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                refreshButton.setBackgroundColor(getResources().getColor(R.color.gray));
                refreshButton.setText("Downloading...");
                refreshButton.setEnabled(false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshButton.setText("Refresh");
                                refreshButton.setBackgroundColor(getResources().getColor(R.color.green));
                                refreshButton.setEnabled(true);

                                if(dataTransfer.communication.sendGetLastDataRequest())
                                {
                                    incomingDataBuffer=dataTransfer.communication.getIncomingData();
                                    updateTime();
                                    showTable();
                                }
                            }
                        });
                    }
                }).start();

            }
        });

    }

    private void showTable()
    {
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();


        String[] data = new String[126];
        if(incomingDataBuffer==null)
        {
            for(int i=0;i<data.length;i++)
            {
                data[i]="-";
            }
        }
        else
        {
            for(int i=0;i<data.length;i++)
            {
                data[i] = String.valueOf(incomingDataBuffer[i+7]);
            }
        }

        int row=0;
        int frequency=2400; //in MHz

        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT));

        TextView headerOne = new TextView(this);
        headerOne.setText("Channel number");
        headerOne.setTextColor(Color.BLUE);
        headerOne.setGravity(Gravity.CENTER);
        headerOne.setPadding(8,8,8,8);
        headerOne.setBackgroundResource(R.drawable.cell);
        headerRow.addView(headerOne);

        TextView headerTwo = new TextView(this);
        headerTwo.setText("Frequency [MHz]");
        headerTwo.setTextColor(Color.BLUE);
        headerTwo.setGravity(Gravity.CENTER);
        headerTwo.setPadding(8, 8, 8, 8);
        headerTwo.setBackgroundResource(R.drawable.cell);
        headerRow.addView(headerTwo);

        TextView headerThree = new TextView(this);
        headerThree.setText("Number of signals");
        headerThree.setTextColor(Color.BLUE);
        headerThree.setGravity(Gravity.CENTER);
        headerThree.setPadding(8, 8, 8, 8);
        headerThree.setBackgroundResource(R.drawable.cell);
        headerRow.addView(headerThree);

        tableLayout.addView(headerRow);

        int numberOfMeas=0;

        for(int i=0;i<378;i=i+3)
        {
            TableRow tableRow=new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT));

            for (int j=0;j<3;j++)
            {
                int idx=i+j;

                TextView textView=new TextView(this);

                if((idx<378)&&(idx%3==0))
                {
                    textView.setText(String.valueOf(row));
                    row++;
                    textView.setTextColor(Color.GRAY);
                }
                else if((idx<378)&&(idx%3==1))
                {
                    String freqRange=frequency+"-";
                    frequency++;
                    freqRange=freqRange+frequency;
                    textView.setText(freqRange);
                    textView.setTextColor(Color.GRAY);
                }
                else if(idx<378)
                {
                    textView.setText(data[numberOfMeas]);
                    textView.setTextColor(Color.BLACK);
                    numberOfMeas++;
                }
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(8,8,8,8);
                textView.setBackgroundResource(R.drawable.cell);
                tableRow.addView(textView);
            }

            tableLayout.addView(tableRow);
        }
    }


    private void updateTime()
    {
        int timeOfMessage =0;
        timeOfMessage |= incomingDataBuffer[1];
        timeOfMessage |= incomingDataBuffer[2]<<8;
        timeOfMessage |= incomingDataBuffer[3]<<16;
        timeOfMessage |= incomingDataBuffer[4]<<24;

        int timeOfMeasure=0;
        timeOfMeasure |= incomingDataBuffer[133];
        timeOfMeasure |= incomingDataBuffer[134]<<8;
        timeOfMeasure |= incomingDataBuffer[135]<<16;
        timeOfMeasure |= incomingDataBuffer[136]<<24;

        int difference=timeOfMessage-timeOfMeasure;
        System.out.println(timeOfMessage);
        System.out.println(timeOfMeasure);
        System.out.println(difference);

        Instant instant = Instant.now().minusSeconds(difference);
        ZoneId zoneId = ZoneId.of("Europe/Warsaw");
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        formattedDateTime = zonedDateTime.format(dateTimeFormatter);
        timeTextView.setText(formattedDateTime);
    }

    /* Data format in NodeAddress.txt
        Line | data
        1    | timeOfMeasurement ("dd.MM.yyyy HH:mm:ss")
        2    | numberOfSignalsOnChannel0
        ...  | ...
        126    | numberOfSignalsOnChannel125
         */
    private void getDataFromFileIfExist()
    {
        String fileName=dataTransfer.currentNodeAddress+".txt";
        Vector<String> fileContent;
        fileContent=new Vector<>();

        File file = getFileStreamPath(fileName);
        if(file.exists()==false) return;
        try(FileInputStream fileInputStream = this.openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String bufferLine;
            while ((bufferLine = reader.readLine()) != null) fileContent.add(bufferLine);
        } catch (IOException e) { e.printStackTrace(); }

        if(fileContent.isEmpty()) return;
        formattedDateTime=fileContent.elementAt(0);
        timeTextView.setText(formattedDateTime);

        incomingDataBuffer=new int[137];
        for(int i=0;i<7;i++)
        {
            incomingDataBuffer[i]=0;
        }
        for(int i=7;i<137;i++)
        {
            incomingDataBuffer[i]=Integer.parseInt(fileContent.elementAt(i-6));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(dataTransfer.nameChanged)
        {
            nameText.setText(dataTransfer.newName);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if(incomingDataBuffer!=null)
        {
            try (FileOutputStream fileOutputStream = openFileOutput(dataTransfer.currentNodeAddress+".txt", this.MODE_PRIVATE)) {
                fileOutputStream.write(formattedDateTime.getBytes());
                fileOutputStream.write("\n".getBytes());
                for(int i=0;i<130;i++)
                {
                    fileOutputStream.write(String.valueOf(incomingDataBuffer[i+7]).getBytes());
                    fileOutputStream.write("\n".getBytes());
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }

    }
}
