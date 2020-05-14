package g.o.gotechpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;

public class ChoosePictureActivity extends Activity {
    FirebaseDatabase database;
    ChildEventListener childListener;
    DatabaseReference reference;
    LinearLayout productsGridLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        database=FirebaseDatabase.getInstance();
        productsGridLayout=findViewById(R.id.layout_products);

        reference=database.getReference("ProductionDB/Products/");
        childListener=new ChildEventListener() {
            LinearLayout subLinearLayout;
            int i=0;
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                final String url=dataSnapshot.getValue(String.class);
                final ImageView productImageView=new ImageView(ChoosePictureActivity.this);
                productImageView.setTag(url);
                productImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(ChoosePictureActivity.this)
                                .setTitle("Product Picture Confirmation")
                                .setMessage("Set as picture")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        database.getReference("ProductionDB/Company/"+getIntent().getStringExtra("company")+"/Stock").child(getIntent().getStringExtra("tag")).child("5").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getApplicationContext(),"image set",Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Toast.makeText(getApplicationContext(),"image not set",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create()
                                .show();
                    }
                });
                Glide.with(productImageView.getContext()).asBitmap().load(url).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            FileOutputStream fos2 = new FileOutputStream(new File("/data/data/g.o.gotechpos/files/" +getIntent().getStringExtra("company")+dataSnapshot.getKey() + ".png"));
                            //Bitmap bitmap = ((BitmapDrawable) poster.getDrawable()).getBitmap();
                            resource.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                            fos2.flush();
                            fos2.close();
                        } catch (Exception exception) {
                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }).into(productImageView);

                if(i%3==0){
                    subLinearLayout=new LinearLayout(ChoosePictureActivity.this);
                    subLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    subLinearLayout.setWeightSum(3f);
                    subLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200));
                    productsGridLayout.addView(subLinearLayout);
                }

                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.0f
                );
                productImageView.setLayoutParams(param);
                subLinearLayout.addView(productImageView);
                i=i+1;

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
        reference.addChildEventListener(childListener);

    }//onCreate


    @Override
    protected void onDestroy() {
        reference.removeEventListener(childListener);
        super.onDestroy();
    }//onDestroy


}//ChoosePictureActivity
