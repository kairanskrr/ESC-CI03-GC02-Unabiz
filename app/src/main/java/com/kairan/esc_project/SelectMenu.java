package com.kairan.esc_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kairan.esc_project.Instructions.Instructions1;

/**
 * The Main Activity page to select which mode of usage the user wants, with different buttons
 */

public class SelectMenu extends AppCompatActivity {
    Button MappingButton, TestingButton, WifiScanningButton, InstructionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_menu);

        MappingButton = findViewById(R.id.MappingModeButton);
        TestingButton = findViewById(R.id.TestingModeButton);
//        WifiScanningButton = findViewById(R.id.WifiScannerButton);
        InstructionsButton = findViewById(R.id.Instructions);


        MappingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MappingMode.class);
                startActivity(intent);
            }
        });

        TestingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TestingMode.class);
                startActivity(intent);
            }
        });

//        WifiScanningButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//            }
//        });
        InstructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Instructions1.class);
                startActivity(intent);
            }
        });

    }
}