package service.instagram_123;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UsersList extends AppCompatActivity {

    ListView listOfUsers;
    String key = "";
    String imagedownloadUrl = "";
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    //private DatabaseReference refusers;
    FirebaseAuth mAuth;

    private FirebaseUser user;
    ArrayAdapter adapter;
    private FirebaseStorage storage;
    private StorageReference storageref;
    private SharedPreferences pref;
    DatabaseReference refimg;
    String shusername;
    ValueEventListener listener;
    boolean flage = false;
    ArrayList<String> lisid;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Instagram_DB");
        //  refusers = databaseReference.child("Users");
        listOfUsers = (ListView) findViewById(R.id.list_user_list);
        adapter = new ArrayAdapter(UsersList.this, android.R.layout.simple_list_item_1, new ArrayList());
        listOfUsers.setAdapter(adapter);
        storage = FirebaseStorage.getInstance();
        storageref = storage.getReferenceFromUrl("gs://instagram-123.appspot.com/");
        pref = this.getSharedPreferences("instagram", 0);

        lisid = new ArrayList<>();
        listOfUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent in = new Intent(UsersList.this, UsersFeeds.class);
                in.putExtra("key", lisid.get(i));
                in.putExtra("name", adapter.getItem(i).toString());
                startActivity(in);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUsersList();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.i("on Data change", "on Data Change");
                    key = dataSnapshot.getChildren().iterator().next().getKey();
                    databaseReference.removeEventListener(this);
                    //11databaseReference.child("Users").child(key).push().child("User_Images").setValue(imageName + ".png");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                flage = false;
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        shusername = pref.getString("username", "");
    }

    private void getUsersList() {
     dialog = ProgressDialog.show(this, "Loading...", "Please waite untie finish ...");
        // final String userName = pref.getString("username", null);
        // user = mAuth.getCurrentUser();
        //databaseReference.orderByChild("Users")
        if (adapter != null) {
            adapter.clear();
        }
        databaseReference.orderByChild("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    dialog.dismiss();
                
                //dataSnapshot.child("name").equals("");
                Log.i("getuserlist", "enter");
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    //key = dataSnapshot.getChildren().iterator().next().getKey();
                    if (shot.getValue() != null && shot.hasChild("name") && !shot.child("name").getValue().equals(shusername)) {
                        adapter.add(shot.child("name").getValue());
                        Log.i("keys", shot.getKey());
                        lisid.add(shot.getKey());
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.userlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.share) {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(in, 1);
        } else if (item.getItemId() == R.id.logout) {
            user = mAuth.getCurrentUser();
            FirebaseAuth.getInstance().signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                databaseReference.child("Users").orderByChild("name").equalTo(shusername).addValueEventListener(listener);
                // storageref2 = storageref.child("Instagram-images");
                Bitmap bitmapimage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                Log.i("AppInfo", "get image here");

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmapimage.compress(Bitmap.CompressFormat.PNG, 100, os);
                byte[] data2 = os.toByteArray();
                ByteArrayInputStream in = new ByteArrayInputStream(data2);
                long time = System.currentTimeMillis();

                //get user name from shared prefrance + time
                //String imagename =

                String imageName = shusername + String.valueOf(time);

                storageref.child("Instagram-images").child(imageName.trim()).putStream(in).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UsersList.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("error error error", e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        flage = true;

                        //get image download url
                        imagedownloadUrl = taskSnapshot.getDownloadUrl().toString();
                        addimageUrl(imagedownloadUrl);
                        Log.i("imagedownloadUrl", imagedownloadUrl);
                        Log.i("oncomplete", "storage");
                        //refimg =  databaseReference.child("Users").orderByChild("name").equalTo(shusername).getRef();

                        if (flage) {
                            Log.i("log in flag", "yes log in");

                            flage = false;
                        }
                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    private void addimageUrl(String imageName) {
        databaseReference.removeEventListener(listener);
        flage = false;
        Log.i("add image url", "add image url");
        if (key != "") {
            databaseReference.child("Users").child(key).child("User_Images").push().child("image").setValue(imageName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor edit = pref.edit();
        if (!key.isEmpty()) {
            edit.putString("key", key);
            edit.commit();
        }
    }

    private void get_user_name() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        ValueEventListener listen = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                dataSnapshot.child(user.getEmail()).getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if (user != null) {
            //refusers.orderByChild("name").addValueEventListener(listen);
        }
    }


}
