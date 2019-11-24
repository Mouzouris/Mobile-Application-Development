package com.example.comp6239.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comp6239.Fragments.Approve_tutor;
import com.example.comp6239.Fragments.Edit_profile;
import com.example.comp6239.Fragments.Manage_subjects;
import com.example.comp6239.Fragments.My_Lectures;
import com.example.comp6239.Fragments.My_Requests;
import com.example.comp6239.Fragments.Search_tutors;
import com.example.comp6239.Model.Lecture;
import com.example.comp6239.Model.Subject;
import com.example.comp6239.Model.User;
import com.example.comp6239.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class main_activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private static final String TAG = "Main_Activity";
    private NavigationView navigationView;
    private User current_user;
    private ArrayList unapproved_tutors;
    private ArrayList unapproved_tutor_ids;
    private ArrayList all_tutors;
    private ArrayList all_tutor_ids;
    private ArrayList all_subjects;
    private ArrayList all_subjects_ids;
    private ArrayList all_lectures;
    private ArrayList all_lectures_ids;
    private ArrayList all_requests;
    private ArrayList all_requests_ids;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean[] sign_in_type=new boolean[3];
    private DatabaseReference userRef;
    private TextView textView_hidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"On Create");
        mAuth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();
        userRef=FirebaseDatabase.getInstance().getReference().child("User");
        userRef.keepSynced(true);

        Log.d(TAG,"User with ID:"+user.getUid()+" is currently signed in");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            if(bundle.get("Google").equals(false) && bundle.get("Facebook").equals(false))//Email sign in
                sign_in_type[0]=true;
            else if(bundle.get("Google").equals(true))
                sign_in_type[1]=true;
            else if(bundle.get("Facebook").equals(true))
                sign_in_type[2]=true;
        }
        if(sign_in_type[0]) {
            Log.d(TAG, "Email Account");
            Toast.makeText(getApplicationContext(),"You have signed with an email account",Toast.LENGTH_SHORT).show();
        }
        if(sign_in_type[1]) {
            Log.d(TAG, "Google Account");
            Toast.makeText(getApplicationContext(),"You have signed with a Google account",Toast.LENGTH_SHORT).show();
        }
        if(sign_in_type[2]) {
            Log.d(TAG, "Facebook Account");
            Toast.makeText(getApplicationContext(),"You have signed with a facebook account",Toast.LENGTH_SHORT).show();
        }
        initialiseUser();
        load_unapproved_tutors();
        load_all_subjects();
        load_all_tutors();
        load_all_lectures_requests();
        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        textView_hidden=findViewById(R.id.textView_hidden);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //displaySelectedScreen(R.id.nav_edit_profile);//this will set the default welcoming page of the system
    }

    public void hideItems(){//Hides items depending on the user type
        if(current_user!=null) {
            String type=current_user.getType();
            ((TextView) findViewById(R.id.name)).setText(current_user.getName());
            ((TextView) findViewById(R.id.username)).setText(current_user.getUsername());
            ((TextView) findViewById(R.id.user_type)).setText(current_user.getType());
            switch (type){
                case "Administrator":
                    Log.d(TAG, "Admin");
                    navigationView.getMenu().findItem(R.id.nav_edit_profile).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_requests).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_my_lectures).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_search_tutors).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_messages).setVisible(false);
                    break;
                case "Tutor":
                    Log.d(TAG, "Tutor");
                    if(current_user.getApproved().equals("True")) {
                        navigationView.getMenu().findItem(R.id.nav_approve_tutor).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_search_tutors).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_manage_subjects).setVisible(false);
                    }
                    else{
                        navigationView.getMenu().findItem(R.id.nav_approve_tutor).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_search_tutors).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_manage_subjects).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_requests).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_my_lectures).setVisible(false);
                        navigationView.getMenu().findItem(R.id.nav_messages).setVisible(false);
                        textView_hidden.setText("Account not yet approved");
                    }
                    break;
                case "Student":
                    Log.d(TAG, "Student");
                    navigationView.getMenu().findItem(R.id.nav_manage_subjects).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_approve_tutor).setVisible(false);
                    break;
                default:
                    Log.d(TAG, "Problem");
            }
        }else{
            Log.d(TAG,"shit");
        }
    }

    public void signOut(){//Signs out of the system
        mAuth.signOut();
        if(sign_in_type[1]==true) {//Google sign out
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
            Log.d(TAG, "Google Account Signed Out");
        }
        if(sign_in_type[2]==true) {//Facebook sign out
            LoginManager.getInstance().logOut();
            Log.d(TAG, "Facebook Account Signed Out");
        }
        Intent intent=new Intent(getApplicationContext(), Sign_in.class);
        intent.putExtra("Persistance",true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    public void onStart() {
        super.onStart();
        Log.d(TAG,"On Start");
    }
    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(TAG,"On Restart");
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"On Pause");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"On Destroy");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"On Stop");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"On Resume");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.secundary_menu, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        hideItems();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displaySelectedScreen(int id){//Initialises and calls the fragments
        Fragment fragment=null;
        Bundle bundle;
        switch(id){
            case R.id.nav_edit_profile:
                fragment=new Edit_profile();
                bundle=new Bundle();
                fill_bundle(bundle);
                fill_bundle(bundle, all_subjects, all_subjects_ids);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_my_lectures:
                fragment=new My_Lectures();
                bundle=new Bundle();
                fill_bundle(bundle, all_lectures, all_lectures_ids);
                bundle.putString("type",current_user.getType());
                fragment.setArguments(bundle);
                break;
            case R.id.nav_search_tutors:
                fragment=new Search_tutors();
                bundle=new Bundle();
                fill_bundle(bundle, all_subjects, all_subjects_ids);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_requests:
                fragment=new My_Requests();
                bundle=new Bundle();
                fill_bundle(bundle);
                fill_bundle(bundle, all_requests, all_requests_ids);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_manage_subjects:
                fragment=new Manage_subjects();
                bundle=new Bundle();
                fill_bundle(bundle, all_subjects, all_subjects_ids);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_approve_tutor:
                fragment=new Approve_tutor();
                bundle=new Bundle();
                fragment.setArguments(fill_bundle(bundle, unapproved_tutors, unapproved_tutor_ids));
                break;
            case R.id.nav_messages:
                Intent intent=new Intent(this,Chat.class);
                intent.putExtra("id",current_user.getName());
                intent.putExtra("name",current_user.getName());
                intent.putExtra("surname",current_user.getSurname());
                intent.putExtra("username",current_user.getUsername());
                intent.putExtra("type",current_user.getType());
                intent.putExtra("approved",current_user.getApproved());
                intent.putExtra("imageurl",current_user.getImageURL());
                startActivity(intent);
                break;
            default:
                break;
        }
        if(fragment!=null){
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main,fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(id);
        return true;
    }
    public Bundle fill_bundle(Bundle bundle,ArrayList items,ArrayList item_ids){//Fill the ids and values of items into the bundle
        int i=0;
        if(items.size()>0) {
            for (Object o : items) {
                bundle.putString(item_ids.get(i).toString(), o.toString());
                i++;
            }
        }
        return bundle;
    }
    public Bundle fill_bundle(Bundle bundle){//Fill user information into the bundle
        int i=0;
        bundle.putString("id",current_user.getName());
        bundle.putString("name",current_user.getName());
        bundle.putString("surname",current_user.getSurname());
        bundle.putString("username",current_user.getUsername());
        bundle.putString("type",current_user.getType());
        bundle.putString("approved",current_user.getApproved());
        bundle.putString("imageurl",current_user.getImageURL());
        return bundle;
    }

    /*************************************Loading methods******************************************/

    public void initialiseUser(){//Loads the current user
        final String userId=user.getUid();
        database=FirebaseDatabase.getInstance().getReference().child("User");
        ValueEventListener user_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if(childSnapshot.getKey().equals(userId)){
                        current_user=childSnapshot.getValue(User.class);
                        Log.d(TAG,current_user.toString());
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
    public void load_unapproved_tutors(){//Loads unapproved unapproved_tutors only
        unapproved_tutors =new ArrayList();
        unapproved_tutor_ids =new ArrayList();
        database=FirebaseDatabase.getInstance().getReference().child("User");
        User iterating_user;
        ValueEventListener user_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if(childSnapshot.getValue(User.class).getType().equals("Tutor") && childSnapshot.getValue(User.class).getApproved().equals("False")){
                        unapproved_tutors.add(childSnapshot.getValue(User.class));
                        unapproved_tutor_ids.add(childSnapshot.getKey());
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
    public void load_all_tutors(){//Loads all the tutors
        all_tutors =new ArrayList();
        all_tutor_ids =new ArrayList();
        database=FirebaseDatabase.getInstance().getReference().child("User");
        ValueEventListener user_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if(childSnapshot.getValue(User.class).getType().equals("Tutor")){
                        all_tutors.add(childSnapshot.getValue(User.class));
                        all_tutor_ids.add(childSnapshot.getKey());
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
    public void load_all_subjects(){//Loads all all_subjects
        all_subjects =new ArrayList();
        all_subjects_ids =new ArrayList();
        database=FirebaseDatabase.getInstance().getReference().child("Subject");
        ValueEventListener subject_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    all_subjects.add(childSnapshot.getValue(Subject.class));
                    all_subjects_ids.add(childSnapshot.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "User load:Cancelled", databaseError.toException());
            }
        };
        database.addListenerForSingleValueEvent(subject_listener);
    }
    public void load_all_lectures_requests(){//Loads all the lectures and the requests(unapproved lectures)
        all_lectures=new ArrayList();
        all_lectures_ids=new ArrayList();
        all_requests=new ArrayList();
        all_requests_ids=new ArrayList();
        final Lecture[] lecture = new Lecture[1];
        database=FirebaseDatabase.getInstance().getReference().child("Lecture");
        ValueEventListener lecture_listener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    lecture[0] =childSnapshot.getValue(Lecture.class);
                    if(lecture[0].getApproved().equals("True")) {
                        all_lectures.add(childSnapshot.getValue(Lecture.class));
                        all_lectures_ids.add(childSnapshot.getKey());
                    }
                    else{
                        all_requests.add(childSnapshot.getValue(Lecture.class));
                        all_requests_ids.add(childSnapshot.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "User load:Cancelled", databaseError.toException());
            }
        };
        database.addListenerForSingleValueEvent(lecture_listener);
    }
}
