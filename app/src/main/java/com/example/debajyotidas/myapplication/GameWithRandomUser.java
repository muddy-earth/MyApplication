package com.example.debajyotidas.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by overtatech-4 on 5/3/17.
 */

public class GameWithRandomUser extends GameActivity {

    private static final String TAG = "GameWithRandomUser";
    String queue = null, myKey=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UID.equals("no_uid")) {
            Log.d(TAG, "onCreate: no_uid");
            return;
        }

        betRequested= getIntent().getLongExtra("bet",-1);
        if (betRequested==-1){
            Log.d(TAG, "onCreate: no bet requested");
            return;
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Waiting for user to be live...");
            progressDialog.show();

        startCountDown();

        //getName of queue to look for active users

        switch ((int)betRequested){
            case Constants.BET.BEGINNER:
                queue=Constants.KEYS.QUEUE.BIGINNER_QUEUE;
                break;
            case Constants.BET.MEDIUM:
                queue=Constants.KEYS.QUEUE.MEDIUM_QUEUE;
                break;
            case Constants.BET.HIGHER:
                queue=Constants.KEYS.QUEUE.HIGHER_QUEUE;
                break;
            default:
                Log.d(TAG, "onCreate: no queue matched");

        }
        //final String finalQueue = queue;
        final ValueEventListener valueEventListener1=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //TODO do something to stop waiting at you end 05/03/2017

                Iterator<DataSnapshot> dataSnapshotIterator=dataSnapshot.getChildren().iterator();
                while (dataSnapshotIterator.hasNext()){
                    DataSnapshot dataSnapshot1=dataSnapshotIterator.next();
                    if (dataSnapshot1.getValue().equals(UID))
                        continue;
                    FirebaseDatabase.getInstance().getReference("queue/"+ queue).removeEventListener(this);
                    startGameNow(dataSnapshot1);
                    break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        FirebaseDatabase.getInstance().getReference("queue/"+queue).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot==null||dataSnapshot.getChildrenCount()==0){
                    //No player in queue
                    //add myself to the queue so that others can see me
                    FirebaseDatabase.getInstance().getReference("queue/"+ queue).addValueEventListener(valueEventListener1);
                    myKey=FirebaseDatabase.getInstance().getReference("queue/"+ queue).push().getKey();
                    FirebaseDatabase.getInstance().getReference("queue/"+ queue+"/"+myKey).setValue(UID);
                }else {
                    startGameNow(dataSnapshot.getChildren().iterator().next());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void startGameNow(DataSnapshot dataSnapshot) {
        progressDialog.dismiss();
        timer.cancel();
        otherUID= (String) dataSnapshot.getValue();
        FirebaseDatabase.getInstance().getReference("game/"+UID+"/isLive").setValue(true);
        FirebaseDatabase.getInstance().getReference("game/"+otherUID+"/isLive").addValueEventListener(valueEventListener);
        addListeners();
        FirebaseDatabase.getInstance().getReference("queue/"+ queue+"/"+dataSnapshot.getKey()).setValue(null);
        if (myKey!=null)
        FirebaseDatabase.getInstance().getReference("queue/"+ queue+"/"+myKey).setValue(null);
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
                    //}
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
