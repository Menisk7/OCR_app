package com.example.ocrapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    //image that you can rotate using btnleft/btnright
    ImageView img;

    //results of ocr will be here
    EditText recognizedtext;
    Button pickimage;
    Button btnrec;
    Button btnleft;
    Button btnright;
    int rotation;

    Bitmap myBitmap;


    int rotationposition=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        recognizedtext =findViewById(R.id.recognizedtext);
        img=findViewById(R.id.imageView);






        //btnrec
        btnrec=findViewById(R.id.btnrec);
        btnrec.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);


                //imgFile = new  File(filepath);

                //myBitmap = BitmapFactory.decodeFile(imgFile.getPath());

                //img.setImageBitmap(myBitmap);

                InputImage image = InputImage.fromBitmap(myBitmap, rotation);

                Task<Text> result =
                        recognizer.process(image)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {
                                        // Task completed successfully
                                        // ...
                                        //
                                        recognizedtext.setText(visionText.getText());



                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure( @NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                                if(e.getMessage().toString()=="Waiting for the text recognition module to be downloaded. Please wait.")
                                                {
                                                    recognizedtext.setClickable(true);
                                                    recognizedtext.setMovementMethod(LinkMovementMethod.getInstance());
                                                    String link = "<a href='https://play.google.com/store/apps/details?id=com.google.android.gms&referrer=utm_source%3DAndroidPIT%26utm_medium%3DAndroidPIT%26utm_campaign%3DAndroidPIT'> Update Google Play Services </a>";
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                        //only >=24 api version
                                                        recognizedtext.setText(Html.fromHtml(link, Html.FROM_HTML_MODE_COMPACT));
                                                    }else{
                                                        recognizedtext.setText(Html.fromHtml(link));

                                                    }

                                                }



                                            }
                                        });





            }
        });

        //btnleft
        btnleft=findViewById(R.id.btnleft);
        btnleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ++rotationposition;
                if(rotationposition>4)
                    rotationposition=1;
                rotation=nextrotation(rotationposition);



                img.setRotation(rotation);
            }
        });

        //btnright
        btnright=findViewById(R.id.btnright);
        btnright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                --rotationposition;
                if(rotationposition<1)
                    rotationposition=4;

                rotation=nextrotation(rotationposition);



                img.setRotation(rotation);

            }
        });
        //disable buttons
        btnleft.setEnabled(false);
        btnright.setEnabled(false);
        btnrec.setEnabled(false);

        //pick image btn
        pickimage=findViewById(R.id.pickimage);
        pickimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
        



    }


    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {

                @Override
                public void onActivityResult(Uri uri) {

                    if(uri==null)
                    {
                        //do nothing
                    }else {
                        btnleft.setEnabled(true);
                        btnright.setEnabled(true);
                        btnrec.setEnabled(true);
                        recognizedtext.setEnabled(true);
                        try {
                            myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        img.setImageBitmap(myBitmap);
                    }
                }
            });






    public int nextrotation(int number) {
        switch (number) {
            //0 degree
            case 1: return 0;
            //270 degree
            case 2: return 270;
            //180 degree
            case 3: return 180;
            //90 degree
            case 4: return 90;

            default: throw new IllegalArgumentException();

        }
    }



}


