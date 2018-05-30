package com.format.gesturelauncher;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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

import static com.format.gesturelauncher.MainActivity.finishedSync;
import static com.format.gesturelauncher.MainActivity.main;
import static com.format.gesturelauncher.MainActivity.mobileconnect;
import static com.format.gesturelauncher.MainActivity.versionNote;

public class MobileConnectService extends Service implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{



    public static GestureLibrary lib; //gesture lib

    static GoogleApiClient mGoogleApiClient; //Wearable

    public static String WEARABLE_PATH = "/gestures"; //Send location
    public static String MOBILE_RECEIVE = "/receive";//receive location

    public final static String TAG = "fz"; //tag


    Boolean mobileAlone = false; //not in use currently, thought that maybe phone app can be used alone
    String PATH;//PATH from wear


    public static String[] wearPackList; //wear package names
    public static String[] wearAppList; //wear package names corresponding app labels


    static boolean alreadyCreated =false ;
    static boolean Overwrite=false;//overwrite wear's library or not

    static int WEAR_VERSION;
    static int MOBILE_VERSION;

    boolean updateInfoShowed =false;

    public Tracker mTracker;

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


        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //-----------------------------------------------------Connect to Wearable

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


//MsgT("Connection");

        //-------------------------------------------Gestures

        final File mStoreFile = new File(getFilesDir(), "gesturesNew");

        lib = GestureLibraries.fromFile(mStoreFile);//Import



        MOBILE_VERSION=BuildConfig.VERSION_CODE;
        WEAR_VERSION=-1;



        alreadyCreated=true;
        mobileconnect=this;
        mGoogleApiClient.connect();
//        Sync(mobileconnect);//尝试同步
    }



    //=============================================================================Send data to wear

    final static public void Sync(MobileConnectService connect, boolean overwrite){

        finishedSync(false);

        if(connect.mobileAlone || overwrite){ //if running old version or need overwrite
            Overwrite=true;
            connect.sendDataMapToDataLayer("/gestures");//overwrite wear using mobile's lib

        }else {
            connect.sendDataMapToDataLayer("/needupdate"); //request wear lib to overwrite mobile

        }

    }//used outside this service




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
    }   //transfer the gesture file to bytes to send as a data

    private DataMap createDatamap() {
        DataMap dataMap = new DataMap();

        dataMap.putByteArray("File",file2byte()); //Sync gesture in bytes
        dataMap.putString("Sent","From mobile");
        dataMap.putString("Time", Long.toString(System.currentTimeMillis()) ); //send different time to complete sync every time
        dataMap.putInt("version",BuildConfig.VERSION_CODE);//send version，wear = mobile + 1000
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




    //-----------------------------------------------------------------------RECEIVE from wear

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

    }  //get byte transfered to file

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {

            PutDataMapRequest putDataMapRequest =
                    PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.getDataItem()));//!!!!!!!!!!!!!!!!https://medium.com/@manuelvicnt/android-wear-accessing-the-data-layer-api-d64fd55982e3

            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();


                DataMap map = putDataMapRequest.getDataMap();

                PATH=item.getUri().getPath();


                //Analytics
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Mobile Sync")
                        .setAction("mobileSyncDataChanged")
                        .setLabel(PATH)
                        .build());

                if(PATH.equals(MOBILE_RECEIVE)){   //If the path equals 'receive'（To make sure the receved data isn't send by mobile itself but by wear）
                                //----------------------------------------------------------------------------------------------------------------Version check

                                 WEAR_VERSION=map.getInt("version");

                                if(WEAR_VERSION!=MOBILE_VERSION+1000){ //If version inconsistent

//                                        Toast.makeText(this, "Mobile app " + MOBILE_VERSION + "\nWear app " + WEAR_VERSION + "\nVersion not consistent, please update your apps to make sure sync running properly.", Toast.LENGTH_LONG).show();
                                        if(!updateInfoShowed){
                                            try{
                                                versionNote();
                                                updateInfoShowed=true;
                                            }catch (NullPointerException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        Sync(mobileconnect,false);//overwrite from wear

                                }else {
                                    mobileAlone=false;
                                    Sync(mobileconnect,false);//overwrite from wear
                                }
                                //----------------------------------------------------------------------------------------------------------------

                    if(mobileAlone) {
                        MsgS("Successfully Synced!", Snackbar.LENGTH_LONG);
                    }



                }else if(PATH.equals("/update")){ //if path = update, overwrite phone gestures from wear
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

                }else if(PATH.equals("/action")){


                    openRequest(map.getString("action"));
                }


                wearPackList = map.getStringArray("packList");
                wearAppList = map.getStringArray("appList");


            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    //----------------------------------------------------Version notice



    //-------------------------------------------------------------------------------Open apps and Tasker

    public void openRequest(String action){
        NameFilter filter= new NameFilter(action);
        if(filter.getMethod().equals("mapp")){
            Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(filter.getPackName());

            Log.v("mapp",filter.getPackName());
            try {
                startActivity(LaunchIntent);
                MsgT(String.format(getString(R.string.receiver_open_m_app), filter.getFilteredName()));
            }catch (Exception e){
                MsgT(String.format(getString(R.string.receiver_open_app_failed), filter.getFilteredName()));
                e.printStackTrace();
            }


        }else if(filter.getMethod().equals("tasker")){
            runTakser(filter.getPackName());
            MsgT(String.format(getString(R.string.receiver_tasker_task), filter.getFilteredName()));
        }

        //Analytics
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Mobile Sync")
                .setAction("mobileAction")
                .setLabel(action)
                .build());

    }

    public void runTakser(String taskName){
        if ( TaskerIntent.testStatus( getApplicationContext() ).equals( TaskerIntent.Status.OK ) ) {
            TaskerIntent i = new TaskerIntent( taskName);
            sendBroadcast( i );
        }else {
            Toast.makeText(getApplicationContext(),getString(R.string.receiver_tasker_error)+TaskerIntent.testStatus( getApplicationContext() ).toString() , Toast.LENGTH_LONG).show();//"Sorry, please check your Tasker preference to open external access"
            //Analytics
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Mobile Tasker")
                    .setAction("taskerError")
                    .setLabel(TaskerIntent.testStatus( getApplicationContext() ).toString())
                    .build());
        }
    }

    //===============================================================


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
            sendDataMapToDataLayer("/gestures");//first sync to wear, confirm version code

            finishedSync(false);//false makes the syncing bar visible in main activity
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
        MsgT(getString(R.string.receiver_connection_suspend));
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
