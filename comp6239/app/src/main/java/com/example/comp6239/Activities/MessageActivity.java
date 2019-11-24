package com.example.comp6239.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.comp6239.Adapter.MessageAdapter;
import com.example.comp6239.Model.ChatModel;
import com.example.comp6239.Model.User;
import com.example.comp6239.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    FirebaseUser Fuser;
    DatabaseReference reference;
    ImageButton btn_send;
    EditText text_send;
    MessageAdapter messageAdapter;
    List<ChatModel> mChat;
    RecyclerView recyclerView;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        if(getIntent().getExtras()!=null) {
            final String userid = intent.getStringExtra("userid");
            Fuser = FirebaseAuth.getInstance().getCurrentUser();
            btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msg = text_send.getText().toString();
                    if (!msg.equals("")) {
                        sendMessage(Fuser.getUid(), userid, msg);
                    } else {
                        Toast.makeText(MessageActivity.this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
                    }
                    text_send.setText("");
                }
            });

            reference = FirebaseDatabase.getInstance().getReference("User").child(userid);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        if(profile_image!=null)
                            profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        if(profile_image!=null)
                            Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_image);
                    }
                    readMessages(Fuser.getUid(), userid, user.getImageURL());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void  sendMessage (String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message",message);
        reference.child("Chats").push().setValue(hashMap);

    }

    private void readMessages (final String myid, final String userid, final String imageurl){
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatModel chatModel = snapshot.getValue(ChatModel.class);
                    if (chatModel.getReceiver().equals(myid) && chatModel.getSender().equals(userid) ||
                        chatModel.getReceiver().equals(userid) && chatModel.getSender().equals(myid)){
                            mChat.add(chatModel);
                        }
                        messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
                        recyclerView.setAdapter(messageAdapter);

                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
