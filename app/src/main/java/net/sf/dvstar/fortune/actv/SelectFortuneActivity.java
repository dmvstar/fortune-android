package net.sf.dvstar.fortune.actv;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.dvstar.fortune.R;
import net.sf.dvstar.fortune.data.FortuneDBHelper;
import net.sf.dvstar.fortune.util.Const;
import net.sf.dvstar.fortune.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

/**
 * Created by dstarzhynskyi on 09.04.2015.
 */
public class SelectFortuneActivity extends ListActivity {
    private static final String TAG = "SelectFortuneActivity";
    private TextView content;

    SelectFortuneActivity selfParent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfParent = this;
        Log.d(TAG, "onCreate Start");

        setContentView(R.layout.activity_select_fortune);

        content = (TextView) findViewById(R.id.output);

        try {
            ArrayAdapter<Utils.FortuneFileItem> adapter = new ArrayAdapter<Utils.FortuneFileItem>(this,
                    android.R.layout.simple_list_item_1, Utils.listAssetsFileTitles(this, "fortune"));

            setListAdapter(adapter);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onListItemClick(final ListView l, View v, final int position, long id) {

        super.onListItemClick(l, v, position, id);
        Log.d(TAG, "onListItemClick [" + position + "][" + id + "]");

        // ListView Clicked item index
        int itemPosition = position;

        if (l != null && content != null) {
            // ListView Clicked item value

            String itemValue = ((Utils.FortuneFileItem) l.getItemAtPosition(position)).getFileName();
            //content.setText("Click : \n  Position :" + itemPosition + "  \n  ListItem : " + itemValue);
            /*
            Toast.makeText(this,
                    "Click : \n  Position :" + itemPosition + "  \n  ListItem : " + itemValue,
                    Toast.LENGTH_SHORT).show();
            */
            final Handler updateBarHandler;
            final ProgressDialog progressDialog;

                progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);
                progressDialog.setTitle(
                        getResources().getString(R.string.dialog_message_load_title)
                );
                progressDialog.setMessage(
                        getResources().getString(R.string.dialog_message_load_fortune)
                );
                progressDialog.setIndeterminate(true);
                //progressDialog.setProgressDrawable( getResources().getDrawable(R.drawable.custom_progress_circle));
                progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.custom_progress_circle_0));
                progressDialog.show();

                updateBarHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //update UI here depending on what message is received.\
                    String str = msg.getData().getString("message", "Finish");
                    //Log.d(TAG, "handleMessage=["+msg.what+"] "+str);
                    if(msg.what != 0){
                        progressDialog.setMessage(str);
                    }
                    switch(msg.what){
                        case 0:
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //progressDialog.dismiss();
                    }
                    super.handleMessage(msg);
                }
                };


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loadFortuneFile(updateBarHandler, (Utils.FortuneFileItem) l.getItemAtPosition(position), "fortune");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        } catch (Exception e) {

                        } finally {
                            updateBarHandler.sendEmptyMessage(0);
                            progressDialog.dismiss();
                            selfParent.finish();
                        }
                    }
                }).start();

        Utils.saveCurrentSelection(this, itemValue);
        }
    }


    private void threadMsg(Handler handler, String msg, int cnt) {
        if (!msg.equals(null) && !msg.equals("")) {
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("message", msg);
            msgObj.setData(b);
            msgObj.what = cnt;
            handler.sendMessage(msgObj);
        }
    }


    private void loadFortuneFile(Handler handler, Utils.FortuneFileItem fortuneFileItem, String dirFrom) throws IOException {
        FortuneDBHelper dbHelper = new FortuneDBHelper(this);
        dbHelper.removeAll(FortuneDBHelper.FORTUNE_TABLE_PHRASES);
        Resources res = getResources();
        AssetManager am = res.getAssets();
        StringBuilder buf = new StringBuilder();
        InputStream is = am.open(dirFrom + "/" + fortuneFileItem.getFileName());
        BufferedReader br =
                new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String template = getResources().getString(R.string.dialog_message_load_fortune);
        String str;
        int id = 1;

        String strAll = "";
        while ((str = br.readLine()) != null) {

            threadMsg(handler, "[" + id + "] "+template, id);
            if (str.contains("TITLE=")) continue;
            if(fortuneFileItem.getFileFormat().equals(Const.FILE_BLOCK_SEPAR)) {
               if(str.equals(Const.FILE_BLOCK_SEPAR)){
                   dbHelper.addItem(FortuneDBHelper.FORTUNE_TABLE_PHRASES, id, strAll);
                   strAll = "";
                   id ++;
               } else {
                   strAll += str + "\n";
               }
            } else {
                dbHelper.addItem(FortuneDBHelper.FORTUNE_TABLE_PHRASES, id, str);
                id ++;
            }
        }
        int count = dbHelper.getCountRows(FortuneDBHelper.FORTUNE_TABLE_PHRASES);
        Log.w(TAG,"Loaded :"+count+" rows.");
        threadMsg(handler, "Loaded :" + count+" rows." ,count);

        br.close();
        is.close();

    }


    //static inner class doesn't hold an implicit reference to the outer class
    private static class HandlerProgress extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<SelectFortuneActivity> myClassWeakReference;

        public HandlerProgress(SelectFortuneActivity myClassInstance) {
            myClassWeakReference = new WeakReference<SelectFortuneActivity>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            SelectFortuneActivity myClass = myClassWeakReference.get();
            if (myClass != null) {

            }
        }
    }

    /**
     * An example getter to provide it to some external class
     * or just use 'new MyHandler(this)' if you are using it internally.
     * If you only use it internally you might even want it as final member:
     * private final MyHandler mHandler = new MyHandler(this);
     */
    public Handler getHandler() {
        return new HandlerProgress(this);
    }


}

