package com.example.debajyotidas.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameActivity extends BaseActivity implements View.OnClickListener {

    CountDownTimer timer;
    String otherUID,regToken;
    ProgressDialog progressDialog;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    ValueEventListener valueEventListener;

    private ImageButton img_0,img_1,img_2,img_3,img_4,img_5,img_6,img_7,img_8;

    private final ArrayList<Integer> WIN_CASES=new ArrayList<>();

    Map<String,Object> movesHistory=new HashMap<>();

    ArrayList<Integer> moves=new ArrayList<>();
    ArrayList<Integer> otherPlayerMoves=new ArrayList<>();
    int counter=0;
    private boolean canExit=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        init();

        WIN_CASES.add(3);
        WIN_CASES.add(9);
        WIN_CASES.add(12);
        WIN_CASES.add(15);
        WIN_CASES.add(21);

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
                    try {
                        if (value) {
                            progressDialog.dismiss();
                            timer.cancel();
                            Toast.makeText(GameActivity.this, "Other player is online now", Toast.LENGTH_SHORT).show();
                        } else if (!canExit){
                            Toast.makeText(GameActivity.this, "Other player is offline now", Toast.LENGTH_SHORT).show();
                            progressDialog.show();
                            timer.start();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if (UID.equals("no_uid")){
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

        Log.d("key_set", "UID : "+UID+
        "otherUID : "+otherUID);

        FirebaseDatabase.getInstance().getReference("game/"+UID+"/isLive").setValue(true);
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
        FirebaseDatabase.getInstance().getReference("game/" + otherUID + "/current_game").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null){
                    Map<String,Object> map= (Map<String, Object>) dataSnapshot.getValue();
                    if (map.containsKey((otherPlayerMoves.size())+"_you")){
                        long move= (long) map.get((otherPlayerMoves.size())+"_you");
                        otherPlayerMoves.add((int) move);
                        movesHistory.put(counter+"_other",(int) move);
                        setPlayerMove((int) move, R.drawable.ic_mood_bad);
                        enableEverything(true);
                        canExit=false;
                        if (otherPlayerMoves.size()>=3) {
                            ArrayList<Integer> winOrNot=checkIfWin(otherPlayerMoves);
                            if (winOrNot!=null) {
                                canExit=true;
                                Toast.makeText(GameActivity.this, "Ohh!! You loose", Toast.LENGTH_SHORT).show();
                                winImageShow(winOrNot,R.drawable.ic_mood_red);
                                enableEverything(false);
                                writeToFinalNode();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void writeToFinalNode() {
        FirebaseDatabase.getInstance().getReference("game/"+UID+"/"+otherUID).push().setValue(movesHistory);
    }

    private void setPlayerMove(int move, int resource) {

        switch (move){
            case 0:
                img_0.setImageResource(resource);
                break;
            case 1:
                img_1.setImageResource(resource);
                break;
            case 2:
                img_2.setImageResource(resource);
                break;
            case 3:
                img_3.setImageResource(resource);
                break;
            case 4:
                img_4.setImageResource(resource);
                break;
            case 5:
                img_5.setImageResource(resource);
                break;
            case 6:
                img_6.setImageResource(resource);
                break;
            case 7:
                img_7.setImageResource(resource);
                break;
            case 8:
                img_8.setImageResource(resource);
                break;

        }
    }

    private void init() {
        img_0=(ImageButton) findViewById(R.id.img_0);
        img_1=(ImageButton) findViewById(R.id.img_1);
        img_2=(ImageButton) findViewById(R.id.img_2);
        img_3=(ImageButton) findViewById(R.id.img_3);
        img_4=(ImageButton) findViewById(R.id.img_4);
        img_5=(ImageButton) findViewById(R.id.img_5);
        img_6=(ImageButton) findViewById(R.id.img_6);
        img_7=(ImageButton) findViewById(R.id.img_7);
        img_8=(ImageButton) findViewById(R.id.img_8);

        img_0.setOnClickListener(this);
        img_1.setOnClickListener(this);
        img_2.setOnClickListener(this);
        img_3.setOnClickListener(this);
        img_4.setOnClickListener(this);
        img_5.setOnClickListener(this);
        img_6.setOnClickListener(this);
        img_7.setOnClickListener(this);
        img_8.setOnClickListener(this);
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
    public void onBackPressed() {

        if (!canExit)
            new AlertDialog.Builder(this)
                    .setTitle("Alert")
                    .setMessage("Game not completed yet. Do u want to exit from game")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GameActivity.super.onBackPressed();
                        }
                    }).setNegativeButton(android.R.string.no,null).show();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // if (canExit) {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference("game/" + UID + "/isLive").setValue(false);
        FirebaseDatabase.getInstance().getReference("game/" + UID + "/current_game").setValue(null);
    }
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        if (counter%2==0)
            ((ImageButton)v).setImageResource(R.drawable.ic_mood_happy);
            else ((ImageButton)v).setImageResource(R.drawable.ic_mood_bad);
        int positionClicked=-1;

        switch (v.getId()){
            case R.id.img_0:
                positionClicked=0;
                break;
            case R.id.img_1:
                positionClicked=1;
                break;
            case R.id.img_2:
                positionClicked=2;
                break;
            case R.id.img_3:
                positionClicked=3;
                break;
            case R.id.img_4:
                positionClicked=4;
                break;
            case R.id.img_5:
                positionClicked=5;
                break;
            case R.id.img_6:
                positionClicked=6;
                break;
            case R.id.img_7:
                positionClicked=7;
                break;
            case R.id.img_8:
                positionClicked=8;
                break;
        }
        if (positionClicked!=-1) {
            setPlayerMove(positionClicked, R.drawable.ic_mood_happy);
            //FirebaseDatabase.getInstance().getReference("game/" + otherUID + "/current_game/" + counter + "_other").setValue(positionClicked);
            FirebaseDatabase.getInstance().getReference("game/" + UID + "/current_game/" + counter + "_you").setValue(positionClicked);
            moves.add(positionClicked);
            movesHistory.put(counter+"_you",positionClicked);
            counter++;
            canExit=false;
            enableEverything(false);
            if (moves.size()>=3) {
                ArrayList<Integer> winOrNot=checkIfWin(moves);
                if (winOrNot!=null) {
                    canExit=true;
                    Toast.makeText(this, "Hurray!!! You won", Toast.LENGTH_SHORT).show();
                    winImageShow(winOrNot,R.drawable.ic_mood_green);
                    enableEverything(false);
                    writeToFinalNode();
                }
            }
        }
    }

    private void winImageShow(ArrayList<Integer> winOrNot, int resource) {
        for (Integer integer :
                winOrNot) {
            setPlayerMove(integer,resource);
        }
    }

    private void enableEverything(boolean enable) {
        img_0.setEnabled(enable);
        img_1.setEnabled(enable);
        img_2.setEnabled(enable);
        img_3.setEnabled(enable);
        img_4.setEnabled(enable);
        img_5.setEnabled(enable);
        img_6.setEnabled(enable);
        img_7.setEnabled(enable);
        img_8.setEnabled(enable);
    }

    private ArrayList<Integer> checkIfWin(ArrayList<Integer> moves) {
        ArrayList<ArrayList<Integer>> arrayLists=new ArrayList<>();
        getCombi(moves,arrayLists,3);
        for (ArrayList<Integer> arr :
                arrayLists) {
            int sum = 0;
            for (Integer move :
                    arr) {
                sum += move;
            }
            if (WIN_CASES.contains(sum)) {
                Log.d("main","final arr "+arr);
                return arr;
            }
        }
        Log.d("main","final arr "+arrayLists);
        return null;
    }

    private void getCombi(ArrayList<Integer> integers, ArrayList<ArrayList<Integer>> arrayLists, int r) {

        for (int i = 0; i < integers.size(); i++) {

            ArrayList<Integer> combis=new ArrayList<>();
            combis.addAll(integers);
            if (combis.size()==r) {
                if (!checkifAlreadyAdded(combis, arrayLists))
                    arrayLists.add(combis);
                Log.d("combi", "iteration "+i+" arr "+combis);
            }
            else {
                combis.remove(i);
                getCombi(combis,arrayLists, r);
            }
        }
    }

    private boolean checkifAlreadyAdded(ArrayList<Integer> combis, ArrayList<ArrayList<Integer>> arrayLists) {

        for (int i = 0; i < arrayLists.size(); i++) {
            boolean matched=true;
            for (int j = 0; j < combis.size(); j++) {
                matched&=arrayLists.get(i).contains(combis.get(j));
            }
            if (matched) return true;
        }
        return false;
    }

}
