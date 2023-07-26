package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostActivity extends AppCompatActivity {
    private Toolbar myPost_appBar;
    private RecyclerView myPost_recView;
    private String posts_of_whom, current_user_id;
    private FirebaseRecyclerOptions<Post> options;
    private FirebaseRecyclerAdapter<Post, MyViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference post_ref, user_ref, like_ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);
        initViews();
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(post_ref.orderByChild("uid").equalTo(posts_of_whom), Post.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Post model) {
                holder.all_posts_txt_date.setText("  "+model.getDate());
                String poster_id = model.getUid();
                user_ref.child(poster_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            holder.all_posts_txt_username.setText(snapshot.child("fullname").getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.all_posts_txt_description.setText(model.getDescription());
                holder.all_posts_txt_time.setText("  "+model.getTime());
                if(!isDestroyed()){
                    Glide.with(MyPostActivity.this).asBitmap().load(model.getPost_image()).into(holder.all_posts_img_img);
                }

                if(model.getProfile_image()!= null && !isDestroyed()){
                    Glide.with(MyPostActivity.this).asBitmap().load(model.getProfile_image()).into(holder.all_posts_circle_img_profile_img);
                }




                // 這篇貼文在 Firebase 的 key
                final String post_key = getRef(position).getKey(); // getRef 是 Firebase 內建的方法
                holder.all_posts_parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MyPostActivity.this, ClickPostActivity.class);
                        intent.putExtra("post_key", post_key);
                        startActivity(intent);
                    }
                });

                // 這篇貼文在 Firebase 有多少 comment
                post_ref.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("comment")!= null){
                            holder.all_posts_txt_numOfComments.setText(String.valueOf(snapshot.child("comment").getChildrenCount()));
//                            Toast.makeText(MainActivity.this, String.valueOf(snapshot.child("comment").getChildrenCount()), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MyPostActivity.this, "null", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




                // 取得 Likes 的相關資料 思考邏輯：從firebasedatabase 的 likes_ref 裡看有沒有某 PO文(post_key) 裡有沒有當前使用者id資料在裡面(有代表曾經點過讚)
                // ，若有則將圖片改成有愛心的，無則相反
                final boolean[] likeChecker = new boolean[1]; // 設置的參數，用來記錄現在是否有like該貼文
                like_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(post_key).hasChild(current_user_id)){
                            int countLikes = (int)snapshot.child(post_key).getChildrenCount();
                            holder.all_posts_imgBtn_like.setImageResource(R.drawable.like);
                            holder.all_posts_txt_numOfLikes.setText(countLikes + " likes");
                            likeChecker[0] = true;
                        }else{
                            int countLikes = (int)snapshot.child(post_key).getChildrenCount();
                            holder.all_posts_imgBtn_like.setImageResource(R.drawable.dislike);
                            holder.all_posts_txt_numOfLikes.setText(countLikes + " likes");
                            likeChecker[0] = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // 按讚按鈕，只要修改雲端資料庫即可，修改後 firebasedatabase 會更新資料，整個view會重新渲染，因此不用再設置按讚按鈕的圖片，或是修改likeChecker[0]的數值
                holder.all_posts_imgBtn_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(likeChecker[0]){
                            like_ref.child(post_key).child(current_user_id).removeValue();

                        }else{
                            like_ref.child(post_key).child(current_user_id).setValue(true);

                        }
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout, parent, false);
                return new MyPostActivity.MyViewHolder(view);
            }
        };

        myPost_recView.setAdapter(firebaseRecyclerAdapter);

        user_ref.child(posts_of_whom).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    getSupportActionBar().setTitle(snapshot.child("fullname").getValue().toString() + "'s Posts");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void initViews(){
        posts_of_whom = getIntent().getExtras().get("posts_of_whom").toString();
        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myPost_appBar = findViewById(R.id.myPost_appBar);
        setSupportActionBar(myPost_appBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myPost_recView = findViewById(R.id.myPost_recView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPost_recView.setLayoutManager(linearLayoutManager);

        post_ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        user_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        like_ref = FirebaseDatabase.getInstance().getReference().child("Likes");
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView all_posts_circle_img_profile_img;
        TextView all_posts_txt_username, all_posts_txt_date,all_posts_txt_time, all_posts_txt_description, all_posts_txt_numOfLikes, all_posts_txt_numOfComments;
        ImageView all_posts_img_img;
        ConstraintLayout all_posts_parent;
        ImageButton all_posts_imgBtn_like, all_posts_imgBtn_comment;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            all_posts_circle_img_profile_img = itemView.findViewById(R.id.all_posts_circle_img_profile_img);
            all_posts_txt_username = itemView.findViewById(R.id.all_posts_txt_username);
            all_posts_txt_date = itemView.findViewById(R.id.all_posts_txt_date);
            all_posts_txt_time = itemView.findViewById(R.id.all_posts_txt_time);
            all_posts_txt_description = itemView.findViewById(R.id.all_posts_txt_description);
            all_posts_img_img = itemView.findViewById(R.id.all_posts_img_img);
            all_posts_parent = itemView.findViewById(R.id.all_post_parent);
            all_posts_txt_numOfLikes = itemView.findViewById(R.id.all_posts_txt_numOfLikes);
            all_posts_imgBtn_like = itemView.findViewById(R.id.all_posts_imgBtn_like);
            all_posts_imgBtn_comment = itemView.findViewById(R.id.all_posts_imgBtn_comment);
            all_posts_txt_numOfComments = itemView.findViewById(R.id.all_posts_txt_numOfComments);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}