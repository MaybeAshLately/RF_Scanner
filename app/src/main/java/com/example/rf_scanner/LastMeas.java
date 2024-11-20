package com.example.rf_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LastMeas extends AppCompatActivity {
    private DataTransfer dataTransfer;
    private Button goBackButton;
    private Button changeNameButton;
    private TextView nameText;
    private TextView addressText;

    private String name;
    private String address;

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
        nameText=findViewById(R.id.name);
        addressText=findViewById(R.id.address);

        address=dataTransfer.currentNodeAddress;
        name=dataTransfer.currentNodeName;

        nameText.setText(name);
        addressText.setText(address);


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
