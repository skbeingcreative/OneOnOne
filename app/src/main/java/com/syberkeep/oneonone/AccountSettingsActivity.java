package com.syberkeep.oneonone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private CircleImageView userProfileImage;
    private TextView displayNameText;
    private TextView statusText;
    private Button changeImageBtn;
    private Button changeStatusBtn;

    //Firebase
    private FirebaseUser currentUser;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        init();
    }

    public void init(){
        userProfileImage = (CircleImageView) findViewById(R.id.image_user_settings);
        displayNameText = (TextView) findViewById(R.id.display_name_settings);
        statusText = (TextView) findViewById(R.id.status_settings);
        changeImageBtn = (Button) findViewById(R.id.change_image_btn_settings);
        changeStatusBtn = (Button) findViewById(R.id.change_status_btn_settings);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String currentUid = currentUser.getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mDatabaseRef.keepSynced(true);      //firebase offline
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                displayNameText.setText(display_name);
                statusText.setText(status);

                if(!image.equals("default")) {
                    //Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(userProfileImage);
                    Picasso.with(AccountSettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(userProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(userProfileImage);
                        }
                    });  //Network poilicy is Offline.
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(AccountSettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status", statusText.getText().toString());
                startActivity(statusIntent);
            }
        });

        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(AccountSettingsActivity.this, ImageActivity.class);
                startActivity(cameraIntent);
            }
        });
    }

}