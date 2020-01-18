package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddStock extends AppCompatActivity {
    String costPrice="";
    AutoCompleteTextView autoCompleteTextView;
    String[] units={"mL","L","g","Kg"};

    List<String> categories;
    EditText editTextName,editTextCount,editTextPrice,editTextBarcode,editTextUnit,editTextCostPrice;

    FirebaseDatabase database;
    DatabaseReference reference;

    Spinner spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_stock);


        Intent intent=getIntent();
        categories=(List<String>) intent.getSerializableExtra("category");

        autoCompleteTextView=findViewById(R.id.categories);
        autoCompleteTextView.setAdapter(new ArrayAdapter<String>(AddStock.this,android.R.layout.simple_list_item_1,categories));


        editTextName=findViewById(R.id.product_name);
        editTextPrice=findViewById(R.id.product_price);
        editTextCount=findViewById(R.id.product_count);
        editTextBarcode=findViewById(R.id.product_barcode);
        editTextUnit=findViewById(R.id.product_unit);
        editTextCostPrice=findViewById(R.id.product_cost_price);
        spinner=findViewById(R.id.spinner);

        ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,units);
        spinner.setAdapter(adapter);

        database= FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Stock/");

    }


    public void onSubmit(View view){
        String name=editTextName.getText().toString();
        String count=editTextCount.getText().toString();
        String price=editTextPrice.getText().toString();
        String barcode=editTextBarcode.getText().toString();
        String unit=editTextUnit.getText().toString()+" "+spinner.getSelectedItem().toString();
        String category=autoCompleteTextView.getText().toString();
        if(!editTextCostPrice.getText().toString().equals("")){
            costPrice=editTextCostPrice.getText().toString();
        }

        /*try {
            FileOutputStream fos = openFileOutput(getIntent().getStringExtra("barcode") + ".txt", MODE_PRIVATE);
            PrintWriter pw=new PrintWriter(fos);
            pw.println(editTextName.getText().toString()+"\n"+editTextCount.getText().toString());
            pw.close();
            fos.close();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.toString(),Toast.LENGTH_LONG).show();
        }*/

        reference.child(barcode)
                .setValue(new ArrayList<String>(
                        Arrays.asList(name,count,price,unit,category,"",UUID.randomUUID().toString(), costPrice)));


        finish();
    }


}
