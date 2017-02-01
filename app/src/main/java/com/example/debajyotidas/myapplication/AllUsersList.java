package com.example.debajyotidas.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.debajyotidas.myapplication.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AllUsersList extends BaseActivity {

    private RecyclerView recyclerView;
    private ArrayList<User> users=new ArrayList<>();
    private final String TAG ="AllUsersList";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users_list);

        recyclerView=(RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter(users));

        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator=dataSnapshot.getChildren().iterator();
                users.clear();
                while (iterator.hasNext())
                {
                    DataSnapshot datasnap=iterator.next();
                    if (!datasnap.getKey().equals(UID)) {
                        Map<String, Object> map = (Map<String, Object>) datasnap.getValue();

                        boolean isOnline=Boolean.parseBoolean(String.valueOf(map.get("online")));
                        User user=new User(String.valueOf(map.get("name")),
                                String.valueOf(map.get("img_url")),
                                isOnline,String.valueOf(map.get("reg_token")));
                        user.setUID(datasnap.getKey());

                        if (!isOnline){

                            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            try {
                                String timestamp = sfd.format(new Date(Long.parseLong(String.valueOf(map.get("lastOnline")))));

                                user.setLast_seen(timestamp);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        users.add(user);
                    }
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

        private ArrayList<User> datas;

        public MyAdapter(ArrayList<User> datas) {
            this.datas = datas;
        }

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
         * an item.
         * <p>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p>
         * The new ViewHolder will be used to display items of the adapter using
         * . Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * @see #onBindViewHolder(ViewHolder, int)
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_list_item,parent,false));
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
         * position.
         * <p>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Override  instead if Adapter can
         * handle efficient partial bind.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final User data=datas.get(position);

            holder.name.setText(data.getName());

            Glide.with(getApplicationContext()).load(data.getImg_url()).placeholder(R.drawable.ic_person).into(holder.profile);

            if (data.isOnline()) {
                holder.online_offline.setImageResource(R.drawable.ic_online);
                holder.last_seen.setVisibility(View.GONE);
            }
                else{
                holder.last_seen.setVisibility(View.VISIBLE);
                holder.online_offline.setImageResource(R.drawable.ic_offline);
            }
            String lastSeen;
            if (data.getLast_seen()==null)
            lastSeen="last seen not known";
            else lastSeen="last seen "+data.getLast_seen();
            holder.last_seen.setText(lastSeen);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    /*new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                OkHttpClient client = new OkHttpClient();
                                JSONObject json=new JSONObject();
                                JSONObject dataJson=new JSONObject();
                                dataJson.put("message","Hi this is sent from device to device");
                                dataJson.put("sender",UID);
                                json.put("data",dataJson);
                                json.put("to",data.getReg_token());
                                RequestBody body = RequestBody.create(JSON, json.toString());
                                Request request = new Request.Builder()
                                        .header("Authorization","key="+Constants.LEGACY_SERVER_KEY)
                                        .url(Constants.FIREBASE_PUSH_URL)
                                        .post(body)
                                        .build();
                                Response response = client.newCall(request).execute();
                                String finalResponse = response.body().string();
                                Log.d(TAG, "token : "+data.getReg_token()+"/////"+finalResponse);

                                startActivity(new Intent(AllUsersList.this,GameActivity.class).putExtra("uid",data.getUID()));

                            }catch (Exception e){
                                Log.d(TAG,e+"");
                            }
                            return null;
                        }
                    }.execute();

*/
                    startActivity(new Intent(AllUsersList.this,GameActivity.class)
                            .putExtra("uid",data.getUID())
                    .putExtra("reg_token",data.getReg_token()));
                }
            });
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView profile, online_offline;
            TextView name, last_seen;
            CardView cardView;
            public ViewHolder(View itemView) {
                super(itemView);
                cardView=(CardView) itemView.findViewById(R.id.card);
                profile=(ImageView) itemView.findViewById(R.id.img_profile);
                online_offline=(ImageView) itemView.findViewById(R.id.img_online_offline);
                name=(TextView) itemView.findViewById(R.id.name_profile);
                last_seen=(TextView) itemView.findViewById(R.id.subtext_profile);

            }
        }
    }

        /*String post(String url, String json) throws IOException {

        }*/

}
