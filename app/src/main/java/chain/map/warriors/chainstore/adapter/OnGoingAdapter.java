package chain.map.warriors.chainstore.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import chain.map.warriors.chainstore.R;
import chain.map.warriors.chainstore.model.OnGoing;
import chain.map.warriors.chainstore.model.OnGoingViewHolders;

public class OnGoingAdapter extends RecyclerView.Adapter<OnGoingViewHolders>{

    private List<OnGoing> itemList;
    Context context;
    public OnGoingAdapter(List<OnGoing> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public OnGoingViewHolders onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ongoing_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        OnGoingViewHolders ogv = new OnGoingViewHolders(layoutView);
        return ogv;
    }

    @Override
    public void onBindViewHolder(@NonNull OnGoingViewHolders onGoingViewHolders, int i) {
        onGoingViewHolders.mDestination.setText(itemList.get(i).getDestination());
        if (itemList.get(i).getTime() != null) {
            onGoingViewHolders.mTime.setText(itemList.get(i).getTime());
        }
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
