package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AddCategory extends AppCompatActivity {
  EditText editTextName;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.add_category);
    editTextName=findViewById(R.id.name);

  }

  public void sendCategoryBack(View view){
    Intent intent=new Intent();
    intent.putExtra("name",editTextName.getText().toString());
    setResult(RESULT_OK,intent);
    finish();
  }

}