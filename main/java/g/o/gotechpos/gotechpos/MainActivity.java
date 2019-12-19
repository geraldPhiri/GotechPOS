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
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//ToDo:only read from stock if file doesnt exis. Only enter Scanner if file exists
public class MainActivity extends AppCompatActivity {
  FirebaseDatabase database;
  DatabaseReference reference;
  ChildEventListener childEventListener;

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
    reference=database.getReference("ProductionDB/Stock/");

    childEventListener=new ChildEventListener() {
        //LayoutInflater li=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        int count=0;
      @Override
      public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
          FileOutputStream fos=null;
          PrintWriter pw=null;
          //View v=li.inflate(R.layout.progress,null,true);
          /*CircleProgress circleProgress=new CircleProgress(MainActivity.this);
          circleProgress.setMax(100);
          circleProgress.setProgress();*/
          //ProgressDialog dialog = new ProgressDialog(MainActivity.this);
          //dialog.show();

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

        //ToDo:show dialog of pecentage of children added to file. allow user to continue even if they are not all saved

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

        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }


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
    };

    Bundle bundle=getIntent().getExtras();
    if(bundle!=null){
        reference.addChildEventListener(childEventListener);
    }
    else {
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
            reference.addChildEventListener(childEventListener);
        } catch (IOException ioe) {

        }
    }

  }


  public void onCardClick(View view){
    switch (view.getId()){
      case R.id.card_profile:
        startActivity(new Intent(this, Profile.class));
        break;

      case R.id.card_scanner:
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
    reference.removeEventListener(childEventListener);
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

}
