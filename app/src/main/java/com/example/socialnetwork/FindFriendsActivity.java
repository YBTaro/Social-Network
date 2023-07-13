package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText find_friends_edt_name;
    private ImageButton find_friends_imgBtn_search;
    private RecyclerView recyclerView, friends_requests_recyclerView;
    private DatabaseReference users_ref, users_friends_ref;
    private FirebaseRecyclerAdapter<FindFriends, MyViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<FriendsRequests, FriendsRequestViewHolder> friends_requests_firebaseRecyclerAdapter;
    private FirebaseRecyclerOptions<FindFriends> options;
    private FirebaseRecyclerOptions<FriendsRequests> friends_requests_option;
    private String current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        initViews();

        options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(users_ref, FindFriends.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull FindFriends model) {
                holder.card_txt_fullname.setText(model.getFullname());
                holder.card_txt_status.setText(model.getStatus());

                if(model.getProfile_img()!= null){
                    Glide.with(FindFriendsActivity.this).asBitmap().load(model.getProfile_img()).into(holder.card_profile_img);
                }else{ // 要加這個不然其他recyclerView在展開時會影響到預設的照片
                    holder.card_profile_img.setImageResource(R.drawable.profile);
                }


                TransitionManager.beginDelayedTransition(holder.find_friends_card);

                if(model.isExpanded()){
                    holder.card_corrupted_layout.setVisibility(View.VISIBLE);
                    holder.card_img_downArrow.setVisibility(View.GONE);
                }else{
                    holder.card_corrupted_layout.setVisibility(View.GONE);
                    holder.card_img_downArrow.setVisibility(View.VISIBLE);
                }

                holder.card_img_downArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        model.isExpanded = true; // 資料要放在model，不要放在holder
                        notifyItemChanged(holder.getBindingAdapterPosition());

                    }
                });

                holder.card_img_upArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        model.isExpanded = false;
                        notifyItemChanged(holder.getBindingAdapterPosition());

                    }
                });

                holder.find_friends_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String friend_id = getRef(holder.getBindingAdapterPosition()).getKey();
                        Intent intent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        intent.putExtra("visit_user_id", friend_id);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);
                return new FindFriendsActivity.MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);

        find_friends_imgBtn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String find_name = find_friends_edt_name.getText().toString();
                if(find_name.equals("")){
                    options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                            .setQuery(users_ref, FindFriends.class)
                            .setLifecycleOwner(FindFriendsActivity.this)
                            .build();
                }else{
                    options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                            .setQuery(users_ref.orderByChild("fullname").startAt(find_name).endAt(find_name+"\uf8ff"), FindFriends.class)
                            .setLifecycleOwner(FindFriendsActivity.this)
                            .build();
                }
                firebaseRecyclerAdapter.updateOptions(options);
                firebaseRecyclerAdapter.notifyDataSetChanged(); // 要加這行，不然會閃退
            }
        });

        show_friends_requests_handler();
    }

    private void show_friends_requests_handler(){
        friends_requests_option = new FirebaseRecyclerOptions.Builder<FriendsRequests>()
                .setQuery(users_friends_ref.orderByChild("status").equalTo("invited"), FriendsRequests.class)
                .build();

        friends_requests_firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendsRequests, FriendsRequestViewHolder>(friends_requests_option) { // 取得所有邀請我好友的人的資料
            @Override
            protected void onBindViewHolder(@NonNull FriendsRequestViewHolder holder, int position, @NonNull FriendsRequests model) {

                final String key = getRef(holder.getBindingAdapterPosition()).getKey(); // key 是邀請我好友的人的ID
                users_ref.child(key).addValueEventListener(new ValueEventListener() { // 取得邀請我好友的人的資料，顯示在recyclerview
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            holder.all_friends_requests_layout_username.setText(snapshot.child("fullname").getValue().toString());

                            if(snapshot.child("profile_img").exists()&& !isDestroyed()){
                                Glide.with(FindFriendsActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(holder.all_friends_requests_layout_imgView_profile);
                            }

                            holder.all_friends_requests_layout_imgBtn_accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    users_ref.child(key).child("friends").child(current_user).child("status").setValue("friend"); // 在對方的好友資料裡加上我ID
                                    users_ref.child(current_user).child("friends").child(key).child("status").setValue("friend"); // 在我的好友資料裡加上對方ID
                                }
                            });

                            holder.all_friends_requests_layout_imgBtn_reject.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    users_ref.child(current_user).child("friends").child(key).child("status").removeValue(); // 刪除對方對我的邀請
                                }
                            });



                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_friend_requests_layout, parent, false);
                return new FindFriendsActivity.FriendsRequestViewHolder(view);
            }
        };

        friends_requests_recyclerView.setAdapter(friends_requests_firebaseRecyclerAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
        friends_requests_firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
        friends_requests_firebaseRecyclerAdapter.stopListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        CardView find_friends_card;
        TextView card_txt_fullname,card_txt_status;
        ImageView card_img_downArrow, card_img_upArrow, card_profile_img;
        RelativeLayout card_corrupted_layout;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            find_friends_card = itemView.findViewById(R.id.find_friends_card);
            card_txt_fullname = itemView.findViewById(R.id.card_txt_fullname);
            card_txt_status = itemView.findViewById(R.id.card_txt_status);
            card_img_upArrow = itemView.findViewById(R.id.card_img_upArrow);
            card_img_downArrow = itemView.findViewById(R.id.card_img_downArrow);
            card_profile_img = itemView.findViewById(R.id.card_profile_img);
            card_corrupted_layout = itemView.findViewById(R.id.card_corrupted_layout);

        }

    }

    public static class FriendsRequestViewHolder extends RecyclerView.ViewHolder{
        CircleImageView all_friends_requests_layout_imgView_profile;
        TextView all_friends_requests_layout_username;
        ImageButton all_friends_requests_layout_imgBtn_accept, all_friends_requests_layout_imgBtn_reject;
        public FriendsRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            all_friends_requests_layout_imgBtn_accept = itemView.findViewById(R.id.all_friends_requests_layout_imgBtn_accept);
            all_friends_requests_layout_imgBtn_reject = itemView.findViewById(R.id.all_friends_requests_layout_imgBtn_reject);
            all_friends_requests_layout_username = itemView.findViewById(R.id.all_friends_requests_layout_username );
            all_friends_requests_layout_imgView_profile = itemView.findViewById(R.id.all_friends_requests_layout_imgView_profile);

        }
    }

    private void initViews(){
        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        find_friends_edt_name = findViewById(R.id.find_friends_edt_name);
        find_friends_imgBtn_search = findViewById(R.id.find_friends_imgBtn_search);

        recyclerView = findViewById(R.id.find_friends_recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        current_user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        users_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        users_friends_ref = users_ref.child(current_user).child("friends");

        friends_requests_recyclerView = findViewById(R.id.find_friends_friends_request_recView);
        friends_requests_recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}