package net.sf.dvstar.fortune.actv;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.content.Context;

import net.sf.dvstar.fortune.R;

public class IntroActivity extends Activity implements View.OnClickListener{
    private ImageView mAuthorImage;
    private int mCountClick=0;
    private boolean mRestoreOrig=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        mAuthorImage = (ImageView) findViewById(R.id.imageView);
        mAuthorImage.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
       Drawable introDravable;
       if(mRestoreOrig==true) {
            introDravable = ResourcesCompat.getDrawable(getResources(), R.drawable.intro_1, null);
            //mAuthorImage.setImageDrawable(introDravable);
            imageViewAnimatedChange(this, mAuthorImage, introDravable);
            mRestoreOrig = false;
        } else mCountClick++;
        if(mCountClick==4) {
            introDravable = ResourcesCompat.getDrawable(getResources(), R.drawable.intro_2, null);
            //mAuthorImage.setImageDrawable(introDravable);
            imageViewAnimatedChange(this, mAuthorImage, introDravable);
            mCountClick=0;
            mRestoreOrig = true;
        }

    }

    /**
     *
     * @param context
     * @param imageView
     * @param newDrawable
     */
    public static void imageViewAnimatedChange(Context context, final ImageView imageView, final Drawable newDrawable) {
        final Animation anim_out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        anim_out.setAnimationListener(new AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                imageView.setImageDrawable(newDrawable);
                anim_in.setAnimationListener(new AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                imageView.startAnimation(anim_in);
            }
        });
        imageView.startAnimation(anim_out);
    }


}
