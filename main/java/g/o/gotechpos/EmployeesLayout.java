package g.o.gotechpos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class EmployeesLayout extends AppCompatActivity {

    LinearLayout linearLayout;
    FirebaseDatabase database;
    DatabaseReference reference;
    ChildEventListener childEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employees_layout);
        linearLayout=findViewById(R.id.rLayout);
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Users/");
        Query query=reference.orderByChild("Company").equalTo(getIntent().getStringExtra("company"));
        childEventListener=new ChildEventListener() {
            int count=0;

            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                FileOutputStream fos=null;
                PrintWriter pw=null;
                try {
                    if(count==0) {
                        fos = openFileOutput("Employees" + FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
                        fos.close();
                        linearLayout.removeAllViews();
                        ++count;
                    }

                    fos=openFileOutput("Employees"+ FirebaseAuth.getInstance().getCurrentUser().getUid(),MODE_APPEND);
                    pw=new PrintWriter(fos);
                }
                catch (Exception exception){
                    fos=null;
                    pw=null;
                }

                Map<String,String> nameAndImageUrl=dataSnapshot.getValue(new GenericTypeIndicator<Map<String,String>>(){});
                final String key=dataSnapshot.getKey();

                final String url=nameAndImageUrl.get("Uri");
                final String name=nameAndImageUrl.get("Name");
                String phone=nameAndImageUrl.get("Phone");
                String details=nameAndImageUrl.get("Details");

                LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View convertView=layoutInflater.inflate(R.layout.posts_list_view_item_1,null,true);

                ImageView poster=convertView.findViewById(R.id.poster_image);
                Glide.with(poster.getContext()).asBitmap().load(url).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            FileOutputStream fos2 = new FileOutputStream(new File("/data/data/g.o.gotechpos/files/" + dataSnapshot.getKey() + ".png"));
                            //Bitmap bitmap = ((BitmapDrawable) poster.getDrawable()).getBitmap();
                            resource.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                            fos2.flush();
                            fos2.close();
                        }
                        catch (Exception exception){
                            Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }).into(poster);

                //goto profile of user on click on their profile pic
                poster.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //startActivity(new Intent(EmployeesLayout.this,OthersProfile.class).putExtra("key",key));
                    }
                });

                TextView tname=convertView.findViewById(R.id.name);
                TextView taddress=convertView.findViewById(R.id.address);
                TextView tphone=convertView.findViewById(R.id.phone);
                tname.setText(name);
                taddress.setText(details);
                tphone.setText(phone);

                try{
                    //write to file
                    if(pw!=null){
                        pw.println(key);
                        pw.println(name);
                        pw.println(details);
                        pw.println(phone);
                        //pw.println(url);
                    }
                }
                catch (Exception exception){

                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //ToDo:make intent
                        setResult(RESULT_OK,new Intent().putExtra("uuid",key));
                        finish();
                    }
                });

                linearLayout.addView(convertView);

                //make sure file is saved
                try {
                    pw.close();
                    fos.close();
                }
                catch (Exception exception){

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addChildEventListener(childEventListener);

    }
}
