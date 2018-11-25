package chain.map.warriors.chainstore.activity;

import android.os.Bundle;

import chain.map.warriors.chainstore.R;
import chain.map.warriors.chainstore.base.BaseActivity;

public class RegisterActivity extends BaseActivity{

    @Override
    public int getLayoutResource() {
        return R.layout.register;
    }

    @Override
    public void loadControl(Bundle savedInstanceState) {

    }

    @Override
    public int getFragmentContainerViewId() {
        return 0;
    }
}
