package service.instagram_123;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by admin on 1/13/2017.
 */
public class AddData extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private FirebaseStorage storage;
    private StorageReference storageref;
    private  StorageReference storageref2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adddata);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("InstagramDB");
        databaseReference.setValue("Hello World!");
        storage = FirebaseStorage.getInstance();
        storageref = storage.getReferenceFromUrl("gs://instagram-123.appspot.com/");


    }


    @Override
    protected void onResume() {
        super.onResume();

        //storageref.child("images123");
        storageref2 = storageref.child("Instagram-images");

        Bitmap bit = BitmapFactory.decodeResource(this.getResources() , R.mipmap.ic_launcher);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.PNG , 100 , os);
        byte[] data = os.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        long time = System.currentTimeMillis();

        //get user name from shared prefrance + time
        //String imagename =
        storageref2.child("images").putStream(in).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddData.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("error error error" , e.getMessage());
            }
        });
    }
}
