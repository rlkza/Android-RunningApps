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
import com.papbl.runningapps.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email, etPassword;
    private Button login;
    private TextView singUp;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!=null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        email = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        login = findViewById(R.id.btnLoginLogin);
        singUp = findViewById(R.id.tvRegisterLogin);
        progress = new ProgressDialog(this);
        progress.setMessage("Logging in, please wait");

        login.setOnClickListener(this);
        singUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLoginLogin:
                login();
                break;
            case R.id.tvRegisterLogin:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }
    private void login(){
        if (inputValidated()){
            String email = this.email.getText().toString();
            String password = this.etPassword.getText().toString();
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), "sign in successfull", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else{
                        progress.dismiss();
                        String err = task.getException().getMessage();
                        if (err.contains("password")){
                            etPassword.setError(err);
                        } else {
                            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }
    private boolean inputValidated(){
        boolean res = true;
        if (email.getText().toString().isEmpty()){
            res = false;
            email.setError("This is required");
        }if (etPassword.getText().toString().isEmpty()){
            res = false;
            etPassword.setError("This is required");
        }
        return res;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
