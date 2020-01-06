package g.o.gotechpos;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

/**
 * This class updates the ListView in activity_main with
 * images and names
 * @author Gerald Phiri
 */
public class Group extends AppCompatActivity {
    String startAtDate=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").
            format(new Date());
    String key=null;

    String selectedGroup=null;
    LinearLayout relativeLayout;
    FirebaseDatabase database;
    DatabaseReference reference;
    ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group);
        relativeLayout=findViewById(R.id.rLayout);
        database=FirebaseDatabase.getInstance();
        selectedGroup=getIntent().getStringExtra("group_selected_by_user");
        reference=database.getReference("ProductionDB/GroupPosts/"+selectedGroup); //reference posts in database based on group selected by used

        /*
         *load post related information from device storage.
         */
        try {
            FileInputStream is = openFileInput( selectedGroup+ FirebaseAuth.getInstance().getCurrentUser().getUid());//uid so each users posts file is unique
            Scanner sc=new java.util.Scanner(is);
            while(sc.hasNextLine()) {
                key=sc.nextLine();
                final String uid=sc.nextLine();
                final String name=sc.nextLine();
                String date=sc.nextLine();
                //startAtDate=date;
                String post=sc.nextLine();
                final String url=sc.nextLine();

                LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View convertView=layoutInflater.inflate(R.layout.posts_list_view_item_1,null,true);

                ImageView poster=convertView.findViewById(R.id.poster_image);
                TextView tname=convertView.findViewById(R.id.name);
                TextView tdate=convertView.findViewById(R.id.date);
                TextView tpost=convertView.findViewById(R.id.post);
                try {
                    Glide.with(poster.getContext()).load(new File("/data/data/g.o.gotechpos/files/" + uid + ".png"))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).into(poster);
                }
                catch(Exception exception){

                }
                tname.setText(name);
                tdate.setText(date);
                tpost.setText(post);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*startActivity(new Intent(Group.this,OthersProfile.class).
                                putExtra("uid",uid).
                                putExtra("profile_photo",url).
                                putExtra("name",name));*/
                    }
                });

                relativeLayout.addView(convertView);

            }
            /*String sToReplace=startAtDate.substring(startAtDate.lastIndexOf(":")+1);
            startAtDate=startAtDate.replace(sToReplace,Integer.parseInt(sToReplace)+1+"");
            */

        }
        catch(Exception exception){

        }//catch()

        /*
         *update profile pictures loaded from device with those in firebase.
         */


        /*  LinkedHashSet<String> uidSet=new LinkedHashSet<String>(uidList); //will be used to retrieve and name profile photos.
         *
         */

        //Retrieve posts and posts' information from firebase
        childEventListener=new ChildEventListener() {
            // int count=0;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FileOutputStream fos=null;
                PrintWriter pw=null;
                try {
                /*    if(count==0) {
                        fos = openFileOutput(selectedGroup + FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
                        fos.close();
                        relativeLayout.removeAllViews();
                        ++count;
                    }
                  */
                    fos=openFileOutput(selectedGroup+FirebaseAuth.getInstance().getCurrentUser().getUid(),MODE_APPEND);
                    pw=new PrintWriter(fos);
                }
                catch (Exception exception){
                    fos=null;
                    pw=null;
                }

                ArrayList<String> nameAndImageUrl=dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<String>>(){});
                String key=dataSnapshot.getKey();

                final String uid=nameAndImageUrl.get(0);
                final String name=nameAndImageUrl.get(1);
                String post=nameAndImageUrl.get(2);
                String date=nameAndImageUrl.get(3);
                final String url=nameAndImageUrl.get(4);

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
                            FileOutputStream fos2 = new FileOutputStream(new File("/data/data/g.o.gotechpos/files/" + uid + ".png"));
                            //Bitmap bitmap = ((BitmapDrawable) poster.getDrawable()).getBitmap();
                            resource.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                            fos2.flush();
                            fos2.close();
                        }
                        catch (Exception exception){
                            Toast.makeText(getApplicationContext(),"error", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }).into(poster);

                TextView tname=convertView.findViewById(R.id.name);
                TextView tdate=convertView.findViewById(R.id.date);
                TextView tpost=convertView.findViewById(R.id.post);
                tname.setText(name);
                tdate.setText(date);
                tpost.setText(post);

                try{
                    //write to file
                    if(pw!=null){
                        pw.println(key);
                        pw.println(uid);
                        pw.println(name);
                        pw.println(date);
                        pw.println(post);
                        pw.println(url);
                    }
                }
                catch (Exception exception){

                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*startActivity(new Intent(Group.this,OthersProfile.class).
                                putExtra("uid",uid).
                                putExtra("profile_photo",url).
                                putExtra("name",name));*/
                    }
                });

                relativeLayout.addView(convertView);

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

        if(key==null) {
            Query query = reference.orderByChild("3").startAt(startAtDate);
            query.addChildEventListener(childEventListener);
        }
        else{
            String reverse="";
            for(int index=key.length()-1;index!=-1;index--){
                char charAtindex=key.charAt(index);
                reverse+=charAtindex;
            }

            for(int index=0;index<reverse.length();index++){
                char charAtindex=reverse.charAt(index);
                if(charAtindex=='z'){
                    continue;
                }
                else if(charAtindex=='Z' || charAtindex=='_'){
                    key=reverse.replaceFirst(charAtindex+"", "a");
                }
                else if(charAtindex=='-' || charAtindex==' '){
                    key=reverse.replaceFirst(charAtindex+"", "0");
                }
                else if(charAtindex=='9'){
                    key=reverse.replaceFirst(charAtindex+"", "A");
                }
                else{
                    key=reverse.replaceFirst(charAtindex+"", ++charAtindex+"");
                }

                String reverseTheReverse="";
                for(int index2=key.length()-1;index2!=-1;index2--){
                    char charAtindex2=key.charAt(index2);
                    reverseTheReverse+=charAtindex2;
                }
                key=reverseTheReverse;
                break;
            }
            Query query = reference.orderByKey().startAt(key);
            query.addChildEventListener(childEventListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //know whether user should be given option to subscribe or unsubscribe
        /*
         *TO-DO:add code here
         */

        menu.add("subscribe");
        menu.add("unsubscribe");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle()=="subscribe") {
            FirebaseMessaging.getInstance().subscribeToTopic(selectedGroup.replace(" ",
                    ""))         //was getting error when spaces were in selected group.
                    // every topic in cloud functions shouldnt have spaces.
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "successfully subscribed";
                            if (!task.isSuccessful()) {
                                msg = "failed to subscribe";
                            }
                            Toast.makeText(Group.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic(selectedGroup.replace(" ",
                    "")).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Group.this, "you have unsubscribed", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(Group.this, "failed to unsubscribe", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    //send post to database only if post contains characters other than space
    public void sendClicked(View view){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            EditText editText = (EditText) findViewById(R.id.edit_text_post);
            String post = editText.getText().toString().trim();
            if (!post.isEmpty()) {
                //TO-DO:research on how getDisplayName works to make user your not making people use alot of data, bundles
                Uri photoUrl = user.getPhotoUrl();
                if (photoUrl != null) {
                    reference.push().setValue(Arrays.asList(user.getUid(), user.getDisplayName(), post,
                            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()), photoUrl.toString()));
                    editText.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "upload profile picture", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "type in a message to send", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "login and make sure you have a profile photo set", Toast.LENGTH_SHORT).show();
        }
    }// end of sendClicked()

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        reference.removeEventListener(childEventListener);
    }

}