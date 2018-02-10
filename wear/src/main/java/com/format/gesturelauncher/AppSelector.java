package com.format.gesturelauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.format.gesturelauncher.WearConnectService.appNameList;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.packNameList;

//TODO You can omly get the app list after sync
//TODO Mobile delete/ add not instantly sync
//gestures


public class AppSelector extends Activity {

    ArrayList<String> packageName = new ArrayList<String>();
    String methodNameForReturn;
    ListView mainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_app_selector);


            mainListView = (ListView) findViewById(R.id.listviewApp);

//            try{

                switch (getIntent().getStringExtra("method")){
                    case "wearapp":
                        loadWearApps();
                        break;
                    case "timer":
                        loadTimers();
                        break;
                    case "call":
//                        selectContact();
//                         finish();
                        break;
                    case "test":
                        loadTest();
                        break;

                }

//            }catch (Exception e){
//
//            }


        try {
            if (mainListView.getAdapter().getCount() <= 0) {
                Toast.makeText(getApplicationContext(), "You have created all items in this section!", Toast.LENGTH_SHORT).show();
                finish();

            }
        }catch (NullPointerException e){
//            finish();
        }


        }


    public void loadWearApps(){
        //------------------------------------------Message
//        Toast t = Toast.makeText(this,"\n\n\n\nPlease select an App you would like to create a gesture with",Toast.LENGTH_SHORT);
//        t.setGravity(Gravity.FILL_HORIZONTAL|Gravity.FILL_VERTICAL, 0, 0);
//        t.show();

        //------------------------------------------List Adapter

        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String> listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);

        //----------------------------------------------------------------------
        String[] wearPacks = packNameList;
        String[] wearPacksAppName = appNameList;

        for (int i=0;i < wearPacks.length;  i++) {
//            try {
//                String appLabel = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(wearPacks[i],PackageManager.GET_META_DATA));
//                listAdapter.add(appLabel); //列表加入程序名
//                packageName.add(wearPacks[i]); //包名加入当前包名
//            } catch (PackageManager.NameNotFoundException e) {
//                listAdapter.add(wearPacks[i]); //列表加入程序名！！！
//                packageName.add(wearPacks[i]); //包名加入当前包名！！！
//                Log.v(MainActivity.tag,e+ ", " + wearPacks[i]+ " Not Found");
//                e.printStackTrace();
//            }

            listAdapter.add(wearPacksAppName[i]);
            packageName.add(wearPacks[i]);
        }
//
//        //----------------------------------------------------------------------
//
//        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter( listAdapter );

        //------------------------------------------Listener

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//               String packName = mainListView.getItemAtPosition(position).toString();//Get the item user clicked
                String packName = packageName.get(position);

                generateMethod("wearapp",packName,mainListView.getItemAtPosition(position).toString());
            }

        });


    }


    public void loadTimers(){
        //------------------------------------------List Adapter

        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String> listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);

        //----------------------------------------------------------------------
        final String[] methods = {"Alarm","Alarm List","Timer","Stopwatch"};
        final String[] methodsIndicator = {"New Alarm","Manage Alarms","Open Timer","Open Stopwatch"};


        final ArrayList<String> nonExistMethods=new ArrayList<>();

        for (int i=0;i < methods.length;  i++) {

            if (!timerCheckExist(methods[i])) { //If not exist then add
                listAdapter.add(methodsIndicator[i]);
                nonExistMethods.add(methods[i]);

            }
        }


        mainListView.setAdapter( listAdapter );

        //------------------------------------------Listener

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String packName = nonExistMethods.get(position);

                generateMethod("timer", packName, mainListView.getItemAtPosition(position).toString());


            }

        });
    }


    public boolean timerCheckExist(String method){
        for(String name : lib.getGestureEntries()){
            NameFilter filter= new NameFilter(name);
            if(filter.getMethod().equals("timer")&&filter.getPackName().equals(method)){
                return true;
            }
        }
        return false;

    }


//    static final int REQUEST_SELECT_PHONE_NUMBER = 1;
//
//    public void selectContact() {
//        // Start an activity for the user to pick a phone number from contacts
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
//            // Get the URI and query the content provider for the phone number
//            Uri contactUri = data.getData();
//            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
//            Cursor cursor = getContentResolver().query(contactUri, projection,
//                    null, null, null);
//            // If the cursor returned is valid, get the phone number
//            if (cursor != null && cursor.moveToFirst()) {
//                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//                String number = cursor.getString(numberIndex);
////                String peoplename = cursor.getString
//                // Do something with the phone number
////            ...
//                generateMethod("call",number,"Call "+number );
//            }
//        }
//    }











    public void loadTest(){
//        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
//        List<ResolveInfo> shortcuts = getPackageManager().queryIntentActivities(shortcutsIntent, 0);
        List<PackageInfo> shortcuts=getAllApps(getApplicationContext());


        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String> listAdapter;
        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);

        //----------------------------------------------------------------------


        for (int i=0;i < shortcuts.size();  i++) {

            listAdapter.add(shortcuts.get(i).packageName);
            packageName.add(shortcuts.get(i).packageName);

            Log.v("tag","------------------------"+shortcuts.get(i).packageName);
            ActivityInfo[] activityInfos=shortcuts.get(i).activities;

            try {
                for (int j = 0; j < activityInfos.length; j++) {
                    Log.v("tag", activityInfos[i].parentActivityName);

                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }


       //----------------------------------------------------------------------

        mainListView.setAdapter( listAdapter );

        //------------------------------------------Listener

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//               String packName = mainListView.getItemAtPosition(position).toString();//Get the item user clicked
                String packName = packageName.get(position);

                generateMethod("wearapp",packName,mainListView.getItemAtPosition(position).toString());
            }

        });

    }







//    private List<ApplicationInfo> queryFilterAppInfo() {
//        PackageManager pm = this.getPackageManager();
//        // 查询所有已经安装的应用程序
//        List<ApplicationInfo> appInfos= pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
//        List<ApplicationInfo> applicationInfos=new ArrayList<>();
//
//        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
//        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
//        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        // 通过getPackageManager()的queryIntentActivities方法遍历,得到所有能打开的app的packageName
//        List<ResolveInfo>  resolveinfoList = getPackageManager()
//                .queryIntentActivities(resolveIntent, 0);
//        Set<String> allowPackages=new HashSet();
//        for (ResolveInfo resolveInfo:resolveinfoList){
//            allowPackages.add(resolveInfo.activityInfo.packageName);
//        }
//
//        for (ApplicationInfo app:appInfos) {
////            if((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)//通过flag排除系统应用，会将电话、短信也排除掉
////            {
////                applicationInfos.add(app);
////            }
////            if(app.uid > 10000){//通过uid排除系统应用，在一些手机上效果不好
////                applicationInfos.add(app);
////            }
//            if (allowPackages.contains(app.packageName)){
//                applicationInfos.add(app);
//            }
//        }
//        return applicationInfos;
//    }



    public static List<PackageInfo> getAllApps(Context context) {

        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        PackageManager pManager = context.getPackageManager();
        // 获取手机内所有应用
        List<PackageInfo> packlist = pManager.getInstalledPackages(0);
        for (int i = 0; i < packlist.size(); i++) {
            PackageInfo pak = (PackageInfo) packlist.get(i);
            // if()里的值如果<=0则为自己装的程序，否则为系统工程自带
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                // 添加自己已经安装的应用程序
                // apps.add(pak);
            }
            apps.add(pak);
        }
        return apps;
    }


//----------------------------------------------====================================================
    public void generateMethod(String runType, String runMethod, String Label ){
        try {


            methodNameForReturn = Label + "##"+runType+"##"+ runMethod;  //eg: Shazam##wearapp##com.shazam.shazam

            Intent addgesture = new Intent(this,AddGesture.class);
            addgesture.putExtra("method", methodNameForReturn);
            addgesture.putExtra("name",new NameFilter(methodNameForReturn).getFilteredName());
            startActivity(addgesture);


        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Fail to run " + runMethod+ "\n Error message: " +e.toString(),Toast.LENGTH_LONG).show();
//            Log.v(MainActivity.tag,e+ ", Fail to run " + packageName);
        }
    }
}

