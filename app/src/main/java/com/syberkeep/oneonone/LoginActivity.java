package com.syberkeep.oneonone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    //find bugs: BUG_<no.>
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginBtn;
    private Toolbar toolbar;
    private ProgressDialog progressDialogLogin;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    public void init(){
        emailEditText = (TextInputEditText) findViewById(R.id.edit_text_email_reg);
        passwordEditText = (TextInputEditText) findViewById(R.id.edit_text_password_reg);
        loginBtn = (Button) findViewById(R.id.btn_login);
        progressDialogLogin = new ProgressDialog(this);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_login);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email_text = emailEditText.getText().toString();
                String password_text = passwordEditText.getText().toString();

                if(TextUtils.isEmpty(email_text)) {
                    Toast.makeText(LoginActivity.this, "Please fill out Email field", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(password_text)) {
                    Toast.makeText(LoginActivity.this, "Please fill out Password field", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialogLogin.setTitle("Logging in");
                progressDialogLogin.setMessage("Please wait while we log you in...");
                progressDialogLogin.setCanceledOnTouchOutside(false);
                progressDialogLogin.show();
                loginUser(email_text, password_text);
            }
        });
    }

    private void loginUser(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialogLogin.dismiss();

                    String uid = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mDatabaseRef.child(uid).child("device_token").setValue(deviceToken)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();

                                }
                            });
                } else {
                    progressDialogLogin.hide();
                    if(!isNetworkAvailable()) {
                        Toast.makeText(LoginActivity.this, "No internet Connection!", Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(LoginActivity.this, "There is some bug at BUG_02", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
