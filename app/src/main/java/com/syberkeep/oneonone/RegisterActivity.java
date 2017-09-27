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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //find bugs: BUG_<no.>
    private TextInputEditText displayNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button registerBtn;
    private Toolbar toolbar;
    private ProgressDialog progressDialogReg;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
    }

    public void init(){
        displayNameEditText = (TextInputEditText) findViewById(R.id.edit_text_display_name_reg);
        emailEditText = (TextInputEditText) findViewById(R.id.edit_text_email_reg);
        passwordEditText = (TextInputEditText) findViewById(R.id.edit_text_password_reg);
        registerBtn = (Button) findViewById(R.id.btn_register_reg);
        progressDialogReg = new ProgressDialog(this);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_register);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name_text = displayNameEditText.getText().toString();
                String email_text = emailEditText.getText().toString();
                String password_text = passwordEditText.getText().toString();

                if(TextUtils.isEmpty(display_name_text)) {
                    Toast.makeText(RegisterActivity.this, "Please fill out Display name field", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(email_text)) {
                    Toast.makeText(RegisterActivity.this, "Please fill out Email field", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(password_text)) {
                    Toast.makeText(RegisterActivity.this, "Please fill out Password field", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialogReg.setTitle("Creating account");
                progressDialogReg.setMessage("Please wait while we create your account...");
                progressDialogReg.setCanceledOnTouchOutside(false);
                progressDialogReg.show();

                registerUser(display_name_text, email_text, password_text);
            }
        });
    }

    private void registerUser(final String display_name, String email, String pass) {

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("device_token", deviceToken);
                    userMap.put("name", display_name);
                    userMap.put("status", "Woah! the status.");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressDialogReg.dismiss();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });
                } else {
                    progressDialogReg.hide();
                    if(!isNetworkAvailable())
                        Toast.makeText(RegisterActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                    else {
                        String error = "";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            error = "Weak password!";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            error = "Invalid email!";
                        } catch (FirebaseAuthUserCollisionException e) {
                            error = "Existing account!";
                        } catch (Exception e) {
                            error = "Unknown error!";
                            e.printStackTrace();
                        }
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
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
