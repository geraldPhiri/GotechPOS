package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MiniAddActivity extends AppCompatActivity {
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
        reference=database.getReference("ProductionDB/Stock/");
        editTextCount=findViewById(R.id.product_count);
        editTextCostPrice=findViewById(R.id.product_cost_price);


    }

    public void onSubmit(View view){
        reference.child(key).child("8").setValue(editTextCostPrice+"-"+editTextCount);
    }
}
