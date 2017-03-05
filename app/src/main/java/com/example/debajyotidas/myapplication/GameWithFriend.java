package com.example.debajyotidas.myapplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by overtatech-4 on 5/3/17.
 */

public class GameWithFriend extends GameActivity {

    private static final String TAG = "GameWithFriend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UID.equals("no_uid")) {
            Log.d(TAG, "onCreate: no_uid");
            return;
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Waiting for user to be live...");
            progressDialog.show();

        startCountDown();

        otherUID = getIntent().getStringExtra("uid");
        if (otherUID == null) {
            Log.d(TAG, "onCreate: no other_uid");
            return;
        }
        regToken = getIntent().getStringExtra("reg_token");
        if (regToken == null) {
            Log.d(TAG, "onCreate: no reg_token");
            return;
        }
        betRequested= getIntent().getLongExtra("bet",-1);
        if (betRequested==-1){
            Log.d(TAG, "onCreate: no bet requested");
            return;
        }
        FirebaseDatabase.getInstance().getReference("game/"+UID+"/isLive").setValue(true);
        FirebaseDatabase.getInstance().getReference("game/"+otherUID+"/isLive")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: "+dataSnapshot.getValue());
                        if (dataSnapshot.getValue()!=null) {
                            boolean value = (boolean) dataSnapshot.getValue();
                            //if (value!=null)
                            if (value) {
                            }else {
                                String reg_token=preferences.getString(Constants.SHARED_PREFS.REG_TOKEN,"no_token");
                                if (reg_token.equals("no_token")){
                                    return ;
                                }
                                sendNotification(reg_token);
                            }
                        }else {
                            String reg_token=preferences.getString(Constants.SHARED_PREFS.REG_TOKEN,"no_token");
                            if (reg_token.equals("no_token")){
                                return ;
                            }
                            sendNotification(reg_token);
                        }
                        FirebaseDatabase.getInstance().getReference("game/"+otherUID+"/isLive").addValueEventListener(valueEventListener);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        addListeners();

    }
    private void sendNotification(final String reg_token) {
        Log.d(TAG, "sendNotification: "+reg_token);
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json=new JSONObject();
                    JSONObject dataJson=new JSONObject();
                    dataJson.put("message","Hi this is sent from device to device");
                    dataJson.put("sender",UID);
                    dataJson.put("bet",calculatePoint(pointsOther));
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
    protected void onGridItemClick(int positionClicked) {
        if (moves.contains(positionClicked)||otherPlayerMoves.contains(positionClicked)){
            Toast.makeText(this, "Not allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        /*if (counter%2==0)
            ((ImageButton)v).setImageResource(R.drawable.cross_black);
        else ((ImageButton)v).setImageResource(R.drawable.cross_black);*/
        if (positionClicked!=-1) {
            setPlayerMove(positionClicked, R.drawable.zero_black);
            //if (!isWithComputer)
                FirebaseDatabase.getInstance().getReference("game/" + UID + "/current_game/" + counter + "_you").setValue(positionClicked);
            moves.add(positionClicked);
            movesHistory.put(counter+"_you",positionClicked);
            counter++;
            canExit=false;
            enableEverything(false);
            findViewById(R.id.wait_text).setVisibility(View.VISIBLE);
            if (moves.size()>=3) {
                Set<Integer> winOrNot=checkIfWin(moves);
                if (winOrNot!=null) {
                    canExit=true;
                    long updatedPoint;
                    /*if (isWithComputer){
                        updatedPoint=points + Constants.BET.COMPUTER;
                        FirebaseDatabase.getInstance().getReference("users/" + UID + "/" + Constants.KEYS.USERS.POINTS)
                                .setValue(updatedPoint);
                    }else {*/
                        FirebaseDatabase.getInstance().getReference("users/" + UID + "/" + Constants.KEYS.USERS.WIN_COUNT)
                                .setValue(wingCount + 1);
                        int pointsGained = calculatePoint(pointsOther);
                        updatedPoint=points + pointsGained;
                        FirebaseDatabase.getInstance().getReference("users/" + UID + "/" + Constants.KEYS.USERS.POINTS)
                                .setValue(updatedPoint);
                   // }
                    Toast.makeText(this, "Hurray!!! You won", Toast.LENGTH_SHORT).show();

                    //#####################################Dialog of winner#########################
                    showDialogForWin(updatedPoint);
                    //##########################################################################

                    winImageShow(winOrNot,R.drawable.zero_green);
                    enableEverything(false);
                    writeToFinalNode();
                }
            }
            /*if (isWithComputer&&canExit==false)
            {
                //call for computer move
                computerMove();
            }*/
        }
    }
}
