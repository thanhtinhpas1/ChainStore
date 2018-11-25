package chain.map.warriors.chainstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import chain.map.warriors.chainstore.R;
import chain.map.warriors.chainstore.base.BaseActivity;


public class ChooseRoleActivity extends BaseActivity{

    @Override
    public int getLayoutResource() {
        return  R.layout.base_activity;
    }

    @Override
    public void loadControl(Bundle savedInstanceState) {
        startFirstFragment();
    }

    @Override
    public int getFragmentContainerViewId() {
        return R.id.container;
    }

    boolean twice;
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Toast.makeText(ChooseRoleActivity.this,R.string.Exit, Toast.LENGTH_SHORT).show();

        if(twice == true)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishAffinity();

            finish();

            System.exit(0);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                twice = false;
            }
        }, 2000);
        twice = true;
    }

}
