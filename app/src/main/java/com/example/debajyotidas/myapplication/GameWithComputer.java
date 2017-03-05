package com.example.debajyotidas.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Set;

/**
 * Created by overtatech-4 on 5/3/17.
 */

public class GameWithComputer extends GameActivity {
    private static final String TAG = "GameWithComputer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UID.equals("no_uid")) {
            Log.d(TAG, "onCreate: no_uid");
            return;
        }

        otherUID="with_computer";

        FirebaseDatabase.getInstance().getReference("users/"+UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> map= (Map<String, Object>) dataSnapshot.getValue();
                if (map==null){
                    return;
                }
                if (map.containsKey(Constants.KEYS.USERS.PHOTO_URL))
                    Glide.with(GameWithComputer.this).load(map.get(Constants.KEYS.USERS.PHOTO_URL)).into(img_mine);
                /*if (map.containsKey(Constants.KEYS.USERS.RATING)){
                    Object o =  map.get(Constants.KEYS.USERS.RATING);
                    try{
                        rating=(Long) o;
                    }catch (ClassCastException e){
                        rating=(Double) o;
                    }
                }*/
                if (map.containsKey(Constants.KEYS.USERS.WIN_COUNT))
                    wingCount= (Long) map.get(Constants.KEYS.USERS.WIN_COUNT);
                if (map.containsKey(Constants.KEYS.USERS.LOOSE_COUNT))
                    looseCount= (Long) map.get(Constants.KEYS.USERS.LOOSE_COUNT);
                if (map.containsKey(Constants.KEYS.USERS.POINTS))
                    points= (Long) map.get(Constants.KEYS.USERS.POINTS);

                text_mine.setText(String.valueOf(wingCount)+"/"+looseCount+" : "+String.valueOf(points));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
             //   FirebaseDatabase.getInstance().getReference("game/" + UID + "/current_game/" + counter + "_you").setValue(positionClicked);
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
                    long updatedPoint=0;
                    //if (isWithComputer){
                        updatedPoint=points + Constants.BET.COMPUTER;
                        FirebaseDatabase.getInstance().getReference("users/" + UID + "/" + Constants.KEYS.USERS.POINTS)
                                .setValue(updatedPoint);
                    /*}else {
                        FirebaseDatabase.getInstance().getReference("users/" + UID + "/" + Constants.KEYS.USERS.WIN_COUNT)
                                .setValue(wingCount + 1);
                        int pointsGained = calculatePoint(pointsOther);
                        updatedPoint=points + pointsGained;
                        FirebaseDatabase.getInstance().getReference("users/" + UID + "/" + Constants.KEYS.USERS.POINTS)
                                .setValue(updatedPoint);
                    }*/
                    Toast.makeText(this, "Hurray!!! You won", Toast.LENGTH_SHORT).show();

                    //#####################################Dialog of winner#########################
                    showDialogForWin(updatedPoint);
                    //##########################################################################

                    winImageShow(winOrNot,R.drawable.zero_green);
                    enableEverything(false);
                    writeToFinalNode();
                }
            }
            if (canExit==false)
            {
                //call for computer move
                computerMove();
            }
        }
    }
}
