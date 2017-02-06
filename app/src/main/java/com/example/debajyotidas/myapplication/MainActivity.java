package com.example.debajyotidas.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.debajyotidas.myapplication.model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final String TAG="MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private final int RC_SIGN_IN=100;
    private boolean IS_FIRST_TIME=false;
    private AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/



        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        /*SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });*/


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid()+" n"+user.getDisplayName());

                    //if (!Constants.IS_USER_CREATED) {
                        //Constants.NAME = user.getDisplayName() == null ? "anonymous" : user.getDisplayName();
                        //Constants.PHOTO_URI = user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString();
                        //Constants.UID = user.getUid();

                        preferences.edit().putString(Constants.SHARED_PREFS.UID,user.getUid()).apply();
                        String reg_token=preferences.getString(Constants.SHARED_PREFS.REG_TOKEN,"no_token_yet");
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("users");
                        myRef.child(user.getUid()+"/reg_token").setValue(reg_token);

                        //Constants.IS_USER_CREATED=true;
                   // }

                }
            }
        };
        mAuth.signInAnonymously()
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }
    public void signIn() {
        Toast.makeText(this, "in method", Toast.LENGTH_SHORT).show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void showDialog(View view){
        View view1= LayoutInflater.from(this).inflate(R.layout.dialog_view,null);

        view1.findViewById(R.id.rl1).setOnClickListener(this);
        view1.findViewById(R.id.rl2).setOnClickListener(this);
        view1.findViewById(R.id.rl3).setOnClickListener(this);
        view1.findViewById(R.id.rl4).setOnClickListener(this);

        alertDialog=new AlertDialog.Builder(this)
                .setTitle(R.string.challenge)
        .setView(view1).show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                //firebaseAuthWithGoogle(account);
                if (account != null) {
                    String UID=preferences.getString(Constants.SHARED_PREFS.UID,"no_uid");
                    if (!UID.equals("no_uid")){

                        FirebaseDatabase.getInstance().getReference("users/"+UID+"/img_url").setValue(String.valueOf(account.getPhotoUrl()));
                        FirebaseDatabase.getInstance().getReference("users/"+UID+"/name").setValue(account.getDisplayName());

                    }
                    linkAccount(account.getIdToken());
                }else Toast.makeText(this, "ac is null", Toast.LENGTH_SHORT).show();
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void linkAccount(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ...
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    public void startNewGame(View view){
        startActivity(new Intent(this,AllUsersList.class));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        alertDialog.dismiss();
        switch (v.getId()){
            case R.id.rl1:
                break;
            case R.id.rl2:
                break;
            case R.id.rl3:
                break;
            case R.id.rl4:
                break;
        }
        FirebaseDatabase.getInstance().getReference("users").orderByChild("online").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount()==0) {
                    Toast.makeText(MainActivity.this, "No user Online now", Toast.LENGTH_SHORT).show();
                    return;
                }

                Iterator<DataSnapshot> iterator=dataSnapshot.getChildren().iterator();
                Random random=new Random();
                int randomNum=random.nextInt((int) dataSnapshot.getChildrenCount());
                int i=0;
                User user=null;
                while (iterator.hasNext())
                {
                    if (i==randomNum){
                        DataSnapshot datasnap=iterator.next();
                        if (!datasnap.getKey().equals(UID)) {
                            Map<String, Object> map = (Map<String, Object>) datasnap.getValue();

                            boolean isOnline=Boolean.parseBoolean(String.valueOf(map.get("online")));
                            user=new User(String.valueOf(map.get("name")),
                                    String.valueOf(map.get("img_url")),
                                    isOnline,String.valueOf(map.get("reg_token")));
                            user.setUID(datasnap.getKey());
                        }
                        break;
                    }
                    i++;
                }

                if (user!=null){
                    startActivity(new Intent(MainActivity.this,GameActivity.class)
                            .putExtra("uid",user.getUID())
                            .putExtra("reg_token",user.getReg_token()));
                }else
                    Toast.makeText(MainActivity.this, "No user online now", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}
