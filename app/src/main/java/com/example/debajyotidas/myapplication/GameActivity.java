package com.example.debajyotidas.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameActivity extends BaseActivity {

    CountDownTimer timer;
    String myUID,otherUID,regToken;
    ProgressDialog progressDialog;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Waiting for user to be live...");
        progressDialog.show();

        timer=new CountDownTimer(30000,1000){
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
                new AlertDialog.Builder(GameActivity.this)
                        .setTitle("Oops..")
                        .setMessage("It seems 2'nd player is offline now. Relax, we will inform you when available")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                supportFinishAfterTransition();
                            }
                        })
                        .show();
            }
        };
        timer.start();

        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    boolean value = (boolean) dataSnapshot.getValue();
                    //if (value!=null)
                    if (value) {
                        progressDialog.dismiss();
                        timer.cancel();
                        Toast.makeText(GameActivity.this, "Other player is online now", Toast.LENGTH_SHORT).show();
                    }else  Toast.makeText(GameActivity.this, "Other player is offline now", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myUID=preferences.getString(Constants.SHARED_PREFS.UID,"no_uid");
        if (myUID.equals("no_uid")){
            return;
        }
        otherUID=getIntent().getStringExtra("uid");
        if (otherUID==null){
            return;
        }
        regToken=getIntent().getStringExtra("reg_token");
        if (regToken==null){
            return;
        }

        FirebaseDatabase.getInstance().getReference("game/"+myUID+"/isLive").setValue(true);
        FirebaseDatabase.getInstance().getReference("game/"+otherUID+"/isLive")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    boolean value = (boolean) dataSnapshot.getValue();
                    //if (value!=null)
                    if (value) {
                    }else  sendNotification();
                }else sendNotification();
                FirebaseDatabase.getInstance().getReference("game/"+otherUID+"/isLive").addValueEventListener(valueEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json=new JSONObject();
                    JSONObject dataJson=new JSONObject();
                    dataJson.put("message","Hi this is sent from device to device");
                    dataJson.put("sender",UID);
                    String reg_token=preferences.getString(Constants.SHARED_PREFS.REG_TOKEN,"no_token");
                    if (reg_token.equals("no_token")){
                        this.cancel(true);
                        return null;
                    }
                    dataJson.put("reg_token",reg_token);
                    json.put("data",dataJson);
                    json.put("to",regToken);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization","key="+Constants.LEGACY_SERVER_KEY)
                            .url(Constants.FIREBASE_PUSH_URL)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                }catch (Exception e){
                    //Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference("game/"+myUID+"/isLive").setValue(false);
    }
}
