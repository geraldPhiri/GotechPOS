package g.o.gotechpos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference reference;
    ValueEventListener valueEventListener;

    DatabaseReference referenceToUserReport;

    private List<String> fullProductNames=new ArrayList<String>();
    private List<String> fullProductPrices=new ArrayList<String>();
    private List<String> fullBarcodes=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        database= FirebaseDatabase.getInstance();
        referenceToUserReport=database.getReference("ProductionDB/Reports/");

        /*
         *ask to update reports that havent been added to database
         */
        try {
            FileInputStream ff = openFileInput("ReportFailsKeys.txt");
            java.util.Scanner snr = new java.util.Scanner(ff);
            //List<String> keys = new ArrayList<>();
            while (snr.hasNextLine()) {
                //update using file key leads to
                final String key=snr.nextLine();
                FileInputStream file=openFileInput(key+".txt");
                java.util.Scanner sc = new java.util.Scanner(file);
                List<List<String>> content = new ArrayList<>();//hold content key leads to
                while (sc.hasNextLine()) {
                    content.add(new ArrayList<String>(Arrays.asList(
                            sc.nextLine(),
                            sc.nextLine(),
                            sc.nextLine(),
                            sc.nextLine(),
                            sc.nextLine()
                    )));
                }
                //upload. delete key onsuccess
                referenceToUserReport.child(key).setValue(content).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            try {
                                /*
                                 *read all report fails and put them in a list
                                 */
                                File file=new File(key+".txt");
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
                                if(keys.contains(key)) {
                                    keys.remove(key);

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



        }
        catch (Exception e){

        }


        reference=database.getReference("ProductionDB/Stock/");

        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setMax(100);
                dialog.setMessage("loading names and prices");
                dialog.setTitle("Updating");
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.show();

                long total=dataSnapshot2.getChildrenCount();
                int count=0;
                int progressCount=0;
                for(DataSnapshot dataSnapshot:dataSnapshot2.getChildren()){
                    FileOutputStream fos=null;
                    PrintWriter pw=null;



                    try {
                        if(count==0) {
                            fos = openFileOutput("name_price_code.txt", MODE_PRIVATE);
                            fos.close();
                            ++count;
                        }


                        fos=openFileOutput("name_price_code.txt",MODE_APPEND);
                        pw=new PrintWriter(fos);
                    }
                    catch (Exception exception){
                        fos=null;
                        pw=null;
                    }
                    ArrayList<String> item=dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<String>>(){});
                    final String itemName=item.get(0);
                    final String itemCount=item.get(1);
                    final String itemPrice=item.get(2);
                    final String itemBarcode=dataSnapshot.getKey();
                    Toast.makeText(MainActivity.this, itemName+" "+itemBarcode, Toast.LENGTH_SHORT).show();


                    try {

                        fos = openFileOutput("name_price_code.txt",MODE_APPEND);
                        PrintWriter printWritter=new PrintWriter(fos);
                        printWritter.println(itemName);
                        printWritter.println(itemPrice);
                        printWritter.println(itemBarcode);
                        //printWritter.flush();
                        //fos.flush();
                        printWritter.close();
                        fos.close();
                        progressCount=progressCount+1;
                        dialog.setProgress((int)(progressCount/total*100));
                        if(progressCount==total){
                           // dialog.dismiss();
                        }
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };



        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){//if notification update file on device
            Toast.makeText(getApplicationContext(),"bundle",Toast.LENGTH_SHORT).show();
            reference.addValueEventListener(valueEventListener);

        }
        else {
            loadInfo();
        }

    }


    public void onCardClick(View view){
        switch (view.getId()){
            case R.id.card_profile:
                startActivity(new Intent(this, Profile.class));
                break;

            case R.id.card_scanner:
                if(fullProductNames.isEmpty()){
                    loadInfo();
                }
                Intent intent=new Intent(MainActivity.this,Scanner.class);
                intent.putExtra("names", (Serializable) fullProductNames);
                intent.putExtra("prices", (Serializable) fullProductPrices);
                intent.putExtra("barcodes",(Serializable)fullBarcodes);
                startActivity(intent);
                break;

            case R.id.card_stock:
                startActivity(new Intent(this,Stock.class));
                break;

            case R.id.card_reports:
                startActivity(new Intent(this,Reports.class));
                break;

            case R.id.card_subscribe:
                startActivity(new Intent(this,Subscribe.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        reference.removeEventListener(valueEventListener);
        super.onDestroy();
    }


    public void subscribe2Topic(View view){
        FirebaseMessaging.getInstance().subscribeToTopic("update")         //was getting error when spaces were in selected group.
                // every topic in cloud functions shouldnt have spaces.
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "successfully subscribed";
                        if (!task.isSuccessful()) {
                            msg = "failed to subscribe";
                        }
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void loadInfo(){
        try {
            FileInputStream fis = openFileInput("name_price_code.txt");
            java.util.Scanner sc = new java.util.Scanner(fis);
            while (sc.hasNextLine()) {
                String name = sc.nextLine();
                fullProductNames.add(name);
                String price = sc.nextLine();
                fullProductPrices.add(price);
                String barcode1 = sc.nextLine();
                fullBarcodes.add(barcode1);

            }
            fis.close();
        } catch (FileNotFoundException fNF) {
            reference.addValueEventListener(valueEventListener);
        } catch (IOException ioe) {

        }
    }
}