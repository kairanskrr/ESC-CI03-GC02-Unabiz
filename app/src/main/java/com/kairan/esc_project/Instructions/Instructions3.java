package com.kairan.esc_project.Instructions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kairan.esc_project.R;
import com.kairan.esc_project.SelectMenu;

public class Instructions3 extends AppCompatActivity {

    Button NextButton, PrevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions3);

        NextButton = findViewById(R.id.buttPressNext);
        PrevButton = findViewById(R.id.buttPressPrev);

        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectMenu.class);
                startActivity(intent);
            }
        });

        PrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Instructions2.class);
                startActivity(intent);
            }
        });
    }
}