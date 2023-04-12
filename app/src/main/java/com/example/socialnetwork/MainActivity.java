package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
    private DatabaseReference user_ref;
    private CircleImageView nav_profile_img; // nav 的 圓形頭像
    private TextView nav_user_full_name; // nav 的 使用者全名
    private ImageButton img_btn_add_new_post;


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
                    if(snapshot.hasChild("profile_img")){
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
    }

    private void initViews(){
        navigationView = findViewById(R.id.navView);
        drawerLayout = findViewById(R.id.drawable_layout);
        myToolbar = findViewById(R.id.main_page_bar);
        mAuth = FirebaseAuth.getInstance();
        user_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        img_btn_add_new_post = findViewById(R.id.img_btn_add_new_post);



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
                        Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_home:
                        Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_friends:
                        Toast.makeText(MainActivity.this, "Friends", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_find_friends:
                        Toast.makeText(MainActivity.this, "Find Friends", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_messages:
                        Toast.makeText(MainActivity.this, "Messages", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_settings:
                        Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
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