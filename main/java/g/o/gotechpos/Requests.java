package g.o.gotechpos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;

public class Requests extends AppCompatActivity {

    LinearLayout linearLayout;
    FirebaseDatabase database;
    DatabaseReference reference, referenceToRequests, referenceToEmployees, referenceToOwners;
    ChildEventListener childEventListener;
    private AdView ad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requests);

        MobileAds.initialize(this);

        ad=(AdView)findViewById(R.id.ad);

        AdRequest adRequest=new AdRequest.Builder().addTestDevice("26880EC7D79E15BF2C65A06B4ABD3C7E").build();
        ad.loadAd(adRequest);

        linearLayout=findViewById(R.id.rLayout);
        database=FirebaseDatabase.getInstance();

        referenceToOwners=database.getReference("ProductionDB/Company/" + getIntent().getStringExtra("company")+ "/Owners");
        referenceToEmployees=database.getReference("ProductionDB/Company/" + getIntent().getStringExtra("company")+ "/Employee");
        referenceToRequests=database.getReference("ProductionDB/Company/" + getIntent().getStringExtra("company")+ "/Request");
        reference=database.getReference("ProductionDB/Users/");
        Query query=reference.orderByChild("Company").equalTo("Request_"+getIntent().getStringExtra("company"));

        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //getView to inflate
                LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View convertView=layoutInflater.inflate(R.layout.posts_list_view_item_1,null,true);

                final String uid=dataSnapshot.getKey();

                if(uid!=null){
                    convertView.setTag(uid);
                    //get user info
                    database.getReference("ProductionDB/Users/"+uid+"/Phone").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final TextView tphone=convertView.findViewById(R.id.phone);
                            String phone=dataSnapshot.getValue(String.class);
                            tphone.setText(phone);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    database.getReference("ProductionDB/Users/"+uid+"/Name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            final TextView tname=convertView.findViewById(R.id.name);
                            String name=dataSnapshot.getValue(String.class);
                            if(name!=null) {
                                tname.setText(name);
                                convertView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new AlertDialog.Builder(Requests.this).setTitle("Employee or Admin")
                                                .setMessage("Add user as Employee or Admin")
                                                .setPositiveButton("Employee", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        referenceToEmployees.child(uid).setValue(uid);
                                                        linearLayout.removeView(convertView);
                                                    }
                                                })
                                                .setNegativeButton("Admin", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        referenceToOwners.child(uid).setValue(uid);
                                                        linearLayout.removeView(convertView);
                                                    }
                                                })
                                                .create()
                                                .show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    database.getReference("ProductionDB/Users/"+uid+"/Uri").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final ImageView poster=convertView.findViewById(R.id.poster_image);
                            String url=dataSnapshot.getValue(String.class);
                            if(url!=null) {
                                Glide.with(poster.getContext()).asBitmap().load(url).listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).into(poster);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                linearLayout.addView(convertView);
            }//onChildAdded

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

    @Override
    protected void onDestroy() {
        reference.removeEventListener(childEventListener);
        super.onDestroy();
    }
}
