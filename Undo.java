package g.o.gotechpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import co.dift.ui.SwipeToAction;

public class Undo extends AppCompatActivity {
    RecyclerView recyclerView;
    MyAdapter adapter;
    SwipeToAction swipeToAction;

    private List<String> productName=new ArrayList<String>();
    private List<String> productPrice=new ArrayList<String>();
    private List<String> date=new ArrayList<String>();
    private List<String> removed=new ArrayList<String>();

    public void onConfirm(View view){
        Intent i=new Intent();
        i.putExtra("names", (Serializable) productName);
        i.putExtra("prices", (Serializable) productPrice);
        i.putExtra("dates",(Serializable)date);
        i.putExtra("removed",(Serializable)removed);

        setResult(RESULT_OK,i);
        finish();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.undo_inteface);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView=findViewById(R.id.undo_list);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        Intent intent=getIntent();

        productName=(List<String>) intent.getSerializableExtra("names");
        productPrice=(List<String>) intent.getSerializableExtra("prices");
        date=(List<String>) intent.getSerializableExtra("dates");
        adapter = new MyAdapter(productName,productPrice,date);
        recyclerView.setAdapter(adapter);

        swipeToAction = new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<String>() {
            @Override
            public boolean swipeLeft(final String itemData) {
                final int pos = productName.indexOf(itemData);
                removed.add(productPrice.get(pos));
                productName.remove(pos);
                productPrice.remove(pos);
                date.remove(pos);
                adapter.notifyItemRemoved(pos);
                return true;
            }

            @Override
            public boolean swipeRight(String itemData) {
                final int pos = productName.indexOf(itemData);
                removed.add(productPrice.get(pos));
                productName.remove(pos);
                productPrice.remove(pos);
                date.remove(pos);
                adapter.notifyItemRemoved(pos);
                return true;
            }

            @Override
            public void onClick(String itemData) {

            }

            @Override
            public void onLongClick(String  itemData) {

            }
        });
    }
}

class MyAdapter extends RecyclerView.Adapter{
    List<String> names;
    List<String> prices;
    List<String> dates;

    /** References to the views for each data item **/
    public class Holder extends SwipeToAction.ViewHolder<String> {
        public TextView titleView;


        public Holder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.title);
        }
    }

    public MyAdapter(List<String> names,List<String> prices,List<String> dates){
        this.names=names;
        this.prices=prices;
        this.dates=dates;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
        String s = names.get(i);
        Holder vh = (Holder) holder;
        vh.titleView.setText(names.get(i)+"   "+"k"+prices.get(i));
        vh.data = s;
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.undo_item, parent, false);

        return new Holder(view);
    }
}


