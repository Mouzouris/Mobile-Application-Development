package com.example.comp6239.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.comp6239.R;
import com.example.comp6239.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class Edit_profile extends Fragment implements View.OnClickListener{

    private DatabaseReference database;
    private FirebaseUser user;
    private EditText editText_update_name;
    private EditText editText_update_surname;
    private User current_user;
    private Spinner spinner_subjects;
    private ArrayList all_subjects;
    private ArrayList all_subjects_ids;
    private ArrayList user_subjects_ids;
    private ArrayList updated_subjects;
    private ArrayAdapter<String> listAdapter;

    private CircleImageView image_profile;
    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    private String TAG="Edit Profile Fragment:";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database= FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        load_user_subjects();
        if (getArguments() != null) {
            all_subjects =new ArrayList();
            all_subjects_ids =new ArrayList();
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
                    all_subjects_ids.add(key);//all subjects ids
                    all_subjects.add(getArguments().get(key));//all subjects
                }
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Profile");
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.edit_profile,container,false);
        image_profile = view.findViewById(R.id.profile_image);
        editText_update_name=view.findViewById(R.id.editText_update_name);
        editText_update_surname=view.findViewById(R.id.editText_update_surname);
        Button button_update=view.findViewById(R.id.button_update);
        Button button_add_subjects_user=view.findViewById(R.id.button_add_subjects_user);
        Button button_delete_subjects_user=view.findViewById(R.id.button_delete_subjects_user);
        ListView list_subjects=view.findViewById(R.id.listView_subjects_user);
        spinner_subjects=view.findViewById(R.id.spinner_subjects);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, all_subjects);
        listAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,updated_subjects);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_subjects.setAdapter(dataAdapter);
        list_subjects.setAdapter(listAdapter);
        editText_update_surname.setText(current_user.getSurname());
        editText_update_name.setText(current_user.getName());
        button_update.setOnClickListener(this);
        button_add_subjects_user.setOnClickListener(this);
        button_delete_subjects_user.setOnClickListener(this);
        listAdapter.notifyDataSetChanged();
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        if (current_user.getImageURL().equals("default")){
            Log.d(TAG,"image is default");
            image_profile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(getContext()).load(current_user.getImageURL()).into(image_profile);
        }
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"opens function for image");
                openImage();

            }
        });
        return view;
    }
    @Override
    public void onClick(View v) {//On Click method
        int id=v.getId();
        update_list(updated_subjects);
        listAdapter.notifyDataSetChanged();
        if(id==R.id.button_update){//Update
            database= FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid());
            current_user.setName(editText_update_name.getText().toString());
            current_user.setSurname(editText_update_surname.getText().toString());
            database.setValue(current_user);
            Toast.makeText(getActivity(), "User information updated",Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.button_add_subjects_user){//Add subjects
            int pos=spinner_subjects.getSelectedItemPosition();
            String subject_id= all_subjects_ids.get(pos).toString();
            if(!isinList(user_subjects_ids,subject_id)) {
                database = FirebaseDatabase.getInstance().getReference().child("User Relations").child(user.getUid()).child("Subjects").child(subject_id);
                database.setValue(all_subjects.get(pos).toString());
                database = FirebaseDatabase.getInstance().getReference().child("Subject Relations").child(subject_id).child(current_user.getType()).child(user.getUid());
                database.setValue(current_user.toString());
                user_subjects_ids.add(subject_id);
                //user_subjects.add(all_subjects.get(pos).toString());
                Toast.makeText(getActivity(), "User: "+current_user.getUsername()+" has added the subject:"+ all_subjects.get(pos),Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(getActivity(), "You already have that subject added",Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.button_delete_subjects_user){//Delete Subjects
            int pos=spinner_subjects.getSelectedItemPosition();
            String subject_id= all_subjects_ids.get(pos).toString();
            database= FirebaseDatabase.getInstance().getReference().child("User Relations").child(user.getUid()).child("Subjects").child(subject_id);
            if(isinList(user_subjects_ids,subject_id)) {
                database.removeValue();
                database = FirebaseDatabase.getInstance().getReference().child("Subject Relations").child(subject_id).child(current_user.getType()).child(user.getUid());
                database.removeValue();
                remove_values(subject_id);
                Toast.makeText(getActivity(), "User: "+current_user.getUsername()+" has removed the subject:"+ all_subjects.get(pos),Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getActivity(), "You cannot remove a subject you have not registered",Toast.LENGTH_SHORT).show();
        }
        update_list(updated_subjects);
        listAdapter.notifyDataSetChanged();
        Log.d(TAG,"User: "+current_user.toString()+"has edited his profile");
    }

    public void remove_values(String subject_id){
        int i=0;
        for(Object o:user_subjects_ids)
            if(o.toString().equals(subject_id))
                i=user_subjects_ids.indexOf(o);
        user_subjects_ids.remove(i);
    }
    public boolean isinList(ArrayList list,String str){
        for(Object o:list)
            if(o.toString().equals(str))
                return true;
        return false;
    }
    public void update_list(ArrayList list){
        list.clear();
        int i=0;
        for(Object o:all_subjects_ids) {
            for (Object ob : user_subjects_ids){
                if (o.toString().equals(ob.toString()))
                    list.add(all_subjects.get(i));
            }
            i++;
        }
    }

    /********************************Image uploading*********************************************/

    private void openImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task <UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        database = FirebaseDatabase.getInstance().getReference("User").child(user.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        database.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(),"No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void  onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Upload in Progress", Toast.LENGTH_SHORT). show();
            }else {
                uploadImage();
            }
        }
    }







    /********************************Loading functions*********************************************/

    public void load_user_subjects() {//Loads the keys of the subjects the user has previously selected
        updated_subjects=new ArrayList();
        user_subjects_ids=new ArrayList();
        database = FirebaseDatabase.getInstance().getReference().child("User Relations").child(user.getUid()).child("Subjects");
        ValueEventListener user_subjects_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    user_subjects_ids.add(childSnapshot.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        database.addListenerForSingleValueEvent(user_subjects_listener);
    }
}
