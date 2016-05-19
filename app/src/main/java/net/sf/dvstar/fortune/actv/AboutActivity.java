package net.sf.dvstar.fortune.actv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.sf.dvstar.fortune.R;

/**
 * Created by dstarzhynskyi on 17.04.2015.
 */
public class AboutActivity  extends Activity implements View.OnClickListener {
    private TextView mTvAuthorName;
    private ImageView mIvAuthorTace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mTvAuthorName = (TextView) findViewById(R.id.lbAuthorName);
        mIvAuthorTace = (ImageView) findViewById(R.id.imageViewAuthor);
        mTvAuthorName.setOnClickListener(this);
        mIvAuthorTace.setOnClickListener(this);
    }


    public void onClickImgMakeDonate(View v){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(getResources().getString(R.string.donate_info));
        dlgAlert.setTitle(getResources().getString(R.string.app_name));
        dlgAlert.setPositiveButton( getResources().getString(android.R.string.ok), null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();    }


    @Override
    public void onClick(View v) {
        showIntroActivity();
    }

    private void showIntroActivity() {

        Intent intent = new Intent(this, IntroActivity.class );
        this.startActivity(intent);
    }
}
