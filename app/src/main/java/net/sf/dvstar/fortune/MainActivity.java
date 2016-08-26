package net.sf.dvstar.fortune;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.dvstar.fortune.actv.AboutActivity;
import net.sf.dvstar.fortune.actv.ConfigActivity;
import net.sf.dvstar.fortune.actv.OnSwipeTouchListener;
import net.sf.dvstar.fortune.actv.SelectFortuneActivity;
import net.sf.dvstar.fortune.data.FortuneDBHelper;
import net.sf.dvstar.fortune.util.SystemUiHider;
import net.sf.dvstar.fortune.util.Utils;
import net.sf.dvstar.fortune.view.ScrollingTextView;

import java.util.Random;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements View.OnClickListener {

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
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;
    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };
    int mCheckerSteps;
    private boolean mStartStopShow = false;
    private FortuneDBHelper mDbHelper;
    private SystemUiHider mSystemUiHider;
    private TextView mTvContent;
    private ScrollView mTvContentScroll;
    private String TAG = "MainActivity";
    private TextView mTvStatus;
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandlerFortuneShow;
    private Handler mHandlerFortuneDelay;

    /**
     * Get random (pseudo) from diapason
     * @param min min value
     * @param max max value
     * @return result
     */
    public static int getRandInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View statusView = findViewById(R.id.fullscreen_content_status);
        final View contentView = findViewById(R.id.fullscreen_content);
        mTvContentScroll = (ScrollView) findViewById(R.id.scrollView);

        if(mTvContent instanceof ScrollingTextView) {
            ScrollingTextView mTvContentSV = (ScrollingTextView) mTvContent;
            mTvContentSV.setSpeed( 30.0f );
        }

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

                        Log.v(TAG, "Visible="+visible);
                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }

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
                Log.v(TAG, "setOnClickListener="+TOGGLE_ON_CLICK);
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
        mTvContent = (TextView) findViewById(R.id.fullscreen_content);
        mTvStatus = (TextView) findViewById(R.id.fullscreen_content_status);
        mDbHelper = new FortuneDBHelper(this);
        setContentFontParams();
        mHandlerFortuneShow = new Handler();
        mHandlerFortuneDelay = new Handler();

        mTvContent.setOnClickListener(this);

        mTvContent.setOnTouchListener(
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

        mTvContentScroll.setOnTouchListener(
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

        Log.d(TAG, "onCreate End");
    }

    private void setContentFontParams(){
        if(mTvContent ==null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int tvContentFontSize = Integer.parseInt(preferences.getString(getResources().getString(R.string.pref_maintext_size),"30"));
        mTvContent.setTextSize(tvContentFontSize);
        int tvContentFontStyle = getFontStyle(preferences.getString(getResources().getString(R.string.pref_maintext_style), "Normal"));
        mTvContent.setTypeface(null, tvContentFontStyle);
    }

    private int getFontStyle(String style) {
        int ret = 0;
        try {
            ret = Integer.parseInt(style);//Typeface.BOLD;
        } catch (Exception e) {
            ret = 0;
        }
        Log.v(TAG, "getFontStyle("+style+")="+ret);
        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentFontParams();
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
        Log.v(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        Toast.makeText(this,
                item.getItemId() + " onOptionsItemSelected" + item.getTitle(),
                Toast.LENGTH_SHORT).show();
        */
        switch (item.getItemId()) {


            case R.id.main_menu_fortune_show: {
                showFortune();
            }
            break;

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
     * Start stop slide show
     */
    private void showFortune() {
        String message;
        if(!mStartStopShow){
            mStartStopShow = true ;
            mTvStatus.setText(""+(mInterval/1000));
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mInterval = Integer.parseInt(preferences.getString(getResources().getString(R.string.pref_slideshow_delay),"3000"));
            startRepeatingTask();
            message = getResources().getString(R.string.meaage_start_show);
        }
        else {
            mStartStopShow = false;
            stopRepeatingTask();
            mTvStatus.setText("");
            message = getResources().getString(R.string.meaage_stop_show);
        }
        Toast.makeText(this,
                message,
                Toast.LENGTH_SHORT).show();
        Log.v(TAG, "showFortune [" + mStartStopShow + "] "+message);
    }

    Runnable vStatusDelay = new Runnable() {
        @Override
        public void run() {
            fillFortuneContent();
            mCheckerSteps = mInterval/1000;
            startStatusTask();
            Log.v(TAG,"vStatusDelay="+mCheckerSteps);
            mHandlerFortuneShow.postDelayed(vStatusDelay, mInterval);
        }
    };

    Runnable vStatusChecker = new Runnable() {
                @Override
                public void run() {
                        //mHandlerFortuneDelay.postDelayed(this, 1000);
                        //while(updateStatus()){
                            if(updateStatus())
                                mHandlerFortuneDelay.postDelayed(vStatusChecker, 1000);
                            else
                                mHandlerFortuneDelay.removeCallbacks(vStatusChecker);
                        //}
                        //this function can change value of mInterval.
                    //mHandlerFortuneDelay.postDelayed(vStatusChecker, 1000);
                }
    };

    private boolean updateStatus() {
        boolean ret = true;

        if(mCheckerSteps>0){
            mTvStatus.setText(""+mCheckerSteps);
            mCheckerSteps--;
        }
        if(mCheckerSteps<=0) ret = false;

        Log.v(TAG,"mCheckerSteps ["+ret+"]="+mCheckerSteps);

        return ret;
    }

    void startRepeatingTask() {
        vStatusDelay.run();
    }

    void startStatusTask() {
        vStatusChecker.run();
    }
    void stopStatusTask() {
        mHandlerFortuneDelay.removeCallbacks(vStatusChecker);
    }
    void stopRepeatingTask() {
        mHandlerFortuneShow.removeCallbacks(vStatusDelay);
        mHandlerFortuneDelay.removeCallbacks(vStatusChecker);
    }

    /**
     * Launch About screen
     */
    private void launchAbout() {
        Intent intent = new Intent(this, AboutActivity.class );
        this.startActivity(intent);
    }

    /**
     * Launch Settings screen
     */
    private void launchSettings() {
        // Display the fragment as the main content.
        Intent intent = new Intent(this, ConfigActivity.class );
        this.startActivity(intent);
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
        if(mTvContent !=null && selection.length()>0){
            mTvContent.setText("");
            int count = mDbHelper.getCountRows(FortuneDBHelper.FORTUNE_TABLE_PHRASES);
            Log.d(TAG, Utils.loadCurrentSelection(this)+" record count "+count);
            int id = getRandInt(1, count);
            String phrase = "("+id+")\n\n"+ mDbHelper.getPhraseById(id);
            mTvContent.setText(phrase);
            mTvContentScroll.scrollTo(0, 0);

            /*
            if(mTvContent instanceof ScrollingTextView) {
                ScrollingTextView mTvContentSV = (ScrollingTextView) mTvContent;
                mTvContentSV.setStartHeight( mTvContentScroll.getHeight() );
            } else {
                mTvContentScroll.fullScroll(ScrollView.FOCUS_DOWN);
                mTvContentScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //replace this line to scroll up or down
                        mTvContentScroll.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }, 100L);
            }
            */

        }
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void onClickBtnNext(View v){
        //final TextView controlsView = (TextView) findViewById(R.id.fullscreen_content);
        //controlsView.setText("weqwe qwe qweqe qwe qwe qweqw eqwe qwe qwe qweqwe qwe qwe ");
        fillFortuneContent();
    }


    @Override
    public void onClick(View v) {
        fillFortuneContent();
    }
}
