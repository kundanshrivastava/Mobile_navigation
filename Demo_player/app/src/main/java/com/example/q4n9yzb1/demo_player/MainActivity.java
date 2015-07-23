package com.example.q4n9yzb1.demo_player;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;

import java.io.InputStream;

public class MainActivity extends Activity {

    Button button;
    Uri video_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the layout from video_main.xml
        setContentView(R.layout.activity_main);

        // Locate the button in activity_main.xml
        button = (Button) findViewById(R.id.MyButton);
        Log.d("kushal demo player", "" + getIntent().getData());
        try
        {
            InputStream in = getContentResolver().openInputStream(getIntent().getData());
            video_uri=getIntent().getData();
            Log.d("stram kush",in+"");
        }
        catch (Exception e)
        {
            Log.d("exception kush",e+"");
        }


        // Capture button clicks
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                // Start NewActivity.class
                Intent myIntent = new Intent(MainActivity.this,
                        VideoViewActivity.class);
                myIntent.putExtra("videoUri",video_uri.toString());
                startActivity(myIntent);
            }
        });
    }

}