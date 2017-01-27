package service.instagram_123;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UsersFeeds extends AppCompatActivity {


    SharedPreferences pref;
    private FirebaseDatabase storage;
    private DatabaseReference reference;
    ProgressDialog dialog ;
GridView gridView;
     ImageAdapter adapter;
    ArrayList<String>resultStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_feeds);
        pref =  this.getSharedPreferences("instagram" , 0);
      //  String key = pref.getString("key" , "");
        storage = FirebaseDatabase.getInstance();
        reference = storage.getReference("Instagram_DB");
        gridView = (GridView) findViewById(R.id.grid);
        resultStr = new ArrayList<>();
         adapter = new ImageAdapter(this);
        Intent in = getIntent();
        if(!in.getStringExtra("key").isEmpty())
        {
            String key = in.getStringExtra("key");

            reference.child("Users").child(key).orderByChild("User_Images").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i("inside " , "datachange");
                    if(dataSnapshot.hasChild("User_Images"))
                    {
                        for(DataSnapshot sh : dataSnapshot.getChildren())
                        {
                            for(DataSnapshot d : sh.getChildren()) {
                                resultStr.add(d.child("image").getValue().toString());
                            }
                        }
                        gridView.setAdapter(adapter);

                    }
                    else
                    {
                        Toast.makeText(UsersFeeds.this, "There No Image For That User", Toast.LENGTH_SHORT).show();
                    }
                    if(dialog.isShowing())
                    {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            getSupportActionBar().setTitle(in.getStringExtra("name") + "'s Feed");
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        dialog  =    ProgressDialog.show(this , "Loading..." , "Please waite untie finish ...");
    }

    // custom adapter to make view alittle butiful
    public class ImageAdapter extends BaseAdapter
    {
        Context mContext;

        public ImageAdapter(Context c)
        {
            mContext=c;
        }

        @Override
        public int getCount() {
            if(resultStr != null)
            {
                return resultStr.size();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;

            if(convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.custom_grid, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //viewHolder.Title.setText(TITle.get(position));
            viewHolder.poster_img.setScaleType(ImageView.ScaleType.CENTER);


                Picasso.with(mContext).load(resultStr.get(position)).resize(200, 400).into(viewHolder.poster_img);

            return convertView;
        }
    }

    // viewholder to not init the view evry time
    public static class ViewHolder
    {
        ImageView poster_img;
        TextView Title;

        public ViewHolder(View view)
        {
            poster_img = (ImageView) view.findViewById(R.id.imggrid);

            //Title = (TextView) view.findViewById(R.id.movieTitle);
        }
    }



}
