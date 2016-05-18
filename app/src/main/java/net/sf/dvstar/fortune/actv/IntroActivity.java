package net.sf.dvstar.fortune.actv;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;

import net.sf.dvstar.fortune.R;

/**
 * Created by sdv on 18.05.16.
 */
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
       if(mRestoreOrig==true) {
            Drawable introDravable = ResourcesCompat.getDrawable(getResources(), R.drawable.intro_1, null);
            mAuthorImage.setImageDrawable(introDravable);
            mRestoreOrig = false;
        } else mCountClick++;
        if(mCountClick==4) {
            Drawable introDravable = ResourcesCompat.getDrawable(getResources(), R.drawable.intro_2, null);
            mAuthorImage.setImageDrawable(introDravable);
            mCountClick=0;
            mRestoreOrig = true;
        }

    }
}
