package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class FriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<FriendsRequests, MyViewHolder> firebaseRecyclerAdapter;  // 借用FriendsRequest class
    private FirebaseRecyclerOptions<FriendsRequests> firebaseRecyclerOptions;
    private DatabaseReference users_ref;
    private String current_user_id, friends_of_whom;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        initViews();
        firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<FriendsRequests>()
                .setQuery(users_ref.child(friends_of_whom).child("friends").orderByChild("status").equalTo("friend"), FriendsRequests.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendsRequests, MyViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull FriendsRequests model) {

                String friend_id = getRef(holder.getBindingAdapterPosition()).getKey();
                users_ref.child(friend_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            holder.friends_txt_username.setText(snapshot.child("fullname").getValue().toString());
                            holder.friends_txt_status.setText(snapshot.child("status").getValue().toString());
                            if(snapshot.hasChild("profile_img")&&!isDestroyed()){
                                Glide.with(FriendsActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(holder.friends_imgView_profile);
                            }
                            holder.friends_list_layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");
                                    String[] items = {snapshot.child("fullname").getValue().toString()+"'s profile", "Send Message"};
                                    builder.setItems(items, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which){
                                                case 0:
                                                    Intent intent = new Intent(FriendsActivity.this, ProfileActivity.class);
                                                    intent.putExtra("visit_user_id", friend_id);
                                                    startActivity(intent);
                                                    break;
                                                case 1:
                                                    Intent chat_intent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                    chat_intent.putExtra("visit_user_id", friend_id);
                                                    startActivity(chat_intent);
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                    });
                                    builder.show();
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
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friends_list_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.notifyDataSetChanged(); // 不寫會出錯
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void initViews(){
        users_ref = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = findViewById(R.id.friends_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        textView = findViewById(R.id.TextView0);

        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friends_of_whom = getIntent().getExtras().get("friends_of_whom").toString();


        mToolbar = findViewById(R.id.friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        users_ref.child(friends_of_whom).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("fullname").exists()){
                    getSupportActionBar().setTitle(snapshot.child("fullname").getValue().toString() + "'s Friends");
                    textView.setText(snapshot.child("fullname").getValue().toString() + "'s Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView friends_imgView_profile;
        private TextView friends_txt_username, friends_txt_status;
        private LinearLayout friends_list_layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            friends_imgView_profile = itemView.findViewById(R.id.friends_imgView_profile);
            friends_txt_username = itemView.findViewById(R.id.friends_txt_username);
            friends_txt_status = itemView.findViewById(R.id.friends_txt_status);
            friends_list_layout = itemView.findViewById(R.id.friends_list_layout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}