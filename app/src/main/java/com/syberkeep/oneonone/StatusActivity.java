package com.syberkeep.oneonone;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatusEditLayout;
    private Button mUpdateBtn;

    //Firebase
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_status);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUpdateBtn = (Button) findViewById(R.id.update_status_btn);
        mStatusEditLayout = (TextInputLayout) findViewById(R.id.text_input_layout_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Updating status");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();

                String status = mStatusEditLayout.getEditText().getText().toString();
                mDatabaseRef.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialog.dismiss();
                            finish();
                        } else {
                            Toast.makeText(StatusActivity.this, "Oops! Some error occured!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStatusEditLayout.getEditText().setText(getIntent().getStringExtra("status"));
    }
}