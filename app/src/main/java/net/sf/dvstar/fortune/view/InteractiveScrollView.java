package net.sf.dvstar.fortune.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;


/**
 * Triggers a event when scrolling reaches bottom.
 *
 * Created by martinsandstrom on 2010-05-12.
 * Updated by martinsandstrom on 2014-07-22.
 *
 * Usage:
 *
 *  scrollView.setOnBottomReachedListener(
 *      new InteractiveScrollView.OnBottomReachedListener() {
 *          @Override
 *          public void onBottomReached() {
 *              // do something
 *          }
 *      }
 *  );
 * 
 *
 * Include in layout:
 *  
 *  <se.marteinn.ui.InteractiveScrollView
 *      android:layout_width="match_parent"
 *      android:layout_height="match_parent" />
 *  
 */
public class InteractiveScrollView extends ScrollView {
    OnBottomReachedListener mListener;

    public InteractiveScrollView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public InteractiveScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InteractiveScrollView(Context context) {
        super(context);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = (View) getChildAt(getChildCount()-1);
        int diff = (view.getBottom()-(getHeight()+getScrollY()));
        
        if (diff == 0 && mListener != null) {
            mListener.onBottomReached();
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }


    // Getters & Setters

    public OnBottomReachedListener getOnBottomReachedListener() {
        return mListener;
    }

    public void setOnBottomReachedListener(
            OnBottomReachedListener onBottomReachedListener) {
        mListener = onBottomReachedListener;
    }
    

    /**
     * Event listener.
     */
    public interface OnBottomReachedListener{
        public void onBottomReached();
    }
    
}