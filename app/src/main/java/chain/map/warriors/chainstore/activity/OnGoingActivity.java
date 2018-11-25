package chain.map.warriors.chainstore.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import chain.map.warriors.chainstore.R;
import chain.map.warriors.chainstore.adapter.OnGoingAdapter;
import chain.map.warriors.chainstore.base.BaseActivity;
import chain.map.warriors.chainstore.model.OnGoing;

public class OnGoingActivity extends BaseActivity {
    private String storeId, shipperId;
    private RecyclerView mRecycleView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public int getLayoutResource() {
        return R.layout.ongoing_activity;
    }

    private void getShipperOnGoingIds() {
        //DatabaseReference shipperOnGoing = FirebaseDatabase.getInstance().getReference().child("Users").child(shipperId);
    }

    @Override
    public void loadControl(Bundle savedInstanceState) {
        mRecycleView = findViewById(R.id.rcView_onGoing);
        mRecycleView.setNestedScrollingEnabled(false);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(OnGoingActivity.this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdapter = new OnGoingAdapter(getDataSetOnGoing(), OnGoingActivity.this);
        mRecycleView.setAdapter(mAdapter);

        for(int i = 0; i < 100; i++) {
            OnGoing obj = new OnGoing(Integer.toString(i), "");
            resultOnGoing.add(obj);
        }
    }

    private ArrayList resultOnGoing = new ArrayList<OnGoing>();
    private List<OnGoing> getDataSetOnGoing() {

        return resultOnGoing;
    }

    @Override
    public int getFragmentContainerViewId() {
        return 0;
    }
}
