package com.kairan.esc_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {
    FirebaseAuth mAuth; //Firebase Authentication
    EditText EditTextEmail, EditTextPassword;
    Button LoginButton, SignUpButton;
    String email,password;

//    public void onStart(){
//        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null){
//            Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(getApplicationContext(),MainActivity.class));
//        }     //Check if user is Signed In, move to mainactivity if user already signed in
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        EditTextEmail = findViewById(R.id.EditTextEmail);
        EditTextPassword = findViewById(R.id.EditTextPassword);
        LoginButton = findViewById(R.id.LoginButton);
        SignUpButton = findViewById(R.id.SignUpButton);
        mAuth = FirebaseAuth.getInstance();

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