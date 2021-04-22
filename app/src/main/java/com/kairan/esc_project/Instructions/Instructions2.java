package com.kairan.esc_project.Instructions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kairan.esc_project.R;
import com.kairan.esc_project.SelectMenu;

public class Instructions2 extends AppCompatActivity {

    Button NextButton, PrevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions2);

        NextButton = findViewById(R.id.buttHoldNext);
        PrevButton = findViewById(R.id.buttHoldPrev);

        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Instructions3.class);
                startActivity(intent);
            }
        });

        PrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Instructions1.class);
                startActivity(intent);
            }
        });
    }
}