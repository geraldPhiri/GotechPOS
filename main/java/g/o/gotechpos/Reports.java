package g.o.gotechpos;

import android.content.Context;
import android.graphics.Color;
import androidx.appcompat.widget.PopupMenu;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.vincent.bottomnavigationbar.BottomItem;
import com.vincent.bottomnavigationbar.BottomNavigationBar;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;


public class Reports extends AppCompatActivity {
    TextView reportTitle;
    LinearLayout linearLayout;

    FirebaseDatabase database;
    DatabaseReference reference;
    ChildEventListener childEventListener;

    List<ReportItem> reportItems=new ArrayList<ReportItem>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports);
        linearLayout=findViewById(R.id.listview_report);
        reportTitle=findViewById(R.id.report_title);

        //load from device
        FileInputStream fis2=null;
        Scanner sc2=null;
        try {
            fis2=openFileInput("Reports.txt");
            sc2=new java.util.Scanner(fis2);
            while(sc2.hasNextLine()){
                ReportItem reportItem=new ReportItem(sc2.nextLine(),sc2.nextLine(),sc2.nextLine());
                reportItems.add(reportItem);
            }

        }
        catch (Exception exception){

        }
        finally {
            fis2=null;
            sc2=null;
        }


        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Reports/");
        childEventListener=new ChildEventListener() {
            int count=0;

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FileOutputStream fos=null;
                PrintWriter pw=null;
                try {
                    if(count==0) {
                        fos = openFileOutput("Reports.txt", MODE_PRIVATE);
                        fos.close();
                        reportItems.clear();
                        linearLayout.removeAllViews();
                        ++count;
                    }

                    fos=openFileOutput("Reports.txt",MODE_APPEND);
                    pw=new PrintWriter(fos);
                }
                catch (Exception exception){
                    fos=null;
                    pw=null;
                }


                String test="";
                List<List<String>> items=dataSnapshot.getValue(new GenericTypeIndicator<List<List<String>>>(){});
                for(List<String> item:items) {
                        final String itemName = item.get(0);
                        final String itemPrice = item.get(1);
                        final String date = item.get(2);

                        reportItems.add(new ReportItem(itemName,itemPrice,date));

                        /*LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
                        TextView textViewName = convertView.findViewById(R.id.product_name);
                        TextView textViewCount = convertView.findViewById(R.id.product_price);
                        TextView textViewDate = convertView.findViewById(R.id.date);

                        textViewName.setText(itemName);
                        textViewCount.setText("k"+itemPrice);
                        //textViewDate.setText(date);
                        */

                    try{
                        //write to file
                        if(pw!=null){
                            pw.println(itemName);
                            pw.println(itemPrice);
                            pw.println(date);
                            //pw.println(itemPrice);
                            //pw.println(barcode);
                        }
                    }
                    catch (Exception exception){

                    }

                        //group by date
                        /*if(!test.equals(date)){
                            TextView textView=new TextView(Reports.this);
                            textView.setText(date);
                            linearLayout.addView(textView);
                            test=date;
                        }
                        linearLayout.addView(convertView);*/

                    //make sure file is saved
                    try {
                        pw.close();
                        fos.close();
                    }
                    catch (Exception exception){

                    }
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

        BottomNavigationBar bnb = (BottomNavigationBar) findViewById(R.id.bottom_nav);

        BottomItem item = new BottomItem();
        item.setMode(BottomItem.DRAWABLE_MODE);
        item.setText("Daily");
        item.setActiveIconResID(getResources().getIdentifier("ic_event", "drawable",
                getApplicationInfo().packageName));
        item.setInactiveIconResID(getResources().getIdentifier("ic_event", "drawable",
                getApplicationInfo().packageName));
        item.setActiveTextColor(Color.parseColor("#E64B4E"));
        bnb.addItem(item);


        BottomItem item3 = new BottomItem();
        item3.setMode(BottomItem.DRAWABLE_MODE);
        item3.setText("Monthly");
        item3.setActiveIconResID(getResources().getIdentifier("ic_event", "drawable",
                getApplicationInfo().packageName));
        item3.setInactiveIconResID(getResources().getIdentifier("ic_event", "drawable",
                getApplicationInfo().packageName));
        item3.setActiveTextColor(Color.parseColor("#E64B4E"));
        bnb.addItem(item3);


        BottomItem item2 = new BottomItem();
        item2.setMode(BottomItem.DRAWABLE_MODE);
        item2.setText("Weekly");
        item2.setActiveIconResID(getResources().getIdentifier("ic_event", "drawable",
                getApplicationInfo().packageName));
        item2.setInactiveIconResID(getResources().getIdentifier("ic_event", "drawable",
                getApplicationInfo().packageName));
        item2.setActiveTextColor(Color.parseColor("#E64B4E"));
        bnb.addItem(item2);

        bnb.addOnSelectedListener(new BottomNavigationBar.OnSelectedListener() {
            @Override
            public void OnSelected(int oldPosition, int newPosition) {
                if(newPosition==0){
                    reportTitle.setText("Daily");
                    linearLayout.removeAllViews();
                    dailyIndicator(reportItems);
                }
                else if(newPosition==1){
                    reportTitle.setText("Weekly");
                    linearLayout.removeAllViews();
                    weeklyIndicator(reportItems);
                }
                else if(newPosition==2){
                    reportTitle.setText("Monthly");
                    linearLayout.removeAllViews();
                    monthlyIndicator(reportItems);

                }
            }
        });
        bnb.setSelectedPosition(0); //Set default item



        bnb.initialize();

        Query query=reference.orderByChild("2");
        query.addChildEventListener(childEventListener);

    }


    @Override
    protected void onDestroy() {
        reference.removeEventListener(childEventListener);

        super.onDestroy();
    }

    public void dailyIndicator(List<ReportItem> reportItems){
        //to help set tags that will be used to show a popup of products sold on a particular day
        int tagStart=0;
        int tagEnd=-1;

        Float dailyTotal=0F;
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String testDay="";
        for(ReportItem reportItem:reportItems) {
            tagEnd=tagEnd+1;

            View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
            //TextView textViewName = convertView.findViewById(R.id.product_name);
            //TextView textViewCount = convertView.findViewById(R.id.product_price);
            //TextView textViewDate = convertView.findViewById(R.id.date);

            //textViewName.setText(reportItem.productName);
            //textViewCount.setText("k"+reportItem.price);

            String date=reportItem.dateSold;
            //group by date
            if(!testDay.equals(date)){
                if(dailyTotal!=0F){
                    View totalView = layoutInflater.inflate(R.layout.total_layout, null, true);
                    TextView t=totalView.findViewById(R.id.textview_total);
                    t.setText("k"+dailyTotal);

                    totalView.setTag(tagStart+"-"+tagEnd);
                    totalView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popupMenu=new PopupMenu(Reports.this,v);
                            String tag=v.getTag().toString();
                           // Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
                            int end=Integer.parseInt(tag.split("-")[1]);
                            for(int i=Integer.parseInt(tag.split("-")[0]); i<end; i++){
                                popupMenu.getMenu().add(reportItems.get(i).productName+"\t"+"k"+reportItems.get(i).price);
                            }
                            popupMenu.show();
                        }
                    });
                    tagStart=tagEnd;

                    linearLayout.addView(totalView);
                    dailyTotal=0F;

                }
                TextView textView=new TextView(Reports.this);
                textView.setText(date);
                linearLayout.addView(textView);
                testDay=date;

            }
            dailyTotal+=Float.parseFloat(reportItem.price);
            //linearLayout.addView(convertView);
        }
        View totalView = layoutInflater.inflate(R.layout.total_layout, null, true);
        TextView t=totalView.findViewById(R.id.textview_total);
        t.setText("k"+dailyTotal);

        totalView.setTag(tagStart+"-"+(tagEnd+1));
        totalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(Reports.this,v);
                String tag=v.getTag().toString();
                //Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
                int end=Integer.parseInt(tag.split("-")[1]);
                for(int i=Integer.parseInt(tag.split("-")[0]); i<end; i++){
                    popupMenu.getMenu().add(reportItems.get(i).productName+"\t"+"k"+reportItems.get(i).price);
                }
                popupMenu.show();
            }
        });
        linearLayout.addView(totalView);

    }

    public void weeklyIndicator(List<ReportItem> reportItems){
        int tagStart=0;
        int tagEnd=-1;

        Float weeklyTotal=0F;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, 1);
        String testMonth="";
        int testWeek=-1;
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(ReportItem reportItem:reportItems) {
            tagEnd=tagEnd+1;

            //View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
            //TextView textViewName = convertView.findViewById(R.id.product_name);
            //TextView textViewCount = convertView.findViewById(R.id.product_price);
            //TextView textViewDate = convertView.findViewById(R.id.date);

            //textViewName.setText(reportItem.productName);
            //textViewCount.setText("k"+reportItem.price);
            //textViewDate.setText(reportItem.dateSold);


            String date=reportItem.dateSold;
            //group by month and consider year
            String month=reportItem.dateSold.split("-")[1]+"-"+reportItem.dateSold.split("-")[2];
            if (!testMonth.equals(month)) {
                testWeek=-1;
                TextView textView = new TextView(Reports.this);
                textView.setText(month);
                linearLayout.addView(textView);
                testMonth = month;
                cal.set(Calendar.MONTH, Integer.parseInt(reportItem.dateSold.split("-")[1]));
                cal.set(Calendar.YEAR,Integer.parseInt(reportItem.dateSold.split("-")[2]));
            }



            //group by week
            int week=((Integer.parseInt(reportItem.dateSold.split("-")[0])+(cal.get(Calendar.DAY_OF_WEEK)-2))/7)+1;
            if(testWeek!=week){
                if(weeklyTotal!=0F){
                    View totalView = layoutInflater.inflate(R.layout.total_layout, null, true);
                    TextView t=totalView.findViewById(R.id.textview_total);
                    t.setText("k"+weeklyTotal);

                    totalView.setTag(tagStart+"-"+tagEnd);
                    totalView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popupMenu=new PopupMenu(Reports.this,v);
                            String tag=v.getTag().toString();
                            //Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
                            int end=Integer.parseInt(tag.split("-")[1]);
                            for(int i=Integer.parseInt(tag.split("-")[0]); i<end; i++){
                                popupMenu.getMenu().add(reportItems.get(i).productName+"\t"+"k"+reportItems.get(i).price);
                            }
                            popupMenu.show();
                        }
                    });
                    tagStart=tagEnd;
                    linearLayout.addView(totalView);
                    weeklyTotal=0F;
                }

                TextView textView = new TextView(Reports.this);
                textView.setText("Week:"+week);
                linearLayout.addView(textView);
                testWeek = week;
            }
            weeklyTotal+=Float.parseFloat(reportItem.price);

            //linearLayout.addView(convertView);
        }
        View totalView = layoutInflater.inflate(R.layout.total_layout, null, true);
        TextView t=totalView.findViewById(R.id.textview_total);
        t.setText("k"+weeklyTotal);

        totalView.setTag(tagStart+"-"+(tagEnd+1));
        totalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(Reports.this,v);
                String tag=v.getTag().toString();
                //Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
                int end=Integer.parseInt(tag.split("-")[1]);
                for(int i=Integer.parseInt(tag.split("-")[0]); i<end; i++){
                    popupMenu.getMenu().add(reportItems.get(i).productName+"\t"+"k"+reportItems.get(i).price);
                }
                popupMenu.show();
            }
        });
        linearLayout.addView(totalView);
        weeklyTotal=0F;
    }

    public void monthlyIndicator(List<ReportItem> reportItems){
        int tagStart=0;
        int tagEnd=-1;

        String testMonth="";
        Float monthlyTotal=0F;
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(ReportItem reportItem:reportItems) {
            tagEnd=tagEnd+1;

            //View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
            //TextView textViewName = convertView.findViewById(R.id.product_name);
            //TextView textViewCount = convertView.findViewById(R.id.product_price);
            //TextView textViewDate = convertView.findViewById(R.id.date);

            //textViewName.setText(reportItem.productName);
            //textViewCount.setText("k"+reportItem.price);
            //textViewDate.setText(reportItem.dateSold);


            //group by month
            String month=reportItem.dateSold.split("-")[1]+"-"+reportItem.dateSold.split("-")[2];
            if (!testMonth.equals(month)) {
                if(monthlyTotal!=0F){
                    View totalView = layoutInflater.inflate(R.layout.total_layout, null, true);
                    TextView t=totalView.findViewById(R.id.textview_total);
                    t.setText("k"+monthlyTotal);
                    totalView.setTag(tagStart+"-"+tagEnd);
                    totalView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popupMenu=new PopupMenu(Reports.this,v);
                            String tag=v.getTag().toString();
                            //Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
                            int end=Integer.parseInt(tag.split("-")[1]);
                            for(int i=Integer.parseInt(tag.split("-")[0]); i<end; i++){
                                popupMenu.getMenu().add(reportItems.get(i).productName+"\t"+"k"+reportItems.get(i).price);
                            }
                            popupMenu.show();
                        }
                    });
                    tagStart=tagEnd;

                    linearLayout.addView(totalView);
                    monthlyTotal=0F;
                }

                TextView textView = new TextView(Reports.this);
                textView.setText(month);
                linearLayout.addView(textView);
                testMonth = month;
            }
            monthlyTotal+=Float.parseFloat(reportItem.price);
            //linearLayout.addView(convertView);
        }
        View totalView = layoutInflater.inflate(R.layout.total_layout, null, true);
        TextView t=totalView.findViewById(R.id.textview_total);
        t.setText("k"+monthlyTotal);
        totalView.setTag(tagStart+"-"+(tagEnd+1));
        totalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(Reports.this,v);
                String tag=v.getTag().toString();
                //Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
                int end=Integer.parseInt(tag.split("-")[1]);
                for(int i=Integer.parseInt(tag.split("-")[0]); i<end; i++){
                    popupMenu.getMenu().add(reportItems.get(i).productName+"\t"+"k"+reportItems.get(i).price);
                }
                popupMenu.show();
            }
        });

        linearLayout.addView(totalView);

    }

}

class ReportItem{
    String productName;
    String price;
    String dateSold;
    String seller;

    ReportItem(String productName,String price,String dateSold){
        this.productName=productName;
        this.price=price;
        this.dateSold=dateSold;
    }
}
