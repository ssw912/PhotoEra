package com.example.photoera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegistActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int MY_PERMISSION_STORAGE=1111;
    private static final int REQUEST_TAKE_ALBUM=2222;
    private static final int REQUEST_IMAGE_CROP=3333;
    private static final String TAG="RegistActivity";
    ImageView profile;
    EditText email;
    EditText password;
    EditText passwordConfirm;
    EditText name;
    EditText birth;
    Button signupButton;
    Button email_confirm;
    TextView textviewMessage;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    Uri photoURI,albumURI;
    String mCurrentPhotoPath;
    private DatePickerDialog.OnDateSetListener callbackMethod;
    private FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference=firebaseDatabase.getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        checkPermission();

        firebaseAuth= FirebaseAuth.getInstance();

        email=(EditText)findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirm);
        name = (EditText) findViewById(R.id.name);
        birth = (EditText) findViewById(R.id.birth);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        signupButton = (Button) findViewById(R.id.signupButton);
        progressDialog = new ProgressDialog(this);
        profile=(ImageView)findViewById(R.id.profile);
        profile.setOnClickListener(this);


        signupButton.setOnClickListener(this);
        email_confirm=(Button)findViewById(R.id.email_confirm);
        email_confirm.setOnClickListener(this);
        this.InitializeListener();
    }

    private void InitializeListener(){
        callbackMethod= new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                birth.setText(year+"/"+(month+1)+"/"+dayOfMonth);
            }
        };
    }

    public void OnClickHandler(View view)
    {
        DatePickerDialog dialog = new DatePickerDialog(this, callbackMethod, 2019, 11, 11);

        dialog.show();
    }

    public void dbQuery(){
        final String emailText2 = email.getText().toString().trim();
        Query queries=databaseReference.orderByChild("email").equalTo(emailText2);
        queries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent intent=new Intent(getApplicationContext(), PopupActivity.class);
                    intent.putExtra("data","????????? ????????? ?????????.");
                    startActivityForResult(intent,1);
                    databaseReference.removeEventListener(this);
                }else{
                    Intent intent=new Intent(getApplicationContext(), PopupActivity.class);
                    intent.putExtra("data","??????????????? ??????????????????.");
                    startActivityForResult(intent,1);
                    databaseReference.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public File createImageFile() throws IOException{
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName="JPEG_"+timeStamp+".jpg";
        File imageFile=null;
        File storageDir=new File(Environment.getExternalStorageDirectory()+"/Pictures","profiles");
        if(!storageDir.exists()){
            Log.i("mCurrentPhotoPath1",storageDir.toString());
            storageDir.mkdirs();
        }
        imageFile=new File(storageDir,imageFileName);
        mCurrentPhotoPath=imageFile.getAbsolutePath();

        return imageFile;
    }

    private void getAlbum(){
        Log.i("getAlbum","Call");
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Log.i("galleryAddPic","Call");
        Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f=new File(mCurrentPhotoPath);
        Uri contentUri=Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this,"????????? ????????? ?????????????????????.",Toast.LENGTH_SHORT).show();
    }

    public void cropImage(){
        Log.i("cropImage","Call");
        Log.i("cropImage","photoURI:"+photoURI+"/albumURI:"+albumURI);
        Intent cropIntent=new Intent("com.android.camera.action.CROP");
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI,"image/*");
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("scale",true);
        cropIntent.putExtra("output",albumURI);
        startActivityForResult(cropIntent,REQUEST_IMAGE_CROP);
    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage("????????? ????????? ?????????????????????. ????????? ???????????? ???????????? ?????? ????????? ?????? ??????????????????.")
                        .setNeutralButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestCode){
            case MY_PERMISSION_STORAGE:
                for(int i=0;i<grantResults.length;i++){
                    if(grantResults[i]<0){
                        Toast.makeText(RegistActivity.this, "?????? ????????? ????????? ????????? ?????????.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getData() != null) {
                        try {
                            File albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);
                            cropImage();
                        } catch (Exception e) {
                            Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                        }
                    }
                }
                break;

            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    galleryAddPic();
                    profile.setImageURI(albumURI);
                }
                break;
        }
    }


    private void uploadFile() {
        //???????????? ????????? ????????? ??????
        if (albumURI != null) {
            //????????? ?????? Dialog ?????????
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("????????????...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique??? ???????????? ?????????.
            final String name2=name.getText().toString().trim();
            String filename = name2 + ".png";
            //storage ????????? ?????? ???????????? ????????? ??????.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://project-2dfe0.appspot.com").child("profiles/" + filename);
            //???????????????...
            storageRef.putFile(albumURI)
                    //?????????
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            /*progressDialog.dismiss(); //????????? ?????? Dialog ?????? ??????*/
                            finish();
                            Toast.makeText(getApplicationContext(), "????????? ??????!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //?????????
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "????????? ??????!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //?????????
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //?????? ?????? ?????? ???????????? ????????? ????????????. ??? ??????????
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog??? ???????????? ???????????? ????????? ??????
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        //???????????? ???????????? email, password??? ????????????.
        final String emailText = email.getText().toString().trim();
        final String passwordText = password.getText().toString().trim();
        final String confirmText = passwordConfirm.getText().toString().trim();
        final String nameText = name.getText().toString().trim();
        final String birthText = birth.getText().toString().trim();
        final String profileText="gs://project-2dfe0.appspot.com"+"/profiles/"+nameText+".png";
        if (TextUtils.isEmpty(emailText)) {
            Toast.makeText(this, "???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(this, "????????? ???????????? ????????????", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(this, "??????????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(confirmText)) {
            Toast.makeText(this, "??????????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(nameText)) {
            Toast.makeText(this, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(birthText)) {
            Toast.makeText(this, "??????????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
        }

        //email??? password??? ????????? ???????????? ????????? ?????? ????????????.
        progressDialog.setMessage("??????????????????. ????????? ?????????...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(RegistActivity.this, "?????? ????????? ??????????????????!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            //???????????????
                            textviewMessage.setText("????????????\n - ?????? ????????? ?????????  \n -?????? ?????? 6?????? ?????? \n - ????????????");
                            Toast.makeText(RegistActivity.this, "?????? ??????!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                        finish();
                    }
                });

        User user=new User(nameText,emailText,birthText,passwordText,profileText);
        databaseReference.child(nameText).setValue(user);

    }

    @Override
    public void onClick(View v) {
        if (v==signupButton){
            registerUser();
            uploadFile();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        if (v==email_confirm){
            dbQuery();
        }
        if(v==profile){
            getAlbum();
        }
    }

    @Override

    protected void onDestroy() {

        Log.d(TAG, "called onDestroy");

        progressDialog.dismiss();

        super.onDestroy();

    }
}
