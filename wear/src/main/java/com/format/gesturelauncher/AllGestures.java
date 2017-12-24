package com.format.gesturelauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.ConfirmationOverlay;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static com.format.gesturelauncher.MainActivity.wearConnect;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.sendMobile;

public class AllGestures extends WearableActivity {



    private BoxInsetLayout mContainerView;
    ArrayList<String> titles = new ArrayList<String>(); //用于列表,原标题
    ArrayList<String> shortenTitles = new ArrayList<String>(); //用于列表,用于显示的
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(); //用于列表

    Boolean openMain=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_gestures);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mContainerView.setBackground(null);

        try {
            if (getIntent().getStringExtra("open").equals("y")) {
                openMain = true;
            }
        }catch (Exception e){

        }

        refreshList();

    }



    public void refreshList() {

        //-------------------------------------------手势

//        final File mStoreFile = new File(getFilesDir(), "gesturesNew");
//
//        lib = GestureLibraries.fromFile(mStoreFile);//导入手势
//
//        //        lib= GestureLibraries.fromRawResource(this,R.raw.gesturesm);//导入手势
//        if (!lib.load()) {          //必须要这个
////            MsgT("Warning: Library unload, initiating first time welcome screen");
//            startActivity(new Intent(getApplicationContext(),WelcomeActivity.class));
////            finish();
//
//        }
        titles.clear();
        bitmaps.clear();
        shortenTitles.clear();
        //----------------------------------------------------------------------------手势


        Set<String> gestureNameSet = lib.getGestureEntries(); //获得所有手势的名称(lib是在wearConnectService里的）

        if(gestureNameSet.size()<=0){

            new ConfirmationOverlay().setDuration(99000)
                    .setMessage("Gesture library is empty, add a gesture now!")
                    .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                    .showOn(AllGestures.this);
        }


        for (String gestureName : gestureNameSet) { //每一个名称，做

            ArrayList<Gesture> gesturesList = lib.getGestures(gestureName);//获得这个名称里的手势（可能会有多个）

//            Log.d(tag, gestureName);

            NameFilter filter = new NameFilter(gestureName);//自己声明的，用于去掉##后的内容


            for (Gesture gesture : gesturesList) {
                titles.add(gestureName);
                bitmaps.add(gesture.toBitmap(125, 125, 30, Color.YELLOW));//生成bitmap并添加到列表
                shortenTitles.add(filter.getFilteredName());//用于去掉##后的内容

//                        setImageBitmap(gesture.toBitmap(100,100,10,defColor));
            }
        }

        //--------------------------------------------------------------------------Grid View

        final ListView listview = (ListView) findViewById(R.id.listview);
        final ImageAdapter adapter = new ImageAdapter(getApplicationContext(), shortenTitles, bitmaps); //建立继承，adaper，放入bitmap和标题
        listview.setAdapter(adapter);//采用adaper

//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) { //当点击项目
//
//                delete(position);
//            }
//        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                delete(i);
                return true;
            }
        });

//        Sync();//尝试同步

    } //刷新listView,采用adapter以展示图片和文本,this code is copy from mobile

    public void delete( final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete gesture");
        builder.setMessage("Do you want to delete Gesture '" + shortenTitles.get(position)+"' ?");


        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem(position);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();

    } //提示Delete


    public void deleteItem(int position) {

        lib.removeEntry(titles.get(position));
        lib.save();
//        msg("Item deleted");
        refreshList();
        sendMobile(wearConnect);//TODO A static method cannot call a non-static method, but we can use a reference, which include a non-static method to the static method. https://stackoverflow.com/questions/31661110/calling-a-non-static-method-in-an-android-onpreferenceclicklistener
        new ConfirmationOverlay()
                .setMessage("Item deleted")
                .setType(ConfirmationOverlay.SUCCESS_ANIMATION)
                .showOn(AllGestures.this);


//        Sync();

    }  //------------------------------------删除项目，需要添加确认

    @Override
    protected void onPause() {
        super.onPause();
        if (!this.isFinishing()){ //Home pressed
            finish();
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(openMain) {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("extra","notini");
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    public void msg(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


//    //_____________________________________________________________________Ambient
    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));

        } else {
            mContainerView.setBackground(null);

        }
    }
}
