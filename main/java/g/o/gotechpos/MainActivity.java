package g.o.gotechpos;

//Deadline 25 May
//ToDo:reports order by employee
//Todo:Profits
//Todo:Tax
//Todo:Ads
//Todo:Dark mode
//Todo:Scanner speed
//ToDo: print reports
//ToDo. Allow sharing of reports, and stock via a file via bluetooth, wifi, internet



//in future release
//ToDo:Allow customers to order
//ToDo:Allow businesses to order
//ToDo:Graphs
//ToDo:Allow business to advertise
//ToDo:Video, Voice chat, Voice notes
//ToDo:Receipts
//ToDo:reccomendation System

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.ads.mediation.AbstractAdViewAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    //long notificationHelper=0L;

    ValueEventListener childEventListener;
    FirebaseDatabase database;
    DatabaseReference reference;
    ValueEventListener valueEventListener;

    DatabaseReference referenceToUserReport;
    DatabaseReference referenceToNotificationHelper, referenceToProfileCompany, referenceToCurrency;
    //DatabaseReference referenceToProfits;

    ValueEventListener profileCompanyListener;

    private String companyName=null, currency="";

    private List<String> fullProductNames=new ArrayList<String>();
    private List<String> fullProductPrices=new ArrayList<String>();
    private List<String> cost1=new ArrayList<>();
    private List<String> cost2=new ArrayList<>();
    private List<String> fullBarcodes=new ArrayList<String>();

    private InterstitialAd mInterstitialAd;
    private AdView ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        MobileAds.initialize(this);

        ad=(AdView)findViewById(R.id.ad);

        AdRequest adRequest=new AdRequest.Builder().addTestDevice("26880EC7D79E15BF2C65A06B4ABD3C7E").build();
        ad.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8853847321207681/1699356915");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        SharedPreferences sharedPreferences=getSharedPreferences("company ",MODE_PRIVATE);
        companyName=sharedPreferences.getString(FirebaseAuth.getInstance().getCurrentUser().getUid(),null);

        SharedPreferences preferences2=getSharedPreferences("currency ",MODE_PRIVATE);
        if(preferences2!=null) {
            currency =preferences2.getString(FirebaseAuth.getInstance().getCurrentUser().getUid(), "");
        }

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        database= FirebaseDatabase.getInstance();

        //referenceToProfits=database.getReference("ProductionDB/Profits/");
        referenceToProfileCompany=database.getReference("ProductionDB/Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/Company");


        profileCompanyListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null){

                }
                else if(dataSnapshot.getValue(String.class).equals("Request")){

                }
                else {
                    companyName=dataSnapshot.getValue(String.class);
                    referenceToCurrency=database.getReference("ProductionDB/Company/"+companyName+"/Currency/");

                    referenceToCurrency.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()==null){

                            }
                            else {
                                //Toast.makeText(getApplicationContext(),"got companyName",Toast.LENGTH_SHORT).show();
                                currency=dataSnapshot.getValue(String.class);
                                getSharedPreferences("currency ",MODE_PRIVATE).edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid(),currency).commit();
                                //remove listener
                                referenceToCurrency.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    getSharedPreferences("company ",MODE_PRIVATE).edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid(),companyName).commit();

                    referenceToNotificationHelper=database.getReference("ProductionDB/Company/"+companyName+"/notificationHelper/");
                    reference=database.getReference("ProductionDB/Company/"+companyName+"/Stock/");
                    referenceToUserReport=database.getReference("ProductionDB/Company/"+companyName+"/Reports/");
                    childEventListener=new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Toast.makeText(getApplicationContext(),"onChildedAdded",Toast.LENGTH_SHORT).show();
                            final long notificationHelper;

                            notificationHelper=dataSnapshot.getValue(Long.class);

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
                                                fos = openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"name_price_code.txt", MODE_PRIVATE);
                                                fos.close();
                                                ++count;
                                            }

                                            fos=openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"name_price_code.txt",MODE_APPEND);
                                            pw=new PrintWriter(fos);
                                        }
                                        catch (Exception exception){
                                            fos=null;
                                            pw=null;
                                        }
                                        ArrayList<String> item=dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<String>>(){});
                                        final String itemBarcode=dataSnapshot.getKey();
                                        final String itemName=item.get(0);
                                        final String itemCount=item.get(1);
                                        final String itemPrice=item.get(2);
                                        String itemCost1="Unknown";
                                        try{
                                            itemCost1=item.get(7);
                                        }
                                        catch (Exception e){

                                        }
                                        String itemCost2="Unknown";
                                        try{
                                            itemCost2=item.get(8);
                                        }
                                        catch(Exception e){

                                        }

                                        //Toast.makeText(MainActivity.this, itemName+" "+itemBarcode, Toast.LENGTH_SHORT).show();


                                        try {
                                            fos = openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"name_price_code.txt",MODE_APPEND);
                                            PrintWriter printWritter=new PrintWriter(fos);
                                            printWritter.println(itemName);
                                            printWritter.println(itemPrice);
                                            printWritter.println(itemBarcode);
                                            printWritter.println(itemCost1);
                                            printWritter.println(itemCost2);
                                            //printWritter.flush();
                                            //fos.flush();
                                            printWritter.close();
                                            fos.close();
                                            progressCount=progressCount+1;
                                            dialog.setProgress((int)(progressCount/total*100));
                                            if(progressCount==total){
                                                dialog.dismiss();

                                            }

                                            FileOutputStream fos2 = openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"notification.txt", MODE_PRIVATE);
                                            PrintWriter printWriter2=new PrintWriter(fos2);
                                            printWriter2.println(notificationHelper);
                                            printWriter2.close();
                                            fos2.close();
                                        }
                                        catch (Exception e){
                                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                                        }
                                    }


                                    reference.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };


                            //check value of notificationHelper stored on device
                            try {
                                FileInputStream fis = openFileInput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"notification.txt");
                                java.util.Scanner sc=new java.util.Scanner(fis);
                                if(sc.hasNextLine()) {
                                    long l = Long.parseLong(sc.nextLine());
                                    if (notificationHelper != l) {
                                        reference.addValueEventListener(valueEventListener);
                                        //onsuccess store notificationHelper
                                    }
                                }

                            }
                            catch (FileNotFoundException e){
                                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                try {
                                    FileOutputStream fos = openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"notification.txt", MODE_PRIVATE);
                                    PrintWriter printWriter=new PrintWriter(fos);
                                    printWriter.close();
                                    //printWriter.println(notificationHelper);//put this in update that updates prices as at this point prices might notr yet be updated.
                                    fos.close();
                                    reference.addValueEventListener(valueEventListener);

                                }
                                catch (Exception f){
                                    Toast.makeText(getApplicationContext(),f.toString(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };

                    referenceToNotificationHelper.addValueEventListener(childEventListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        referenceToProfileCompany.addValueEventListener(profileCompanyListener);

        subscribe2Topic(null);


        /*
         *ask to update reports that haven't been added to database
         */
        try {
            FileInputStream ff = openFileInput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"ReportFailsKeys.txt");
            java.util.Scanner snr = new java.util.Scanner(ff);
            //List<String> keys = new ArrayList<>();
            while (snr.hasNextLine()) {
                //update using file key leads to
                final String key=snr.nextLine();
                FileInputStream file=openFileInput(getIntent().getStringExtra("company")+"_"+key+".txt");
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
                if(!content.isEmpty()) {
                    referenceToUserReport.child(key).setValue(content).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                try {
                                    /*
                                     *read all report fails and put them in a list
                                     */
                                    File file = new File(key + ".txt");
                                    if (file.exists()) {
                                        file.delete();
                                        Toast.makeText(getApplicationContext(), "file deleted", Toast.LENGTH_SHORT).show();
                                    }

                                    FileInputStream ff = openFileInput(FirebaseAuth.getInstance().getCurrentUser().getUid() + "_" + companyName + "_" + "ReportFailsKeys.txt");
                                    java.util.Scanner snr = new java.util.Scanner(ff);
                                    List<String> keys = new ArrayList<>();
                                    while (snr.hasNextLine()) {
                                        keys.add(snr.nextLine());
                                    }
                                    if (keys.contains(key)) {
                                        keys.remove(key);

                                        FileOutputStream fos = openFileOutput(FirebaseAuth.getInstance().getCurrentUser().getUid() + "_" + companyName + "_" + "ReportFailsKeys.txt", MODE_PRIVATE);
                                        PrintWriter p = new PrintWriter(fos);
                                        for (String key : keys) {
                                            p.println(key);
                                        }

                                        p.close();
                                        Toast.makeText(getApplicationContext(), "key deleted", Toast.LENGTH_SHORT).show();
                                    }


                                } catch (Exception e) {

                                }
                            } else {

                            }
                        }
                    });
                }

            }



        }
        catch (Exception e){
        }

    }//onCreate


    public void onCardClick(View view){
        if(FirebaseAuth.getInstance().getCurrentUser().getUid()!=null) {
            switch (view.getId()){
                case R.id.card_profile:
                    startActivity(new Intent(this, Profile.class));
                    break;

                case R.id.card_scanner:
                    if(companyName==null){
                        Toast.makeText(getApplicationContext(),"Please set Company in Profile",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        fullProductNames.clear();
                        fullProductPrices.clear();
                        cost1.clear();
                        cost2.clear();
                        fullBarcodes.clear();

                        loadInfo();

                        Intent intent = new Intent(MainActivity.this, Scanner.class);
                        intent.putExtra("names", (Serializable) fullProductNames);
                        intent.putExtra("prices", (Serializable) fullProductPrices);
                        intent.putExtra("cost1", (Serializable) cost1);
                        intent.putExtra("cost2", (Serializable) cost2);
                        intent.putExtra("barcodes", (Serializable) fullBarcodes);
                        startActivity(intent.putExtra("company",companyName));
                    }
                    break;

                case R.id.card_stock:
                    if(companyName==null){
                        Toast.makeText(getApplicationContext(),"Please set Company in Profile",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        startActivity(new Intent(this, Stock.class)
                                .putExtra("company",companyName)
                                .putExtra("currency",currency));
                    }
                    break;

                case R.id.card_reports:
                    if(companyName==null){
                        Toast.makeText(getApplicationContext(),"Please set Company in Profile",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        startActivity(new Intent(this, Reports.class)
                                .putExtra("company",companyName)
                                .putExtra("currency",currency));
                    }
                    break;

                /*case R.id.card_subscribe:
                    startActivity(new Intent(this,Subscribe.class).putExtra("company",companyName));
                    break;*/

                case R.id.card_groupchat:
                    if(companyName!=null) {
                        startActivity(new Intent(this, Group.class).putExtra("company", companyName));
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Please set Company",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        else{
            Toast.makeText(MainActivity.this,"Please login", Toast.LENGTH_SHORT).show();
        }

    }//onCardClick

    @Override
    protected void onDestroy() {
        if(referenceToNotificationHelper!=null) {
            referenceToNotificationHelper.removeEventListener(childEventListener);
        }
        referenceToProfileCompany.removeEventListener(profileCompanyListener);
        //reference.removeEventListener(valueEventListener);
        super.onDestroy();
    }//onDestroy


    public void subscribe2Topic(final View view){
        FirebaseMessaging.getInstance().subscribeToTopic("update")         //was getting error when spaces were in selected group.
                // every topic in cloud functions shouldnt have spaces.
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "successfully subscribed";
                        if (!task.isSuccessful()) {
                            msg = "failed to subscribe";
                        }
                        if(view!=null) {
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void loadInfo(){
        try {
            FileInputStream fis = openFileInput(FirebaseAuth.getInstance().getCurrentUser().getUid()+"_"+companyName+"_"+"name_price_code.txt");
            java.util.Scanner sc = new java.util.Scanner(fis);
            while (sc.hasNextLine()) {
                String name = sc.nextLine();
                fullProductNames.add(name);
                String price = sc.nextLine();
                fullProductPrices.add(price);
                String barcode1 = sc.nextLine();
                fullBarcodes.add(barcode1);
                String cost1String=sc.nextLine();
                cost1.add(cost1String);
                String cost2String=sc.nextLine();
                cost2.add(cost2String);
            }
            fis.close();
        } catch (FileNotFoundException fNF) {
            reference.addValueEventListener(valueEventListener);
        } catch (IOException ioe) {

        }
    }

    public void showMenu(View view){
        PopupMenu popupMenu=new PopupMenu(MainActivity.this,view);
        popupMenu.getMenu().add("Support Us");
        if(!SplashScreenActivity.isNightModeEnabled()) {
            //popupMenu.getMenu().add("Dark mode");
        }
        else if(SplashScreenActivity.isNightModeEnabled()){
            //popupMenu.getMenu().add("Light mode");
        }

        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            popupMenu.getMenu().add("Logout");
        }
        else{
            popupMenu.getMenu().add("Login");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title=item.getTitle().toString();
                if(title.equals("Logout")){
                    FirebaseAuth.getInstance().signOut();
                    setResult(RESULT_OK);
                    finish();
                }
                else if(title.equals("Dark mode")){
                        setTheme(R.style.AppThemeDM);
                        SplashScreenActivity.setIsNightModeEnabled(true);
                }
                else if(title.equals("Light mode")){
                    setTheme(R.style.AppTheme);
                    SplashScreenActivity.setIsNightModeEnabled(false);
                }
                else if(title.equals("Support Us")){
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    }
                }
                else if(title.equals("Login")){
                    setResult(RESULT_OK);
                    finish();
                }
                return true;
            }
        });

        popupMenu.show();
    }//showMenu

    public void privacyPolicy(View view){
        startActivity(new Intent().setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://github.com/geraldPhiri/GotechDocs/blob/master/POS%20privacy%20Policy.md")));
    }

    public void termsAndConditions(View view){
        startActivity(new Intent().setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://github.com/geraldPhiri/GotechDocs/blob/master/POS%20terms%20and%20conditions.md")));
    }


}