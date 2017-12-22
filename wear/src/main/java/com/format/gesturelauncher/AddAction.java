package com.format.gesturelauncher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class AddAction extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_action);



        //------------------EACH TIME after create a gesture, AddConfirm will call back to here to start the main screen
//        if(getIntent().hasExtra("main")) {
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//
////            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("message", "Gesture saved");
//            intent.putExtra("extra", "notini");
//            startActivity(intent);
//
//
//            finish();
//        }
        //------------------------------

        findViewById(R.id.buttonApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),AppSelector.class);
                    intent.putExtra("method","wearapp");
                    startActivity(intent);
            }
        });


        findViewById(R.id.buttonTimer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),AppSelector.class);
                intent.putExtra("method","timer");
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(),AppSelector.class);
//                intent.putExtra("method","call");
//                startActivity(intent);
                Toast.makeText(getApplicationContext(),"Use the mobile app to add this action",Toast.LENGTH_SHORT).show();
//
//                Intent launchIntent = new Intent(Intent.ACTION_ALL_APPS);
////                launchIntent.addFlags(Intent.ACTION_CHOOSER);
////                launchIntent.addCategory(Intent.ACTION_ALL_APPS);
////                launchIntent.setType()
//                Intent chooser  = Intent.createChooser(launchIntent,"ssss");
//                startActivity(chooser);


//                Intent sendIntent = new Intent(Intent.ACTION_SEND);
//// Always use string resources for UI text.
//// This says something like "Share this photo with"
//                String title = "!!!!";
//// Create intent to show the chooser dialog
//                Intent chooser = Intent.createChooser(sendIntent, title);
//
//// Verify the original intent will resolve to at least one activity
//                if (sendIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(chooser);
//                }



//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//                sendIntent.setType("text/plain");
//                startActivity(Intent.createChooser(sendIntent,"ss"));

//                Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
//
//                // TODO - Create a chooser intent, for choosing which Activity
//                // will carry out the baseIntent
//                // (HINT: Use the Intent class' createChooser() method)
//                Intent chooserIntent = Intent.createChooser(baseIntent, "text");
//
//                // TODO - Start the chooser Activity, using the chooser intent
//                startActivity(chooserIntent);
            }
        });

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

//        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("extra","notini");
        startActivity(intent);
    }
}
