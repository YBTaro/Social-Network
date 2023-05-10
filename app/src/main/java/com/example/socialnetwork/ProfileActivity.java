package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
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
    private String userId;
    private DatabaseReference user_ref;
    private Toolbar profile_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    profile_txt_name.setText(snapshot.child("fullname").getValue().toString());
                    profile_txt_username.setText("@"+snapshot.child("username").getValue().toString());
                    profile_txt_status.setText(snapshot.child("status").getValue().toString());
                    profile_txt_country.setText("Country: "+snapshot.child("username").getValue().toString());
                    profile_txt_dob.setText("Birthday: "+snapshot.child("dob").getValue().toString());
                    profile_txt_gender.setText("Gender: "+snapshot.child("gender").getValue().toString());
                    profile_txt_relationship.setText("Relationship status: "+snapshot.child("relationshipstatus").getValue().toString());
                    Glide.with(ProfileActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(profile_img_profile_img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initViews(){
        profile_img_profile_img = findViewById(R.id.profile_img_profile_img);
        profile_txt_name = findViewById(R.id.profile_txt_name);
        profile_txt_username = findViewById(R.id.profile_txt_username);
        profile_txt_status = findViewById(R.id.profile_txt_status);
        profile_txt_country = findViewById(R.id.profile_txt_country);
        profile_txt_dob = findViewById(R.id.profile_txt_dob);
        profile_txt_gender = findViewById(R.id.profile_txt_gender);
        profile_txt_relationship = findViewById(R.id.profile_txt_relationship);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        user_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        profile_toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(profile_toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}