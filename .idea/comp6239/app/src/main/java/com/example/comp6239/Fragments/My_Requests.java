package com.example.comp6239.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comp6239.R;
import com.example.comp6239.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class My_Requests extends Fragment {

    private DatabaseReference database;
    private FirebaseUser user;
    private ArrayList all_requests;
    private ArrayList all_requests_ids;
    private ArrayList user_requests;
    private ArrayList user_requests_ids;
    private ArrayAdapter<String> listAdapter;
    private SwipeRefreshLayout pullToRefresh;
    private User current_user;
    private String TAG="My_Requests Fragment:";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database= FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        load_user_requests();
        if(getArguments()!=null){
            all_requests=new ArrayList();
            all_requests_ids=new ArrayList();
            String id=getArguments().get("id").toString();
            String username=getArguments().get("username").toString();
            String name=getArguments().get("name").toString();
            String surname=getArguments().get("surname").toString();
            String type=getArguments().get("type").toString();
            String approved=getArguments().get("approved").toString();
            String imageurl=getArguments().get("imageurl").toString();
            current_user=new User(id,username,name,surname,type,approved,imageurl);
            for(String key: getArguments().keySet()) {
                if(!key.equals("id") && !key.equals("username") && !key.equals("name") && !key.equals("surname") && !key.equals("type") && !key.equals("approved") && !key.equals("imageurl")) {
                    all_requests_ids.add(key);//all requests ids
                    all_requests.add(getArguments().get(key));//all requests
                }
            }
        }

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("My Requests");
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_lectures_requests, container, false);
        ((TextView)view.findViewById(R.id.textView_title)).setText("My Requests");
        ((TextView)view.findViewById(R.id.textView_info)).setText("These are your pending requests for lectures,click on them to accept them. Also, swipe to refresh the page");
        pullToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.pullToRefresh);
        final ListView listView_requests=view.findViewById(R.id.listView_lectures_requests);
        listAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,user_requests);
        listView_requests.setAdapter(listAdapter);
        listView_requests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!pullToRefresh.isRefreshing()) {
                    String lecture_id = user_requests_ids.get(position).toString();
                    if (current_user.getType().equals("Tutor")) {
                        database = FirebaseDatabase.getInstance().getReference().child("Lecture").child(lecture_id).child("approved");
                        database.setValue("True");
                        user_requests_ids.remove(position);
                        user_requests.remove(position);
                        listAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "You have approved the selected lecture request", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getActivity(), "You have to wait for the tutor to approve your request", Toast.LENGTH_SHORT).show();
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
        return view;
    }
    public boolean isinList(ArrayList list,String str){
        for(Object o:list)
            if(o.toString().equals(str))
                return true;
        return false;
    }

    /********************************Loading functions*********************************************/
    public void load_user_requests() {//Loads the requests the current user has
        user_requests=new ArrayList();
        user_requests_ids=new ArrayList();
        database = FirebaseDatabase.getInstance().getReference().child("User Relations").child(user.getUid()).child("Lectures");
        ValueEventListener user_subjects_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(isinList(all_requests_ids,childSnapshot.getKey())){
                        user_requests_ids.add(childSnapshot.getKey());
                        user_requests.add(childSnapshot.getValue(String.class));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        database.addListenerForSingleValueEvent(user_subjects_listener);
    }
}
