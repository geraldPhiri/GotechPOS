package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class Menu extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        ListView listView=findViewById(R.id.list_view);
        Intent intent=getIntent();
        List<String> content=(List<String>) intent.getSerializableExtra("content");
        ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,content);
        listView.setAdapter(adapter);


    }
}
