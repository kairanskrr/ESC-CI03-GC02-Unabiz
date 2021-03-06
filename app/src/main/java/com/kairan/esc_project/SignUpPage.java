package com.kairan.esc_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * The sign up page
 * */

public class SignUpPage extends AppCompatActivity {
    EditText newName, newEmail, newPassword, newPassword2;
    Button RegisterButton;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        newName = findViewById(R.id.editTextName);
        newEmail = findViewById(R.id.editTextEmail2);
        newPassword = findViewById(R.id.editTextPassword2);
        newPassword2 = findViewById(R.id.editTextPassword3);
        RegisterButton = findViewById(R.id.buttonRegister);
        fAuth = FirebaseAuth.getInstance();

        /**
         * The register button, when clicked, check if all the fields are filled up and correct, then create user in firebase
         * */

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = newEmail.getText().toString();
                String password = newPassword.getText().toString().trim();
                String password2 = newPassword2.getText().toString().trim();
                if (TextUtils.isEmpty(newName.getText().toString()) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(password2)){
                    Toast.makeText(SignUpPage.this, "There is one or more fields that are empty",Toast.LENGTH_LONG).show();
                    return;
                }
                if(password.equals(password2)) {
                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SignUpPage.this, "User Created",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpPage.this, SelectMenu.class));
                            }
                            else{
                                Toast.makeText(SignUpPage.this, "Not Successful\n"+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
        }
        });
    }
}