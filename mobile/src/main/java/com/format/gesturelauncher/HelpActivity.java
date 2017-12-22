package com.format.gesturelauncher;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        String type = getIntent().getStringExtra("image");
        ImageView image = (ImageView)findViewById(R.id.imageViewHelp);


        switch (type){
            case "connect":
                image.setImageDrawable(getResources().getDrawable(R.drawable.help_connect));
                break;
            case "help":
                image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.help_tutorial));//记住：大图要放在res/drawable-xxhdpi中
                break;
        }


        findViewById(R.id.buttonClose2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
