package net.sf.dvstar.fortune.actv;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import net.sf.dvstar.fortune.R;

/**
 * Created by dstarzhynskyi on 17.04.2015.
 */
public class AboutActivity  extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }


    public void onClickImgMakeDonate(View v){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(getResources().getString(R.string.donate_info));
        dlgAlert.setTitle(getResources().getString(R.string.app_name));
        dlgAlert.setPositiveButton( getResources().getString(android.R.string.ok), null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();    }



}
