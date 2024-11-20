package com.example.rf_scanner;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditName extends AppCompatActivity {
    private DataTransfer dataTransfer;
    private EditText editText;
    private Button cancelButton;
    private Button changeButton;
    private Context context;
    private TextView address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_name);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dataTransfer = DataTransfer.getInstance();

        editText=findViewById(R.id.name_edit);
        address=findViewById(R.id.address);
        cancelButton=findViewById(R.id.cancel_button);
        changeButton=findViewById(R.id.change_name);
        context = this;
        editText.setText(dataTransfer.currentNodeName);
        address.setText(dataTransfer.currentNodeAddress);


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataTransfer.nameChanged=false;
                finish();
            }
        });


        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName=editText.getText().toString();
                if(newName.matches("[a-zA-Z0-9]+"))
                {
                    dataTransfer.nameChanged=true;
                    dataTransfer.newName= newName;
                    Toast toast = Toast.makeText(context, "Name changed", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
                else
                {
                    Toast toast = Toast.makeText(context, "Wrong name", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });
    }
}
