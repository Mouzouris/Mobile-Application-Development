package com.example.comp6239.Fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.comp6239.Model.Lecture;
import com.example.comp6239.Model.User;
import com.example.comp6239.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    private CircleImageView image_profile;
    private TextView username;
    private TextView textView_date;
    private TextView textView_time;
    private DatabaseReference database;
    private String student_id,subject_id,tutor_id;
    private User tutor;
    private int flag=0;
    private String TAG="Profile Fragment:";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null) {
            if(getArguments().get("student_id")!=null&&getArguments().get("subject_id")!=null&&getArguments().get("tutor_id")!=null) {
                student_id = getArguments().get("student_id").toString();
                subject_id = getArguments().get("subject_id").toString();
                tutor_id = getArguments().get("tutor_id").toString();
            }
            else if(getArguments().get("tutor_id")!=null){
                tutor_id = getArguments().get("tutor_id").toString();
                flag=1;
            }else if(getArguments().get("student_id")!=null){
                tutor_id=getArguments().get("student_id").toString();
                flag=1;
            }
            load_tutor();
        }
        else{
            flag=1;
            load_user();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container, false);
        image_profile = view.findViewById(R.id.profile_image);
        username = view. findViewById(R.id.username);
        textView_date=view.findViewById(R.id.textView_date);
        textView_time=view.findViewById(R.id.textView_time);
        Button button_date = view.findViewById(R.id.button_date);
        Button button_time = view.findViewById(R.id.button_time);
        Button button_submit = view.findViewById(R.id.button_submit);
        if(flag==0) {
            button_date.setOnClickListener(this);
            button_submit.setOnClickListener(this);
            button_time.setOnClickListener(this);
        }else{
            button_date.setVisibility(View.INVISIBLE);
            button_submit.setVisibility(View.INVISIBLE);
            button_time.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.button_date){
            DialogFragment fragment=new DatePicker();
            fragment.show(getFragmentManager(),"DatePicker");
        }
        else if(id==R.id.button_time){
            DialogFragment fragment=new TimePicker();
            fragment.show(getFragmentManager(),"TimePicker");

        }
        else if(id==R.id.button_submit){
            String date=textView_date.getText().toString();
            String time=textView_time.getText().toString();
            Lecture lecture=new Lecture(student_id,tutor_id,subject_id,"False",date,time);
            Toast.makeText(getActivity(),"The date chosen is"+date,Toast.LENGTH_SHORT);
            String lecture_id=database.push().getKey();
            database=FirebaseDatabase.getInstance().getReference().child("Lecture").child(lecture_id);
            database.setValue(lecture);
            database=FirebaseDatabase.getInstance().getReference().child("User Relations").child(lecture.getStudent_id()).child("Lectures").child(lecture_id);
            database.setValue(lecture.toString());
            database=FirebaseDatabase.getInstance().getReference().child("User Relations").child(lecture.getTutor_id()).child("Lectures").child(lecture_id);
            database.setValue(lecture.toString());
            Log.d(TAG,"The lecture"+lecture.toString()+"has been saved");

        }
    }
    public void load_tutor(){
        database=FirebaseDatabase.getInstance().getReference().child("User");
        ValueEventListener user_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if(childSnapshot.getKey().equals(tutor_id)){
                        tutor=childSnapshot.getValue(User.class);
                        username.setText(tutor.getUsername());
                        image_profile.setImageResource(R.mipmap.ic_launcher);
                        if (tutor.getImageURL().equals("default")){
                            image_profile.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Glide.with(getContext()).load(tutor.getImageURL()).into(image_profile);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "User load:Cancelled", databaseError.toException());
            }
        };
        database.addListenerForSingleValueEvent(user_listener);
    }
    public void load_user(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        final User[] current_user = new User[1];
        database=FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid());
        ValueEventListener user_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                current_user[0] =dataSnapshot.getValue(User.class);
                username.setText(current_user[0].getUsername());
                image_profile.setImageResource(R.mipmap.ic_launcher);
                if (current_user[0].getImageURL().equals("default")){
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getContext()).load(current_user[0].getImageURL()).into(image_profile);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "User load:Cancelled", databaseError.toException());
            }
        };
        database.addListenerForSingleValueEvent(user_listener);
    }
}
