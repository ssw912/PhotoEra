package com.example.photoera;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText email;
    EditText password;
    Button loginButton;
    TextView registerButton;
    TextView findButton;
    TextView textviewMessage;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;

    private final int MY_PERMISSIONS_REQUEST_LOCATION=1003;

    String nameText;
    UserToken userToken = new UserToken();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        int permssionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permssionCheck!= PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"?????? ????????? ???????????????",Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this,"?????? ????????? ???????????????",Toast.LENGTH_LONG).show();

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                Toast.makeText(this,"?????? ????????? ???????????????",Toast.LENGTH_LONG).show();
            }
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM LOG", "GetInstanceId failed ", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.d("FCM LOG", "FCM ??????: " + token);

                    }
                });
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (TextView) findViewById(R.id.registerButton);
        findButton = (TextView) findViewById(R.id.findButton);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        progressDialog = new ProgressDialog(this);

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        findButton.setOnClickListener(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,"????????? ???????????? ????????????.",Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this,"?????? ???????????? ???????????????.",Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    private void updateToken() {
        final String emailText2 = email.getText().toString().trim();

        Query queries = databaseReference.child("users").orderByChild("email").equalTo(emailText2);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot datas : dataSnapshot.getChildren()) {
                    nameText = datas.child("name").getValue().toString();
                    userToken.userName = nameText;
                    Log.d("token.name = ", " " + userToken.userName);
                    userToken.fcmToken = FirebaseInstanceId.getInstance().getToken();
                    Log.d("token.token = ", " " + userToken.fcmToken);

                    databaseReference.child("usertoken").child(userToken.userName).setValue(userToken);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void userLogin() {
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (TextUtils.isEmpty(emailText)) {
            Toast.makeText(this, "email??? ????????? ?????????", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(this, "password??? ????????? ?????????", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("?????????????????????. ?????? ????????? ?????????...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            updateToken();

                            finish();
                            startActivity(new Intent(getApplicationContext(), UnityPlayerActivity.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "????????? ??????!", Toast.LENGTH_LONG).show();
                            textviewMessage.setText("????????? ?????? ??????\n - password??? ?????? ????????????.\n -????????????");
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == loginButton) {
            userLogin();
        }
        if (v == registerButton) {
            startActivity(new Intent(this, RegistActivity.class));
        }
        if (v == findButton) {
            startActivity(new Intent(this, FindActivity.class));
        }
    }
}