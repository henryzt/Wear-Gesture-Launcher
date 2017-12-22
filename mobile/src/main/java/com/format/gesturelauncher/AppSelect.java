package com.format.gesturelauncher;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.gesture.Gesture;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.provider.ContactsContract.Directory.DISPLAY_NAME;
import static com.format.gesturelauncher.MobileConnectService.lib;
import static com.format.gesturelauncher.MobileConnectService.wearAppList;
import static com.format.gesturelauncher.MobileConnectService.wearPackList;

public class AppSelect extends AppCompatActivity {



    ArrayList<String> packagename= new ArrayList<String>(); //用于存放应用包名
    String MethodNameForReturn;
    ListView mainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_app_select);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.ListViewApps);


//        //-------------------------------------------------------------------------------fab事件
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Adding...", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                        LoadMobileApps(getApplicationContext());
//            }
//        });

//        findViewById(R.id.buttonPhone).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Adding...", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                        LoadMobileApps(getApplicationContext());
//            }
//        });
//
//
//        findViewById(R.id.buttonWear).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Adding...", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                LoadWearApps(getApplicationContext());
//            }
//        });


        Intent intent = getIntent();
        String message = intent.getStringExtra("type"); //获取启动信息
        switch (message){
            case "wear":
                LoadWearApps(getApplicationContext());
                break;
            case "mobile":
                LoadMobileApps(getApplicationContext());
                break;
            case "timer":
                LoadTimers();
                break;
            case "call":
                GenerateMethod("call",intent.getStringExtra("number"),intent.getStringExtra("name"));

//                finish();
                break;

        }

    try {
        if (mainListView.getAdapter().getCount() <= 0) {
            Toast.makeText(getApplicationContext(), "You have created all items in this section!", Toast.LENGTH_SHORT).show();
            finish();

        }
    }catch (NullPointerException e){
//            finish();
    }

    }




    public void LoadMobileApps(Context context){

        Toast.makeText(getApplicationContext(),"This section is under development, stay tuned!",Toast.LENGTH_SHORT).show();

        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String>  listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);


        //---------------------------------------------------------------------取程序列表
        final PackageManager pm = getPackageManager(); //packge manager
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // get list of installed program package
        for (ApplicationInfo packageInfo : packages) {

            if(checkForLaunchIntent(packageInfo)==true && checkAlreadyExist(packageInfo)==false){ //如果这个包是可以运行的
                listAdapter.add(pm.getApplicationLabel(packageInfo).toString());
                packagename.add(packageInfo.packageName);
            }


        }
        //----------------------------------------------------------------------


        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter( listAdapter );


        //-------------------------------------------------------------------------------列表框点击事件
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String packName = packagename.get(position);

                Toast.makeText(getApplicationContext(),"This section is under development, currently is not available to add. Stay tuned!",Toast.LENGTH_SHORT).show();
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//                GenerateMethod("mobileapp",packName,position);
            }

        });

    }
    private boolean checkForLaunchIntent(ApplicationInfo info) {
        //检查这个程序确保可以运行 而不是系统程序
//load launchable list 原：https://github.com/StackTipsLab/Advance-Android-Tutorials/blob/master/ListInstalledApps/src/com/javatechig/listapps/AllAppsActivity.java
            try {
                if (null != getPackageManager().getLaunchIntentForPackage(info.packageName)) { //如果可以打开这个程序
                    return true;
                }else {
                    Log.v(MainActivity.TAG,"已删除不能打开的程序 " + info.packageName);
                }
            } catch (Exception e) {

                e.printStackTrace();

            }

            return false;
    }//检查这个程序确保可以运行 而不是系统程序
    private boolean checkAlreadyExist(ApplicationInfo info) {

          for(String methodName : lib.getGestureEntries()){
              NameFilter filter =new NameFilter(methodName);
              if(filter.getMethod().equals("mobileapp") && filter.getPackName().equals(info.packageName)){
                  return true;
              }
          }


        return false;
    }//检查这个程序exist or not



    public void LoadWearApps(Context context){



        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String>  listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);

        //----------------------------------------------------------------------
        String[] wearPacks =wearPackList;
        String[] wearPacksAppName = wearAppList;


        for (int i=0;i < wearPacks.length;  i++) {
//            try {
//                String appLabel = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(wearPacks[i],PackageManager.GET_META_DATA));
//                listAdapter.add(appLabel); //列表加入程序名
//                packagename.add(wearPacks[i]); //包名加入当前包名
//            } catch (PackageManager.NameNotFoundException e) {
//                listAdapter.add(wearPacks[i]); //列表加入程序名！！！
//                packagename.add(wearPacks[i]); //包名加入当前包名！！！
//                Log.v(MainActivity.TAG,e+ ", " + wearPacks[i]+ " Not Found");
//                e.printStackTrace();
//            }

            listAdapter.add(wearPacksAppName[i]); //列表加入程序名
            packagename.add(wearPacks[i]); //包名加入当前包名
        }

        //----------------------------------------------------------------------

        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter( listAdapter );


        //-------------------------------------------------------------------------------列表框点击事件
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String packName = packagename.get(position);

                GenerateMethod("wearapp",packName,mainListView.getItemAtPosition(position).toString());
            }

        });



    }



    public void LoadTimers(){



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

            if (!TimerCheckExist(methods[i])) { //If not exist then add
                listAdapter.add(methodsIndicator[i]); //列表加入程序名
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

                GenerateMethod("timer", packName, mainListView.getItemAtPosition(position).toString());


            }

        });


    }
    public boolean TimerCheckExist(String method){
        for(String name : lib.getGestureEntries()){
            NameFilter filter= new NameFilter(name);
            if(filter.getMethod().equals("timer")&&filter.getPackName().equals(method)){
                return true;
            }
        }
        return false;

    }





public void GenerateMethod(String runType,String runMethod, String Label ){
    try {


        MethodNameForReturn = Label + "##"+runType+"##"+ runMethod;  //eg: Shazam##wearapp##com.shazam.shazam

        Intent addgesture = new Intent(this,GestureActivity.class);
        addgesture.putExtra("method",MethodNameForReturn);
        addgesture.putExtra("name",new NameFilter(MethodNameForReturn).GetfiltedName());
        startActivity(addgesture);
        finish();


//            String method = "Open " +  MethodNameForReturn;

//            String[] spilt =method.split("##");


        //TODO dialog_confirm.setActivity(MethodNameForReturn);

//            Toast.makeText(getApplicationContext(),MethodNameForReturn,Toast.LENGTH_LONG).show();

//            finish();


    }catch (Exception e){
        Toast.makeText(getApplicationContext(),"Fail to run " + runMethod+ "\n Error message: " +e.toString(),Toast.LENGTH_LONG).show();
//            Log.v(MainActivity.TAG,e+ ", Fail to run " + packageName);
    }




}//启动指定的应用程序


}

