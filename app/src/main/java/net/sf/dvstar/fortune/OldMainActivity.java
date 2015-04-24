package net.sf.dvstar.fortune;

import net.sf.dvstar.fortune.actv.SelectFortuneActivity;
import net.sf.dvstar.fortune.data.FortuneDBHelper;
import net.sf.dvstar.fortune.util.SystemUiHider;
import net.sf.dvstar.fortune.util.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class OldMainActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private TextView tvContent;
    private String TAG = "MainActivity";

    private FortuneDBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Start");

        setContentView(R.layout.activity_main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView  = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }

                        Log.d(TAG, "Visible "+visible);

                        if(visible)
                            getActionBar().show();
                        else
                            getActionBar().hide();


                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.button_next).setOnTouchListener(mDelayHideTouchListener);
        tvContent = (TextView) findViewById(R.id.fullscreen_content);
        dbHelper = new FortuneDBHelper(this);
        Log.d(TAG, "onCreate End");
        /*
        tvContent.setOnTouchListener(
                new OnSwipeTouchListener(this) {
                    public void onSwipeRight() {
                        Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                        fillFortuneContent();
                    }
                    public void onSwipeLeft() {
                        Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
                        fillFortuneContent();
                    }
                });
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillFortuneContent();
        Log.d(TAG, "onResume End");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        Log.d(TAG, "onPostCreate End");
        delayedHide(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Toast.makeText(this,
                item.getItemId() + " onOptionsItemSelected" + item.getTitle(),
                Toast.LENGTH_SHORT).show();

        switch (item.getItemId()) {

            case R.id.main_menu_select_fortune: {
                launchSelectFortune();
            }
            break;

            case R.id.main_menu_settings: {
                launchSettings();
            }
            break;

            case R.id.main_menu_about: {
                launchAbout();
            }
            break;

            case R.id.main_menu_exit: {
                finish();
            }
            break;

        }

        return true;
    }

    /**
     * Launch About screen
     */
    private void launchAbout() {

    }

    /**
     * Launch Settings screen
     */
    private void launchSettings() {

    }

    /**
     * Launch Select screen
     */
    private void launchSelectFortune() {
        Intent intent = new Intent(this, SelectFortuneActivity.class );
        this.startActivity(intent);
    }

    /**
     * Fill content from random phrase
     */
    private void fillFortuneContent() {
        String selection = Utils.loadCurrentSelection(this);
        if(tvContent!=null && selection.length()>0){
            int count = dbHelper.getCountRows(FortuneDBHelper.FORTUNE_TABLE_PHRASES);
            Log.d(TAG, Utils.loadCurrentSelection(this)+" record count "+count);
            int id = getRandInt(0, count);
            String phrase = "("+id+")\n"+dbHelper.getPhraseById(id);
            tvContent.setText(phrase);
        }
    }

    public static int getRandInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
/*
                getActionBar().show();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                //getActionBar().hide();
*/
            }
            return false;
        }
    };

    Handler  mHideHandler  = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
            getActionBar().hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void btnNext_Click(View v){
        fillFortuneContent();
    }

    //----------------------------------------------------------------------------------------------
    public void hideTitle() {
        try {
            ((View) findViewById(android.R.id.title).getParent())
                    .setVisibility(View.GONE);
        } catch (Exception e) {
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    public void showTitle() {
        try {
            ((View) findViewById(android.R.id.title).getParent())
                    .setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    private void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

}
