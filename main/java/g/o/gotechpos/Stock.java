package g.o.gotechpos;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Stock extends AppCompatActivity {
    private List<String> productName;
    private List<String> productCount;
    private ListView listViewStock;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock);


        //ToDo:Replace productName, productCount with real world data
        productName=new ArrayList<String>(Arrays.asList("Adidas spray","Tennis biscuit","Dettol Soap","Boom Washing Soap","Fanta","Pure Joy","Tissue","Milkit","Cooking oil","Sugar","Macaroni"));
        productCount=new ArrayList<String>(Arrays.asList("12","47","10","2","67","22","64","38","55","0","22"));


        listViewStock=findViewById(R.id.listview_stock);
        listViewStock.setAdapter(new StockAdapter(Stock.this,productName,productCount));
    }



}

class StockAdapter extends ArrayAdapter{
    private List<String> productName;
    private List<String> productCount;

    public StockAdapter(Context context, List<String> productName, List<String> productCount){
        super(context,android.R.layout.simple_list_item_1,productName);
        this.productName=productName;
        this.productCount=productCount;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflator=(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View stockItem=inflator.inflate(R.layout.stock_item,null,true);
        TextView productName=stockItem.findViewById(R.id.product_name);
        TextView productCount=stockItem.findViewById(R.id.product_count);
        productName.setText(this.productName.get(position));
        productCount.setText(this.productCount.get(position));
        if(Float.parseFloat(this.productCount.get(position))==0){
            View line=stockItem.findViewById(R.id.line);
            line.setBackgroundColor(Color.parseColor("#FF0000"));
        }
        else if(Float.parseFloat(this.productCount.get(position))<20){
            View line=stockItem.findViewById(R.id.line);
            line.setBackgroundColor(Color.parseColor("#FFA500"));
        }

        return stockItem;
    }

}
