package service.instagram_123;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener, View.OnClickListener {


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    SharedPreferences pref;
    Button btn_login;
    EditText ed_email, ed_pass;
    TextView txt_change;
    ImageView logo;
    RelativeLayout relative;
    EditText ed_userName;
    ValueEventListener listener;
    ProgressDialog dialog;


    static boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Instagram_DB");
        FirebaseUser user = mAuth.getCurrentUser();
        pref = this.getSharedPreferences("instagram", 0);
        if (user != null) {
           ShowUserList();
        }
        InitializeWidget();
        flag = true;
    }

    @Override
    public void onClick(View view) {
        final String email = ed_email.getText().toString();
        //pass must not less 6 digit
        final String pass = ed_pass.getText().toString();
        final String username = ed_userName.getText().toString();

        if (view == btn_login) {

            if (!email.isEmpty() && !pass.isEmpty()) {

                if (btn_login.getText().toString().equals("Sign Up")) {
                    if (!username.isEmpty()) {

                     dialog  =    ProgressDialog.show(this , "Loading..." , "Please waite untie finish ...");
                        //register user
                        RegisterUser(email, pass, username);

                        listener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    // databaseReference.removeEventListener(this);
                                    databaseReference.child("Users").push().child("name").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.i("success", "completetask");
                                                databaseReference.removeEventListener(listener);
                                                if(dialog.isShowing())
                                                {
                                                    dialog.dismiss();
                                                }
                                                EmptyData();
                                                Toast.makeText(MainActivity.this, " Thank You For Register ", Toast.LENGTH_SHORT).show();
                                                SharedPreferences.Editor edit = pref.edit();
                                                edit.putString("username" , username);
                                                edit.commit();
                                                databaseReference.removeEventListener(listener);
                                                ShowUserList();
                                            } else {
                                                flag = true;
                                            }
                                        }
                                    });

                                } else {
                                    if (flag) {
                                        Toast.makeText(MainActivity.this, "This User Name Exists !", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        };
                    } else {
                        Toast.makeText(MainActivity.this, " Please, Type UserName ! ", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    //String dot = email.replace(".","");


                } else {
                    //SingIn
                    mAuth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            // Log.i("sesese" , "sesese");
                            ShowUserList();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            // Log.e("code" , e.getLocalizedMessage());
                        }
                    });

                }
            } else {
                Toast.makeText(MainActivity.this, " Please, Type Email and Password ! ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (view == relative || view == logo)

        {
            InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

    }

    private void RegisterUser(final String email, String pass, final String userName) {
        flag = false;

        // SignUp
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    //not success
                    Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    flag = true;
                    return;
                } else {
                    //success
                    //check for user name if exists
                    databaseReference.child("Users").orderByChild("name").equalTo(userName).addValueEventListener(listener);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(listener);
    }

    private void ShowUserList() {
        Intent in = new Intent(MainActivity.this, UsersList.class);
        startActivity(in);
    }


    private void EmptyData() {
        ed_userName.setText("");
        ed_email.setText("");
        ed_pass.setText("");
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {

        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            onClick(view);
        }

        return false;
    }



    private void InitializeWidget() {
        ed_userName = (EditText) findViewById(R.id.ed_userName);
        logo = (ImageView) findViewById(R.id.imglogin);
        relative = (RelativeLayout) findViewById(R.id.relative);

        logo.setOnClickListener(this);
        relative.setOnClickListener(this);
        txt_change = (TextView) findViewById(R.id.txt_change);
        btn_login = (Button) findViewById(R.id.btn_login);
        txt_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (txt_change.getText().toString()) {
                    case "Sign Up":
                        txt_change.setText("Log In");
                        btn_login.setText("Sign Up");
                        ed_userName.setVisibility(View.VISIBLE);
                        break;

                    case "Log In":
                        txt_change.setText("Sign Up");
                        btn_login.setText("Log In");
                        ed_userName.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });
        btn_login.setOnClickListener(this);
        ed_email = (EditText) findViewById(R.id.edemail);
        ed_pass = (EditText) findViewById(R.id.edpass);

        ed_email.setOnKeyListener(this);
        ed_pass.setOnKeyListener(this);
        ed_userName.setOnKeyListener(this);

        ed_userName.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.i("log in ", "Success!");
                } else {
                    Log.i("log in ", "Not Success");
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }


}
