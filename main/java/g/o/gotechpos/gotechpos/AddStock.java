package g.o.gotechpos;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class AddStock extends AppCompatActivity {
    EditText editTextName,editTextCount,editTextPrice,editTextBarcode;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_stock);
        editTextName=findViewById(R.id.product_name);
        editTextPrice=findViewById(R.id.product_price);
        editTextCount=findViewById(R.id.product_count);
        editTextBarcode=findViewById(R.id.product_barcode);

        database= FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Stock/");

    }


    public void onSubmit(View view){
        String name=editTextName.getText().toString();
        String count=editTextCount.getText().toString();
        String price=editTextPrice.getText().toString();
        String barcode=editTextBarcode.getText().toString();

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

        reference.child(barcode).setValue(new ArrayList<String>(Arrays.asList(name,count,price)));


        finish();
    }


}
