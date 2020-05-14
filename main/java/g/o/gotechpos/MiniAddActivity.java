package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MiniAddActivity extends AppCompatActivity {
    List<String> categories=new ArrayList<>();
    String costPrice;
    String url="";
    String name;
    String price;
    String count1;
    String unit;


    String key;
    FirebaseDatabase database;
    DatabaseReference reference;
    EditText editTextCount, editTextCostPrice;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mini);
        key=getIntent().getStringExtra("key");
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Company/"+getIntent().getStringExtra("company")+"/Stock/");
        editTextCount=findViewById(R.id.product_count);
        editTextCostPrice=findViewById(R.id.product_cost_price);

        Intent intent=getIntent();
        editTextCount.setText(intent.getStringExtra("count2"));
        editTextCostPrice.setText(intent.getStringExtra("cost_price"));

        name=intent.getStringExtra("name");
        price=intent.getStringExtra("price");
        count1=intent.getStringExtra("count");
        unit=intent.getStringExtra("unit");
        categories=(List<String>)intent.getSerializableExtra("category");
        url=intent.getStringExtra("url");
        costPrice=intent.getStringExtra("cost_prices");


    }


    public void onSubmit(View view){
        reference.child(key).setValue(
                new ArrayList<>(Arrays.asList(
                        name,
                        count1,
                        price,
                        unit,
                        categories,
                        url,
                        UUID.randomUUID(),
                        costPrice,
                        editTextCostPrice.getText().toString(),
                        editTextCount.getText().toString()
                ))
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"cost price and count added",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"failed to add cost price and count",Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }
}
