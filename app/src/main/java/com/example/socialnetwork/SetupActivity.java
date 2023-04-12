package com.example.socialnetwork;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText username, fullName, country;
    private Button btn_save;
    private CircleImageView selfie;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String current_user_id;
    private ProgressDialog loadingBar;
    private StorageReference selfie_img_reference;
//    private ActivityResultLauncher<String> gallery_photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        initViews();
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s_username = username.getText().toString();
                String s_fullName = fullName.getText().toString();
                String s_country = country.getText().toString();

                if(TextUtils.isEmpty(s_username)){
                    Toast.makeText(SetupActivity.this, "Please write your username.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(s_fullName)){
                    Toast.makeText(SetupActivity.this, "Please write your full name.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(s_country)){
                    Toast.makeText(SetupActivity.this, "Please write your country.", Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.setTitle("Saving information");
                    loadingBar.setMessage("Please wait, while we are creating your new account.");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();
                    HashMap userMap = new HashMap();
                    userMap.put("username", s_username);
                    userMap.put("fullname", s_fullName);
                    userMap.put("country", s_country);
                    userMap.put("status", "");
                    userMap.put("gender", "none");
                    userMap.put("relationshipstatus", "none");
                    userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SetupActivity.this, "Your account is created successfully.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                loadingBar.dismiss();
                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Error occurred : "+ error, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

                }
            }
        });
        selfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SetupActivity.this);
//                gallery_photo.launch("image/*");
            }
        });

        // 當使用者資料變動時，重新下載資料並顯示在畫面上
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("profile_img")){
                        Glide.with(SetupActivity.this).asBitmap().load(snapshot.child("profile_img").getValue()).into(selfie);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initViews(){
        username = findViewById(R.id.edt_setup_username);
        fullName = findViewById(R.id.edt_setup_fullName);
        country = findViewById(R.id.edt_setup_country);
        btn_save = findViewById(R.id.btn_setup_save);
        selfie = findViewById(R.id.img_setup_selfie);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        selfie_img_reference = FirebaseStorage.getInstance().getReference().child("User Images");
        loadingBar = new ProgressDialog(this);
//         讓使用者可以將手機的圖片傳上來
//        gallery_photo = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
//            @Override
//            public void onActivityResult(Uri result) {
//            }
//        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(this, "requestCode: "+requestCode+" "+CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE+ "resultCode: "+ resultCode+" "+ RESULT_OK, Toast.LENGTH_LONG).show();

        // 如果回傳的東西來自於CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE，且成功完成任務
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK ){

            loadingBar.setTitle("Saving your profile image...");
            loadingBar.setMessage("Please wait, while we are saving your profile image.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri(); // 取得選擇檔案的Uri


            selfie_img_reference.child(current_user_id+".jpg").putFile(resultUri) // 將該檔案存到Storage
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){ // 若成功存到storage，取得該檔案的下載uri
                                Toast.makeText(SetupActivity.this, "Profile image is saved successfully.", Toast.LENGTH_SHORT).show();
                                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() { // 取得該檔案的下載uri
                                    @Override
                                    public void onSuccess(Uri uri) { // 如果成功下載該uri，就執行...
                                        String downloadUrl = uri.toString();
                                        userRef.child("profile_img").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {  // 將該檔案在storage的uri，存到database 的 user 資料中
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SetupActivity.this, "Profile image store to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                                    }
                                });

                            }
                        }
                    });

        }
    }
}