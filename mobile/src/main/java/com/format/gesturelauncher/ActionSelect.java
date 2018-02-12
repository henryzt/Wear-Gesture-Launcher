package com.format.gesturelauncher;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ActionSelect extends AppCompatActivity {

    private static final String TAG = "format";

    static final int REQUEST_CODE_SELECT_PHONE_NUMBER = 1;
    static final int REQUEST_CODE_SELECT_APP = 2;
    static final int REQUEST_CODE_TASKER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_select);


        //---------------------------------------------按钮
        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        findViewById(R.id.buttonApps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAppSelect("wear");

            }
        });

        findViewById(R.id.buttonAppMobile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAppSelect("mobile");
//                Intent localIntent2 = new Intent("android.intent.action.PICK_ACTIVITY");
//                Intent localIntent3 = new Intent("android.intent.action.MAIN",null);
//                localIntent3.addCategory("android.intent.category.LAUNCHER");
//                localIntent2.putExtra("android.intent.extra.INTENT",localIntent3);
//                startActivityForResult(localIntent2, REQUEST_CODE_SELECT_APP);
            }
        });

        findViewById(R.id.buttonTimer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAppSelect("timer");

            }
        });

        findViewById(R.id.buttonPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openAppSelect("call");
//                pickContact(view);
                selectContact();


            }
        });

        findViewById(R.id.buttonIntent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openAppSelect("mobile");
//                AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
//                List<AppWidgetProviderInfo> infoList = manager.getInstalledProviders();
//                for (AppWidgetProviderInfo info : infoList) {
//                    Log.d(TAG, "Name: " + info.label);
//                    Log.d(TAG, "Provider Name: " + info.provider);
//                    Log.d(TAG, "Configure Name: " + info.configure);
//                }


                Intent localIntent2 = new Intent("android.intent.action.PICK_ACTIVITY");
                Intent localIntent3 = new Intent("android.intent.action.MAIN",null);
                localIntent3.addCategory("android.intent.category.DEFAULT");
                localIntent2.putExtra("android.intent.extra.shortcut",localIntent3);
                startActivityForResult(localIntent2, 2);
            }
        });

        findViewById(R.id.buttonTasker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TaskerIntent.testStatus( getApplicationContext() ).equals( TaskerIntent.Status.OK )) {
                    startActivityForResult(TaskerIntent.getTaskSelectIntent(), REQUEST_CODE_TASKER);
                }else {
                   taskerNote( TaskerIntent.testStatus( getApplicationContext() ));

                }



            }
        });

    }



    public void taskerNote(TaskerIntent.Status problem){


            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

            if(problem.equals(TaskerIntent.Status.NotInstalled)){
                builder.setMessage("It looks like Tasker is not installed. With Tasker, you can control much more operations and connect with other apps. You can use this function once Tasker is installed!");
            }else if(problem.equals(TaskerIntent.Status.AccessBlocked)){
                builder.setMessage("Access to Tasker is denied, please make sure you Enabled External Access from Tasker -> Preference -> Misc.");
            }else{
                builder.setMessage("There was a problem while connecting to Tasker: "+problem.toString()+"\n\nIf this problem continue to occur, please try reinstall Wear Gesture Launcher on this device after installing Tasker, your gesture library will be saved on your wearable.");
            }

            builder.setTitle("Tasker");
            builder.setCancelable(true);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });


            builder.show();

    }








    public void openAppSelect(String type){
        Intent intent = new Intent(this,AppSelect.class);
        intent.putExtra("type",type);
        startActivity(intent);
    }





    public void selectContact() {
        // Start an activity for the user to pick a phone number from contacts
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_PHONE_NUMBER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //-------------------------------------------------------------------------------------On phone activity
        if (requestCode == REQUEST_CODE_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//                int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHONETIC_NAME);
                String number = cursor.getString(numberIndex);
//                String name =number;

//                name =
//                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                if (cursor.moveToFirst()) {
//                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FULL_NAME_STYLE));
//                }
                // Do something with the phone number
//            ...

                getName(number);


            }else {
                Toast.makeText(getApplicationContext(), "Action canceled", Toast.LENGTH_SHORT).show();
//                finish();
            }
        }




        if (requestCode == REQUEST_CODE_SELECT_APP && resultCode == RESULT_OK){
            Toast.makeText(getApplicationContext(), "This function is still under development, keep in tune!", Toast.LENGTH_LONG).show();
        }




        if (requestCode == REQUEST_CODE_TASKER && resultCode == RESULT_OK){
//            Toast.makeText(getApplicationContext(), data.getDataString(), Toast.LENGTH_LONG).show();
//           runTakser(data.getDataString());
            Intent intent = new Intent(getApplicationContext(),AppSelect.class);
            intent.putExtra("type","tasker");
            intent.putExtra("task",data.getDataString());
            startActivity(intent);
        }




    }

    public void getName(final String number){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter action name for calling number "+ number);

        builder.setCancelable(false);
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        //            input.setPadding(20,0,20,0);
        input.setText("Call "+number);
        //            input.setHint("e.g. Call Helen");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text= input.getText().toString();
                if(text.equals("")){
                    text="Call "+number;
                }

                Intent intent = new Intent(getApplicationContext(),AppSelect.class);
                intent.putExtra("type","call");
                intent.putExtra("number",number);
                intent.putExtra("name",text);
                startActivity(intent);


                dialog.cancel();
//                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
//                finish();
            }
        });

        builder.show();
//            finish();
    }





//
//
//    public void RunChosenApp(String packageName, int listViewPosition){
//        try {
////            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
////            startActivity( intent );
//
//
//            //icon
////            ImageView image = (ImageView)findViewById(R.id.imageView2);
////            image.setImageDrawable(getPackageManager().getActivityIcon(intent));
//
//
//            //Test spilt text
//
////            String appLabel = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(packageName,PackageManager.GET_META_DATA));
//            String appLabel = mainListView.getItemAtPosition(listViewPosition).toString();
//            MethodNameForReturn = appLabel + "##wearapp##"+ packageName;
//
//
//
//
//            String method = "Open " +  MethodNameForReturn;
//
//            String[] spilt =method.split("##");
//
//
//            dialog_confirm.setActivity(MethodNameForReturn);
//
////            Toast.makeText(getApplicationContext(),method,Toast.LENGTH_LONG).show();
////            Toast.makeText(getApplicationContext(),spilt[0],Toast.LENGTH_LONG).show();
//
//
//            finish();
//
//
//        }catch (Exception e){
//            Toast.makeText(getApplicationContext(),"Fail to run " + packageName+ "\n Error message: " +e.toString(),Toast.LENGTH_LONG).show();
//            Log.v(MainActivity.TAG,e+ ", Fail to run " + packageName);
//        }
//
//
//
//    }//启动指定的应用程序




}




