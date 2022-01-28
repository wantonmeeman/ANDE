package com.example.ca1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.IntentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private final static int RC_SIGN_IN = 123;
    //This handles dismissing of a notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_act);
        this.getSupportActionBar().hide();

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);

        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation");

        //Handles Google Signin
        if(GoogleSignIn.getLastSignedInAccount(this) != null || !(pref.getString("firebaseUserId","1").equals("1"))){//If User is already logged in via google.
            Intent intent = new Intent(this,HomeActivity.class);;
            startActivity(intent);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ((SignInButton)findViewById(R.id.googleSignIn)).setSize(SignInButton.SIZE_WIDE);
        
        findViewById(R.id.googleSignIn).setOnClickListener( v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        //Handles Registration
        Button regButton = findViewById(R.id.Register);
        regButton.setOnClickListener(v -> {
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);
        });

        //Handles Login via Schedular Account
        Button logButton = findViewById(R.id.Login);
        logButton.setOnClickListener(v -> {
                myDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //gets all accounts with user information
                            User user = snapshot.getValue(User.class);
                            if(user.getUsername() != null) {
                                if (//User can use Username/Email in the first text input
                                  (user.getUsername().equals(username.getText().toString()) || user.getEmail().equals(username.getText().toString()))
                                  && user.getPassword().equals(password.getText().toString())
                                ) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("firebaseUserId",snapshot.getKey());
                                    editor.commit();
                                    Intent intent = new Intent(getApplication(), HomeActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }

                        if(getSharedPreferences("MyPref", Context.MODE_PRIVATE).getString("firebaseUserId","1") == "1") {//Handles No acc found
                            Toast.makeText(getApplication(), "Invalid Login Credentials", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        // Failed to read value
                    }
                });
            });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        //if (requestCode == RC_SIGN_IN ) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        //}
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        Toast.makeText(this,"Successful",Toast.LENGTH_LONG);
        if(account != null) {
            Intent intent = new Intent(getApplication(), HomeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
