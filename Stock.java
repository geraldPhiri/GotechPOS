package g.o.gotechpos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


//ToDo:add notifications to update device stock when stock firebase database is updated
public class Stock extends AppCompatActivity {
    LinearLayout linearLayout;

    FirebaseDatabase database;
    DatabaseReference reference;
    ChildEventListener childEventListener;

    private List<String> productName=new ArrayList();
    private List<String> productCount=new ArrayList();
    private ListView listViewStock;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock);
        linearLayout=findViewById(R.id.listview_stock);

        //load from device
        FileInputStream fis2=null;
        Scanner sc2=null;
        try {
            LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fis2=openFileInput("Stock.txt");
            sc2=new java.util.Scanner(fis2);
            while(sc2.hasNextLine()){
                View convertView=layoutInflater.inflate(R.layout.stock_item,null,true);
                TextView textViewName=convertView.findViewById(R.id.product_name);
                TextView textViewCount=convertView.findViewById(R.id.product_count);

                textViewName.setText(sc2.nextLine());
                textViewCount.setText(sc2.nextLine());

                linearLayout.addView(convertView);
            }

        }
        catch (Exception exception){

        }
        finally {
            fis2=null;
            sc2=null;
        }

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Stock/");

        childEventListener=new ChildEventListener() {
            int count=0;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FileOutputStream fos=null;
                PrintWriter pw=null;
                try {
                    if(count==0) {
                        fos = openFileOutput("Stock.txt", MODE_PRIVATE);
                        fos.close();
                        linearLayout.removeAllViews();
                        ++count;
                    }

                    fos=openFileOutput("Stock.txt",MODE_APPEND);
                    pw=new PrintWriter(fos);
                }
                catch (Exception exception){
                    fos=null;
                    pw=null;
                }
                ArrayList<String> item=dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<String>>(){});
                final String itemName=item.get(0);
                final String itemCount=item.get(1);
                productName.add(itemName);
                productCount.add(itemCount);

                LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View convertView=layoutInflater.inflate(R.layout.stock_item,null,true);
                TextView textViewName=convertView.findViewById(R.id.product_name);
                TextView textViewCount=convertView.findViewById(R.id.product_count);

                textViewName.setText(itemName);
                textViewCount.setText(itemCount);

                try{
                    //write to file
                    if(pw!=null){
                        pw.println(itemName);
                        pw.println(itemCount);
                        //pw.println(itemPrice);
                        //pw.println(barcode);
                    }
                }
                catch (Exception exception){

                }

                convertView.setTag(dataSnapshot.getKey());
                convertView.findViewById(R.id.edit_button3).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Stock.this,AddProduct.class).putExtra("barcode",convertView.getTag().toString()));
                    }
                });
                convertView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reference.child(convertView.getTag().toString()).setValue(null);
                    }
                });

                linearLayout.addView(convertView);

                //make sure file is saved
                try {
                    pw.close();
                    fos.close();
                }
                catch (Exception exception){

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
        reference.addChildEventListener(childEventListener);


    }


    @Override
    protected void onDestroy() {
        reference.removeEventListener(childEventListener);
        super.onDestroy();
    }


    public void addToStock(View view){
        startActivity(new Intent(this,AddStock.class));
    }

}
