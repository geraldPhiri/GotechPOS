package g.o.gotechpos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onCardClick(View view){
        switch (view.getId()){
            case R.id.card_scanner:
                startActivity(new Intent(this, Scanner.class));
                break;

            case R.id.card_stock:
                startActivity(new Intent(this,Stock.class));
                break;
        }
    }

}
