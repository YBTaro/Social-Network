package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView chat_recView;
    private ImageButton chat_imgBtn_select_photo, chat_imgBtn_send;
    private EditText chat_edt_message;
    private String visit_user_id, visit_user_name, current_user_id;
    private DatabaseReference users_ref, message_ref;
    private TextView chat_custom_bar_username;
    private CircleImageView chat_custom_bar_imgView;
    private FirebaseRecyclerOptions<Messages> options;
    private FirebaseRecyclerAdapter<Messages, MyViewHolder> firebaseRecyclerAdapter;
    private boolean first_time_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();

        chat_imgBtn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_message();
            }
        });


        chat_recView.setLayoutManager(new LinearLayoutManager(this));
        message_firebase_recyclerView_adapter();
        chat_recView.setAdapter(firebaseRecyclerAdapter);








    }

    private void message_firebase_recyclerView_adapter(){
        options = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(message_ref.child(current_user_id).child(visit_user_id).orderByChild("timestamp"), Messages.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Messages model) {
                if(model.getFrom().equals(current_user_id)){
                    holder.message_layout_imgView_profile.setVisibility(View.GONE);
                    holder.message_layout_txt_receiver.setVisibility(View.GONE);
                    holder.message_layout_txt_sender.setVisibility(View.VISIBLE);
                    holder.message_layout_txt_sender.setText(model.getMessage());
                }else{
                    holder.message_layout_imgView_profile.setVisibility(View.VISIBLE);
                    holder.message_layout_txt_receiver.setVisibility(View.VISIBLE);
                    holder.message_layout_txt_sender.setVisibility(View.GONE);
                    holder.message_layout_txt_receiver.setText(model.getMessage());
                    users_ref.child(visit_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.child("profile_img").exists()&&!isDestroyed()){
                                Glide.with(ChatActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(holder.message_layout_imgView_profile);
                            }else{
                                holder.message_layout_imgView_profile.setImageResource(R.drawable.profile);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_layout_of_user, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            public void onDataChanged() { // 當firebase的資料變動，或是首次載入資料時，呼叫此程式
                super.onDataChanged();
                if(first_time_loading){
                    chat_recView.scrollToPosition(getItemCount()-1);
                    first_time_loading = false;
                }else {
                    if(getItem(getItemCount()-1).getFrom().equals(current_user_id)){
                        chat_recView.scrollToPosition(getItemCount()-1);
                    }else{
                        Toast.makeText(ChatActivity.this, visit_user_name+" send new message", Toast.LENGTH_SHORT).show();
                    }
                }



            }
        };



    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView message_layout_imgView_profile;
        private TextView message_layout_txt_sender, message_layout_txt_receiver;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            message_layout_imgView_profile = itemView.findViewById(R.id.message_layout_imgView_profile);
            message_layout_txt_sender = itemView.findViewById(R.id.message_layout_txt_sender);
            message_layout_txt_receiver = itemView.findViewById(R.id.message_layout_txt_receiver);
        }
    }

    private void send_message(){
        String message = chat_edt_message.getText().toString();
        if(!TextUtils.isEmpty(message)){
            DatabaseReference sender_ref = message_ref.child(current_user_id).child(visit_user_id);
            DatabaseReference receiver_ref = message_ref.child(visit_user_id).child(current_user_id);
            String message_key = sender_ref.push().getKey(); // 先建一個訊息節點出來，取得key值，sender 和 receiver 在相同訊息的 key 要是一樣的

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat current_date = new SimpleDateFormat("yyyy-MM-dd");
            String current_date_string = current_date.format(calendar.getTime());
            SimpleDateFormat current_time = new SimpleDateFormat("HH:mm aa");
            String current_time_string = current_time.format(calendar.getTime());
            SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String timestamp_string = timestamp.format(calendar.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("time", current_time_string);
            messageTextBody.put("date", current_date_string);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", current_user_id);
            messageTextBody.put("timestamp",timestamp_string);

            sender_ref.child(message_key).updateChildren(messageTextBody);
            receiver_ref.child(message_key).updateChildren(messageTextBody);

            chat_edt_message.setText("");
        }
    }

    private void initViews(){
        chat_recView = findViewById(R.id.chat_recView);
        chat_imgBtn_select_photo = findViewById(R.id.chat_imgBtn_select_photo);
        chat_imgBtn_send = findViewById(R.id.chat_imgBtn_send);
        chat_edt_message = findViewById(R.id.chat_edt_message);
        first_time_loading = true;


        users_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        message_ref = FirebaseDatabase.getInstance().getReference().child("Messages");
        visit_user_id = getIntent().getExtras().get("visit_user_id").toString();
        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 從系統取得layoutInflater實體
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null); // 用layoutInflater 實體建立View
        getSupportActionBar().setCustomView(action_bar_view); // 把 View 插入 Action bar

        chat_custom_bar_username = findViewById(R.id.chat_custom_bar_username);
        chat_custom_bar_imgView = findViewById(R.id.chat_custom_bar_imgView);

        users_ref.child(visit_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    visit_user_name = snapshot.child("fullname").getValue().toString();
                    chat_custom_bar_username.setText(visit_user_name);
                    if (snapshot.child("profile_img").exists()&&!isDestroyed()){
                        Glide.with(ChatActivity.this).asBitmap().load(snapshot.child("profile_img").getValue().toString()).into(chat_custom_bar_imgView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }
}