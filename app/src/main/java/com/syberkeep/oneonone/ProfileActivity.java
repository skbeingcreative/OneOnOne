package com.syberkeep.oneonone;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileUserImage;
    private TextView mDisplayNameText, mStatusText, mFriendsCountText;
    private Button mFriendReqBtn;
    private Button mDeclineBtn;
    private ProgressDialog mProgressDialog;
    private int currentState;   //currentState = 0/1/2/3 --> not_friends(0)/friends(1)/request_sent(2)/request_received(3)
    String user_id = null;

    //Firebase
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotifDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
    }

    public void init() {
        profileUserImage = (ImageView) findViewById(R.id.profile_user_image);
        mDisplayNameText = (TextView) findViewById(R.id.profile_display_text);
        mStatusText = (TextView) findViewById(R.id.profile_status_text);
        mFriendsCountText = (TextView) findViewById(R.id.profile_friends_count_text);
        mFriendReqBtn = (Button) findViewById(R.id.send_friend_req_btn);
        mDeclineBtn = (Button) findViewById(R.id.decline_friend_req_btn);

        currentState = 0;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading userdata");
        mProgressDialog.setMessage("Please wait while we fetch all the user info...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        user_id = getIntent().getStringExtra("user_id");
        Log.e("TAG_ME", user_id);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_requests");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotifDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mDisplayNameText.setText(display_name);
                mStatusText.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(profileUserImage);

                // ----- FRIEND LIST/REQUESTS FEATURE -----

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //if the user has the request giving user instance in its child, then do this...
                        if(dataSnapshot.hasChild(user_id)){
                            int reqType = Integer.valueOf(dataSnapshot.child(user_id).child("request_state").getValue().toString());

                            if(reqType == 3){

                                //currentState = request_received(3)
                                currentState = 3;
                                mFriendReqBtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if(reqType == 2) {

                                //currentState = request_sent(2)
                                currentState = 2;
                                mFriendReqBtn.setText("Cancel Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        } else {
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){
                                        currentState = 1;   //currentState = friends(1)
                                        mFriendReqBtn.setText("Unfriend this person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFriendReqBtn.setEnabled(false);
                mFriendReqBtn.setBackgroundTintList(ProfileActivity.this.getResources().getColorStateList(R.color.grey_600));

                // ====== NOT FRIENDS STATE ======

                //currentState = not_friends
                if(currentState == 0){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id)
                            .child("request_state").setValue("2").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_state")
                                        .setValue("3").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notifData = new HashMap<>();
                                        notifData.put("from", mCurrentUser.getUid());
                                        notifData.put("type", "request");

                                        mNotifDatabase.child(user_id).push().setValue(notifData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    currentState = 2;   //currentState = request_sent
                                                    mFriendReqBtn.setText("Cancel request");

                                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                                    mDeclineBtn.setEnabled(false);

                                                    Toast.makeText(ProfileActivity.this, "Request Sent successfully!", Toast.LENGTH_SHORT).show();
                                                }
                                            });   //push will create a child with some random value.

                                        }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to do the operation at BUG_03!", Toast.LENGTH_SHORT).show();
                            }
                            mFriendReqBtn.setEnabled(true);
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Request already sent!", Toast.LENGTH_SHORT).show();
                }

                // ====== CANCEL FRIENDS REQUEST STATE ======

                //currentState = request_sent(2)
                if(currentState == 2){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(ProfileActivity.this, "Friend request cancelled!", Toast.LENGTH_SHORT).show();

                                    mFriendReqBtn.setEnabled(true);
                                    currentState = 0;   //currentState = not_friends
                                    mFriendReqBtn.setText("Send Friend Request");
                                    mFriendReqBtn.setBackgroundTintList(ProfileActivity.this.getResources().getColorStateList(R.color.colorAccent));

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);

                                }
                            });
                        }
                    });
                }

                // ====== REQUEST RECEIVED STATE ======

                //currentState = request_received(3)
                if(currentState == 3){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendsDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //delete the request from the database
                                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendReqBtn.setEnabled(true);
                                                    currentState = 1;   //currentState = friends(1)
                                                    mFriendReqBtn.setText("Unfriend this person");

                                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                                    mDeclineBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        }
                    });
                }

            }
        });
    }
}
