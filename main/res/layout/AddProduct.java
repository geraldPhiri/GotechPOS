package g.o.gotechpos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class AddProduct extends AppCompatActivity {
    String url="";

    AutoCompleteTextView autoCompleteTextView;
    List<String> categories=new ArrayList<>();

    String[] units={"mL","L","g","Kg"};

    String costPrices;

    EditText editTextName,editTextPrice,editTextCount,editTextUnit;
    String barcode;
    Spinner spinner;

    FirebaseDatabase database;
    DatabaseReference reference;

    //ToDo: only allow admin to update stock with prices and barcodes
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);
        editTextName=findViewById(R.id.product_name);
        editTextPrice=findViewById(R.id.product_price);
        editTextCount=findViewById(R.id.product_count);
        editTextUnit=findViewById(R.id.product_unit);
        spinner=findViewById(R.id.spinner);
        ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,units);
        spinner.setAdapter(adapter);
        autoCompleteTextView=findViewById(R.id.categories);

        //load categories from file
        FileInputStream fis3=null;
        Scanner sc3=null;
        try {
            LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fis3=openFileInput("Category.txt");
            sc3=new java.util.Scanner(fis3);
            while(sc3.hasNextLine()){
                String name=sc3.nextLine();
                categories.add(name);
            }

        }
        catch (Exception exception){

        }
        finally {
            fis3=null;
            sc3=null;
        }

        Intent intent=getIntent();
        barcode=intent.getStringExtra("barcode");
        if(intent.getStringExtra("name")!=null){
            editTextName.setText(intent.getStringExtra("name"));
            editTextPrice.setText(intent.getStringExtra("price"));
            editTextCount.setText(intent.getStringExtra("count"));
            editTextUnit.setText(intent.getStringExtra("unit"));
            categories=(List<String>)intent.getSerializableExtra("category");
            url=intent.getStringExtra("url");
            costPrices=intent.getStringExtra("cost_prices");

        }
        autoCompleteTextView.setAdapter(new ArrayAdapter<String>(AddProduct.this,android.R.layout.simple_list_item_1,categories));

        database= FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Stock/");


    }


    public void onSubmit(View view){
        reference.child(barcode).setValue(new ArrayList<String>(Arrays.asList(
                editTextName.getText().toString(),
                editTextCount.getText().toString(),
                editTextPrice.getText().toString(),
                editTextUnit.getText().toString()+" "+spinner.getSelectedItem().toString(),
                autoCompleteTextView.getText().toString(),
                url,
                UUID.randomUUID().toString()/*,
                costPrice*/))
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Added to Stock",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Failed to Add to Stock",Toast.LENGTH_LONG).show();
                }

            }
        });
        finish();
    }


}