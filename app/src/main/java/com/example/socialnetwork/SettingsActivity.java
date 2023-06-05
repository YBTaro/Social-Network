package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar settings_toolbar;
    private EditText settings_edt_status, settings_edt_username, settings_edt_profile_name, settings_edt_dob, settings_edt_country;
    private RadioGroup settings_rg_gender, settings_rg_relationship;
    private CircleImageView settings_profile_img;
    private Button settings_btn_update;
    private FirebaseAuth mAuth;
    private DatabaseReference user_ref;
    private String userID;
    private ProgressDialog loadingBar;
    private StorageReference user_profile_img_ref;
    private Uri crop_img_result_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
//                    Toast.makeText(SettingsActivity.this, "snapshot.exists", Toast.LENGTH_SHORT).show();
                    String status = snapshot.child("status").getValue().toString();
                    String username = snapshot.child("username").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String gender = snapshot.child("gender").getValue().toString();
                    String profile_name = snapshot.child("fullname").getValue().toString();
                    String dob = snapshot.child("dob").getValue().toString();
                    String relationship = snapshot.child("relationshipstatus").getValue().toString();
                    String profile_img_url = snapshot.child("profile_img").getValue().toString();

//                    Toast.makeText(SettingsActivity.this, country, Toast.LENGTH_SHORT).show();
                    settings_edt_status.setText(status);
                    settings_edt_username.setText(username);
                    settings_edt_country.setText(country);
                    settings_edt_dob.setText(dob);
                    settings_edt_profile_name.setText(profile_name);
                    if(profile_img_url!=null){
                        Glide.with(SettingsActivity.this).asBitmap().load(profile_img_url).into(settings_profile_img);
                    }


                    switch (gender){
                        case "Male":
                            settings_rg_gender.check(R.id.settings_rb_male);
                            break;
                        case "Female":
                            settings_rg_gender.check(R.id.settings_rb_female);
                            break;
                        case "None":
                            settings_rg_gender.check(R.id.settings_rb_none);
                            break;
                        default:

                    }

                    switch (relationship) {
                        case "Single":
                            settings_rg_relationship.check(R.id.settings_rb_single);
                            break;
                        case "Married":
                            settings_rg_relationship.check(R.id.settings_rb_married);
                            break;
                        case "Unknown":
                            settings_rg_relationship.check(R.id.settings_rb_unknown);
                            break;
                        case "In Relationship":
                            settings_rg_relationship.check(R.id.settings_rb_in_relationship);

                        default:
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        settings_btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = settings_edt_status.getText().toString();
                String username = settings_edt_username.getText().toString();
                String country = settings_edt_country.getText().toString();
                String profile_name = settings_edt_profile_name.getText().toString();
                String dob = settings_edt_dob.getText().toString();
                String relationship;
                String gender;
                switch (settings_rg_gender.getCheckedRadioButtonId()){
                    case R.id.settings_rb_male:
                        gender = "Male";
                        break;
                    case R.id.settings_rb_female:
                        gender = "Female";
                        break;
                    case R.id.settings_rb_none:
                        gender = "None";
                        break;
                    default:
                        gender = "";
                }

                switch (settings_rg_relationship.getCheckedRadioButtonId()){
                    case R.id.settings_rb_single:
                        relationship = "Single";
                        break;
                    case R.id.settings_rb_married:
                        relationship = "Married";
                        break;
                    case R.id.settings_rb_unknown:
                        relationship = "Unknown";
                        break;
                    case R.id.settings_rb_in_relationship:
                        relationship = "In Relationship";
                        break;
                    default:
                        relationship = "";
                }

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(SettingsActivity.this, "Please write your username.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(profile_name)){
                    Toast.makeText(SettingsActivity.this, "Please write your profile name.", Toast.LENGTH_SHORT).show();
                }else{
                    HashMap hashMap = new HashMap();
                    hashMap.put("username", username);
                    hashMap.put("fullname", profile_name);
                    hashMap.put("country", country);
                    hashMap.put("dob", dob);
                    hashMap.put("relationshipstatus", relationship);
                    hashMap.put("gender", gender);
                    hashMap.put("status", status);

                    loadingBar.setTitle("Updating your account settings...");
                    loadingBar.setMessage("Please wait, while we are updating your account settings.");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();



                    user_ref.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){// database 資料處理成功後，才來處理cropimage存到storage 和更新database 裡profile_img 資料

                                // 如果有使用cropimage(crop_img_result_uri 裡會有資料)
                                if(crop_img_result_uri != null){
                                    user_profile_img_ref.child(userID + ".jpg").putFile(crop_img_result_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                // 因為新增的檔案在雲端 Storage的名稱與原本相同，因此不用更新Firebase database 裡的profile_img 資料
                                                loadingBar.dismiss();
                                                backToMainActivity();

                                            }else{
                                                loadingBar.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Something wrong while uploading your profile image. Error Message: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else{
                                    loadingBar.dismiss();
                                    backToMainActivity();

                                }
                            }else{
                                loadingBar.dismiss();
                                Toast.makeText(SettingsActivity.this, "Error message: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });







                }




            }
        });

        settings_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK ){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            crop_img_result_uri = result.getUri();
            Glide.with(this).asBitmap().load(crop_img_result_uri).into(settings_profile_img);


        }
    }

    private void initViews(){
        settings_toolbar = (Toolbar) findViewById(R.id.settings_toolBar);
        setSupportActionBar(settings_toolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settings_edt_status = findViewById(R.id.settings_edt_status);
        settings_edt_username = findViewById(R.id.settings_edt_username);
        settings_edt_profile_name = findViewById(R.id.settings_edt_profile_name);
        settings_edt_dob = findViewById(R.id.settings_edt_dob);
        settings_edt_country = findViewById(R.id.settings_edt_country);
        settings_rg_gender = findViewById(R.id.settings_rg_gender);
        settings_rg_relationship = findViewById(R.id.settings_rg_relationship);
        settings_profile_img = findViewById(R.id.settings_profile_img);
        settings_btn_update = findViewById(R.id.settings_btn_update);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        user_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

        loadingBar = new ProgressDialog(this);
        user_profile_img_ref = FirebaseStorage.getInstance().getReference().child("User Images");
        }


    private void backToMainActivity(){
        Toast.makeText(SettingsActivity.this, "Successfully update your account settings.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}