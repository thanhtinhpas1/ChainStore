package chain.map.warriors.chainstore.utils;

import android.os.Bundle;

import chain.map.warriors.chainstore.base.BaseFragment;

/**
 * Created by kelvin on 07/07/18.
 */

public class FragmentProvider {
    /**
     * CREATE FRAGMENT
     *
     * @param fragmentClassName
     * @param data
     * @return
     */
    public static BaseFragment getFragmentNewInstance(String fragmentClassName, Bundle data) {
        if (data == null)
            data = new Bundle();

        BaseFragment fragment = null;
        switch (fragmentClassName) {
        }

        if (fragment != null)
            fragment.setArguments(data);

        return fragment;
    }
}
