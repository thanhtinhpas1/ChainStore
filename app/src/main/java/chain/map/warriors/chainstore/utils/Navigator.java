package chain.map.warriors.chainstore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import javax.inject.Inject;

import chain.map.warriors.chainstore.base.BaseActivity;
/**
 * Created by verztec-mac-02 on 3/1/18.
 */

public class Navigator {
    private static Navigator mInstance = null;
    public static final String FRAGMENT_CLASS_NAME_START = "FRAGMENT_CLASS_NAME_START";

    @Inject
    public Navigator() {
        //empty
    }

    public static Navigator getInstance(){
        if(mInstance == null)
            mInstance = new Navigator();

        return mInstance;
    }

    /**
     * Goes to the MainActivity screen.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void startActivity(Context context, Class<?> activityClass, Bundle data) {
        if (data == null)
            data = new Bundle();

        if (activityClass != null) {
            Intent intent = new Intent(context, activityClass);
            intent.putExtras(data);
            context.startActivity(intent);
        }
    }

    public void startFragmentIntent(Context context, String fragmentClassName, Intent intent, Bundle data) {
        if (data == null)
            data = new Bundle();

        if (intent!=null) {
            if (fragmentClassName != null) {
                data.putString(FRAGMENT_CLASS_NAME_START, fragmentClassName);
            }
            intent.putExtras(data);
            context.startActivity(intent);
        } else {
            if (context instanceof BaseActivity) {
//                ((BaseActivity) context).addFragment(FragmentProvider.getFragmentNewInstance(fragmentClassName, data));
            }
        }
    }

    public void startFragment(Context context, String fragmentClassName, Class<?> activityClass, Bundle data) {
        if (data == null)
            data = new Bundle();

        if (activityClass != null) {
            if (fragmentClassName != null)
                data.putString(FRAGMENT_CLASS_NAME_START, fragmentClassName);

            Intent intent = new Intent(context, activityClass);
            intent.putExtras(data);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        }
    }

    public void startFragmentWithResult(Activity context, String fragmentClassName, Class<?> activityClass, Bundle data, int request) {
        if (data == null)
            data = new Bundle();

        if (activityClass != null) {
            if (fragmentClassName != null)
                data.putString(FRAGMENT_CLASS_NAME_START, fragmentClassName);

            Intent intent = new Intent(context, activityClass);
            intent.putExtras(data);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivityForResult(intent, request);
        }
    }
}
