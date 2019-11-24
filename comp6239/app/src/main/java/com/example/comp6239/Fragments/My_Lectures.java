package com.example.comp6239.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.comp6239.Model.Lecture;
import com.example.comp6239.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class My_Lectures extends Fragment {

    private DatabaseReference database;
    private FirebaseUser user;
    private ArrayAdapter<String> listAdapter;
    private ArrayList all_lectures;
    private ArrayList all_lectures_ids;
    private ArrayList user_lectures;
    private ArrayList user_lectures_ids;
    private ArrayList<Lecture> lectures;
    private ArrayList<String> lectures_ids;
    private SwipeRefreshLayout pullToRefresh;
    private String user_type;
    private String TAG="My Lectures Fragment:";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database= FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (getArguments() != null) {
            all_lectures=new ArrayList();
            all_lectures_ids=new ArrayList();
            for(String key: getArguments().keySet()) {
                if(!key.equals("type")) {
                    all_lectures_ids.add(key);//all lectures ids
                    all_lectures.add(getArguments().get(key));//all lectures
                }else
                    user_type=getArguments().get(key).toString();
            }
        }
        load_user_lectures();
        load_all_lectures();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("My Lectures");
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_lectures_requests, container, false);
        pullToRefresh =view.findViewById(R.id.pullToRefresh);
        ListView listView_lectures=view.findViewById(R.id.listView_lectures_requests);
        listAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,user_lectures);
        listView_lectures.setAdapter(listAdapter);
        listView_lectures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!pullToRefresh.isRefreshing()) {
                    Fragment fragment=new ProfileFragment();
                    Bundle bundle=new Bundle();
                    if(user_type.equals("Tutor")) {
                        bundle.putString("student_id", student_id(user_lectures_ids.get(position).toString()));
                    }else
                        bundle.putString("tutor_id", tutor_id(user_lectures_ids.get(position).toString()));
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content_main, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }

            }
        });
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                listAdapter.notifyDataSetChanged();
                pullToRefresh.setRefreshing(false);
            }
        });
        listAdapter.notifyDataSetChanged();
        return view;
    }

    public boolean isinList(ArrayList list,String str){
        for(Object o:list)
            if(o.toString().equals(str))
                return true;
        return false;
    }

    /********************************Loading functions*********************************************/
    public void load_user_lectures() {//Loads the lectures the current user has
        user_lectures=new ArrayList();
        user_lectures_ids=new ArrayList();
        database = FirebaseDatabase.getInstance().getReference().child("User Relations").child(user.getUid()).child("Lectures");
        ValueEventListener user_subjects_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if(isinList(all_lectures_ids,childSnapshot.getKey())){
                            user_lectures_ids.add(childSnapshot.getKey());
                            user_lectures.add(childSnapshot.getValue(String.class));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        database.addListenerForSingleValueEvent(user_subjects_listener);
    }
    public void load_all_lectures(){//Loads all the lectures
        lectures=new ArrayList<Lecture>();
        lectures_ids=new ArrayList<String>();
        database=FirebaseDatabase.getInstance().getReference().child("Lecture");
        ValueEventListener lecture_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    lectures.add(childSnapshot.getValue(Lecture.class));
                    lectures_ids.add(childSnapshot.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "User load:Cancelled", databaseError.toException());
            }
        };
        database.addListenerForSingleValueEvent(lecture_listener);
    }
    public String tutor_id(String key){
        int i=0;
        for(String str:lectures_ids){
            if(str.equals(key)){
                return lectures.get(i).getTutor_id();
            }
            i++;
        }
        return "";
    }
    public String student_id(String key){
        int i=0;
        for(String str:lectures_ids){
            if(str.equals(key)){
                return lectures.get(i).getStudent_id();
            }
            i++;
        }
        return "";
    }
}
