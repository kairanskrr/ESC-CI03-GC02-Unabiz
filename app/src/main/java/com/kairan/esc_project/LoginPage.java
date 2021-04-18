package com.kairan.esc_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * This is the first activity when we open the app, the login page of the app
 *
 * */
public class LoginPage extends AppCompatActivity {
    FirebaseAuth mAuth; //Firebase Authentication
    EditText EditTextEmail, EditTextPassword;
    Button LoginButton, SignUpButton;
    String email,password,invalidC;
    TextView TextViewInvalidC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        EditTextEmail = findViewById(R.id.editTextEmail);
        EditTextPassword = findViewById(R.id.editTextPassword);
        LoginButton = findViewById(R.id.buttonLogin);
        SignUpButton = findViewById(R.id.buttonSignUp);
        TextViewInvalidC = findViewById(R.id.tvInvalidC);
        mAuth = FirebaseAuth.getInstance();
        invalidC = "Invalid Credentials";

        /**
         * The Login Button, and the follow up actions when clicked, to check for correct fields and to inform the user if the field is incorrect
         *
         * */
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = EditTextEmail.getText().toString().trim();
                password = EditTextPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    EditTextEmail.setError("Please Enter A Email");
                    return;
                }
                // Make sure that the Email and Password fields are not left empty, app will crash if it is empty
                if (TextUtils.isEmpty(password)){
                    EditTextPassword.setError("Please Enter A Password");
                    return;
                }
                // Move to MainActivity Page if login credentials is correct
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginPage.this,"Login In Successful",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),SelectMenu.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(LoginPage.this,"Authentication Failed",Toast.LENGTH_LONG).show();    // display an error message
                            TextViewInvalidC.setText(invalidC);
                        }
                    }
                });
            }
        });
        // move to Sign Up activity when sign up button is clicked
        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpPage.class);
                startActivity(intent);
            }
        });


    }
}