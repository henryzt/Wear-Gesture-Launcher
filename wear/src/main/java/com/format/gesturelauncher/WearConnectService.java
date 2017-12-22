package com.format.gesturelauncher;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
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
import java.util.List;
import java.util.Set;


public class WearConnectService extends Service implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    String PATH;
    String MOBILE_RECEIVED = "/receive";


//    GestureLibrary libfromFile; //手势库
    GestureLibrary libInitial; //初始手势库


    public static GestureLibrary lib; //定义手势库

    static boolean alreadyCreated ;


    String TAG ="fzg";
    byte[] fileInBytes;
    static String[] packNameList;//包名
    static String[] appNameList;//包对应程序名

    static int WEAR_VERSION;
    static int MOBILE_VERSION;


    static boolean compatibleMode=false;


    static boolean ShowQuickLauncher;
    static boolean vibratorOn;
    static String location ;
    static int accuracy ;



    public WearConnectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onCreate() {
        super.onCreate();


        mGoogleApiClient = new GoogleApiClient.Builder(this)  //创建谷歌事件
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        alreadyCreated = true;



        MainActivity.wearconnect=this;//A static method cannot call a non-static method, but we can use a reference, which include a non-static method to the static method.


        WEAR_VERSION=BuildConfig.VERSION_CODE;
        MOBILE_VERSION=0;

        LoadLibrary();


    }


    //=====================================================================================================================================================================================

    //---------------------------------------------------加载lib
    public void LoadLibrary(){
        final File mStoreFile = new File(getFilesDir(), "gestureNew");
        lib= GestureLibraries.fromFile(mStoreFile);

        getApplicationPacklist();//取包名'


        if (!lib.load()) {          //必须要这个

//            Toast.makeText(getApplicationContext(), R.string.empty_lib,Toast.LENGTH_LONG).show();
            firstInitiate();
        }

        LoadPref();

    }

    //---------------------------------------------------加载preference
    public void LoadPref() {
        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        ShowQuickLauncher = sharedPref.getBoolean("show", true);
        vibratorOn = sharedPref.getBoolean("vibrate", true);
        location = sharedPref.getString("location", "r");
        accuracy = sharedPref.getInt("accuracy", 2);
    }


    //----------------------------------------------------------------------------------------------------------First time run
    public static void  reload(WearConnectService connect){

        connect.firstInitiate();

        connect.sendDataMapToDataLayerForMobile("/receive");//发送给手机接收确认

    }


    public void firstInitiate(){
        //-------------------------------------------手势

        final File mStoreFile = new File(getFilesDir(), "gestureNew");

        if(!mStoreFile.exists()) { //如果文件不存在就创建一个
            try {
                FileOutputStream stream = new FileOutputStream(mStoreFile);
                stream.write("".getBytes());
                stream.close();
//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mStoreFile);
//                outputStreamWriter.write(data);
//                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }


        lib = GestureLibraries.fromFile(mStoreFile);//从文件导入手势
//        if (!libfromFile.load()) {    //如果加载不成功（文件为空）

        libInitial= GestureLibraries.fromRawResource(this,R.raw.gestureini );//从raw文件导入预录的手势

        if(!libInitial.load()){ //如果预录手势加载不成功
            Toast.makeText(this,"Fatal error (Initial gesture not found). Please contact the developer.",Toast.LENGTH_LONG).show();
//            finish();
        }


        //-------------------------------------------------------------------------------------------------------
        Set<String> gestureNameSet = libInitial.getGestureEntries(); //获得所有手势的名称
        for (String gestureName : gestureNameSet) { //每一个名称，做

            ArrayList<Gesture> gesturesList = libInitial.getGestures(gestureName);//获得这个名称里的手势（可能会有多个）

            Log.d(TAG, gestureName);

            NameFilter filter = new NameFilter(gestureName);//自己声明的，用于去掉##后的内容

            for (Gesture gesture : gesturesList) { //意义不明，可能有多个手势

                if(packExists(filter.getPackName())){
            //        lib.addGesture(gestureName,gesture); //加入lib


                    //TODO-------------------------------------to reload app name (based on the language), only apps are allowed
                    String finalName;
                    PackageManager packageManager= getApplicationContext().getPackageManager();
                    try {
                        String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(filter.getPackName(), PackageManager.GET_META_DATA));
                        finalName = filter.changeFiltedName(appName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        finalName = gestureName;
                    }
                    //--------------------------


                    lib.addGesture(finalName,gesture); //加入lib
//                    packNameWhichExists = packNameWhichExists + filter.GetfiltedName() +", ";
                }

            }
        }


        if(lib.save()){  //当手势文件导入成功后（初始化完成），改变界面
            Msg("Gesture Library initiated!");

        }else{
            Msg("Error: fail to save gesture library");
//            finish();
        }

        if(!lib.load()){   //再确定是否已经成功添加
            Msg("No gestures found, failsafe gesture is added.");
            lib.addGesture("Test",libInitial.getGestures("WearTest##wearapp##com.format.weartest").get(0));
            lib.save();
        }


    }

    public boolean packExists(String packageName){
        for(String packNameWear : packNameList){  //遍历手表程序表
            if(packNameWear.equals(packageName)){  //如果这个packName在里面有的话
                for(String packNameFile : lib.getGestureEntries()){ //遍历已存在于文件里的手势
                    if(packNameFile.equals(packageName)){ //如果手势已经存在于文件里
                        return false;  //表示不需要再次添加
                    }
                }
                return true;//没有，那么就添加
            }
        }
        return false;//不存在于手表里，不需要添加
    }


    //============================================================================================== RECEIVE 接收文件

    public void byte2File(){
        String strFilePath = getFilesDir()+"/gestureNew";
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
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
//        Msg("Phone connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }






    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {

            PutDataMapRequest putDataMapRequest =
                    PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.getDataItem()));//!!!!!!!!!!!!!!!!https://medium.com/@manuelvicnt/android-wear-accessing-the-data-layer-api-d64fd55982e3

            if (event.getType() == DataEvent.TYPE_CHANGED) {


                DataItem item = event.getDataItem();
                PATH=item.getUri().getPath();

                if(PATH.equals("/gestures")) { //1.确保收到的是手机发送的数据，覆盖手表的lib
                    // DataItem changed


                    DataMap map = putDataMapRequest.getDataMap();


                    //----------------------------------------------------------------------------------------------------------------版本检查

                    MOBILE_VERSION=map.getInt("version");

                    if(MOBILE_VERSION!=WEAR_VERSION-1000){ //如果版本不同则提醒更新
                        if(map.getInt("version")<1012){ //如果版本过小
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
                            Toast t = Toast.makeText(this,  "\n\n\nWarning: Gestures overwrote from phone, Mobile app version too old, please update the app on your phone.", Toast.LENGTH_LONG);
                            t.setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
                            t.show();

                                compatibleMode=true;

//                            break;
                        }else {
                            Toast t = Toast.makeText(this, "\n\nWear gesture launcher\nMobile app " + MOBILE_VERSION + "\nWear app " + WEAR_VERSION + "\nVersion not consistent, please update your apps to make sure sync running properly.", Toast.LENGTH_LONG);
                            t.setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
                            t.show();

                        }
                    }else {
                        compatibleMode=false;
                    }
                    //----------------------------------------------------------------------------------------------------------------


                    if(map.getBoolean("overwrite")||map.getInt("version")<1012){  //如果手机说要覆盖则覆盖，如果说false的话证明手机只是在检查连接和版本号,或者如果版本号过老也覆盖
                        fileInBytes = map.getByteArray("File"); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        byte2File();//保存
                        LoadLibrary();
                        Msg("Gesture Library Synced!");
                    }


                    //-------------------------------------------------------------------------------------------------------------Receive pref from phone

                    boolean safe =false;
                    try{

                            if(map.getString("location").length()>0){
                                safe=true;
                            }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        Msg("No pref received");
                    }

                    if(safe) {
                        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("show", map.getBoolean("show"));
                        editor.putBoolean("vibrate", map.getBoolean("vibrate"));
                        editor.putString("location", map.getString("location"));
                        editor.putInt("accuracy", map.getInt("accuracy"));
                        editor.apply();

                        //----------------------------------------------------refresh floater
                        if(FloaterService.frameLayoutfloater!=null) {

                            FloaterService.frameLayoutfloater.removeAllViews();
                            FloaterService.frameLayoutfloater=null;
                            stopService(new Intent(WearConnectService.this, FloaterService.class));
//                            Msg("service stopped");

                            if(sharedPref.getBoolean("show",true)){
                                startService(new Intent(WearConnectService.this, FloaterService.class));
                            }
                        }else{
                            if(sharedPref.getBoolean("show",true)){
                                startService(new Intent(WearConnectService.this, FloaterService.class));
                            }
                        }

                    }


                    LoadPref();
                    //--------------------------------------------------------------



                    sendDataMapToDataLayerForMobile("/receive");//发送给手机接收确认

                    //以下用于测试。可有可无
//                    byte[] data = item.getData();
//                    Msg(data.toString());
//                    Msg(PATH);
//                    String s = new String(data);
//                    TextView text = findViewById(R.id.text);
////                    text.setText("PATH: " + PATH + s);
//                    text.setText("Gesture Library updated!");

                    //---------------------------------------

                }else if(PATH.equals("/initiate")){  //如果PATH等于启动
                    sendDataMapToDataLayerForMobile("/receive");//发送给手机接收确认!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    Msg("Connection Initiated!");

                }else if(PATH.equals("/needupdate")) {  //如果PATH等于需要刷新则提供刷新的手势库
                    sendDataMapToDataLayerForMobile("/update");
//                    Msg("Library updated to mobile");
                }



            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //==============================================================================================



    public static void  sendMobile(WearConnectService connect){

        connect.sendDataMapToDataLayerForMobile("/update");//TODO
    } //用于外部使用，外部call以进行同步


    //------------------------------------------------------------------------------------------------------------------SEND 发送回执给手机

    private void getApplicationPacklist(){ //取手表程序包名列表String[]
        ArrayList<String> packagename= new ArrayList<String>(); //用于存放包名
        ArrayList<String> appname= new ArrayList<String>(); //用于存放包对应程序名
        //---------------------------------------------------------------------取程序列表
        final PackageManager pm = getPackageManager(); //packge manager
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // get list of installed program package
        for (ApplicationInfo packageInfo : packages) {

            if(checkForLaunchIntent(packageInfo)==true && checkForAlreadyExist(packageInfo)==false) { //如果这个包是可以运行的且不存在相应手势
                packagename.add(packageInfo.packageName);//添加包名
                appname.add(pm.getApplicationLabel(packageInfo).toString());//添加程序名
            }

        }
        //----------------------------------------------------------------------
        packNameList = packagename.toArray(new String[packagename.size()]);
        appNameList = appname.toArray(new String[appname.size()]);
//        return packNameList;
    }//取手表程序列表


    private boolean checkForLaunchIntent(ApplicationInfo info) {
        //load launchable list 原：https://github.com/StackTipsLab/Advance-Android-Tutorials/blob/master/ListInstalledApps/src/com/javatechig/listapps/AllAppsActivity.java
        try {
            if (null != getPackageManager().getLaunchIntentForPackage(info.packageName)) { //如果可以打开这个程序
                return true;
            }
        } catch (Exception e) {

            e.printStackTrace();

        }

        return false;
    }//检查这个程序确保可以运行 而不是系统程序（过滤掉不可以运行的，添加列表）


    public static boolean checkForAlreadyExist(ApplicationInfo info){
        if(!lib.load()){
            return false;
        }


        for(String gestureName :lib.getGestureEntries()){
            if(new NameFilter(gestureName).getPackName().equals(info.packageName)){   //名称过滤：取包名，对比
                return true; //存在
            }
        }
        return false;//不存在
    }//检查包名是否已经存在相应的手势





    public byte[] file2byte(){
        File file = new File(getFilesDir(), "gestureNew"); //手表没有s，手机有s

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
    }   //将要update的文件转化成byte以发送到手机


    private DataMap createDatamap(){
        DataMap dataMap = new DataMap();
        dataMap.putString("Received!","From wear");
        dataMap.putString("Time", Long.toString(System.currentTimeMillis()) );

        getApplicationPacklist();
        dataMap.putStringArray("packList", packNameList); //手表程序包名列表发回手机
        dataMap.putStringArray("appList", appNameList); //手表程序名列表发回手机
        dataMap.putByteArray("updatedlib",file2byte()); //发送updated手势
        dataMap.putInt("version",BuildConfig.VERSION_CODE);//发送版本号，手表比手机大1000


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
    } //建立数据包准备发送

    public void sendDataMapToDataLayerForMobile(String location){
        MOBILE_RECEIVED = location;
        if(mGoogleApiClient.isConnected()){
            DataMap dataMap = createDatamap();
            new SendDataMapToDataLayer(MOBILE_RECEIVED,dataMap).start();
        }
    } //发送



    private class SendDataMapToDataLayer extends Thread{
        String path;
        DataMap dataMap;

        public SendDataMapToDataLayer(String path, DataMap dataMap){
            this.path=path;
            this.dataMap=dataMap;
        }

        public void run(){
//            NodeApi.GetConnectedNodesResult nodeList = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(MOBILE_RECEIVED);
            putDataMapReq.getDataMap().putAll(dataMap);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

//            pendingResult.setResultCallback、;

//            for(Node node : nodeList.getNodes()){
//            for(DataApi.DataItemResult result : pendingResult){
////                MessageApi.SendMessageResult messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient,node.getId(),path,me)
//                if()
            Log.v(TAG,"Sent!");
//
//            }
        }


    }//发送类

//----------------------------------------------------------------------------------------------------------------



    public void Msg(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
        Log.v(TAG,message);
    }
}
