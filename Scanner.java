package g.o.gotechpos;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Scanner extends AppCompatActivity {
    String uuid;

    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference referenceToUserReport;

    //to store content in database
    private List<String> fullProductNames=new ArrayList<String>();
    private List<String> fullProductPrices=new ArrayList<String>();
    private List<String> fullBarcodes=new ArrayList<String>();

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

    BarcodeView barcodeView;
    // IntentIntegrator scanIntegrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Stock/");

        referenceToUserReport=database.getReference("ProductionDB/Reports/");


        mediaPlayer = MediaPlayer.create(this, R.raw.beep_1);

        barcodeView=findViewById(R.id.fragmentg);

        //fill full lists
        Intent intent=getIntent();
        fullProductNames=(List<String>) intent.getSerializableExtra("names");
        fullProductPrices=(List<String>) intent.getSerializableExtra("prices");
        fullBarcodes=(List<String>) intent.getSerializableExtra("barcodes");
        /*for(String s:fullProductNames){
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }*/



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
                        startActivity(new Intent(Scanner.this,AddProduct.class).putExtra("barcode",b));
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

    }


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
            Snackbar.make((RelativeLayout) findViewById(R.id.rl), "Change: k" + change, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).setActionTextColor(Color.parseColor("#00AAFF")).show();

            //update firebase database stock
            for (String productName : productName) {
                Query query = reference.orderByChild("0").equalTo(productName);

                query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        ArrayList<String> item = dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<String>>() {
                        });
                        final String itemName = item.get(0);
                        final String itemCount = item.get(1);
                        //Toast.makeText(getApplicationContext(),itemName,Toast.LENGTH_LONG).show();
                        reference.child(dataSnapshot.getKey()).child("1").runTransaction(new Transaction.Handler() {

                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                //ToDo:write logic for fail to update stock
                                String count = mutableData.getValue(String.class);
                                mutableData.setValue(Integer.parseInt(count) - 1 + "");
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                Toast.makeText(getApplicationContext(),b+"",Toast.LENGTH_LONG).show();
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
            String cashierUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final List<List<String>> item = new ArrayList<List<String>>();
            for (int i = 0; i < productName.size(); i++) {
                List singleItem = new ArrayList();
                singleItem.add(productName.get(i));
                singleItem.add(productPrice.get(i));
                singleItem.add(date.get(i));
                singleItem.add(timeStamp.get(i));
                singleItem.add(cashierUID);
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
             *
             */


            //ToDo:remove from file when Undo is done and check logic of code below
            uuid=null; //key
            try {
                FileOutputStream fos = openFileOutput("ReportFailsKeys.txt", MODE_APPEND);
                PrintWriter pw=new PrintWriter(fos);

                uuid= UUID.randomUUID().toString();


                pw.println(uuid);


                FileOutputStream fos2 = openFileOutput(uuid+".txt", MODE_APPEND);
                PrintWriter pw2=new PrintWriter(fos2);
                for(List<String> singleItem:item){
                    pw2.println(singleItem.get(0));//name
                    pw2.println(singleItem.get(1));//price
                    pw2.println(singleItem.get(2));//date
                    pw2.println(singleItem.get(3));//timestamp
                    pw2.println(singleItem.get(4));//cashier
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
                            File file=new File(uuid+".txt");
                            if(file.exists()) {
                                file.delete();
                                Toast.makeText(getApplicationContext(),"file deleted",Toast.LENGTH_SHORT).show();
                            }
                            /*FileInputStream fis = openFileInput(uuid+".txt");
                            java.util.Scanner sc=new java.util.Scanner(fis);
                            List<List<String>> list=new ArrayList<List<String>>();
                            for (int i=0;sc.hasNextLine();i++){
                                list.add(new ArrayList<String>(Arrays.asList(
                                        sc.nextLine(),
                                        sc.nextLine(),
                                        sc.nextLine(),
                                        sc.nextLine(),
                                        sc.nextLine()
                                )));
                            }
*/
                            /*
                             *delete entry from list which matches entry in item
                             */
                            /*for(List<String> l:list){
                                if(item.contains(l)){
                                    list.remove(list.indexOf(l));
                                    Toast.makeText(getApplicationContext(),"removed "+l.get(0),Toast.LENGTH_SHORT).show();
                                }
                            }*/

                            /*
                             *write new list to file
                             */
                            /*FileOutputStream fos = openFileOutput(uuid+".txt", MODE_PRIVATE);
                            PrintWriter p=new PrintWriter(fos);
                            p.writeObject(list);
                            oos.close();*/
                            FileInputStream ff=openFileInput("ReportFailsKeys.txt");
                            java.util.Scanner snr=new java.util.Scanner(ff);
                            List<String> keys=new ArrayList<>();
                            while(snr.hasNextLine()){
                                keys.add(snr.nextLine());
                            }
                            if(keys.contains(uuid)) {
                                keys.remove(uuid);

                                FileOutputStream fos = openFileOutput("ReportFailsKeys.txt", MODE_PRIVATE);
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
        }
        else{
            Toast.makeText(getApplicationContext(),"Make sure to scan atleast one product and to enter cash recieved ",Toast.LENGTH_LONG).show();
        }
    }//calcChange()


    public void undo(View view){
        Intent intent=new Intent(Scanner.this,Undo.class);
        intent.putExtra("names", (Serializable) productName);
        intent.putExtra("prices", (Serializable) productPrice);
        intent.putExtra("dates",(Serializable)date);
        startActivityForResult(intent,1);
    }

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

                textViewPrice.setText("k "+totalPrice);
            }
        }
        else{
            IntentResult intentResult=IntentIntegrator.parseActivityResult(requestCode,resultCode,intent);
            if(intentResult!=null){
                Toast.makeText(getApplicationContext(),intentResult.getContents(),Toast.LENGTH_LONG).show();
            }

            //scanIntegrator.initiateScan();
        }

    }

    @Override
    protected void onResume () {
        barcodeView.resume();
        super.onResume();

    }


    @Override
    protected void onPause(){
        super.onPause();
        barcodeView.pause();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }


}
