package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView profile_img_profile_img;
    private TextView profile_txt_name, profile_txt_username, profile_txt_status, profile_txt_country, profile_txt_dob, profile_txt_gender, profile_txt_relationship;
    private FirebaseAuth mAuth;
    private DatabaseReference user_ref;
    private Toolbar profile_toolbar;
    private Button profile_btn_addFriend;
    private String visit_user_id, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profile_txt_name.setText(snapshot.child("fullname").getValue().toString());
                    profile_txt_username.setText("@" + snapshot.child("username").getValue().toString());
                    profile_txt_status.setText(snapshot.child("status").getValue().toString());
                    profile_txt_country.setText("Country: " + snapshot.child("username").getValue().toString());
                    profile_txt_dob.setText("Birthday: " + snapshot.child("dob").getValue().toString());
                    profile_txt_gender.setText("Gender: " + snapshot.child("gender").getValue().toString());
                    profile_txt_relationship.setText("Relationship status: " + snapshot.child("relationshipstatus").getValue().toString());
                    if (snapshot.child("profile_img").getValue() != null && !isDestroyed()) { //  && !isDestroyed() 就不會閃退
                        Glide.with(ProfileActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(profile_img_profile_img);
                    }

                    if (visit_user_id.equals(current_user_id)) {
                        profile_btn_addFriend.setVisibility(View.GONE);
                    } else {
                        String status = "";
                        if (snapshot.child("friends").child(current_user_id).child("status").exists()) {
                            status = snapshot.child("friends").child(current_user_id).child("status").getValue().toString();
                        }

                        DatabaseReference visit_user_friends_ref = user_ref.child("friends").child(current_user_id).child("status");
                        switch (status) {
                            case "friend":
                                profile_btn_addFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // 兩人彼此好友都刪除
                                        visit_user_friends_ref.removeValue();
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child("friends").child(visit_user_id).child("status").removeValue();
                                    }
                                });
                                profile_btn_addFriend.setText("Delete Friend");
                                profile_btn_addFriend.setBackground(getResources().getDrawable(R.drawable.cancel_button));
                                profile_btn_addFriend.setTextColor(getResources().getColor(R.color.colorPrimary));
                                break;
                            case "invited":
                                profile_btn_addFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        visit_user_friends_ref.removeValue();
                                    }
                                });
                                profile_btn_addFriend.setText("Cancel Invitation");
                                profile_btn_addFriend.setBackground(getResources().getDrawable(R.drawable.cancel_button));
                                profile_btn_addFriend.setTextColor(getResources().getColor(R.color.colorPrimary));
                                break;
                            default:
                                profile_btn_addFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        visit_user_friends_ref.setValue("invited");
                                    }
                                });
                                profile_btn_addFriend.setText("Add Friend");
                                profile_btn_addFriend.setBackground(getResources().getDrawable(R.drawable.button));
                                profile_btn_addFriend.setTextColor(Color.WHITE);
                        }


                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initViews() {
        profile_img_profile_img = findViewById(R.id.profile_img_profile_img);
        profile_txt_name = findViewById(R.id.profile_txt_name);
        profile_txt_username = findViewById(R.id.profile_txt_username);
        profile_txt_status = findViewById(R.id.profile_txt_status);
        profile_txt_country = findViewById(R.id.profile_txt_country);
        profile_txt_dob = findViewById(R.id.profile_txt_dob);
        profile_txt_gender = findViewById(R.id.profile_txt_gender);
        profile_txt_relationship = findViewById(R.id.profile_txt_relationship);
        profile_btn_addFriend = findViewById(R.id.profile_btn_addFriend);

        mAuth = FirebaseAuth.getInstance();


        profile_toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(profile_toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        visit_user_id = getIntent().getExtras().get("visit_user_id").toString();
        current_user_id = mAuth.getCurrentUser().getUid();
        user_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(visit_user_id);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}