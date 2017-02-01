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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    private final ArrayList<Set<Integer>> WIN_CASES=new ArrayList<>();

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

        initWinCases();

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Waiting for user to be live...");
        progressDialog.show();

        timer=new CountDownTimer(30000,1000){
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                try {
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
                }catch (Exception e){
                    e.printStackTrace();
                }
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
                        findViewById(R.id.wait_text).setVisibility(View.GONE);
                        canExit=false;
                        if (otherPlayerMoves.size()>=3) {
                            Set<Integer> winOrNot=checkIfWin(otherPlayerMoves);
                            if (winOrNot!=null) {
                                canExit=true;
                                Toast.makeText(GameActivity.this, "Ohh!! You loose", Toast.LENGTH_SHORT).show();
                                winImageShow(winOrNot,R.drawable.ic_mood_sad_red);
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

    private void initWinCases() {
        Set<Integer> set1=new HashSet<>();
        set1.add(0);
        set1.add(1);
        set1.add(2);
        Set<Integer> set2=new HashSet<>();
        set2.add(3);
        set2.add(4);
        set2.add(5);
        Set<Integer> set3=new HashSet<>();
        set3.add(6);
        set3.add(7);
        set3.add(8);
        Set<Integer> set4=new HashSet<>();
        set4.add(0);
        set4.add(3);
        set4.add(6);
        Set<Integer> set5=new HashSet<>();
        set5.add(1);
        set5.add(4);
        set5.add(7);
        Set<Integer> set6=new HashSet<>();
        set6.add(2);
        set6.add(5);
        set6.add(8);
        Set<Integer> set7=new HashSet<>();
        set7.add(0);
        set7.add(4);
        set7.add(8);
        Set<Integer> set8=new HashSet<>();
        set8.add(2);
        set8.add(4);
        set8.add(6);

        WIN_CASES.add(set1);
        WIN_CASES.add(set2);
        WIN_CASES.add(set3);
        WIN_CASES.add(set4);
        WIN_CASES.add(set5);
        WIN_CASES.add(set6);
        WIN_CASES.add(set7);
        WIN_CASES.add(set8);

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

    private void sendNotification(final String reg_token) {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json=new JSONObject();
                    JSONObject dataJson=new JSONObject();
                    dataJson.put("message","Hi this is sent from device to device");
                    dataJson.put("sender",UID);
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

        if (moves.contains(positionClicked)||otherPlayerMoves.contains(positionClicked)){
            Toast.makeText(this, "Not allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (counter%2==0)
            ((ImageButton)v).setImageResource(R.drawable.ic_mood_happy);
        else ((ImageButton)v).setImageResource(R.drawable.ic_mood_bad);
        if (positionClicked!=-1) {
            setPlayerMove(positionClicked, R.drawable.ic_mood_happy);
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
                    Toast.makeText(this, "Hurray!!! You won", Toast.LENGTH_SHORT).show();
                    winImageShow(winOrNot,R.drawable.ic_mood_happy_green);
                    enableEverything(false);
                    writeToFinalNode();
                }
            }
        }
    }

    private void winImageShow(Set<Integer> winOrNot, int resource) {
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

    private Set<Integer> checkIfWin(ArrayList<Integer> moves) {
        return getCombi(moves,3);
    }

    private Set<Integer> getCombi(ArrayList<Integer> integers, int r) {

        Log.d("combiG", "iteration "+" arr "+integers);

        if (integers.size()==r) {
            Set<Integer> integers1=new HashSet<>();
            integers1.addAll(integers);
            Log.d("combi", "iteration "+" arr "+integers +" "+WIN_CASES.contains(integers1));
            /*for (Set<Integer> integrs :
                    WIN_CASES) {*/
                if (WIN_CASES.contains(integers1))
                    return integers1;
            //}
            //return null;
        } else /*if (integers.size()>r)*/{
            Log.d("combi-else", "iteration "+" arr "+integers);
        for (int i = 0; i < integers.size(); i++) {
            ArrayList<Integer> combis=new ArrayList<>();
            combis.addAll(integers);
            combis.remove(i);
            Set<Integer> integerSet=getCombi(combis, r);
            if (integerSet!=null) return integerSet;
            }
        }/*else return null;*/
        return null;
    }
    private void getCombi(Set<Integer> integers, ArrayList<Set<Integer>> arrayLists, int r) {

        for (int i = 0; i < integers.size(); i++) {

            Set<Integer> combis=new HashSet<>();
            combis.addAll(integers);
            combis.remove(i);
            if (combis.size()==r) {
                if (!checkifAlreadyAdded(combis, arrayLists))
                    arrayLists.add(combis);
                Log.d("combi", "iteration "+i+" arr "+combis);
            }
            else {
                getCombi(combis,arrayLists, r);
            }
        }
    }
    private boolean checkifAlreadyAdded(Set<Integer> combis, ArrayList<Set<Integer>> arrayLists) {

        for (int i = 0; i < arrayLists.size(); i++) {
            boolean matched=true;
            Iterator<Integer> iterator=combis.iterator();
            while (iterator.hasNext()){
                Integer integer=iterator.next();
                matched&=arrayLists.get(i).contains(integer);
            }
            if (matched) return true;
        }
        return false;
    }

}
