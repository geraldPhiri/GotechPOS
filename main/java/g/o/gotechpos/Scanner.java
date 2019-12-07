package g.o.gotechpos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Scanner extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference referenceToUserReport;

    private List<String> productName=new ArrayList<String>();
    private List<String> productPrice=new ArrayList<String>();
    private List<String> date=new ArrayList<String>();

    private float totalPrice=0F;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    FirebaseVisionBarcodeDetectorOptions options;
    TextView textViewPrice, textViewCustomersCash;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Stock/");
        referenceToUserReport=database.getReference("ProductionDB/Reports/");

        mediaPlayer = MediaPlayer.create(this, R.raw.beep_1);

        textViewPrice=findViewById(R.id.textview_price);
        textViewCustomersCash=findViewById(R.id.cash_from_customer);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_AZTEC)
                        .build();

        textureView = findViewById(R.id.view_finder);
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private void startCamera() {
        CameraX.unbindAll();
        Rational aspectRatio = new Rational (textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen

        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    //to update the surface texture we  have to destroy it first then re-add it
                    @Override
                    public void onUpdated(Preview.PreviewOutput output){
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });


        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        //=================================
        ImageAnalysisConfig config =
                new ImageAnalysisConfig.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(config);

        ImageAnalysis.Analyzer analyzer=new ImageAnalysis.Analyzer() {
            String holder="";

            @Override
            public void analyze(ImageProxy image, int rotationDegrees) {
                if (image == null || image.getImage() == null) {
                    return;
                }
                Image mediaImage = image.getImage();
                int rotation = (int)textureView.getRotation();
                FirebaseVisionImage frame =
                        FirebaseVisionImage.fromMediaImage(mediaImage,rotation);
                // Pass image to an ML Kit Vision API
                FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                        .getVisionBarcodeDetector();
                // Or, to specify the formats to recognize:
                //FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                //       .getVisionBarcodeDetector(options);
                Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(frame)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                // Task completed successfully
                                for (FirebaseVisionBarcode barcode: barcodes) {
                                    //Rect bounds = barcode.getBoundingBox();
                                    //Point[] corners = barcode.getCornerPoints();
                                    String rawValue = barcode.getRawValue();
                                    if(holder.equals(rawValue)){
                                        break;
                                    }
                                    holder=rawValue;
                                    //Toast.makeText(getApplicationContext(),rawValue,Toast.LENGTH_LONG).show();
                                    try {
                                        FileInputStream fis = openFileInput(rawValue+".txt");
                                        java.util.Scanner sc=new java.util.Scanner(fis);
                                        mediaPlayer.start(); // no need to call prepare(); create() does that for you
                                        String name=sc.nextLine();
                                        productName.add(name);
                                        String price=sc.nextLine();
                                        productPrice.add(price);
                                        date.add(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                                        totalPrice+=Integer.parseInt(price);
                                        textViewPrice.setText("k "+totalPrice);


                                    }
                                    catch(FileNotFoundException e){
                                        startActivity(new Intent(Scanner.this,AddProduct.class).putExtra("barcode",rawValue));
                                    }

                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });

            }
        };

        imageAnalysis.setAnalyzer(analyzer);

        CameraX.bindToLifecycle((LifecycleOwner) this, imageAnalysis, preview);
        //=================================

    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }


    public void calcChange(View view){
        float change=Float.parseFloat(textViewCustomersCash.getText().toString())-Float.parseFloat(textViewPrice.getText().toString().substring(2));
        //ToDo:show receipt
        //Toast.makeText(getApplicationContext(),""+change,Toast.LENGTH_LONG).show();
        Snackbar.make((RelativeLayout)findViewById(R.id.rl),"Change: k"+change,Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                
            }
        }).setActionTextColor(Color.parseColor("#00AAFF")).show();

        //update firebase database stock
        for(String productName:productName){
            Query query = reference.orderByChild("0").equalTo(productName);

            query.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    ArrayList<String> item=dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<String>>(){});
                    final String itemName=item.get(0);
                    final String itemCount=item.get(1);
                    //Toast.makeText(getApplicationContext(),itemName,Toast.LENGTH_LONG).show();
                    reference.child(dataSnapshot.getKey()).child("1").runTransaction(new Transaction.Handler() {

                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            //ToDo:write logic for fail to update stock
                            String count=mutableData.getValue(String.class);
                            mutableData.setValue(Integer.parseInt(count)-1+"");
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        //update report
        String cashierUID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<List<String>> item=new ArrayList<List<String>>();
        for(int i=0;i<productName.size();i++){
            List singleItem=new ArrayList();
            singleItem.add(productName.get(i));
            singleItem.add(productPrice.get(i));
            singleItem.add(date.get(i));
            singleItem.add(cashierUID);
            item.add(singleItem);
        }

        referenceToUserReport.push().setValue(item);
    }

}
