package chain.map.warriors.chainstore.model;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import chain.map.warriors.chainstore.R;
import chain.map.warriors.chainstore.activity.SingleOnGoing;
import chain.map.warriors.chainstore.utils.Navigator;
import chain.map.warriors.chainstore.view.customView.TextViewBold;

public class OnGoingViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextViewBold mDestination, mTime;

    public OnGoingViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        mDestination = itemView.findViewById(R.id.item_destination);
        mTime = itemView.findViewById(R.id.item_time);
    }

    @Override
    public void onClick(View v) {
        Bundle b = new Bundle();
        b.putString("destination", mDestination.getText().toString());
        Navigator.getInstance().startActivity(v.getContext(), SingleOnGoing.class, b);
    }
}
