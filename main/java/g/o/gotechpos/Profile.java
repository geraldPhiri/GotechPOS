package g.o.gotechpos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Profile extends AppCompatActivity {
    TextView userName, address;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference userDetailsReference;
    StorageReference sr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        userName=findViewById(R.id.user_name);          //set this value to user's username
        address=findViewById(R.id.programme);
        user=FirebaseAuth.getInstance().getCurrentUser();
        database=FirebaseDatabase.getInstance();
        final String uid=user.getUid();
        userDetailsReference=database.getReference("ProductionDB/Users/"+uid+"/Details");

        /*
         *load programme from internal storage.
         */
        try {
            FileInputStream is = openFileInput(uid + "Address");
            Scanner sc=new Scanner(is);
            if(sc.hasNextLine()) {
                address.setText(sc.nextLine());
            }
        }
        catch(Exception exception){

        }


        {   //load data from firebase to ensure that user is seeing updated information
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                ImageView profilePicture = findViewById(R.id.profile_picture);
                Glide.with(profilePicture.getContext()).load(photoUrl).apply(new RequestOptions().circleCrop()).into(profilePicture);
            }
            userName.setText(user.getDisplayName());

            /*
             *update the "programme" of the user's profile.
             *save the programme on device.
             */
            userDetailsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    address.setText(dataSnapshot.getValue(String.class));
                    //save programme to device
                    try {
                        FileOutputStream file = openFileOutput(uid + "Address", MODE_PRIVATE);
                        PrintWriter pw=new PrintWriter(file);
                        pw.println(address.getText().toString());
                        pw.close();
                    }
                    catch (Exception exception){

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
                                user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Glide.with(profilePicture.getContext()).load(uri).apply(new RequestOptions().circleCrop()).into(profilePicture);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "failed to update user profile", Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(getApplicationContext(), "upload successful", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Profile.this,"Failed to update programme",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "you havent entered a Programme", Toast.LENGTH_SHORT).show();
            }


        }

    }

}
