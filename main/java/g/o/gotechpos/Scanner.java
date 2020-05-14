package g.o.gotechpos;

import android.hardware.Camera;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.os.Bundle;

import android.view.TextureView;
import android.view.View;

import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraParametersCallback;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Scanner extends AppCompatActivity {
    String uuid,uuid2;

    //DatabaseReference referenceToProfits;
    FirebaseDatabase database;
    //DatabaseReference reference;
    DatabaseReference referenceToUserReport;

    //to store content in database
    private List<String> fullProductNames=new ArrayList<String>();
    private List<String> fullProductPrices=new ArrayList<String>();
    private List<String> fullBarcodes=new ArrayList<String>();
    private List<String> cost1=new ArrayList<>();
    private List<String> cost2=new ArrayList<>();

    //to store content read by scanner
    private List<String> productName=new ArrayList<String>();
    private List<String> productPrice=new ArrayList<String>();
    private List<String> date=new ArrayList<String>();

    private List<String> timeStamp=new ArrayList<String>();

    private float totalPrice=0F;


    private final int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    TextView textViewPrice, textViewCustomersCash;
    MediaPlayer mediaPlayer;

    DecoratedBarcodeView barcodeView;
    //IntentIntegrator scanIntegrator;

    /*public String getChosenCount(String productNAme){
        File file = new File(productNAme+"_useCount2.txt");
        if(file.exists()){
            return "9";//for use count2
        }
        return "1";//for use count1
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        database=FirebaseDatabase.getInstance();
        //reference=database.getReference("ProductionDB/Company/"+getIntent().getStringExtra("company")+"/Stock/");
        referenceToUserReport=database.getReference("ProductionDB/Company/"+getIntent().getStringExtra("company")+"/Reports/");
        //referenceToProfits=database.getReference("ProductionDB/Company/"+getIntent().getStringExtra("company")+"/Profits/");


        mediaPlayer = MediaPlayer.create(this, R.raw.beep_1);

        barcodeView=findViewById(R.id.fragmentg);
        barcodeView.setFocusedByDefault(true);

        /*CameraSettings cameraSettings=new CameraSettings();
        CameraSettings.FocusMode focusMode=cameraSettings.getFocusMode();
        cameraSettings.setFocusMode(CameraSettings.FocusMode.CONTINUOUS);*/
        /*barcodeView.changeCameraParameters(new CameraParametersCallback() {
            @Override
            public Camera.Parameters changeCameraParameters(Camera.Parameters parameters) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                return parameters;
            }
        });*/
        //barcodeView2.setTorch(true);

        //fill full lists
        Intent intent=getIntent();
        fullProductNames=(List<String>) intent.getSerializableExtra("names");
        fullProductPrices=(List<String>) intent.getSerializableExtra("prices");
        fullBarcodes=(List<String>) intent.getSerializableExtra("barcodes");
        cost1=(List<String>) intent.getSerializableExtra("cost1");
        cost2=(List<String>) intent.getSerializableExtra("cost2");


        textViewPrice=findViewById(R.id.textview_price);
        textViewCustomersCash=findViewById(R.id.cash_from_customer);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        textureView = findViewById(R.id.view_finder);
        if(allPermissionsGranted()){
            //startCamera(); //start camera if permission has been granted by user

            //scanIntegrator = new IntentIntegrator(Scanner.this);
            barcodeView.resume();
            barcodeView.decodeContinuous(new BarcodeCallback() {
                @Override
                public void barcodeResult(BarcodeResult barcodeResult) {
                    barcodeView.pause();
                    String b=barcodeResult.getText();
                    int index=fullBarcodes.indexOf(b);
                    if(index==-1){
                        String company=getIntent().getStringExtra("company");
                        startActivity(new Intent(Scanner.this,AddProduct.class).putExtra("barcode",b)
                                .putExtra("company",company));
                    }
                    else {
                        mediaPlayer.start(); // no need to call prepare(); create() does that for you
                        timeStamp.add(""+barcodeResult.getTimestamp());
                        String name = fullProductNames.get(index);
                        productName.add(name);
                        String price = fullProductPrices.get(index);
                        productPrice.add(price);
                        date.add(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                        totalPrice += Integer.parseInt(price);
                        textViewPrice.setText("k " + totalPrice);
                    }
                    barcodeView.resume();
                }

                @Override
                public void possibleResultPoints(List<ResultPoint> list) {

                }
            });
            //scanIntegrator.setPrompt("Scan a Barcode");
            /*scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(false);
            scanIntegrator.setBarcodeImageEnabled(true);
            scanIntegrator.initiateScan();
*/
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }//onCreate


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                //startCamera();
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
        if(!textViewCustomersCash.getText().toString().isEmpty() & totalPrice!=0F) {
            float change = Float.parseFloat(textViewCustomersCash.getText().toString()) - Float.parseFloat(textViewPrice.getText().toString().substring(2));
            //Toast.makeText(getApplicationContext(),""+change,Toast.LENGTH_LONG).show();
            Snackbar.make((RelativeLayout) findViewById(R.id.rl), "Change: " + change, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).setActionTextColor(Color.parseColor("#00AAFF")).show();


            //update report
            String cashierUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final List<List<String>> item = new ArrayList<List<String>>();
            for (int i = 0; i < productName.size(); i++) {
                List singleItem = new ArrayList();
                singleItem.add(productName.get(i));
                singleItem.add(productPrice.get(i));
                singleItem.add(date.get(i));
                singleItem.add(timeStamp.get(i));
                singleItem.add(cashierUID);
                singleItem.add(fullBarcodes.get(fullProductNames.indexOf(productName.get(i))));//ToDo:check logic

                item.add(singleItem);
            }

            /**
             * 1. Its possible that an update might fail as their might be no network coverage.
             * Thus if a fail occurs store the update in a file.
             * Then update at a later time when coverage is available or
             * notify user to press a button to make the needed update.
             * 2. Success might be called automatically after a fail thus,
             * thus it might be needed to delete update entry from file if it exists in the file on every success
             *
             *
             */


            //ToDo:check logic of code below
            uuid=null; //key
            try {
                FileOutputStream fos = openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+getIntent().getStringExtra("company")+"_"+"ReportFailsKeys.txt", MODE_APPEND);
                PrintWriter pw=new PrintWriter(fos);

                uuid=UUID.randomUUID().toString();

                pw.println(uuid);


                FileOutputStream fos2 = openFileOutput(getIntent().getStringExtra("company")+"_"+uuid+".txt", MODE_APPEND);
                PrintWriter pw2=new PrintWriter(fos2);
                for(List<String> singleItem:item){
                    pw2.println(singleItem.get(0));//name
                    pw2.println(singleItem.get(1));//price
                    pw2.println(singleItem.get(2));//date
                    pw2.println(singleItem.get(3));//timestamp
                    pw2.println(singleItem.get(4));//cashier
                    pw2.println(singleItem.get(5));//barcode//ToDo: check if can be null
                }
                pw.close();
                pw2.close();

                Toast.makeText(getApplicationContext(),"key saved on device",Toast.LENGTH_SHORT).show();

            }
            catch(Exception e){
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }


            referenceToUserReport.child(uuid).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        try {
                            /*
                             *read all report fails and put them in a list
                             */
                            Toast.makeText(getApplicationContext(),"file "+uuid,Toast.LENGTH_SHORT).show();
                            File file=new File("/data/data/g.o.gotechpos/files/"+getIntent().getStringExtra("company")+"/"+uuid+".txt");
                            if(file.exists()) {
                                file.delete();
                                Toast.makeText(getApplicationContext(),"file deleted",Toast.LENGTH_SHORT).show();
                            }

                            FileInputStream ff=openFileInput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+getIntent().getStringExtra("company")+"_"+"ReportFailsKeys.txt");
                            java.util.Scanner snr=new java.util.Scanner(ff);
                            List<String> keys=new ArrayList<>();
                            while(snr.hasNextLine()){
                                keys.add(snr.nextLine());
                            }
                            if(keys.contains(uuid)) {
                                keys.remove(uuid);
                                Toast.makeText(getApplicationContext(),"key "+uuid,Toast.LENGTH_SHORT).show();

                                FileOutputStream fos = openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+getIntent().getStringExtra("company")+"_"+"ReportFailsKeys.txt", MODE_PRIVATE);
                                PrintWriter p = new PrintWriter(fos);
                                for (String key : keys) {
                                    p.println(key);
                                }

                                p.close();
                                Toast.makeText(getApplicationContext(),"key deleted",Toast.LENGTH_SHORT).show();
                            }


                        }
                        catch (Exception e){

                        }
                    }
                    else{

                    }
                }
            });


            //clear current state
            textViewPrice.setText("0.0");
            textViewCustomersCash.setText("");
            productName.clear();
            productPrice.clear();
            date.clear();
            timeStamp.clear();
            totalPrice=0F;

        }
        else{
            Toast.makeText(getApplicationContext(),"Make sure to scan atleast one product and to enter cash recieved ",Toast.LENGTH_LONG).show();
        }
    }//calcChange()


    public void flashLight(View view){
        if(view.getTag().equals("isOn")) {
            barcodeView.setTorchOff();
            view.setTag("isOff");
        }
        else if(view.getTag().equals("isOff")){
            barcodeView.setTorchOn();
            view.setTag("isOn");
        }
    }//flashLight


    public void undo(View view){
        Intent intent=new Intent(Scanner.this,Undo.class);
        intent.putExtra("names", (Serializable) productName);
        intent.putExtra("prices", (Serializable) productPrice);
        intent.putExtra("dates",(Serializable)date);
        startActivityForResult(intent,1);
    }//undo


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode==1){
            if(resultCode==RESULT_OK){

                productName=(List<String>) intent.getSerializableExtra("names");
                productPrice=(List<String>) intent.getSerializableExtra("prices");
                date=(List<String>) intent.getSerializableExtra("dates");
                //subtract what was deleted from the total
                List<String> removed=(List<String>) intent.getSerializableExtra("removed");

                for(String remove:removed){
                    totalPrice-=Float.parseFloat(remove);
                }

                textViewPrice.setText(" "+totalPrice);
            }
        }
        else{
            IntentResult intentResult=IntentIntegrator.parseActivityResult(requestCode,resultCode,intent);
            if(intentResult!=null){
                Toast.makeText(getApplicationContext(),intentResult.getContents(),Toast.LENGTH_LONG).show();
            }

            //scanIntegrator.initiateScan();
        }

    }//onActivityResult


    @Override
    protected void onResume () {
        super.onResume();
        barcodeView.resume();
    }//onResume


    @Override
    protected void onPause(){
        super.onPause();
        barcodeView.pause();

    }//onPause


    @Override
    public void onDestroy(){
        super.onDestroy();
    }//onDestroy


}//Scanner
