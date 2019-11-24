package com.example.comp6239.Activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.comp6239.Fragments.ChatsFragment;
import com.example.comp6239.Fragments.ProfileFragment;
import com.example.comp6239.Fragments.UsersFragment;
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

import de.hdodenhof.circleimageview.CircleImageView;
public class Chat extends AppCompatActivity{


    CircleImageView profile_image;
    TextView username;
    private FirebaseUser user;
    private DatabaseReference database;
    private static final String TAG = "Chat_Activity";
    private NavigationView navigationView;
    private User current_user;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d(TAG,"On Create of chat");
        if(getIntent().getExtras()!=null){
            String id=getIntent().getExtras().get("id").toString();
            String username=getIntent().getExtras().get("username").toString();
            String name=getIntent().getExtras().get("name").toString();
            String surname=getIntent().getExtras().get("surname").toString();
            String type=getIntent().getExtras().get("type").toString();
            String approved=getIntent().getExtras().get("approved").toString();
            String imageurl=getIntent().getExtras().get("imageurl").toString();
            current_user=new User(id,username,name,surname,type,approved,imageurl);
        }
        if(current_user!=null) {
            ((TextView) findViewById(R.id.name)).setText(current_user.getName());
            ((TextView) findViewById(R.id.username)).setText(current_user.getUsername());
            ((TextView) findViewById(R.id.user_type)).setText(current_user.getType());

            profile_image = findViewById(R.id.image_profile);
            username = findViewById(R.id.username);

            user = FirebaseAuth.getInstance().getCurrentUser();
            mAuth=FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance().getReference("User").child(user.getUid());
            load();
            TabLayout tab_layout = findViewById(R.id.tab_layout);
            ViewPager viewPager = findViewById(R.id.view_pager);
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
            viewPagerAdapter.addFragment(new UsersFragment(), "Users");
            viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
            viewPager.setAdapter(viewPagerAdapter);
            tab_layout.setupWithViewPager(viewPager);
        }
    }
    public void load(){
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (current_user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher_round);

                } else {
                    Glide.with(Chat.this).load(current_user.getImageURL()).into(profile_image);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments =new ArrayList<>();
            this.titles = new ArrayList<>();
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
        @Override
        public int getCount(){
            return fragments.size();
        }
        public void addFragment(Fragment fragment,String title){
            fragments.add(fragment);
            titles.add(title);
        }
        @Nullable
        @Override
        public CharSequence getPageTitle (int position) {
                return titles.get(position);
        }
    }
}
