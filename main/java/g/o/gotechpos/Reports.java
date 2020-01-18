package g.o.gotechpos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.appcompat.widget.PopupMenu;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;


public class Reports extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
  HashSet<String> employeesHashSet=new HashSet<>();

  ArrayList<Entry> values = new ArrayList<>();

  private LineChart chart;
  private SeekBar seekBarX, seekBarY;

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
        String a=sc2.nextLine();
        String b=sc2.nextLine();
        String c=sc2.nextLine();
        String d=sc2.nextLine();
        ReportItem reportItem=new ReportItem(a,b,c,d);
        reportItems.add(reportItem);
        employeesHashSet.add(d);
      }

      dailyIndicator(reportItems);

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
            employeesHashSet.clear();
            //linearLayout.removeAllViews();
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
          final String uuid=item.get(3);
          employeesHashSet.add(uuid);

          reportItems.add(new ReportItem(itemName,itemPrice,date,uuid));

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
              pw.println(uuid);
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
    item.setInactiveTextColor(Color.parseColor("#000000"));
    item.setActiveTextColor(Color.parseColor("#E64B4E"));
    bnb.addItem(item);


    BottomItem item3 = new BottomItem();
    item3.setMode(BottomItem.DRAWABLE_MODE);
    item3.setText("Weekly");
    item3.setActiveIconResID(getResources().getIdentifier("ic_event", "drawable",
            getApplicationInfo().packageName));
    item3.setInactiveIconResID(getResources().getIdentifier("ic_event", "drawable",
            getApplicationInfo().packageName));
    item3.setInactiveTextColor(Color.parseColor("#000000"));
    item3.setActiveTextColor(Color.parseColor("#E64B4E"));
    bnb.addItem(item3);


    BottomItem item2 = new BottomItem();
    item2.setMode(BottomItem.DRAWABLE_MODE);
    item2.setText("Monthly");
    item2.setActiveIconResID(getResources().getIdentifier("ic_event", "drawable",
            getApplicationInfo().packageName));
    item2.setInactiveIconResID(getResources().getIdentifier("ic_event", "drawable",
            getApplicationInfo().packageName));
    item2.setInactiveTextColor(Color.parseColor("#000000"));
    item2.setActiveTextColor(Color.parseColor("#E64B4E"));
    bnb.addItem(item2);

    /*BottomItem item4 = new BottomItem();
    item4.setMode(BottomItem.DRAWABLE_MODE);
    item4.setText("Employees");
    item4.setActiveIconResID(getResources().getIdentifier("ic_people", "drawable",
            getApplicationInfo().packageName));
    item4.setInactiveIconResID(getResources().getIdentifier("ic_people", "drawable",
            getApplicationInfo().packageName));
    item4.setInactiveTextColor(Color.parseColor("#000000"));
    item4.setActiveTextColor(Color.parseColor("#E64B4E"));
    bnb.addItem(item4);*/

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
        else if(newPosition==3){
          reportTitle.setText("Employees");
          linearLayout.removeAllViews();
          listEmployees();

        }
      }
    });
    bnb.setSelectedPosition(0); //Set default item



    bnb.initialize();

    Query query=reference.orderByChild("2");
    query.addChildEventListener(childEventListener);


  }

  public void listEmployees(){
    startActivityForResult(new Intent(Reports.this,EmployeesLayout.class),1);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode==1){
      if(resultCode==RESULT_OK){
        String uuid=data.getStringExtra("uuid");


      }
    }

  }

  @Override
  protected void onDestroy() {
    reference.removeEventListener(childEventListener);

    super.onDestroy();
  }

  public void dailyIndicator(final List<ReportItem> reportItems){
    Collections.sort(reportItems);
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
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(Color.parseColor("#0B6623"));
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

  public void weeklyIndicator(final List<ReportItem> reportItems){
    Collections.sort(reportItems);
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
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(Color.parseColor("#E64B4E"));
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
        textView.setTextColor(Color.parseColor("#0B6623"));
        textView.setTypeface(null, Typeface.BOLD);
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

  public void monthlyIndicator(final List<ReportItem> reportItems){
    values.clear();

    Collections.sort(reportItems);
    int tagStart=0;
    int tagEnd=-1;

    String testMonth="";
    Float monthlyTotal=0F;
    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    int count=0;
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
      String m=reportItem.dateSold.split("-")[1];
      String month=m+"-"+reportItem.dateSold.split("-")[2];
      if (!testMonth.equals(month)) {
        if(monthlyTotal!=0F){
          View totalView = layoutInflater.inflate(R.layout.total_layout, null, true);
          TextView t=totalView.findViewById(R.id.textview_total);
          t.setText("k"+monthlyTotal);

          values.add(new Entry(Integer.parseInt(m), monthlyTotal));
          totalView.setTag(tagStart+"-"+tagEnd);
          totalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                            /*List<String> content=new ArrayList<>();
                            //PopupMenu popupMenu=new PopupMenu(Reports.this,v);
                            String tag=v.getTag().toString();
                            //Toast.makeText(getApplicationContext(),tag,Toast.LENGTH_LONG).show();
                            int end=Integer.parseInt(tag.split("-")[1]);
                            for(int i=Integer.parseInt(tag.split("-")[0]); i<end; i++){
                                content.add(reportItems.get(i).productName+"\t"+"k"+reportItems.get(i).price);
                            }
                            //popupMenu.show();
                            startActivity(new Intent(Reports.this,Menu.class).putExtra("content",(Serializable)content));*/

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
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(Color.parseColor("#0B6623"));
        linearLayout.addView(textView);
        testMonth = month;
      }
      monthlyTotal+=Float.parseFloat(reportItem.price);
      //linearLayout.addView(convertView);
    }

    graphWork();

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

  public void graphWork(){
    setTitle("CubicLineChartActivity");


    seekBarX = findViewById(R.id.seekBar1);
    seekBarY = findViewById(R.id.seekBar2);

    chart = findViewById(R.id.chart1);
    chart.setViewPortOffsets(0, 0, 0, 0);
    chart.setBackgroundColor(Color.rgb(104, 241, 175));

    // no description text
    chart.getDescription().setEnabled(false);

    // enable touch gestures
    chart.setTouchEnabled(true);

    // enable scaling and dragging
    chart.setDragEnabled(true);
    chart.setScaleEnabled(true);

    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(false);

    chart.setDrawGridBackground(false);
    chart.setMaxHighlightDistance(300);

    XAxis x = chart.getXAxis();
    x.setEnabled(false);

    YAxis y = chart.getAxisLeft();
    //y.setTypeface(tfLight);
    y.setLabelCount(6, false);
    y.setTextColor(Color.WHITE);
    y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
    y.setDrawGridLines(false);
    y.setAxisLineColor(Color.WHITE);

    chart.getAxisRight().setEnabled(false);

    // add data
    seekBarY.setOnSeekBarChangeListener(this);
    seekBarX.setOnSeekBarChangeListener(this);

    // lower max, as cubic runs significantly slower than linear
    seekBarX.setMax(700);

    seekBarX.setProgress(45);
    seekBarY.setProgress(100);

    chart.getLegend().setEnabled(false);

    chart.animateXY(2000, 2000);

    // don't forget to refresh the drawing
    chart.invalidate();

  }


  private void setData(int count, float range) {

    LineDataSet set1;

    if (chart.getData() != null &&
            chart.getData().getDataSetCount() > 0) {
      set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
      set1.setValues(values);
      chart.getData().notifyDataChanged();
      chart.notifyDataSetChanged();
    } else {
      // create a dataset and give it a type
      set1 = new LineDataSet(values, "DataSet 1");

      set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
      set1.setCubicIntensity(0.2f);
      set1.setDrawFilled(true);
      set1.setDrawCircles(false);
      set1.setLineWidth(1.8f);
      set1.setCircleRadius(4f);
      set1.setCircleColor(Color.WHITE);
      set1.setHighLightColor(Color.rgb(244, 117, 117));
      set1.setColor(Color.WHITE);
      set1.setFillColor(Color.WHITE);
      set1.setFillAlpha(100);
      set1.setDrawHorizontalHighlightIndicator(false);
      set1.setFillFormatter(new IFillFormatter() {
        @Override
        public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
          return chart.getAxisLeft().getAxisMinimum();
        }
      });

      // create a data object with the data sets
      LineData data = new LineData(set1);
      //data.setValueTypeface(tfLight);
      data.setValueTextSize(9f);
      data.setDrawValues(false);

      // set data
      chart.setData(data);
    }
  }



  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    setData(12, 10);

    // redraw
    chart.invalidate();
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {}

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {}

}



class ReportItem implements Comparable<ReportItem>{
  String productName;
  String price;
  String dateSold;
  String sellerUuid;
  String[] dateSoldItems;

  ReportItem(String productName,String price,String dateSold,String uuid){
    this.productName=productName;
    this.price=price;
    this.dateSold=dateSold;
    this.sellerUuid=uuid;
    dateSoldItems=dateSold.split("-");
  }

  @Override
  public int compareTo(ReportItem o) {

    for(int i=2;i>=0;i--){
      Integer integer1=Integer.parseInt(this.dateSoldItems[i]);
      Integer integer2=Integer.parseInt(o.dateSoldItems[i]);
      int result=integer1.compareTo(integer2);
      if(result==0){
        continue;
      }
      return result;
    }
    return 0;
  }


}
