package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar myToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle; // 左上方的三條線，開啟NavigationView
    private FirebaseAuth mAuth;
    private DatabaseReference user_ref, post_ref, likes_ref;
    private CircleImageView nav_profile_img; // nav 的 圓形頭像
    private TextView nav_user_full_name; // nav 的 使用者全名
    private ImageButton img_btn_add_new_post;
    private RecyclerView rec_posts_list;
    private FirebaseRecyclerAdapter<Post, MyViewHolder> firebaseRecyclerAdapter;
    private String current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Home");

        // 建立 actionBarDrawerToggle (左上點擊會跳出左方視窗)
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState(); // 如果有點擊，更新上方三條線的圖案方向
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 顯示左上方三條線

        user_ref.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){ // snapshot 在該節點(user_ref.child(mAuth.getUid())) 是否存在
                    // 若是存在，確認是否有需要的資料在
                    if(snapshot.hasChild("profile_img") && !isDestroyed()){
                        Glide.with(MainActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(nav_profile_img);
                    }
                    if(snapshot.hasChild("fullname")){
                        nav_user_full_name.setText(snapshot.child("fullname").getValue().toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        img_btn_add_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });


        // 處理Firebase recycler adapter
        // ->先創一個FirebaseRecyclerOptions(規定的)
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(post_ref.orderByChild("timestamp"), Post.class)
                .build();
        // ->建立FirebaseRecyclerAdapter
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Post model) {
                holder.all_posts_txt_date.setText("  "+model.getDate());
//                holder.all_posts_txt_username.setText(model.getFullname()); 改成下面寫法同步更新ID
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
                    Glide.with(MainActivity.this).asBitmap().load(model.getPost_image()).into(holder.all_posts_img_img);
                }

                if(model.getProfile_image()!= null && !isDestroyed()){
                    Glide.with(MainActivity.this).asBitmap().load(model.getProfile_image()).into(holder.all_posts_circle_img_profile_img);
                }




                // 這篇貼文在 Firebase 的 key
                final String post_key = getRef(position).getKey(); // getRef 是 Firebase 內建的方法
                holder.all_posts_parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ClickPostActivity.class);
                        intent.putExtra("post_key", post_key);
                        startActivity(intent);
                    }
                });

                // 這篇貼文在 Firebase 有多少 comment
                post_ref.child(post_key).addValueEventListener(new ValueEventListener() { // 原本是寫 post_ref.child(post_key).child("comment").addValueEventListener，會一直閃退，原因推測是因為在沒有comment，或是從 1 刪到 0 comment時，comment 節點不存在，會監聽失敗
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("comment")!= null){
                            holder.all_posts_txt_numOfComments.setText(String.valueOf(snapshot.child("comment").getChildrenCount()));
//                            Toast.makeText(MainActivity.this, String.valueOf(snapshot.child("comment").getChildrenCount()), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




                // 取得 Likes 的相關資料 思考邏輯：從firebasedatabase 的 likes_ref 裡看有沒有某 PO文(post_key) 裡有沒有當前使用者id資料在裡面(有代表曾經點過讚)
                // ，若有則將圖片改成有愛心的，無則相反
                final boolean[] likeChecker = new boolean[1]; // 設置的參數，用來記錄現在是否有like該貼文
                likes_ref.addValueEventListener(new ValueEventListener() {
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

                // 按讚按鈕，只要修改雲端資料庫即可，修改後firebasedatabase 會更新資料，整個view會重新渲染，因此不用再設置按讚按鈕的圖片，或是修改likeChecker[0]的數值
                holder.all_posts_imgBtn_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(likeChecker[0]){
                            likes_ref.child(post_key).child(current_user_id).removeValue();

                        }else{
                            likes_ref.child(post_key).child(current_user_id).setValue(true);

                        }
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        rec_posts_list.setAdapter(firebaseRecyclerAdapter);



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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){ // 判斷是否為actionBarDrawerToggle被觸發，沒寫的畫點了漢堡會沒反應
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() { //程式啟動時，會直接跑下列程式碼，確認使用者身分
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){ //如果使用者沒有登入的話，切換到登入頁面
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{ //使用者有登入，確認是否已經有profile
            final String user_id = mAuth.getCurrentUser().getUid();
            user_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) { // 當FirebaseDatabase的節點(Users)有資料變動、或是第一次ValueEventListener時，就會跑接下來的程式碼
                    if(!snapshot.hasChild(user_id)){ // 如果在該節點找不到該User的資料，送到 setup 去(有登入了，但還沒有設定資料)
                        Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged(); // 可以解決返回頁面時出現的錯誤



    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();

    }

    private void initViews(){
        navigationView = findViewById(R.id.navView);
        drawerLayout = findViewById(R.id.drawable_layout);
        myToolbar = findViewById(R.id.main_page_bar);
        mAuth = FirebaseAuth.getInstance();
        user_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        img_btn_add_new_post = findViewById(R.id.img_btn_add_new_post);
        post_ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        likes_ref = FirebaseDatabase.getInstance().getReference().child("Likes");
        current_user_id = mAuth.getCurrentUser().getUid();

        // recycler view 相關
        rec_posts_list = findViewById(R.id.all_users_post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); // RecyclerView 會反轉 item 的顯示順序，從最後一個 item 開始顯示。
        linearLayoutManager.setStackFromEnd(true); // 當新增一個 item 時，RecyclerView 會從底部開始疊加新的 item。
        // 所以如果你想要讓 RecyclerView 從底部開始疊加新的 item，並且還想要反轉 item 的顯示順序，就需要同時設置這兩個屬性。
        rec_posts_list.setLayoutManager(linearLayoutManager);




        // 將 nav 的 header layout 插入其中
        View navHeaderView = navigationView.inflateHeaderView(R.layout.navigation_header);
        nav_profile_img = navHeaderView.findViewById(R.id.nav_profile_img);
        nav_user_full_name = navHeaderView.findViewById(R.id.nav_user_full_name);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_post:
                        Intent post_activity_intent = new Intent(MainActivity.this, PostActivity.class);
                        startActivity(post_activity_intent);
                        break;
                    case R.id.nav_profile:
                        Intent profile_intent = new Intent(MainActivity.this, ProfileActivity.class);
                        profile_intent.putExtra("visit_user_id",mAuth.getCurrentUser().getUid());
                        startActivity(profile_intent);
                        break;
                    case R.id.nav_home:
                        Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_friends:
                        Intent friends_intent = new Intent(MainActivity.this, FriendsActivity.class);
                        startActivity(friends_intent);
                        break;
                    case R.id.nav_find_friends:
                        Intent find_friends_intent = new Intent(MainActivity.this, FindFriendsActivity.class);
                        startActivity(find_friends_intent);
                        break;
                    case R.id.nav_messages:
                        Toast.makeText(MainActivity.this, "Messages", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_settings:
                        Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settings_intent);
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                }
                return false;
            }
        });


    }


}