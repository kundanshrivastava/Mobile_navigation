package com.example.q4n9yzb1.demo_yt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.youtube.player.YouTubePlayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class display_cloud extends Activity {

   // Map<String,String> myMap= new HashMap<String,String>();
    YouTubePlayer player;
    MainActivity obj= new MainActivity();
   // ArrayList<String> listOfItems;
    private ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cloud);
        Intent intent=getIntent();
        String title_id=intent.getStringExtra("TOPIC_ID");
        //myMap=obj.myMap;
        lv=(ListView) findViewById(R.id.newlistView);
        //String image_id=myMap.get(title_id);
      //  String resource="R.drawable." + image_id;
        player=obj.YTPlayer;
        FileInputStream in;
        BufferedInputStream buf;
        String str = getFilesDir().getAbsolutePath();
        Log.d(display_cloud.class.getSimpleName(),str);
        ImageView imageView=(ImageView) findViewById(R.id.imageView);
        try {
            in = new FileInputStream("/data/IMG_1");
            buf = new BufferedInputStream(in);
            Bitmap bMap = BitmapFactory.decodeStream(buf);
            final FileOutputStream fos = openFileOutput("IMG_1", Context.MODE_PRIVATE);
            bMap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            imageView.setImageBitmap(bMap);
            if (in != null) {
                in.close();
            }
            if (buf != null) {
                buf.close();
            }
        } catch (Exception e) {
            Log.e("Error_K_ reading file", e.toString());
        }

       // final Bitmap bm = BitmapFactory.decodeFile("/Pictures/IMG_1.JPG");
// The openfileOutput() method creates a file on the phone/internal storage in the context of your application
       // final FileOutputStream fos = openFileOutput("my_new_image.jpg", Context.MODE_PRIVATE); // Use the compress method on the BitMap object to write image to the OutputStream
       // bm.compress(Bitmap.CompressFormat.JPEG, 90, fos);

        //  ImageView imageView=(ImageView) findViewById(R.id.imageView);
        //File file= new File("/Downloads/image1.jpg");
       // imageView.setImageResource(R.drawable.image_1);
        populate_list_view();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_cloud, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void populate_list_view()
    {
        Log.d(display_cloud.class.getSimpleName(),"populating list view");

      /*  ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_2,
                obj.listOfItems );*/
        int k=obj.listOfTitles.size();
        System.out.println("The sizeof list is"+k);
        //Log.d(display_cloud.class.getSimpleName(), );
        lv.setAdapter(obj.arrayAdapter);
        return ;
    }
}
