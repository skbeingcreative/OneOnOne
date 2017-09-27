package com.syberkeep.oneonone;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;


    //Firebase
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_users);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_users);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<UsersModel, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UsersModel, UsersViewHolder>(
                UsersModel.class,
                R.layout.single_user_item,
                UsersViewHolder.class,
                mDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, UsersModel model, int position) {
                usersViewHolder.setName(model.getName());
                usersViewHolder.setStatus(model.getStatus());
                usersViewHolder.setImage(model.getThumbImage(), getApplicationContext());

                final String user_id = getRef(position).getKey();   //this is the uid

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView mUserItemName = mView.findViewById(R.id.display_name_user_item);
            mUserItemName.setText(name);
        }

        public void setStatus(String status) {
            TextView mUserItemStatus = mView.findViewById(R.id.status_user_item);
            mUserItemStatus.setText(status);
        }

        public void setImage(String thumb_image, Context context){
            CircleImageView usersImageView = (CircleImageView) mView.findViewById(R.id.image_user_item);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.default_avatar).into(usersImageView);
        }

    }
}