package com.example.comp6239.Activities;

import android.content.Intent;
import androidx.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.comp6239.Model.User;
import com.example.comp6239.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Sign_in extends BaseActivity implements View.OnClickListener{

    private EditText editText_username;
    private EditText editText_password;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private static final String TAG = "Sign_in Activity:";
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private CallbackManager mCallbackManager;
    private Intent intent;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null )
            Log.d(TAG,"Activity is persistent");
        else
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        setContentView(R.layout.sign_in);

        editText_username=findViewById(R.id.editText_username);
        editText_password=findViewById(R.id.editText_password);

        mAuth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebookButton);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                updateUI(null);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.button_sign_in:
                sign_in(editText_username.getText().toString(),editText_password.getText().toString());
                break;

            case R.id.button_sign_up:
                startActivity(new Intent(this, Sign_up.class));
                break;

            case R.id.imageButton_facebook:
                findViewById(R.id.facebookButton).performClick();
                break;

            case R.id.imageButton_google:
                google_sign_in();
                break;

            default:
                break;
        }

    }
    public void updateUI(FirebaseUser user){
        if(user!=null){
            if(intent==null)
                startActivity(new Intent(this, main_activity.class));
            else
                startActivity(intent);
            finish();
        }
    }
    public void sign_in(String username,String password){
        if(validateEntry(username,password)){
            showProgressDialog();
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        user = mAuth.getCurrentUser();
                        Log.d(TAG,"User with ID"+user.getUid());
                        intent=new Intent(getApplicationContext(),main_activity.class);
                        intent.putExtra("Google",false);
                        intent.putExtra("Facebook",false);
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(Sign_in.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                    if (!task.isSuccessful()) {
                        //mStatusTextView.setText(R.string.auth_failed);
                    }
                    hideProgressDialog();
                }
            });
        }else{
            Toast.makeText(this,"Please fill both username and password fields",Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Please fill both username and password fields");
            return;
        }
    }
    public void google_sign_in(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    public boolean validateEntry(String username,String password){
        boolean valid = true;
        if (TextUtils.isEmpty(username)) {
            editText_username.setError("Required.");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            editText_password.setError("Required.");
            valid = false;
        }
        return valid;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {//Google Sign in
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }else//Facebook Sign in
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            intent=new Intent(getApplicationContext(),main_activity.class);
                            intent.putExtra("Google",true);
                            intent.putExtra("Facebook",false);
                            database=FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid());
                            ValueEventListener user_listener=new ValueEventListener(){
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue()==null) {
                                        database.setValue(new User(user.getUid(),user.getEmail(), user.getDisplayName(), user.getDisplayName(), "Student", "True","default"));
                                        Log.d(TAG,"First time access");
                                    }
                                    else
                                        Log.d(TAG,"Google User has accessed before");
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            };
                            database.addListenerForSingleValueEvent(user_listener);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        showProgressDialog();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            intent=new Intent(getApplicationContext(),main_activity.class);
                            intent.putExtra("Google",false);
                            intent.putExtra("Facebook",true);
                            database=FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid());
                            ValueEventListener user_listener=new ValueEventListener(){
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue()==null) {
                                        database.setValue(new User(user.getUid(),user.getEmail(), user.getDisplayName(), user.getDisplayName(), "Student", "True","default"));
                                        Log.d(TAG,"First time access");
                                    }
                                    else
                                        Log.d(TAG,"Facebook User has accessed before");
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            database.addListenerForSingleValueEvent(user_listener);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
