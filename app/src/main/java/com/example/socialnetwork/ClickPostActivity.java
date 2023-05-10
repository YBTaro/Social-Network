package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView click_post_imgView_img;
    private TextView click_post_txt_description;
    private Button click_post_btn_edt, click_post_btn_delete;
    private String post_key, currentUserId, postUserId;
    private DatabaseReference click_post_ref, current_user_ref, comments_ref, users_ref;
    private FirebaseAuth mAuth;
    private ImageButton click_post_imgBtn_post;
    private EditText click_post_edt_comment;
    private FirebaseRecyclerAdapter<Comments, MyViewHolder> firebaseRecyclerAdapter;
    private RecyclerView click_post_recView_comments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        initViews();
        click_post_btn_delete.setVisibility(View.GONE);
        click_post_btn_edt.setVisibility(View.GONE);

        // 處理firebase 取得的貼文資料


        click_post_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String description = snapshot.child("description").getValue().toString();
                    String post_img = snapshot.child("post_image").getValue().toString();
                    click_post_txt_description.setText(description);
                    Glide.with(ClickPostActivity.this).asBitmap().load(post_img).into(click_post_imgView_img);
                    postUserId = snapshot.child("uid").getValue().toString();
                    if (postUserId.equals(currentUserId)) {
                        click_post_btn_delete.setVisibility(View.VISIBLE);
                        click_post_btn_edt.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        click_post_btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
                builder.setMessage("Are you sure to delete this post?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        click_post_ref.removeValue();
                        Intent intent = new Intent(ClickPostActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                Dialog dialog = builder.create();
                dialog.show();
                // 要取得positive 和 negative Button，要在 dialog.show() 之後，不然會回傳null
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);


            }
        });

        click_post_btn_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
                builder.setMessage("Edit Post");
                EditText edt_post = new EditText(ClickPostActivity.this);
                edt_post.setText(click_post_txt_description.getText().toString());
                // 要改變EditText的寬度，需要將其塞在一個 container 中
                LinearLayout container = new LinearLayout(ClickPostActivity.this);
                LinearLayout.LayoutParams lp =  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(20,0,20,0);
                container.addView(edt_post,lp);
                // ---------------------
                builder.setView(container);
                builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        click_post_ref.child("description").setValue(edt_post.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ClickPostActivity.this, "Successfully update your post.", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ClickPostActivity.this, "Sorry, something wrong when updating your post." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                Dialog dialog = builder.create();
                dialog.show();
                // 要取得positive 和 negative Button，要在 dialog.show() 之後，不然會回傳null
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
            }
        });


        click_post_imgBtn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = click_post_edt_comment.getText().toString();
                if(!TextUtils.isEmpty(comment)){
                    current_user_ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.exists()){
                                String username = snapshot.child("username").getValue().toString();
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy");
                                final String s_date = date.format(calendar.getTime());
                                SimpleDateFormat time = new SimpleDateFormat("HH-mm-ss");
                                final String s_time = time.format(calendar.getTime());

                                final String randoKey = currentUserId + s_date + s_time;
                                HashMap hashMap = new HashMap<>();
                                hashMap.put("uid", currentUserId);
                                hashMap.put("comment", comment);
                                hashMap.put("date", s_date);
                                hashMap.put("time", s_time);
                                hashMap.put("username", username);
                                click_post_ref.child("comment").child(randoKey).updateChildren(hashMap)  // setValue，取代原本資料，updateChildren只更新部分資料
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if(!task.isSuccessful()){
                                                    Toast.makeText(ClickPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                }else{
                                                    click_post_edt_comment.setText("");
                                                    firebaseRecyclerAdapter.notifyDataSetChanged();
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


            }
        });

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(comments_ref, Comments.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Comments model) {
                holder.all_comments_txt_comment.setText(model.getComment());
                holder.all_comments_txt_date.setText(model.getDate());
                holder.all_comments_txt_time.setText(model.getTime());
                // 設定刪除留言按鈕
                if(currentUserId.equals(model.getUid())){
                    holder.all_comments_imgBtn_delete.setVisibility(View.VISIBLE);
                    holder.all_comments_imgBtn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String comment_key = getRef(holder.getBindingAdapterPosition()).getKey();
                            comments_ref.child(comment_key).removeValue();
                            notifyDataSetChanged();
                        }
                    });
                }else{
                    holder.all_comments_imgBtn_delete.setVisibility(View.GONE);
                }


                final String uid = model.getUid();
                users_ref.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            holder.all_comments_txt_username.setText(snapshot.child("username").getValue().toString());
                            Glide.with(ClickPostActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(holder.all_comments_imgView_profile_img);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout,parent, false);
                return new MyViewHolder(view);
            }
        };

        click_post_recView_comments.setAdapter(firebaseRecyclerAdapter);
    }

    private void initViews(){
        click_post_imgView_img = findViewById(R.id.click_post_imgView_img);
        click_post_txt_description = findViewById(R.id.click_post_txt_description);
        click_post_btn_edt = findViewById(R.id.click_post_btn_edt);
        click_post_btn_delete = findViewById(R.id.click_post_btn_delete);
        click_post_imgBtn_post = findViewById(R.id.click_post_imgBtn_post);
        click_post_edt_comment = findViewById(R.id.click_post_edt_comment);

        post_key = getIntent().getExtras().get("post_key").toString();
        click_post_ref = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_key);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        current_user_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        users_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        comments_ref = click_post_ref.child("comment");

        click_post_recView_comments = findViewById(R.id.click_post_recView_comments);
        click_post_recView_comments.setLayoutManager(new LinearLayoutManager(this));





    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView all_comments_imgView_profile_img;
        TextView all_comments_txt_username, all_comments_txt_date, all_comments_txt_time, all_comments_txt_comment;
        ImageButton all_comments_imgBtn_delete;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            all_comments_imgView_profile_img = itemView.findViewById(R.id.all_comments_imgView_profile_img);
            all_comments_txt_username = itemView.findViewById(R.id. all_comments_txt_username);
            all_comments_txt_date = itemView.findViewById(R.id.all_comments_txt_date);
            all_comments_txt_time = itemView.findViewById(R.id.all_comments_txt_time);
            all_comments_txt_comment = itemView.findViewById(R.id.all_comments_txt_comment);
            all_comments_imgBtn_delete = itemView.findViewById(R.id.all_comments_imgBtn_delete);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }
}