package com.example.comp6239.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.comp6239.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Search_tutors extends Fragment implements View.OnClickListener {

    private DatabaseReference database;
    private FirebaseUser user;
    private ArrayList all_subjects;//all the all_subjects
    private ArrayList all_subjects_ids;
    private ArrayAdapter<String> listAdapter;
    private Spinner spinner_subjects;
    private ArrayList tutor_subjects;
    private ArrayList tutor_subjects_ids;
    private ListView list_tutors;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database= FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        tutor_subjects=new ArrayList();
        tutor_subjects_ids=new ArrayList();
        if (getArguments() != null) {
            all_subjects =new ArrayList();
            all_subjects_ids =new ArrayList();
            for(String key: getArguments().keySet()) {
                all_subjects_ids.add(key);
                all_subjects.add(getArguments().get(key));
            }
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Tutor Search");
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_tutors, container, false);
        list_tutors=view.findViewById(R.id.listView_searched_tutors);
        list_tutors.setVisibility(View.INVISIBLE);
        spinner_subjects=view.findViewById(R.id.spinner_search_subjects);
        Button button_search=view.findViewById(R.id.button_search);
        button_search.setOnClickListener(this);
        listAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, all_subjects);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, all_subjects);
        list_tutors.setAdapter(listAdapter);
        spinner_subjects.setAdapter(dataAdapter);
        list_tutors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tutor_id=tutor_subjects_ids.get(position).toString();
                String subject_id=all_subjects_ids.get(spinner_subjects.getSelectedItemPosition()).toString();
                Fragment fragment=new ProfileFragment();
                Bundle bundle=new Bundle();
                bundle.putString("student_id",user.getUid());
                bundle.putString("tutor_id",tutor_id);
                bundle.putString("subject_id",subject_id);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        return view;
    }
    @Override
    public void onClick(View v) {//On Click method
        if(tutor_subjects.size()>0){
            list_tutors.setVisibility(View.VISIBLE);
            listAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, tutor_subjects);
            list_tutors.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }
        else{
            list_tutors.setVisibility(View.INVISIBLE);
        }
        int id = v.getId();
        if(id==R.id.button_search){
            int pos=spinner_subjects.getSelectedItemPosition();
            String subject_id=all_subjects_ids.get(pos).toString();
            load_tutor_subjects(subject_id);
        }
        listAdapter.notifyDataSetChanged();
    }
    public boolean isinList(ArrayList list,String str){
        for(Object o:list)
            if(o.toString().equals(str))
                return true;
        return false;
    }

    /********************************Image uploading*********************************************/

    public void load_tutor_subjects(String subject_id) {//Loads the tutors that teach a selected subject
        tutor_subjects=new ArrayList();
        tutor_subjects_ids=new ArrayList();
        database = FirebaseDatabase.getInstance().getReference().child("Subject Relations").child(subject_id).child("Tutor");
        ValueEventListener user_subjects_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    tutor_subjects.add(childSnapshot.getValue(String.class));
                    tutor_subjects_ids.add(childSnapshot.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        database.addListenerForSingleValueEvent(user_subjects_listener);
    }
}
