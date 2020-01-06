package g.o.gotechpos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
    private List<String> productPrices=new ArrayList<>();
    private List<String> productUnit=new ArrayList<>();

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
                TextView textViewPrice=convertView.findViewById(R.id.product_price);
                TextView textViewUnit=convertView.findViewById(R.id.product_unit);

                textViewName.setText(sc2.nextLine());
                textViewCount.setText(sc2.nextLine());
                textViewPrice.setText("price: k"+sc2.nextLine());
                textViewUnit.setText(sc2.nextLine());

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
                final String itemPrice=item.get(2);

                //try catch block to ensure app doesn't crash if old apps edit database
                String itemUnit;
                try {
                    itemUnit = item.get(3);
                }
                catch(Exception e){
                    itemUnit="";

                }
                productName.add(itemName);
                productCount.add(itemCount);
                productUnit.add(itemUnit);
                productPrices.add(itemPrice);

                LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View convertView=layoutInflater.inflate(R.layout.stock_item,null,true);
                TextView textViewName=convertView.findViewById(R.id.product_name);
                TextView textViewCount=convertView.findViewById(R.id.product_count);
                TextView textViewPrice=convertView.findViewById(R.id.product_price);
                TextView textViewUnit=convertView.findViewById(R.id.product_unit);

                textViewName.setText(itemName);
                textViewCount.setText(itemCount);
                textViewPrice.setText("price: k"+itemPrice);
                textViewUnit.setText(itemUnit);

                try{
                    //write to file
                    if(pw!=null){
                        pw.println(itemName);
                        pw.println(itemCount);
                        pw.println(itemPrice);
                        pw.println(itemUnit);
                        //pw.println(barcode);
                    }
                }
                catch (Exception exception){

                }

                final String i=itemUnit;
                convertView.setTag(dataSnapshot.getKey());
                convertView.findViewById(R.id.edit_button3).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Stock.this,AddProduct.class)
                                .putExtra("barcode",convertView.getTag().toString())
                                .putExtra("name",itemName)
                                .putExtra("price",itemPrice)
                                .putExtra("count",itemCount)
                                .putExtra("unit",i)

                        );
                    }
                });
                convertView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reference.child(convertView.getTag().toString()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //from view from layout
                                int index=linearLayout.indexOfChild(convertView);
                                linearLayout.removeView(convertView);

                                /*
                                 *Read file contents as string.
                                 *Replace substring representing item with ""(empty string)
                                 *Save file
                                 */
                                try {
                                    List<List<String>> list=new ArrayList<>();
                                    FileInputStream file = openFileInput("Stock.txt");
                                    Scanner sc=new Scanner(file);
                                    while(sc.hasNextLine()){
                                        String sc1=sc.nextLine();
                                        String sc2=sc.nextLine();
                                        String sc3=sc.nextLine();
                                        String sc4=sc.nextLine();
                                        if(sc1.equals(convertView.findViewById(R.id.product_name)) &&
                                                sc2.equals(convertView.findViewById(R.id.product_price)) &&
                                                sc3.equals(convertView.findViewById(R.id.product_count)) /*&&
                                                sc4.equals(convertView.findViewById(R.id.product_unit))*/ )
                                        {
                                            //do nothing
                                        }
                                        else {
                                            list.add(new ArrayList<String>(Arrays.asList(
                                                    sc1,
                                                    sc2,
                                                    sc3,
                                                    sc4
                                            )));
                                        }
                                    }
                                    FileOutputStream fileOutputStream=openFileOutput("Stock.txt",MODE_PRIVATE);
                                    fileOutputStream.close();
                                    fileOutputStream=null;
                                    fileOutputStream=openFileOutput("Stock.txt",MODE_APPEND);
                                    PrintWriter pw=new PrintWriter(fileOutputStream);
                                    for(List<String> item:list){
                                        pw.println(item.get(0));
                                        pw.println(item.get(1));
                                        pw.println(item.get(2));
                                        pw.println(item.get(3));
                                    }
                                    pw.close();
                                    fileOutputStream.close();


                                    sc.close();
                                    file.close();
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
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
