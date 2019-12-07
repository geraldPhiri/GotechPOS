package g.o.gotechpos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("ProductionDB/Reports/");
        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String test="";
                List<List<String>> items=dataSnapshot.getValue(new GenericTypeIndicator<List<List<String>>>(){});
                for(List<String> item:items) {
                        final String itemName = item.get(0);
                        final String itemPrice = item.get(1);
                        final String date = item.get(2);

                        reportItems.add(new ReportItem(itemName,itemPrice,date));

                        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
                        TextView textViewName = convertView.findViewById(R.id.product_name);
                        TextView textViewCount = convertView.findViewById(R.id.product_price);
                        TextView textViewDate = convertView.findViewById(R.id.date);

                        textViewName.setText(itemName);
                        textViewCount.setText(itemPrice);
                        //textViewDate.setText(date);

                        //group by date
                        if(!test.equals(date)){
                            TextView textView=new TextView(Reports.this);
                            textView.setText(date);
                            linearLayout.addView(textView);
                            test=date;
                        }
                        linearLayout.addView(convertView);
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
        item.setActiveIconResID(getResources().getIdentifier("ic", "drawable",
                getApplicationInfo().packageName));
        item.setInactiveIconResID(getResources().getIdentifier("ic", "drawable",
                getApplicationInfo().packageName));
        item.setActiveTextColor(Color.parseColor("#E64B4E"));
        bnb.addItem(item);

        BottomItem item2 = new BottomItem();
        item2.setMode(BottomItem.DRAWABLE_MODE);
        item2.setText("Weekly");
        item2.setActiveIconResID(getResources().getIdentifier("ic", "drawable",
                getApplicationInfo().packageName));
        item2.setInactiveIconResID(getResources().getIdentifier("ic", "drawable",
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

        BottomItem item3 = new BottomItem();
        item3.setMode(BottomItem.DRAWABLE_MODE);
        item3.setText("Monthly");
        item3.setActiveIconResID(getResources().getIdentifier("ic", "drawable",
                getApplicationInfo().packageName));
        item3.setInactiveIconResID(getResources().getIdentifier("ic", "drawable",
                getApplicationInfo().packageName));
        item3.setActiveTextColor(Color.parseColor("#E64B4E"));
        bnb.addItem(item3);

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
        String testDay="";
        for(ReportItem reportItem:reportItems) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
            TextView textViewName = convertView.findViewById(R.id.product_name);
            TextView textViewCount = convertView.findViewById(R.id.product_price);
            TextView textViewDate = convertView.findViewById(R.id.date);

            textViewName.setText(reportItem.productName);
            textViewCount.setText(reportItem.price);
            String date=reportItem.dateSold;
            //group by date
            if(!testDay.equals(date)){
                TextView textView=new TextView(Reports.this);
                textView.setText(date);
                linearLayout.addView(textView);
                testDay=date;
            }
            linearLayout.addView(convertView);
        }
    }

    public void weeklyIndicator(List<ReportItem> reportItems){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, 1);
        String testMonth="";
        int testWeek=-1;
        for(ReportItem reportItem:reportItems) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
            TextView textViewName = convertView.findViewById(R.id.product_name);
            TextView textViewCount = convertView.findViewById(R.id.product_price);
            TextView textViewDate = convertView.findViewById(R.id.date);

            textViewName.setText(reportItem.productName);
            textViewCount.setText(reportItem.price);
            textViewDate.setText(reportItem.dateSold);

            //group by month
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
                TextView textView = new TextView(Reports.this);
                textView.setText("Week:"+week);
                linearLayout.addView(textView);
                testWeek = week;
            }

            linearLayout.addView(convertView);
        }
    }

    public void monthlyIndicator(List<ReportItem> reportItems){
        String testMonth="";
        for(ReportItem reportItem:reportItems) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = layoutInflater.inflate(R.layout.report_item, null, true);
            TextView textViewName = convertView.findViewById(R.id.product_name);
            TextView textViewCount = convertView.findViewById(R.id.product_price);
            TextView textViewDate = convertView.findViewById(R.id.date);

            textViewName.setText(reportItem.productName);
            textViewCount.setText(reportItem.price);
            textViewDate.setText(reportItem.dateSold);

            //group by month
            String month=reportItem.dateSold.split("-")[1]+"-"+reportItem.dateSold.split("-")[2];
            if (!testMonth.equals(month)) {
                TextView textView = new TextView(Reports.this);
                textView.setText(month);
                linearLayout.addView(textView);
                testMonth = month;
            }
            linearLayout.addView(convertView);
        }

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
