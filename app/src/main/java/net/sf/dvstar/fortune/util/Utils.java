package net.sf.dvstar.fortune.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by dstarzhynskyi on 08.04.2015.
 */
public class Utils {

    private static final int BUFFER_SIZE = 1024*10;
    private static final String TAG = "Utils";

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }
            }
        }
        finally {
            out.close();
        }
    }

    public static void unzip(String zipFile, String location) throws IOException {
        try {
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream fout = new FileOutputStream(path, false);
                        try {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        } finally {
                            fout.close();
                        }
                    }
                }
            } finally {
                zin.close();
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Unzip exception", e);
        }
    }


    public static List<String> listAssetsFiles(Activity parent, String dirFrom) throws IOException {
        List ret = new ArrayList();
        Resources res = parent.getResources();
        AssetManager am = res.getAssets();
        String fileList[] = am.list(dirFrom);
        Log.d(TAG, "listAssetsFiles : "+fileList.length);
        if (fileList != null)
        {
            for ( int i = 0;i<fileList.length;i++)
            {
                Log.d(TAG, fileList[i]);
                ret.add(fileList[i]);

            }
        }
        return ret;
    }

    public static class FortuneFileItem {
        String mFileName;
        String mFileTitle;
        String mFileFormat="";
        public FortuneFileItem(String vFileName, String vFileTitle){
            mFileName = vFileName;
            mFileTitle = vFileTitle;
        }
        public FortuneFileItem(String vFileName, String vFileTitle, String vFileFormat){
            mFileName = vFileName;
            mFileTitle = vFileTitle;
            mFileFormat = vFileFormat;
        }
        public String toString(){
            return mFileTitle + " ("+mFileName+")";
        }
        public String getFileName(){ return mFileName; }
        public String getFileTitle(){ return mFileTitle;}
        public String getFileFormat(){ return mFileFormat;}
    }


    public static List<FortuneFileItem> listAssetsFileTitles(Activity parent, String dirFrom) throws IOException {
        List ret = new ArrayList();
        Resources res = parent.getResources();
        AssetManager am = res.getAssets();
        String fileList[] = am.list(dirFrom);
        Log.d(TAG, "listAssetsFileTitles : "+fileList.length);
        if (fileList != null)
        {
            for ( int i = 0;i<fileList.length;i++)
            {
                StringBuilder buf=new StringBuilder();
                InputStream json=am.open(dirFrom+"/"+fileList[i]);
                BufferedReader in=
                        new BufferedReader(new InputStreamReader(json, "UTF-8"));
                String str;
                int fIndex=-1;
                if ((str=in.readLine()) != null) {
                    if((fIndex=str.indexOf("FORMAT"))>0){
                        String sTitle  = str.substring(0,fIndex);
                        String sFormat = str.substring(fIndex+1);
                        ret.add(
                                new FortuneFileItem(fileList[i],
                                        sTitle.substring(sTitle.indexOf('=') + 1),
                                        Const.FILE_BLOCK_SEPAR ) //sFormat.substring(sFormat.indexOf('=') + 1))
                        );
                    } else {
                        ret.add(
                                new FortuneFileItem(fileList[i], str.substring(str.indexOf('=') + 1))
                        );
                    }
                }

                in.close();
            }
        }
        return ret;
    }
    public static final String PREF_NAME = "Fortune_Main_Prefs";
    final static String SAVED_TEXT = "fortune_file_name";
    public static void saveCurrentSelection(Activity parent, String item){
        SharedPreferences sPref;
        sPref = parent.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_TEXT, item);
        ed.commit();
    }

    public static String loadCurrentSelection(Activity parent){
        String ret;
        SharedPreferences sPref;
        //sPref = parent.getPreferences(Context.MODE_PRIVATE);
        sPref = parent.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ret = sPref.getString(SAVED_TEXT, "");
        return ret;
    }

}
