package com.format.gesturelauncher;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.format.gesturelauncher.MainActivity.finishedSync;
import static com.format.gesturelauncher.MainActivity.main;
import static com.format.gesturelauncher.MainActivity.mobileconnect;
import static com.format.gesturelauncher.MainActivity.versionNote;
import static com.format.gesturelauncher.MainActivity.warningdialog;

public class MobileConnectService extends Service implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{



    public static GestureLibrary lib; //手势库

    static GoogleApiClient mGoogleApiClient; //Wearable

    public static String WEARABLE_PATH = "/gestures"; //发送位置
    public static String MOBILE_RECEIVE = "/receive";//接收位置

    public final static String TAG = "fz"; //调试tag


    Boolean mobileAlone = false; //指示是否是本地模式，或者较老的手表版本
    String PATH;//指示从手表收到的PATH


    public static String[] wearPackList; //手表的程序包列表
    public static String[] wearAppList; //手表的程序包对应的app名列表


    static boolean alreadyCreated =false ;
    static boolean Overwrite=false;//指示是否要覆盖wear

    static int WEAR_VERSION;
    static int MOBILE_VERSION;

    boolean updateInfoShowed =false;

    public MobileConnectService() {
    }

//
//    //---------------------------------------------https://stackoverflow.com/questions/2272378/android-using-method-from-a-service-in-an-activity
//    IBinder mBinder = new LocalBinder();
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    public class LocalBinder extends Binder {
//        public MobileConnectService getServerInstance() {
//            return MobileConnectService.this;
//        }
//    }
//-------------------------------------------------------------

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();

        //-----------------------------------------------------Connect to Wearable

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


//MsgT("Connection");

        //-------------------------------------------手势

        final File mStoreFile = new File(getFilesDir(), "gesturesNew");

        lib = GestureLibraries.fromFile(mStoreFile);//导入手势



        MOBILE_VERSION=BuildConfig.VERSION_CODE;
        WEAR_VERSION=-1;



        alreadyCreated=true;
        mobileconnect=this;
        mGoogleApiClient.connect();
//        Sync(mobileconnect);//尝试同步
    }



    //=============================================================================发送数据给手表

    final static public void Sync(MobileConnectService connect, boolean overwrite){

        finishedSync(false);

        if(connect.mobileAlone || overwrite){ //如果是老版本或者本地运行  ||或者要覆盖
            Overwrite=true;
            connect.sendDataMapToDataLayer("/gestures");//用手机上的lib覆盖手表

        }else {
            connect.sendDataMapToDataLayer("/needupdate"); //请求手表update

        }

    }//外部使用




    public byte[] file2byte(){
        File file = new File(getFilesDir(), "gesturesNew");

        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            for (int i = 0; i < b.length; i++) {
                System.out.print((char)b[i]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        }
        catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
        return b;
    }   //将文件转化成byte以发送到手表

    private DataMap createDatamap() {
        DataMap dataMap = new DataMap();

        dataMap.putByteArray("File",file2byte()); //同步手势文件
        dataMap.putString("Sent","From mobile");
        dataMap.putString("Time", Long.toString(System.currentTimeMillis()) ); //同步时间，改变数据以完成同步
        dataMap.putInt("version",BuildConfig.VERSION_CODE);//发送版本号，手表比手机大1000
        dataMap.putBoolean("overwrite",Overwrite);


        //---------------------------------------------------------------Sync SharedPreference
        SharedPreferences sharedPref = getSharedPreferences("main",MODE_PRIVATE);
        boolean showq = sharedPref.getBoolean("show",true);
        boolean vib = sharedPref.getBoolean("vibrate",true);
        String loca=sharedPref.getString("location","r");
        int accuracy=sharedPref.getInt("accuracy",2);

        dataMap.putBoolean("show",showq);
        dataMap.putBoolean("vibrate",vib);
        dataMap.putString("location",loca);
        dataMap.putInt("accuracy",accuracy);
//--------------------------------------------------

        return dataMap;
    }

    public void sendDataMapToDataLayer(String location) {
        if (mGoogleApiClient.isConnected()) {
            WEARABLE_PATH = location;
            DataMap dataMap = createDatamap();
            new SendDataMapToDataLayer(WEARABLE_PATH, dataMap).start();
        }
    }

    public class SendDataMapToDataLayer extends Thread {
        String path;
        DataMap dataMap;

        public SendDataMapToDataLayer(String path, DataMap dataMap) {
            this.path = path;
            this.dataMap = dataMap;
        }

        public void run() {

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WEARABLE_PATH);
            putDataMapReq.getDataMap().putAll(dataMap);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

            Log.v(TAG, "Sent!");
//
        }
    }


    //=============================================================================




    //-----------------------------------------------------------------------RECEIVE 接收手表的确认数据

    public void byte2FileAndWrite(byte[] fileInBytes){
        String strFilePath = getFilesDir()+"/gesturesNew";
        try {
            FileOutputStream fos = new FileOutputStream(strFilePath);
            //String strContent = "Write File using Java ";

            fos.write(fileInBytes);
            fos.close();
        }
        catch(FileNotFoundException ex)   {
            System.out.println("FileNotFoundException : " + ex);
        }
        catch(IOException ioe)  {
            System.out.println("IOException : " + ioe);
        }

    }  //将收到的byte转换为文件

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {  //数据改变时
        for (DataEvent event : dataEvents) {

            PutDataMapRequest putDataMapRequest =
                    PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.getDataItem()));//!!!!!!!!!!!!!!!!https://medium.com/@manuelvicnt/android-wear-accessing-the-data-layer-api-d64fd55982e3

            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
//                if (item.getUri().getPath().compareTo("/count") == 0) {
//                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//
//                }
//                byte[] data = item.getData();
//                MsgT(data.toString());

//                MsgS(PATH);

                DataMap map = putDataMapRequest.getDataMap();

                PATH=item.getUri().getPath();

                if(PATH.equals(MOBILE_RECEIVE)){   //如果路径等于receive（确保收到的不是自己的而是手表发过来的）
                                //----------------------------------------------------------------------------------------------------------------版本检查

                                 WEAR_VERSION=map.getInt("version");

                                if(WEAR_VERSION!=MOBILE_VERSION+1000){ //如果版本不同则提醒更新
                                    if(map.getInt("version")<2012){ //如果版本过小


            //                            final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            //                            builder.setTitle(R.string.app_name);
            //                            builder.setMessage("Warning: your mobile app is running an old version, which is trying to overwrote your gestures on the watch using the gestures in your phone.\nDo you wish to overwrote?");
            //
            //                            builder.setPositiveButton("Overwrote - use mobile's", new DialogInterface.OnClickListener() {
            //                                public void onClick(DialogInterface dialog, int id) {
            //
            //                                    dialog.cancel();
            //                                }
            //                            });
            //                            builder.setNegativeButton("Cancel - keep watch's", new DialogInterface.OnClickListener() {
            //                                public void onClick(DialogInterface dialog, int id) {
            //
            //                                    dialog.cancel();
            //                                }
            //                            });
            //
            //                            builder.show();
                                        Toast.makeText(this,  "Warning: Gestures overwrote from phone, Wearable app version too old, please update the app on your watch.", Toast.LENGTH_LONG).show();
                                        if(!mobileAlone){
                                            warningdialog();
                                            mobileAlone=true;
                                        }

                                        break;
                                    }else {
//                                        Toast.makeText(this, "Mobile app " + MOBILE_VERSION + "\nWear app " + WEAR_VERSION + "\nVersion not consistent, please update your apps to make sure sync running properly.", Toast.LENGTH_LONG).show();
                                        if(!updateInfoShowed){
                                            versionNote();
                                            updateInfoShowed=true;
                                        }

                                        Sync(mobileconnect,false);//从手表读取覆盖
                                    }
                                }else {
                                    mobileAlone=false;
                                    Sync(mobileconnect,false);//从手表读取覆盖
                                }
                                //----------------------------------------------------------------------------------------------------------------

                    if(mobileAlone) {
                        MsgS("Successfully Synced!", Snackbar.LENGTH_LONG);
                    }



                }else if(PATH.equals("/update")){ //如果路径等于update，则覆盖手机的gestures
                    MsgS(getString(R.string.sync_success), Snackbar.LENGTH_LONG);
                    byte2FileAndWrite(map.getByteArray("updatedlib")); //从手表得到手表的最新的library然后覆盖
                    finishedSync(true);//刷新列表

                    //-------------------------------------------------------------------------------------------------------------Receive pref from phone

                    boolean safe =false;
                    try{

                        if(map.getString("location").length()>0){
                            safe=true;
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
//                        Msg("No pref received");
                    }

                    if(safe) {
                        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("show", map.getBoolean("show"));
                        editor.putBoolean("vibrate", map.getBoolean("vibrate"));
                        editor.putString("location", map.getString("location"));
                        editor.putInt("accuracy", map.getInt("accuracy"));
                        editor.apply();

                    }

//                    LoadPref();
                    //--------------------------------------------------------------

                }


                wearPackList = map.getStringArray("packList");
                wearAppList = map.getStringArray("appList");


            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    //----------------------------------------------------Version notice


//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Wearable.DataApi.removeListener(mGoogleApiClient, this);
//        mGoogleApiClient.disconnect();
//    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
//        refreshGrid();
//        Sync(mobileconnect);

//        if (!lib.load()) {          //必须要这个
//////            startActivity(new Intent(main,WelcomeActivity.class));
////            Intent i = new Intent();
////            i.setClass(this, WelcomeActivity.class);
////            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////            startActivity(i);
//        }else {
        if(lib.load()){
            Overwrite=false;
            sendDataMapToDataLayer("/gestures");//首次向手表同步，确认版本号

            finishedSync(false);//首次同步，false表示未同步可见
//            MsgT("yes");
        }else {
//            startActivity(new Intent(main,WelcomeActivity.class));
            Intent i = new Intent();
            i.setClass(this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }



    }

    @Override
    public void onConnectionSuspended(int i) {
        MsgT("Connection suspended");
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        MsgT("Connection failed");
    }

    //-----------------------------------------------------------------------


    //-----------------------------------------------------------------------


    public void MsgT(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void MsgS(String message,int Duration){
        try {
            Snackbar.make(main.findViewById(R.id.fab), message, Duration).show();
        }catch (NullPointerException E){
            E.printStackTrace();
        }
    }



}
