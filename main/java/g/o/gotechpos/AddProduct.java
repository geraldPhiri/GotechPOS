package g.o.gotechpos;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class AddProduct extends AppCompatActivity {
    EditText editTextName,editTextPrice;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);
        editTextName=findViewById(R.id.product_name);
        editTextPrice=findViewById(R.id.product_price);

    }


    public void onSubmit(View view){
        try {
            FileOutputStream fos = openFileOutput(getIntent().getStringExtra("barcode") + ".txt", MODE_PRIVATE);
            PrintWriter pw=new PrintWriter(fos);
            pw.println(editTextName.getText().toString()+"\n"+editTextPrice.getText().toString());
            pw.close();
            fos.close();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.toString(),Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
