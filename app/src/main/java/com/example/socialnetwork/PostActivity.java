package com.example.socialnetwork;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton img_btn_select_img;
    private Button btn_post;
    private EditText edt_postDescription;
    private ActivityResultLauncher<String> gallery_photo;
    private Uri img_uri;
    private StorageReference postImgRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mDB_ref;
    private DatabaseReference userRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initViews();

        img_btn_select_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallery_photo.launch("image/*");
            }
        });

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(img_uri == null){
                    Toast.makeText(PostActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(edt_postDescription.getText().toString())){
                    Toast.makeText(PostActivity.this, "Please say something about the image.", Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.setTitle("Updating Post");
                    loadingBar.setMessage("please wait, while saving your post...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();
                    storeImgToFirebase();

                }
            }

            private void storeImgToFirebase(){
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String currentDate = dateFormat.format(calendar.getTime());
                String currentTime = timeFormat.format(calendar.getTime());

                // 檔案名稱：原檔案uri最後片段的名稱(原檔名)+日期+時間
                StorageReference filePath = postImgRef.child(img_uri.getLastPathSegment()+currentDate+"-"+currentTime+".jpg");
                filePath.putFile(img_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(PostActivity.this, "Successfully saved image to firebase.", Toast.LENGTH_SHORT).show();
                            task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    sendDataToFirebase(currentDate,currentTime, uri.toString());
                                }
                            });

                        }else {
                            Toast.makeText(PostActivity.this, "Error Occurred:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            private void sendDataToFirebase(String date, String time, String downloadUrl){


                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            HashMap postMap = new HashMap();
                            if(snapshot.hasChild("profile_img")){
                                postMap.put("profile_image", snapshot.child("profile_img").getValue().toString());
                            }
                            if(snapshot.hasChild("fullname")){
                                postMap.put("fullname",snapshot.child("fullname").getValue().toString());
                            }
                            postMap.put("uid", mAuth.getCurrentUser().getUid());
                            postMap.put("date", date);
                            postMap.put("time", time);
                            postMap.put("description",edt_postDescription.getText().toString());
                            postMap.put("post_image", downloadUrl);

                            mDB_ref.child("Posts").child(mAuth.getCurrentUser().getUid()+date+time)
                                    .updateChildren(postMap)
                                    .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(PostActivity.this, "Error occurred:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }else{
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });
    }


    private void initViews(){
        mToolbar = findViewById(R.id.post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 啟用回到上一頁功能
        getSupportActionBar().setDisplayShowHomeEnabled(true); // 顯示回到上一頁按鈕
        getSupportActionBar().setTitle("Update Post");
        img_btn_select_img = findViewById(R.id.img_btn_select_img);
        btn_post = findViewById(R.id.btn_post);
        edt_postDescription = findViewById(R.id.edt_postDescription);
        gallery_photo = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                img_uri = result;
                img_btn_select_img.setImageURI(result);
            }
        });
        postImgRef = FirebaseStorage.getInstance().getReference().child("Post Images");
        mAuth = FirebaseAuth.getInstance();
        mDB_ref = FirebaseDatabase.getInstance().getReference();
        userRef = mDB_ref.child("Users").child(mAuth.getCurrentUser().getUid());
        loadingBar = new ProgressDialog(this);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}