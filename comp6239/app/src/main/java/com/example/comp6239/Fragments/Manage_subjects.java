package com.example.comp6239.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.comp6239.R;
import com.example.comp6239.Model.Subject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Manage_subjects extends Fragment implements View.OnClickListener{

    private DatabaseReference database;
    private EditText editText_subject_title;
    private ArrayList all_subjects;
    private ArrayList all_subjects_ids;
    private Spinner spinner_manage_subjects;
    private ArrayList all_user_ids;
    private String TAG="Add Subject Fragment:";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database= FirebaseDatabase.getInstance().getReference();
        load_all_user_ids();
        if (getArguments() != null) {
            all_subjects =new ArrayList();
            all_subjects_ids =new ArrayList();
            for(String key: getArguments().keySet()) {
                all_subjects_ids.add(key);//all subjects ids
                all_subjects.add(getArguments().get(key));//all subjects
            }
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Subject Management");
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.manage_subjects,container,false);
        Button button_add_subjects=view.findViewById(R.id.button_add_subject);
        Button button_modify_subjects=view.findViewById(R.id.button_modify_subjects);
        Button button_delete_subjects=view.findViewById(R.id.button_delete_subjects);
        editText_subject_title=view.findViewById(R.id.editText_subject_title);
        spinner_manage_subjects=view.findViewById(R.id.spinner_manage_subjects);
        button_add_subjects.setOnClickListener(this);
        button_modify_subjects.setOnClickListener(this);
        button_delete_subjects.setOnClickListener(this);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, all_subjects);
        spinner_manage_subjects.setAdapter(dataAdapter);
        return view;
    }

    @Override
    public void onClick(View v) {//On Click method
        int id=v.getId();
        ArrayAdapter adapter=(ArrayAdapter)spinner_manage_subjects.getAdapter();
        adapter.notifyDataSetChanged();
        if(id==R.id.button_add_subject){//Add subject
            String title=editText_subject_title.getText().toString();
            if(!isinList(all_subjects,title)) {
                String key=database.push().getKey();
                database = FirebaseDatabase.getInstance().getReference().child("Subject").child(key);
                Subject s=new Subject(title);
                database.setValue(s);
                all_subjects.add(s);
                all_subjects_ids.add(key);
                Log.d(TAG, "A subject has been added");
                Toast.makeText(getActivity(), "The subject " + editText_subject_title.getText().toString() + " has been added to the system", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(), "The subject " + editText_subject_title.getText().toString() + " is already in the system", Toast.LENGTH_SHORT).show();
            }
            editText_subject_title.setText("");
        }
        if(id==R.id.button_modify_subjects){//Modify subject title
            String title=editText_subject_title.getText().toString();
            if(!isinList(all_subjects,title)) {
                int pos=spinner_manage_subjects.getSelectedItemPosition();
                database = FirebaseDatabase.getInstance().getReference().child("Subject").child(all_subjects_ids.get(pos).toString());
                Subject s=new Subject(title);
                database.setValue(s);
                all_subjects.add(pos,s);
                all_subjects.remove(pos+1);
                Toast.makeText(getActivity(), "The subject title has been edited to "+title, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(), "The subject " + editText_subject_title.getText().toString() + " is already in the system", Toast.LENGTH_SHORT).show();
            }
            editText_subject_title.setText("");
        }
        if(id==R.id.button_delete_subjects){
            int pos=spinner_manage_subjects.getSelectedItemPosition();
            String subject_id=all_subjects_ids.get(pos).toString();
            database = FirebaseDatabase.getInstance().getReference().child("Subject").child(subject_id);
            database.removeValue();
            remove_user_subjects(subject_id);
            Toast.makeText(getActivity(), "The subject "+ all_subjects.get(pos).toString()+"has been removed from the system", Toast.LENGTH_SHORT).show();
            all_subjects_ids.remove(pos);
            all_subjects.remove(pos);
        }
        adapter.notifyDataSetChanged();
    }
    public boolean isinList(ArrayList list,String str){
        for(Object o:list)
            if(o.toString().equals(str))
                return true;
        return false;
    }
    public void remove_user_subjects(String subject_id){
        DatabaseReference db;
        for(Object o:all_user_ids){
            db=FirebaseDatabase.getInstance().getReference().child("User Relations").child(o.toString()).child("Subjects").child(subject_id);
            db.removeValue();
        }
    }

    /********************************Loading functions*********************************************/
    public void load_all_user_ids() {
        all_user_ids=new ArrayList();
        database=FirebaseDatabase.getInstance().getReference().child("User");
        ValueEventListener all_user_ids_listener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnapshot:dataSnapshot.getChildren())
                    all_user_ids.add(childSnapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        database.addListenerForSingleValueEvent(all_user_ids_listener);
    }
}
