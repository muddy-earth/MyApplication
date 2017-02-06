package com.example.debajyotidas.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    String UID,other_UID,reg_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent=getIntent();

        other_UID=intent.getStringExtra("other_uid");
        reg_token=intent.getStringExtra("reg_token");
        UID=intent.getStringExtra("uid");

        if (other_UID==null||reg_token==null||UID==null){
            return;
        }

        if (other_UID.equals("")||reg_token.equals("")|UID.equals("")){
            return;
        }



    }

    public void send(View v){
        String message=((EditText)findViewById(R.id.edit_text)).getText().toString();

        Map<String, Object> map=new HashMap<>();
        map.put("message",message);
        map.put("timestamp", ServerValue.TIMESTAMP);

        FirebaseDatabase.getInstance().getReference("chat/"+UID+"/"+other_UID).push().setValue(map);
    }
}
