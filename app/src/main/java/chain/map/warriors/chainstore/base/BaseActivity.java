package chain.map.warriors.chainstore.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import chain.map.warriors.chainstore.utils.FragmentProvider;
import chain.map.warriors.chainstore.utils.Navigator;

public abstract class BaseActivity extends AppCompatActivity {
    public abstract int getLayoutResource();
    public abstract void loadControl(Bundle savedInstanceState);
    public abstract int getFragmentContainerViewId();
    private FragmentManager fragmentManager;
    public ProgressDialog mSpinner;
    public void setLoading(boolean isLoading) {
        if (isFinishing())
            return;

        if (this == null)
            return;

        if (isLoading)
            mSpinner.show();
        else
            mSpinner.dismiss();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner = new ProgressDialog(this);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Waiting...");
        mSpinner.setCanceledOnTouchOutside(false);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        loadControl(savedInstanceState);


    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public static boolean isAvailableActivity(Activity activity) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (!activity.isDestroyed()) {
                    return true;
                }
            }
            if (!activity.isFinishing()) {
                return true;
            }
        }
        return false;
    }

    public BaseFragment startFirstFragment() {
        Bundle data = getIntent().getExtras();
        BaseFragment fragment = null;
        if (data != null) {
            String fragmentName = data.getString(Navigator.FRAGMENT_CLASS_NAME_START, "");
            if (!fragmentName.isEmpty()) {
                fragment = FragmentProvider.getFragmentNewInstance(fragmentName, data);
                replaceFragment(fragment);
            }
        }
        return fragment;
    }


    public void addFragment(BaseFragment fragment) {
        if (fragment == null || getFragmentContainerViewId() == 0 || !isAvailableActivity(this))
            return;
        if (fragmentManager == null) fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(getFragmentContainerViewId(), fragment, fragment.getTagName());
        ft.addToBackStack(fragment.getTagName());
        ft.commit();
    }

    public void replaceFragment(BaseFragment fragment) {
        if (fragment == null) return;
        if (fragmentManager == null) fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(getFragmentContainerViewId(), fragment, fragment.getTagName());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }
}
