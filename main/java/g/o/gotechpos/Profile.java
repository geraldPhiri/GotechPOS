package g.o.gotechpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class Profile extends AppCompatActivity {
    private String companyName=null;
    private String currency=null;

    TextView userName, address,phone,currencyTextView;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference userDetailsReference, phoneDetailsReference, referenceToName, referenceToUri,
                      referenceToCompany, referenceToProfileCompany, referenceToCompanyAsEmployee, referenceToCurrency;
    StorageReference sr;
    private AdView ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        MobileAds.initialize(this);

        ad=(AdView)findViewById(R.id.ad);

        AdRequest adRequest=new AdRequest.Builder().addTestDevice("26880EC7D79E15BF2C65A06B4ABD3C7E").build();
        ad.loadAd(adRequest);


        userName=findViewById(R.id.user_name);          //set this value to user's username
        address=findViewById(R.id.address);
        phone=findViewById(R.id.phone);
        currencyTextView=findViewById(R.id.currency);

        SharedPreferences preferences=getSharedPreferences("company ",MODE_PRIVATE);
        if(preferences!=null) {
            companyName =preferences.getString(FirebaseAuth.getInstance().getCurrentUser().getUid(), null);
            if(companyName!=null){
                ((TextView)findViewById(R.id.company)).setText(companyName);
            }
        }

        SharedPreferences preferences2=getSharedPreferences("currency ",MODE_PRIVATE);
        if(preferences2!=null) {
            currency =preferences2.getString(FirebaseAuth.getInstance().getCurrentUser().getUid(), "");
            if(!currency.equals("")){
                ((TextView)findViewById(R.id.currency)).setText(currency);
            }
        }
    
        user=FirebaseAuth.getInstance().getCurrentUser();
        database=FirebaseDatabase.getInstance();
        final String uid=user.getUid();
        userDetailsReference=database.getReference("ProductionDB/Users/"+uid+"/Details");
        phoneDetailsReference=database.getReference("ProductionDB/Users/"+uid+"/Phone");
        referenceToName=database.getReference("ProductionDB/Users/"+uid+"/Name");
        referenceToUri=database.getReference("ProductionDB/Users/"+uid+"/Uri");
        referenceToProfileCompany=database.getReference("ProductionDB/Users/"+uid+"/Company");


        //check if user company is null, pending, or {company_name}
        //if is null, allow user to send request to existing company or create a company
        //once a company is created, firebase function should update users company by its key
        //

        /*
         *load name, pic, address, phone number from internal storage.
         */
        try{
            FileInputStream is = openFileInput(uid + "Name");
            Scanner sc=new Scanner(is);
            if(sc.hasNextLine()) {
                userName.setText(sc.nextLine());
            }
        }
        catch (Exception e){

        }

        try{
            ImageView poster=findViewById(R.id.profile_picture);
            Glide.with(poster.getContext()).load(new File("/data/data/g.o.gotechpos/files/" + uid + ".png"))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).apply(new RequestOptions().circleCrop()).into(poster);
        }
        catch (Exception e){

        }

        try {
            FileInputStream is = openFileInput(uid + "Address");
            FileInputStream is2 = openFileInput(uid + "Phone");

            Scanner sc=new Scanner(is);
            Scanner sc2=new Scanner(is2);
            if(sc.hasNextLine()) {
                address.setText(sc.nextLine());
            }
            if(sc2.hasNextLine()) {
                phone.setText(sc2.nextLine());
            }
        }
        catch(Exception exception){

        }


        {   //load data from firebase to ensure that user is seeing updated information
            referenceToUri.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        String photoUrl = dataSnapshot.getValue(String.class);
                        if (photoUrl != null) {
                            //ImageView profilePicture = findViewById(R.id.profile_picture);
                            ImageView poster = findViewById(R.id.profile_picture);
                            Glide.with(poster.getContext()).asBitmap().load(photoUrl).listener(new RequestListener<Bitmap>() {

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    try {
                                        FileOutputStream fos2 = new FileOutputStream(new File("/data/data/g.o.gotechpos/files/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".png"));
                                        //Bitmap bitmap = ((BitmapDrawable) poster.getDrawable()).getBitmap();
                                        resource.compress(Bitmap.CompressFormat.PNG, 100, fos2);
                                        fos2.flush();
                                        fos2.close();
                                    } catch (Exception exception) {
                                        Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }
                            }).apply(new RequestOptions().circleCrop()).into(poster);
                            //Glide.with(profilePicture.getContext()).load(photoUrl).apply(new RequestOptions().circleCrop()).into(profilePicture);
                        }
                        //save uri to device
                        try {
                            FileOutputStream file = openFileOutput(uid + "Uri", MODE_PRIVATE);
                            PrintWriter pw = new PrintWriter(file);
                            pw.println(photoUrl);
                            pw.close();
                        } catch (Exception exception) {

                        }
                    }
                    catch(Exception r){

                    }
                    referenceToName.removeEventListener(this);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            referenceToProfileCompany.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()==null){
                        //Toast.makeText(getApplicationContext(),"no companyName",Toast.LENGTH_SHORT).show();
                        //make company info editable
                        LinearLayout layout=findViewById(R.id.layout_with_textview_company);
                        EditText editText4=new EditText(Profile.this);
                        editText4.setText("");
                        editText4.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        int id=findViewById(R.id.company).getId();
                        layout.removeView((TextView)findViewById(R.id.company));
                        editText4.setId(id);
                        layout.addView(editText4);

                        //set edit company button visible
                        findViewById(R.id.edit_button_company).setVisibility(View.VISIBLE);
                    }
                    else if(dataSnapshot.getValue(String.class).equals("Request")){
                        //Toast.makeText(getApplicationContext(),"pending companyName",Toast.LENGTH_SHORT).show();
                        findViewById(R.id.edit_button_company).setVisibility(View.VISIBLE);
                    }
                    else {
                        //Toast.makeText(getApplicationContext(),"got companyName",Toast.LENGTH_SHORT).show();
                        //ToDo: make sure companyTextView wont cause app to crash
                        TextView companyTextView=findViewById(R.id.company);
                        companyName=dataSnapshot.getValue(String.class);
                        getSharedPreferences("company ",MODE_PRIVATE).edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid(),companyName).commit();
                        companyTextView.setText(companyName);
                        referenceToCurrency=database.getReference("ProductionDB/Company/"+companyName+"/Currency/");

                        referenceToCurrency.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()==null){
                                    //Toast.makeText(getApplicationContext(),"no companyName",Toast.LENGTH_SHORT).show();
                                    //make company info editable
                                    LinearLayout layout=findViewById(R.id.layout_with_textview_currency);
                                    EditText editText4=new EditText(Profile.this);
                                    editText4.setText("");
                                    editText4.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    int id=findViewById(R.id.currency).getId();
                                    layout.removeView((TextView)findViewById(R.id.currency));
                                    editText4.setId(id);
                                    layout.addView(editText4);

                                    //set edit company button visible
                                    findViewById(R.id.edit_button_currency).setVisibility(View.VISIBLE);
                                }
                                else if(dataSnapshot.getValue(String.class).equals("Request")){
                                    //Toast.makeText(getApplicationContext(),"pending companyName",Toast.LENGTH_SHORT).show();
                                    findViewById(R.id.edit_button_currency).setVisibility(View.VISIBLE);
                                }
                                else {
                                    //Toast.makeText(getApplicationContext(),"got companyName",Toast.LENGTH_SHORT).show();
                                    //ToDo: make sure companyTextView wont cause app to crash
                                    TextView companyTextView=findViewById(R.id.currency);
                                    currency=dataSnapshot.getValue(String.class);
                                    getSharedPreferences("currency ",MODE_PRIVATE).edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid(),currency).commit();
                                    currencyTextView.setText(currency);
                                    //remove listener
                                    referenceToCurrency.removeEventListener(this);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        referenceToProfileCompany.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



            referenceToName.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        userName.setText(dataSnapshot.getValue(String.class));
                        //save name to device
                        try {
                            FileOutputStream file = openFileOutput(uid + "Name", MODE_PRIVATE);
                            PrintWriter pw = new PrintWriter(file);
                            pw.println(userName.getText().toString());
                            pw.close();
                        } catch (Exception exception) {

                        }
                    }
                    catch (Exception r){

                    }
                    referenceToName.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            /*
             *update the address, phone number of the user's profile.
             *save the address, phone number on device.
             */
            phoneDetailsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        phone.setText(dataSnapshot.getValue(String.class));
                        //save phone number to device
                        try {
                            FileOutputStream file = openFileOutput(uid + "Phone", MODE_PRIVATE);
                            PrintWriter pw = new PrintWriter(file);
                            pw.println(phone.getText().toString());
                            pw.close();
                        } catch (Exception exception) {

                        }
                    }
                    catch (Exception f){

                    }
                    phoneDetailsReference.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            userDetailsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        address.setText(dataSnapshot.getValue(String.class));
                        //save address to device
                        try {
                            FileOutputStream file = openFileOutput(uid + "Address", MODE_PRIVATE);
                            PrintWriter pw = new PrintWriter(file);
                            pw.println(address.getText().toString());
                            pw.close();
                        } catch (Exception exception) {

                        }
                    }
                    catch (Exception t){

                    }
                    userDetailsReference.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

    }//onCreate(...)

    public void onProfilePicClick(View view){
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }//onProfilepicClick(...)

    /*
     *Use path of photo to upload photo to storage(Firebase)
     *inform user if upload was successful or not
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            final ImageView profilePicture = (ImageView) findViewById(R.id.profile_picture);
            final Uri uri = data.getData();
            final String Uid = user.getUid();
            sr = FirebaseStorage.getInstance().getReference().child(Uid);
            sr.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                /*user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                    }
                                });*/
                                referenceToUri.setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "upload successful", Toast.LENGTH_SHORT).show();
                                            //ImageView poster=findViewById(R.id.profile_picture);
                                            /*Glide.with(poster.getContext()).asBitmap().load(uri).listener(new RequestListener<Bitmap>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                    try {
                                                        FileOutputStream fos2 = new FileOutputStream(new File("/data/data/g.o.gotechpos/files/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".png"));
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
                                            }).apply(new RequestOptions().circleCrop()).into(poster);*/
                                            //Glide.with(profilePicture.getContext()).load(uri).apply(new RequestOptions().circleCrop()).into(profilePicture);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "failed to update profile picture", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "failed to get photo url", Toast.LENGTH_SHORT).show();
                            }
                        });


                    } else {
                        Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            super.onActivityResult(requestCode, resultCode, data);
        }

    }//onActivityResult(...)


    public void goToMessages(View view){
        // startActivity(new Intent(this,Messages.class));
    }


    int count=0;
    EditText editText=null;
    public void editProgramme(View view){
        final LinearLayout layout = findViewById(R.id.layout_with_textview);

        if(count==0) {
            editText=new EditText(Profile.this);
            String programmeString = address.getText().toString();
            editText.setText(programmeString);
            editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int id=address.getId();
            layout.removeView(address);
            editText.setId(id);
            layout.addView(editText);
            count=1;
        }
        else{
            final String editTextString=editText.getText().toString().trim();
            if (!editTextString.isEmpty()) {
                userDetailsReference.setValue(editTextString).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            layout.removeView(editText);
                            address.setText(editTextString);
                            layout.addView(address);
                            count = 0;
                        }
                        else{
                            Toast.makeText(Profile.this,"Failed to update address",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "you havent entered an address", Toast.LENGTH_SHORT).show();
            }


        }

    }


    int count2=0;
    EditText editText2=null;
    public void editPhone(View view){
        final LinearLayout layout = findViewById(R.id.layout_with_textview2);

        if(count2==0) {
            editText2=new EditText(Profile.this);
            String programmeString = phone.getText().toString();
            editText2.setText(programmeString);
            editText2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int id=phone.getId();
            layout.removeView(phone);
            editText2.setId(id);
            layout.addView(editText2);
            count2=1;
        }
        else{
            final String editTextString=editText2.getText().toString().trim();
            if (!editTextString.isEmpty()) {
                phoneDetailsReference.setValue(editTextString).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            layout.removeView(editText2);
                            phone.setText(editTextString);
                            layout.addView(phone);
                            count2 = 0;
                        }
                        else{
                            Toast.makeText(Profile.this,"Failed to update phone",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "you havent entered a phone number", Toast.LENGTH_SHORT).show();
            }

        }

    }//editPhone


    int count3=0;
    EditText editText3=null;
    public void editUsername(View view){
        final LinearLayout layout = findViewById(R.id.nl);

        if(count3==0) {
            editText3=new EditText(Profile.this);
            String programmeString = userName.getText().toString();
            editText3.setText(programmeString);
            editText3.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int id=userName.getId();
            layout.removeView(userName);
            editText3.setId(id);
            layout.addView(editText3);
            count3=1;
        }
        else{
            final String editTextString=editText3.getText().toString().trim();
            if (!editTextString.isEmpty()) {
              referenceToName.setValue(editTextString).addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful()) {
                          layout.removeView(editText3);
                          userName.setText(editTextString);
                          layout.addView(userName);
                          count3 = 0;
                      }
                      else{
                          Toast.makeText(Profile.this,"Username not set",Toast.LENGTH_SHORT).show();
                      }
                  }
              });
                /*FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder()
                        .setDisplayName(editTextString).build())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    layout.removeView(editText3);
                                    userName.setText(editTextString);
                                    layout.addView(userName);
                                    count3 = 0;
                                } else {
                                    Toast.makeText(getApplicationContext(), "Username not set", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });*/
            } else {
                Toast.makeText(getApplicationContext(), "you havent entered a Username", Toast.LENGTH_SHORT).show();
            }


        }
    }//editUsername

    public void editCompany(View view) {
        String company = ((TextView) findViewById(R.id.company)).getText().toString().trim();
        if(!company.isEmpty()) {
            referenceToCompany = database.getReference("ProductionDB/Company/" + company + "/Owners");
            referenceToCompanyAsEmployee=database.getReference("ProductionDB/Company/" + company + "/Request");

            referenceToCompany.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() == null) {
                        //create company
                        AlertDialog createCompanyDialog = new AlertDialog.Builder(Profile.this)
                                .setTitle("Create Company as Owner")
                                .setMessage("You 'l have Admin access")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        referenceToCompany.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(FirebaseAuth.getInstance()
                                                .getCurrentUser().getUid());
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();

                        createCompanyDialog.show();

                    } else {

                        AlertDialog createCompanyDialog = new AlertDialog.Builder(Profile.this)
                                .setTitle("Join Company as Employee")
                                .setMessage("You wont have Admin access. Only the Owner(s) will be able to grant you Admin access")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        referenceToCompanyAsEmployee.push().setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();

                        createCompanyDialog.show();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),"Supply field",Toast.LENGTH_SHORT).show();
        }
    }//editCompany

    public void editCurrency(View view){
        String currency=((EditText)findViewById(R.id.currency)).getText().toString().trim();
        if(!currency.isEmpty()){
            referenceToCurrency.setValue(currency);
        }
        else{
            Toast.makeText(getApplicationContext(),"Please fill in field",Toast.LENGTH_SHORT).show();
        }
    }//editCurrency

    public void toDo(View view){

    }


    public void goToRequests(View view){
        if(companyName!=null) {
            //check if is owner
            database.getReference("ProductionDB/Company/" + companyName + "/Owners/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue(String.class)!=null){
                        if(dataSnapshot.getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            startActivity(new Intent(Profile.this, Requests.class).putExtra("company", companyName));
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"You do not have Admin Access",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),"Please set Company",Toast.LENGTH_SHORT).show();
        }

    }

}