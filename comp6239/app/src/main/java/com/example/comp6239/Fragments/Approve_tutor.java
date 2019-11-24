package com.example.comp6239.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.comp6239.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Approve_tutor extends Fragment implements View.OnClickListener{

    private ArrayList unapproved_tutors;
    private ArrayList unapproved_tutors_ids;
    private Spinner spinner_unapproved_tutors;
    private DatabaseReference database;
    private ArrayAdapter<String> dataAdapter;
    private String TAG="Approve Tutor Fragment:";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database= FirebaseDatabase.getInstance().getReference().child("User");
        if (getArguments() != null) {
            unapproved_tutors =new ArrayList();
            unapproved_tutors_ids =new ArrayList();
            for(String key: getArguments().keySet()) {
                unapproved_tutors_ids.add(key);
                unapproved_tutors.add(getArguments().get(key));
            }
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Tutor Approval");
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.approve_tutor,container,false);
        Button button_approve_tutor=view.findViewById(R.id.button_approve_tutor);
        spinner_unapproved_tutors=view.findViewById(R.id.spinner_unapproved_tutors);
        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, unapproved_tutors);
        spinner_unapproved_tutors.setAdapter(dataAdapter);
        button_approve_tutor.setOnClickListener(this);
        return view;
    }
    @Override
    public void onClick(View v) {//OnClick method
        int id=v.getId();
        dataAdapter.notifyDataSetChanged();
        if(id==R.id.button_approve_tutor){
            int pos=spinner_unapproved_tutors.getSelectedItemPosition();
            String tutor_id= unapproved_tutors_ids.get(pos).toString();
            String tutor= unapproved_tutors.get(pos).toString();
            database=FirebaseDatabase.getInstance().getReference().child("User").child(tutor_id).child("approved");
            database.setValue("True");
            Toast.makeText(getActivity(), "The user: "+tutor+" has been approved by you",Toast.LENGTH_SHORT).show();
            remove_values(tutor_id);
        }
        dataAdapter.notifyDataSetChanged();
    }
    public void remove_values(String tutor_id){//Removes the tutor from the unapproved tutors arraylist
        int i=0;
        for(Object o: unapproved_tutors_ids)
            if(o.toString().equals(tutor_id))
                i= unapproved_tutors_ids.indexOf(o);
        unapproved_tutors.remove(i);
        unapproved_tutors_ids.remove(i);
    }
}
