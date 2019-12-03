package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//import com.facebook.CallbackManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
  //  CallbackManager callbackManager;
    FirebaseAuth fa;
    EditText email=null;
    EditText password=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
        email=findViewById(R.id.l_email);
        password=findViewById(R.id.l_password);
        fa=FirebaseAuth.getInstance();
        //callbackManager = CallbackManager.Factory.create();

    }

    public void loginClicked(View view){
        final String emailString=email.getText().toString();
        if (!emailString.trim().isEmpty() && !password.getText().toString().trim().isEmpty()) {
            //if some requirements met create a signed in user
            fa.signInWithEmailAndPassword(emailString, password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        getPreferences(0).edit().putString("email",emailString).commit();
                        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, MainActivity.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),"please fill in email and password(8 characters long)",Toast.LENGTH_SHORT).show();
        }
    }

    public void createAccount(View view){
        startActivity(new Intent(Login.this,Registration.class));

    }
}
