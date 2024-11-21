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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LastMeas extends AppCompatActivity {
    private DataTransfer dataTransfer;
    private Button goBackButton;
    private Button changeNameButton;
    private Button refreshButton;
    private TextView nameText;
    private TextView addressText;
    private TextView timeTextView;

    private String name;
    private String address;
    private int[] incomingDataBuffer;

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
        nameText=findViewById(R.id.name);
        addressText=findViewById(R.id.address);
        timeTextView=findViewById(R.id.time);

        address=dataTransfer.currentNodeAddress;
        name=dataTransfer.currentNodeName;

        nameText.setText(name);
        addressText.setText(address);

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

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataTransfer.communication.sendGetLastDataRequest())
                {
                    incomingDataBuffer=dataTransfer.communication.getIncomingData();
                    updateTime();
                    showTable();
                }
            }
        });

    }

    void showTable()
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


    void updateTime()
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

        Instant instant = Instant.now().minusSeconds(difference);
        ZoneId zoneId = ZoneId.of("Europe/Warsaw");
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDateTime = zonedDateTime.format(dateTimeFormatter);
        timeTextView.setText(formattedDateTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(dataTransfer.nameChanged)
        {
            nameText.setText(dataTransfer.newName);
        }
    }
}
