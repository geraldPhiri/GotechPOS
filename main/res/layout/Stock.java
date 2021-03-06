package g.o.gotechpos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;


public class Stock extends AppCompatActivity {
    private String clickedImageTag;

    private StorageReference sr;
    private FirebaseStorage storage;

    List<String> highLightedCategories=new ArrayList<>();

    EditText editTextSearch;

    LinearLayout linearLayout;

    LinearLayout categoryLayout;

    FirebaseDatabase database;
    private DatabaseReference reference;
    private DatabaseReference categoryRef;

    ChildEventListener childEventListener;
    ChildEventListener categoryChildEventListener;

    List<StockItem> stockItems=new ArrayList<>();
    //private List<String> productName=new ArrayList();
    //private List<String> productCount=new ArrayList();
    //private List<String> productPrices=new ArrayList<>();
    //private List<String> productUnit=new ArrayList<>();

    List<String> category=new ArrayList<>();

    //To help with SearchBY
    TextView searchByName,searchByCount,searchByUnit,searchByPrice;

    ObservableScrollView scrollView;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock);

        storage= FirebaseStorage.getInstance();

        categoryLayout=findViewById(R.id.category_layout);

        scrollView=findViewById(R.id.scroll_view);
        floatingActionButton=findViewById(R.id.fab);
        floatingActionButton.attachToScrollView(scrollView);

        searchByName=findViewById(R.id.by_name);
        searchByCount=findViewById(R.id.by_count);
        searchByUnit=findViewById(R.id.by_unit);
        searchByPrice=findViewById(R.id.by_price);

        linearLayout=findViewById(R.id.listview_stock);
        editTextSearch=findViewById(R.id.search);

        //load categories from device
        FileInputStream fis3=null;
        Scanner sc3=null;
        try {
            LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fis3=openFileInput("Category.txt");
            sc3=new java.util.Scanner(fis3);
            while(sc3.hasNextLine()){
                TextView textView=new TextView(Stock.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,30,0);
                textView.setLayoutParams(params);
                String name=sc3.nextLine();
                textView.setText(name);

                category.add(name);
                categoryLayout.addView(textView);
            }

        }
        catch (Exception exception){

        }
        finally {
            fis3=null;
            sc3=null;
        }



        //load stock from device
        FileInputStream fis2=null;
        Scanner sc2=null;
        try {
            LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fis2=openFileInput("Stock.txt");
            sc2=new java.util.Scanner(fis2);
            while(sc2.hasNextLine()){
                View convertView=layoutInflater.inflate(R.layout.stock_item,null,true);
                ImageView poster=convertView.findViewById(R.id.product_picture);
                String key=sc2.nextLine();
                poster.setTag(key);//tag is used later to upload url of pictur being uploaded
                try {
                    Glide.with(poster.getContext()).load(new File("/data/data/g.o.gotechpos/files/" +key + ".png"))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).into(poster);
                }
                catch(Exception exception){

                }
                TextView textViewName=convertView.findViewById(R.id.product_name);
                TextView textViewCount=convertView.findViewById(R.id.product_count);
                TextView textViewPrice=convertView.findViewById(R.id.product_price);
                TextView textViewUnit=convertView.findViewById(R.id.product_unit);

                textViewName.setText(sc2.nextLine());
                textViewCount.setText(sc2.nextLine());
                textViewPrice.setText("k"+sc2.nextLine());
                textViewUnit.setText(sc2.nextLine());

                linearLayout.addView(convertView);
            }

        }
        catch (Exception exception){

        }
        finally {
            fis2=null;
            sc2=null;
        }

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Stock/");
        categoryRef=database.getReference("ProductionDB/Category/");

        categoryChildEventListener=new ChildEventListener() {
            int count=0;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FileOutputStream fos=null;
                PrintWriter pw=null;
                try {
                    if(count==0) {
                        fos = openFileOutput("Category.txt", MODE_PRIVATE);
                        fos.close();
                        categoryLayout.removeAllViews();
                        category.clear();
                        ++count;
                    }

                    fos=openFileOutput("Category.txt",MODE_APPEND);
                    pw=new PrintWriter(fos);
                }
                catch (Exception exception){
                    fos=null;
                    pw=null;
                }
                final String name=dataSnapshot.getValue(String.class);
                category.add(name);

                //add to layout
                TextView textView=new TextView(Stock.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,30,0);
                textView.setLayoutParams(params);
                textView.setText(name);
                textView.setTag("gray");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView searchByTextView=(TextView)v;
                        if(searchByTextView.getTag().toString().equals("gray")){
                            searchByTextView.setTextColor(Color.parseColor("#000000"));
                            searchByTextView.setTag("black");
                            searchByTextView.setTypeface(Typeface.DEFAULT_BOLD);
                            highLightedCategories.add(name);

                        }
                        else if(searchByTextView.getTag().toString().equals("black")) {
                            searchByTextView.setTextColor(Color.parseColor("#888888"));
                            searchByTextView.setTag("gray");
                            searchByTextView.setTypeface(Typeface.DEFAULT);
                            highLightedCategories.remove(name);

                        }

                        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        linearLayout.removeAllViews();
                        if(!highLightedCategories.isEmpty()) {
                            for (StockItem stockItem : stockItems) {
                                if (highLightedCategories.contains(stockItem.category)) {
                                    final View convertView = layoutInflater.inflate(R.layout.stock_item, null, true);
                                    TextView textViewName = convertView.findViewById(R.id.product_name);
                                    TextView textViewCount = convertView.findViewById(R.id.product_count);
                                    TextView textViewPrice = convertView.findViewById(R.id.product_price);
                                    TextView textViewUnit = convertView.findViewById(R.id.product_unit);
                                    ImageView poster=convertView.findViewById(R.id.product_picture);
                                    try {
                                        Glide.with(poster.getContext()).load(new File("/data/data/g.o.gotechpos/files/" +stockItem.key + ".png"))
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true).into(poster);
                                    }
                                    catch(Exception exception){

                                    }

                                    textViewName.setText(stockItem.name);
                                    textViewCount.setText(stockItem.count);
                                    textViewPrice.setText("k" + stockItem.price);
                                    textViewUnit.setText(stockItem.unit);
                                    linearLayout.addView(convertView);
                                }
                            }
                        }
                        else {
                            for (StockItem stockItem : stockItems) {
                                    final View convertView = layoutInflater.inflate(R.layout.stock_item, null, true);
                                    TextView textViewName = convertView.findViewById(R.id.product_name);
                                    TextView textViewCount = convertView.findViewById(R.id.product_count);
                                    TextView textViewPrice = convertView.findViewById(R.id.product_price);
                                    TextView textViewUnit = convertView.findViewById(R.id.product_unit);

                                    textViewName.setText(stockItem.name);
                                    textViewCount.setText(stockItem.count);
                                    textViewPrice.setText("k" + stockItem.price);
                                    textViewUnit.setText(stockItem.unit);
                                    linearLayout.addView(convertView);
                            }
                        }
                    }
                });
                categoryLayout.addView(textView);


                try{
                    //write to file
                    if(pw!=null){
                        pw.println(name);
                    }
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

        childEventListener=new ChildEventListener() {
            int count=0;
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                FileOutputStream fos=null;
                PrintWriter pw=null;
                try {
                    if(count==0) {
                        fos = openFileOutput("Stock.txt", MODE_PRIVATE);
                        fos.close();
                        linearLayout.removeAllViews();
                        ++count;
                    }

                    fos=openFileOutput("Stock.txt",MODE_APPEND);
                    pw=new PrintWriter(fos);
                }
                catch (Exception exception){
                    fos=null;
                    pw=null;
                }
                ArrayList<String> item=dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<String>>(){});
                String key=dataSnapshot.getKey();
                final String itemName=item.get(0);
                final String itemCount=item.get(1);
                final String itemPrice=item.get(2);

                //try catch block to ensure app doesn't crash if old apps edit database
                String itemUnit="";
                String itemCategory="";
                String url="";
                String costPrices="";
                final String urlCopy;

                try {
                    itemUnit=item.get(3);
                    itemCategory=item.get(4);
                    url=item.get(5);
                }
                catch(Exception e){

                }

                try{
                    costPrices=item.get(7);
                }
                catch (Exception e){

                }


                if (!url.equals("")) {
                    urlCopy = url;
                }
                else {
                    urlCopy="";
                }
                stockItems.add(new StockItem(itemName,itemCount,itemUnit,itemPrice,itemCategory,key));
                /*productName.add(itemName);
                productCount.add(itemCount);
                productUnit.add(itemUnit);
                productPrices.add(itemPrice);*/

                LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View convertView=layoutInflater.inflate(R.layout.stock_item,null,true);
                ImageView poster=convertView.findViewById(R.id.product_picture);
                poster.setTag(dataSnapshot.getKey());//tag is used later to upload url of picture being uploaded
                if(!url.equals("")) {
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
                            } catch (Exception exception) {
                                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    }).into(poster);
                }


                TextView textViewName=convertView.findViewById(R.id.product_name);
                TextView textViewCount=convertView.findViewById(R.id.product_count);
                TextView textViewPrice=convertView.findViewById(R.id.product_price);
                TextView textViewUnit=convertView.findViewById(R.id.product_unit);

                textViewName.setText(itemName);
                textViewCount.setText(itemCount);
                textViewPrice.setText("k"+itemPrice);
                textViewUnit.setText(itemUnit);

                try{
                    //write to file
                    if(pw!=null){
                        pw.println(dataSnapshot.getKey());
                        pw.println(itemName);
                        pw.println(itemCount);
                        pw.println(itemPrice);
                        pw.println(itemUnit);
                        //pw.println(barcode);
                    }
                }
                catch (Exception exception){

                }

                final String costPricesCopy=costPrices;

                final String i=itemUnit;
                convertView.setTag(dataSnapshot.getKey());
                convertView.findViewById(R.id.edit_button3).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Stock.this,AddProduct.class)
                                .putExtra("barcode",convertView.getTag().toString())
                                .putExtra("name",itemName)
                                .putExtra("price",itemPrice)
                                .putExtra("count",itemCount)
                                .putExtra("unit",i)
                                .putExtra("category",(Serializable)category)
                                .putExtra("url",urlCopy)
                                .putExtra("cost_prices",costPricesCopy)

                        );
                    }
                });
                convertView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reference.child(convertView.getTag().toString()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //from view from layout
                                int index=linearLayout.indexOfChild(convertView);
                                linearLayout.removeView(convertView);

                                /*
                                 *Read file contents as string.
                                 *Replace substring representing item with ""(empty string)
                                 *Save file
                                 */
                                try {
                                    List<List<String>> list=new ArrayList<>();
                                    FileInputStream file = openFileInput("Stock.txt");
                                    Scanner sc=new Scanner(file);
                                    while(sc.hasNextLine()){
                                        String sc1=sc.nextLine();
                                        String sc2=sc.nextLine();
                                        String sc3=sc.nextLine();
                                        String sc4=sc.nextLine();
                                        if(sc1.equals(convertView.findViewById(R.id.product_name)) &&
                                                sc2.equals(convertView.findViewById(R.id.product_price)) &&
                                                sc3.equals(convertView.findViewById(R.id.product_count)) /*&&
                                                sc4.equals(convertView.findViewById(R.id.product_unit))*/ )
                                        {
                                            //do nothing
                                        }
                                        else {
                                            list.add(new ArrayList<String>(Arrays.asList(
                                                    sc1,
                                                    sc2,
                                                    sc3,
                                                    sc4
                                            )));
                                        }
                                    }
                                    FileOutputStream fileOutputStream=openFileOutput("Stock.txt",MODE_PRIVATE);
                                    fileOutputStream.close();
                                    fileOutputStream=null;
                                    fileOutputStream=openFileOutput("Stock.txt",MODE_APPEND);
                                    PrintWriter pw=new PrintWriter(fileOutputStream);
                                    for(List<String> item:list){
                                        pw.println(item.get(0));
                                        pw.println(item.get(1));
                                        pw.println(item.get(2));
                                        pw.println(item.get(3));
                                    }
                                    pw.close();
                                    fileOutputStream.close();


                                    sc.close();
                                    file.close();
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
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
        categoryRef.addChildEventListener(categoryChildEventListener);
        reference.addChildEventListener(childEventListener);


    }


    @Override
    protected void onDestroy() {
        reference.removeEventListener(childEventListener);
        categoryRef.removeEventListener(categoryChildEventListener);

        super.onDestroy();
    }


    public void setPhoto(View view) {
        clickedImageTag=view.getTag().toString();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);

    }


    public void addToStock(View view){
        Intent intent=new Intent(this,AddStock.class);
        intent.putExtra("category",(Serializable) category);
        startActivity(intent);
    }



    //set sortBy field and carryout highlight operation
    public  void sortBy(View view){
        List<Integer> ids=new ArrayList<>(Arrays.asList(R.id.sby_name,R.id.sby_count,R.id.sby_unit,R.id.sby_price));
        TextView textView=(TextView)view;
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        StockItem.sortBy=Integer.parseInt(view.getTag().toString());
        for(int id:ids){
            if(view.getId()!=id){
                TextView textView1=findViewById(id);
                textView1.setTextColor(Color.parseColor("#888888"));
                textView1.setTypeface(Typeface.DEFAULT);
            }
        }

        //sort
        sort(stockItems);


    }

    /*
     * 1.sort
     * 2.clear linearlayout
     * 3.add views
     */
    public void sort(List<StockItem> list){
        Collections.sort(list);

        LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        linearLayout.removeAllViews();

        String currentCategory="";


        for(StockItem stockItem:list){
            if(!currentCategory.equals(stockItem.category)){
                //testview to use to indicate categories
                TextView textView=new TextView(Stock.this);
                textView.setText(stockItem.category);
                linearLayout.addView(textView);
                currentCategory=stockItem.category;
            }

            View convertView=layoutInflater.inflate(R.layout.stock_item,null,true);
            TextView textViewName=convertView.findViewById(R.id.product_name);
            TextView textViewCount=convertView.findViewById(R.id.product_count);
            TextView textViewPrice=convertView.findViewById(R.id.product_price);
            TextView textViewUnit=convertView.findViewById(R.id.product_unit);
            ImageView poster=convertView.findViewById(R.id.product_picture);
            try {
                Glide.with(poster.getContext()).load(new File("/data/data/g.o.gotechpos/files/" +stockItem.key + ".png"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(poster);
            }
            catch(Exception exception){

            }

            textViewName.setText(stockItem.name);
            textViewCount.setText(stockItem.count);
            textViewPrice.setText("k"+stockItem.price);
            textViewUnit.setText(stockItem.unit);

            linearLayout.addView(convertView);
        }

    }

    public void searchBy(View view){
        TextView searchByTextView=(TextView)view;
        if(searchByTextView.getTag().toString().equals("gray")){
            searchByTextView.setTextColor(Color.parseColor("#000000"));
            searchByTextView.setTag("black");
            searchByTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else if(searchByTextView.getTag().toString().equals("black")) {
            searchByTextView.setTextColor(Color.parseColor("#888888"));
            searchByTextView.setTag("gray");
            searchByTextView.setTypeface(Typeface.DEFAULT);

        }

    }




    /*
     *1. clear LinearLayout.
     *2. Add views to LinearLayout that have searchString.
     */
    public void onSearch(View view){
        //getSearchBy
        List<Boolean> toSearchProperty=new ArrayList<>(Arrays.asList(
                searchByName.getTag().toString().equals("black"),
                searchByCount.getTag().toString().equals("black"),
                searchByUnit.getTag().toString().equals("black"),
                searchByPrice.getTag().toString().equals("black")
        ));
        String searchString=editTextSearch.getText().toString();
        if(searchString==null || searchString.equals("")){
            linearLayout.removeAllViews();
            for(StockItem stockItem:stockItems){
                linearLayout.addView(stockItem.yieldNullOrHighlightedStockItem(Stock.this,null,null,true));
            }
            return;
        }
        linearLayout.removeAllViews();
        for(StockItem stockItem:stockItems){
            final View convertView=stockItem.yieldNullOrHighlightedStockItem(Stock.this,toSearchProperty,searchString,false);
            if(convertView!=null) {
                linearLayout.addView(convertView);
            }
        }


    }

    public void addCategory(View view){
        startActivityForResult(new Intent(Stock.this,AddCategory.class),1);
    }


    //upload category to firebase database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==1){
                String name=data.getStringExtra("name");
                categoryRef.push().setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"category uploaded",Toast.LENGTH_SHORT).show();
                        }
                        else {

                        }
                    }
                });

            }
            else if(requestCode==2){
                Uri uri=data.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    //upload file
                    sr = storage.getReference(UUID.randomUUID().toString())/*.child(FirebaseAuth.getInstance().getCurrentUser().getUid())*/;
                    if(uri!=null) {
                        sr.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(final Uri uri) {
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null) {
                                                //EditText editText = (EditText) findViewById(R.id.edit_text_post);
                                                //String post = editText.getText().toString().trim();
                                                //Toast.makeText(getApplicationContext(),clickedImageTag,Toast.LENGTH_SHORT).show();
                                                reference.child(clickedImageTag).child("5").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(getApplicationContext(),"image uploaded",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            } else {
                                                /*Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
                                                finish();*/
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "failed to get photo url", Toast.LENGTH_SHORT).show();
                                            //finish();
                                        }
                                    });

                                    //Toast.makeText(getApplicationContext(), "upload successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();
                                    //finish();
                                }
                            }
                        });
                    }

                } catch (Exception e) {
                }


            }
        }


    }//onActivityResult()


    public void onProductPicClick(View view){

    }

    public void miniAddProduct(View view){
        startActivity(new Intent(Stock.this,MiniAddActivity.class).putExtra("barcode",));
    }

}




class StockItem implements Comparable<StockItem>{
    static final int SORTBYNAME=0;
    static final int SORTBYPRICE=1;
    static final int SORTBYCOUNT=2;
    static final int SORTBYUNIT=3;
    //static  final int SORTBYCATEGORY=4;

    static int sortBy=SORTBYNAME;//default sortBY

    String key;
    String name;
    String count;
    String unit;
    String price;
    String category;

    public StockItem(String name, String count, String unit, String price, String category, String key){
        this.name=name;
        this.count=count;
        this.unit=unit;
        this.price=price;
        this.category=category;
        this.key=key;
    }

    @Override
    public int compareTo(StockItem o){
        List<String> list1=new ArrayList(Arrays.asList(/*category,*/name,price,count,unit));
        List<String> list2=new ArrayList(Arrays.asList(/*o.category,*/o.name,o.price,o.count,o.unit));



        //move to sortby item to front of arrays
        String string1=list1.get(sortBy);
        String string2=list2.get(sortBy);

        list1.remove(sortBy);
        list2.remove(sortBy);

        list1.add(0,string1);
        list2.add(0,string2);




        //compare
        for(int i=0;i<list1.size();i++){

            int result=list1.get(i).compareTo(list2.get(i));
            try{
                Double d1=Double.parseDouble(list1.get(i));
                Double d2=Double.parseDouble(list2.get(i));

                result=d1.compareTo(d2);
            }
            catch (Exception e){

            }
            if(result==0){
                continue;
            }
            return result;
        }

        return 0;
    }

    /*public boolean contains(String string){
        return (name.contains(string) || count.contains(string) || unit.contains(string)||price.contains(string));
    }*/

    /**
     * @param string is String to search for and highlight in StockItem
     * @return View with highlighted string highlighted
     */
    public View yieldNullOrHighlightedStockItem(Context context, List<Boolean> toSearchBy,String string,boolean returnUnchanged){
        LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView=layoutInflater.inflate(R.layout.stock_item,null,true);
        ImageView poster=convertView.findViewById(R.id.product_picture);
        try {
            Glide.with(poster.getContext()).load(new File("/data/data/g.o.gotechpos/files/" +key + ".png"))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(poster);
        }
        catch(Exception exception){

        }
        List<TextView> textViews=new ArrayList<>(Arrays.asList(
                (TextView)convertView.findViewById(R.id.product_name),
                (TextView)convertView.findViewById(R.id.product_count),
                (TextView)convertView.findViewById(R.id.product_unit),
                (TextView)convertView.findViewById(R.id.product_price)
        )
        );
        if(returnUnchanged){
            textViews.get(0).setText(this.name);
            textViews.get(1).setText(this.count);
            textViews.get(2).setText(this.unit);
            textViews.get(3).setText(this.price);

            return convertView;
        }


        //base search on searchBy given by user
        String string3=string.toLowerCase();
        List<String> properties=new ArrayList(Arrays.asList(this.name,this.count,this.unit,this.price));
        boolean toReturnNull=true;
        for(int j=0;j<toSearchBy.size();j++){
            if(toSearchBy.get(j)){
                String searchInHere=properties.get(j);
                if(j==3){
                    searchInHere="k"+properties.get(j);
                }
                SpannableString spannableString = new SpannableString((CharSequence)searchInHere);
                String string5 = searchInHere.toLowerCase();
                boolean bl = false;
                for (int i = 0; i < string5.length(); ++i) {
                    if (string3.charAt(0) != string5.charAt(i)) continue;
                    if (string5.substring(i, string5.length()).length() < string3.length()) break;
                    if (!string5.substring(i, i + string3.length()).equals((Object)string3)) continue;
                    spannableString.setSpan((Object)new ForegroundColorSpan(Color.parseColor((String)"blue")), i, i + string3.length(), 18);
                    bl = true;
                    toReturnNull=false;
                }
                if (!bl){
                    textViews.get(j).setText(searchInHere);
                }
                else {
                    textViews.get(j).setText((CharSequence) spannableString);
                }

            }
            else {
                textViews.get(j).setText(j==3?"k"+properties.get(j):properties.get(j));
            }


        }


        /*textViewName.setText(stockItem.name);
        textViewCount.setText(stockItem.count);
        textViewPrice.setText("price: k"+stockItem.price);
        textViewUnit.setText(stockItem.unit);*/
        if(toReturnNull){
            return null;
        }
        return convertView;
    }

}//class StockItem