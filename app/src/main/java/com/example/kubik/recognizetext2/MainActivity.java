package com.example.kubik.recognizetext2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button btn1, btn2;
    TextView tv;
    private File photo;

    private static int REQUEST_PHOTO = 532;
    private static String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        tv = findViewById(R.id.textView);
        btn1 = findViewById(R.id.button);
        btn2 = findViewById(R.id.button2);

        photo = getPhotoFile();

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final Uri uri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseApp.initializeApp(getApplicationContext());
                Bitmap bitmap = BitmapFactory.decodeFile(photo.getPath());
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();

                detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                String text = "";
                                for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()){
                                    text += block.getText();
                                }
                                tv.setText(text);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: ");
                            }
                        });


            }
        });

        updateImgView();
    }

    private String getPhotoName(){
        return "IMG_" + SystemClock.currentThreadTimeMillis() + ".jpg";
    }

    private File getPhotoFile(){
        File extFileDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(extFileDir, getPhotoName());
    }

    private Bitmap getScaledBitmap(String path, int destWidth, int destHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(path, options);
    }

    private Bitmap getScaledBitmap(String path){
        Point size = new Point();

        getWindowManager().getDefaultDisplay()
                .getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    private void updateImgView() {
        if (photo == null || !photo.exists()) {
            imageView.setImageDrawable(null);
        } else {
            Bitmap bitmap = getScaledBitmap(photo.getPath());
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PHOTO) {
                updateImgView();
            }
        }
    }

}
