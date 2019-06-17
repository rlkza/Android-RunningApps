package com.papbl.runningapps.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.papbl.runningapps.R;
import com.papbl.runningapps.model.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nama, email, password, confrimPassword;
    private Button singUp;
    private TextView logIn;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nama = findViewById(R.id.etNamaRegister);
        email = findViewById(R.id.etEmailRegister);
        password = findViewById(R.id.etPasswordRegister);
        confrimPassword = findViewById(R.id.etConfirmPasswordRegister);
        singUp = findViewById(R.id.btnSingupRegister);
        logIn = findViewById(R.id.tvLoginRegister);
        progress = new ProgressDialog(this);
        progress.setMessage("Creating account, please wait");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        singUp.setOnClickListener(this);
        logIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSingupRegister:
                progress.show();
                register();
                break;
            case R.id.tvLoginRegister:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }
    private void register(){
        if (inputValidated()){
            final String email = this.email.getText().toString(),
                    password = this.password.getText().toString(),
                    nama = this.nama.getText().toString();
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        User user = new User(firebaseAuth.getCurrentUser().getUid(),nama, email);
                        databaseReference.child("User").child(firebaseAuth.getCurrentUser().getUid()).setValue(user);
                        Toast.makeText(RegisterActivity.this,"User Created", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        progress.dismiss();
                    } else {
                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                }
            });
        }
    }
    private boolean inputValidated(){
        boolean res = true;
        if (nama.getText().toString().isEmpty()){
            res = false;
            nama.setError("This is required");
        }if (email.getText().toString().isEmpty()){
            res = false;
            email.setError("This is required");
        }if (password.getText().toString().isEmpty()){
            res = false;
            password.setError("This is required");
        }if (confrimPassword.getText().toString().isEmpty()){
            res = false;
            confrimPassword.setError("This is required");
        }if (!password.getText().toString().equals(confrimPassword.getText().toString())){
            res = false;
            password.setError("Different");
            confrimPassword.setError("Different");
        }
        return res;
    }
}
