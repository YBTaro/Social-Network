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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText find_friends_edt_name;
    private ImageButton find_friends_imgBtn_search;
    private RecyclerView recyclerView;
    private DatabaseReference users_ref;
    private FirebaseRecyclerAdapter<FindFriends, MyViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerOptions<FindFriends> options;

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

    private void initViews(){
        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        find_friends_edt_name = findViewById(R.id.find_friends_edt_name);
        find_friends_imgBtn_search = findViewById(R.id.find_friends_imgBtn_search);

        recyclerView = findViewById(R.id.find_friends_recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        users_ref = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}