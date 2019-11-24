/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.comp6239.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.comp6239.Model.User;
import com.example.comp6239.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sign_up extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "Sign_up Activity";
    private EditText editText_username;
    private EditText editText_password;
    private EditText editText_name;
    private EditText editText_surname;
    private RadioGroup radioGroup;
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Views
        editText_username = findViewById(R.id.editText_username);
        editText_password = findViewById(R.id.editText_password);
        editText_name=findViewById(R.id.editText_name);
        editText_surname=findViewById(R.id.editText_surname);

        // Buttons
        findViewById(R.id.button_create_account).setOnClickListener(this);
        findViewById(R.id.button_back).setOnClickListener(this);

        radioGroup=findViewById(R.id.radioGroup);
        //Firebase connection and database
        mAuth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance().getReference();
    }
    private void createAccount(String username, String password, String name, String surname, final RadioGroup radioGroup) {
        if (!validateEntry(username,password,name,surname,radioGroup)) {
            return;
        }
        Log.d(TAG, "createAccount:" + username);
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            User current_user;
                            String type;
                            if(radioGroup.getCheckedRadioButtonId()==R.id.radioButton_student) {
                                type = "Student";
                                current_user = new User(user.getUid(),editText_username.getText().toString(), editText_name.getText().toString(), editText_surname.getText().toString(), type,"True","default");
                            }
                            else {
                                type = "Tutor";
                                current_user = new User(user.getUid(),editText_username.getText().toString(), editText_name.getText().toString(), editText_surname.getText().toString(), type,"False","default");
                            }
                            database.child("User").child(user.getUid()).setValue(current_user);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Sign_up.this, "Username is already in use",
                                    Toast.LENGTH_SHORT).show();
                            clear_editTexts();
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }
    public boolean validateEntry(String username,String password,String name,String surname,RadioGroup radioGroup){
        boolean valid = true;
        if (TextUtils.isEmpty(username)) {
            editText_username.setError("Required.");
            valid = false;
        } else {
            editText_username.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            editText_password.setError("Required.");
            valid = false;
        } else {
            editText_password.setError(null);
        }
        if (TextUtils.isEmpty(name)) {
            editText_name.setError("Required.");
            valid = false;
        } else {
            editText_name.setError(null);
        }
        if (TextUtils.isEmpty(surname)) {
            editText_surname.setError("Required.");
            valid = false;
        } else {
            editText_surname.setError(null);
        }
        if(radioGroup.getCheckedRadioButtonId()==-1)
            valid=false;
        if(password.length()<6) {
            Toast.makeText(getApplicationContext(), "There must be at least 6 characters on the password", Toast.LENGTH_SHORT).show();
            clear_editTexts();
            return false;
        }
        if(!valid) {
            Toast.makeText(getApplicationContext(), "Please fill the information correctly", Toast.LENGTH_SHORT).show();
            clear_editTexts();
        }
        return valid;
    }
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            startActivity(new Intent(this, main_activity.class));
            finish();
        } else {

        }
    }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_create_account) {
            createAccount(editText_username.getText().toString(), editText_password.getText().toString(),editText_name.getText().toString(),editText_surname.getText().toString(),radioGroup);
        } else if (i == R.id.button_back) {
            Intent intent=new Intent(getApplicationContext(), Sign_in.class);
            intent.putExtra("Persistance",true);
            startActivity(intent);
            finish();
        }

    }
    public void clear_editTexts(){
        editText_name.setText("");
        editText_name.setText("");
        editText_username.setText("");
        editText_password.setText("");
    }
}
/*

private void sendEmailVerification() {
    // Disable button
    findViewById(R.id.verifyEmailButton).setEnabled(false);

    // Send verification email
    // [START send_email_verification]
    final FirebaseUser user = mAuth.getCurrentUser();
    user.sendEmailVerification()
            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // [START_EXCLUDE]
                    // Re-enable button
                    findViewById(R.id.verifyEmailButton).setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(Sign_up.this,
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(Sign_up.this,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                    // [END_EXCLUDE]
                }
            });
    // [END send_email_verification]
}*/

