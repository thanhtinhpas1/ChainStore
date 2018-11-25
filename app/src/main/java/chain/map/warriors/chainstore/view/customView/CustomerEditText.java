package chain.map.warriors.chainstore.view.customView;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class CustomerEditText extends android.support.v7.widget.AppCompatEditText {
    public CustomerEditText(Context context) {
        super(context);
        init();
    }

    public CustomerEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomerEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
            setTypeface(tf);
        }
    }
}
