package com.example.photoera;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;


public class PhotoActivity extends Activity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth auth;

    Button Title1;
    ImageView photo1, Profile1;
    ImageButton Btncomment1, Btnlike1, btnReturn1;
    TextView LikeCount1, date1;


    String Photo_title1, TestID1, getkey1, getdate1, getname1, setCount1;
    int getCount1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoacivity);
        Title1 = (Button) findViewById(R.id.Title1);
        LikeCount1 = (TextView) findViewById(R.id.likecount1);
        date1 = (TextView) findViewById(R.id.date1);
        btnReturn1 = (ImageButton) findViewById(R.id.return_photo1);
        Btncomment1 = (ImageButton) findViewById(R.id.btncomment1);
        Btnlike1 = (ImageButton) findViewById(R.id.btnlike1);
        photo1 = (ImageView) findViewById(R.id.photo1);
        Profile1 = (ImageView) findViewById(R.id.profile1);

        Intent intent = getIntent();

        Btnlike1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStarClicked2();
            }
        });

     //????????? ID ????????????
        auth = FirebaseAuth.getInstance();
        final String email1 = auth.getCurrentUser().getEmail().toString().trim();
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();
        Query queries = ref1.child("users").orderByChild("email").equalTo(email1);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot datas : dataSnapshot.getChildren()) {
                    TestID1 = datas.child("name").getValue().toString().trim();


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        //???????????? ?????? ????????? ??????
        getkey1 = intent.getStringExtra("pid");



        //?????? ?????? ????????????
        databaseReference.child("upload").orderByKey().equalTo(getkey1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        getname1 = snapshot.child("Name").getValue().toString();
                        getdate1 = snapshot.child("Date").getValue().toString();
                        Photo_title1 = snapshot.getKey();
                        Title1.setText(getname1);
                        date1.setText(getdate1.substring(0,4) + "??? " + getdate1.substring(4,6) + "??? " + getdate1.substring(6,8) + "???");
                    }
                } else {
                    System.out.println("No data");
                }

                //?????? ????????????
                StorageReference imageRef = mStorageRef.child("upload/" + getdate1 + ".png");
                try {
                    // Storage ?????? ???????????? ???????????? ????????????
                    final File imageFile = File.createTempFile("upload", "png");
                    imageRef.getFile(imageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Success Case
                            Bitmap bitmapImage = BitmapFactory.decodeFile(imageFile.getPath());
                            photo1.setImageBitmap(bitmapImage);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Fail Case
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "?????? ???????????? ?????? !!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //????????? ?????? ?????????
                StorageReference profileRef = mStorageRef.child("profiles/" + getname1 + ".png");
                try {
                    // Storage ?????? ???????????? ???????????? ????????????
                    final File profileFile = File.createTempFile("upload", "png");
                    profileRef.getFile(profileFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Success Case
                            Bitmap bitmapprofileImage = BitmapFactory.decodeFile(profileFile.getPath());
                            Profile1.setImageBitmap(bitmapprofileImage);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Fail Case
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "????????? ???????????? ?????? !!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //????????? ?????? ????????????
        databaseReference.child("Like").orderByKey().equalTo(getkey1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    //???????????? ????????? ????????? DB??? ????????????
                    if (snapshot.child("countusers").exists()) {
                        getCount1 = (int) snapshot.child("countusers").getChildrenCount();
                        setCount1 = Integer.toString(getCount1);
                        LikeCount1.setText(setCount1);
                    } else { //???????????? ????????? ????????? 0??????
                        getCount1 = 0;
                        LikeCount1.setText("0");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        //?????? ?????? ??????
        Title1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(PhotoActivity.this);
                dlg.setTitle(getname1+"?????? ?????? ???????????????????");
                dlg.setPositiveButton("???",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(PhotoActivity.this, ChatActivity.class);

                                intent.putExtra("chatName", getname1);
                                intent.putExtra("TestID", TestID1);
                                startActivity(intent);
                            }
                        });
                dlg.setNegativeButton("?????????", null);
                dlg.show();
            }
        });

        //???????????? ??????
        btnReturn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),UnityPlayerActivity.class));
                finish();
            }
        });

        Btncomment1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        CommentActivity.class);
                intent.putExtra("TestID", TestID1);
                intent.putExtra("phototitle", Photo_title1);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),UnityPlayerActivity.class));
        finish();
    }

    private void onStarClicked2() {

        //????????? ????????? 0???????????? DB??? ??????
        if (getCount1 == 0) {
            databaseReference.child("Like").child(getkey1).child("countusers").child(TestID1).setValue("Like");
            getCount1 += 1;
            LikeCount1.setText("1");
        } else { //????????? ????????? 0??? ?????????
            databaseReference.child("Like").orderByKey().equalTo(getkey1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("countusers").child(TestID1).exists()) {
                            Toast.makeText(getApplicationContext(), "????????? ?????? !!", Toast.LENGTH_LONG).show();
                            databaseReference.child("Like").child(getkey1).child("countusers").child(TestID1).removeValue();
                            getCount1 -= 1;
                            setCount1 = Integer.toString(getCount1);
                            LikeCount1.setText(setCount1);
                        } else {
                            Toast.makeText(getApplicationContext(), "????????? !!", Toast.LENGTH_LONG).show();
                            databaseReference.child("Like").child(getkey1).child("countusers").child(TestID1).setValue("Like");
                            getCount1 += 1;
                            setCount1 = Integer.toString(getCount1);
                            LikeCount1.setText(setCount1);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}