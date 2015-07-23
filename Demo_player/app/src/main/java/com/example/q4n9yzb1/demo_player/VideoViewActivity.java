package com.example.q4n9yzb1.demo_player;

/**
 * Created by q4N9YZB1 on 21-07-2015.
 */
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VideoViewActivity extends Activity {

    // Declare variables
    ProgressDialog pDialog;
    VideoView videoview;
    /////////// variables copied from demo_yt

    private static ListView lv;
    public static ArrayList<String> listOfTitles;
    // private ArrayAdapter<String> arrayAdapter2;
   // static private final String DEVELOPER_KEY = "AIzaSyAmt4DCDNkxyqciSiifcu1XUHVJZNcp1Pk";
    static private final String VIDEO = "-4XBm5t7_Jg";
    public static ArrayAdapter<String> arrayAdapter;
    final Context context = this;
    private int image_id=1;
    private ImageView image;
    // final Dialog dialog;
    float x1=0.0f,x2=0.0f,y1=0.0f,y2=0.0f;
    private static GestureDetector gdt;
    public static Map<String,ArrayList<String>> titleToimagelist=new HashMap<String,ArrayList<String>>();// mapping from title ---> list of images for that title
    ArrayList<String> title_list=new ArrayList<String>();
    private Map<String,String> titleHead= new HashMap<String,String>();
    private Map<Integer,String> rectToword= new HashMap<Integer,String>();
    private Map<String,Integer> titleTotime= new HashMap<>();
    private Map<String,String> titleTofile = new HashMap<>();
    private Map<String, ArrayList<String> > word2Line= new HashMap<>();/////// to store the lines corressponding to words / processed from srt files
    private Map<String,ArrayList<Integer>> word2time= new HashMap<>();
    ////// DS to process word to lines results /////////
    private ArrayList<Integer> times_srt;  // to storre all the starting times of srt file
    private Map<Integer,String> time_to_line=new HashMap<>();// srt file processed startng time of srt ----> line said
    private Map<String,Integer> wordTimes= new HashMap<>(); // // store the info from word to time file

    //////////////
    private ArrayList<Rect> rectangles;
    // String[] image_list; // not to be used in further scene of implementation
    String current_title;
    private String appname="Player_xerox";
    private Dialog dialog;
    float scaledImageOffsetX,scaledImageOffsetY,originalImageOffsetX,originalImageOffsetY;
    boolean imageset=false,dialogSet=false;
    boolean showcloud=false;
    private ListView list_dialog;
    //////////Logging files Environment.getExternalStorageDirectory()/appname/VIDEO/extra/log.txt
    File log;
    PrintWriter out;
    Date date;
    SimpleDateFormat sdf;
    // BitmapFactory.Options options;

    //original height and width of the bitmap
    private static int intrinsicHeight,intrinsicWidth,scaledHeight,scaledWidth;
    ////////////speech variables
    private static final int VR_REQUEST = 999;
    private int MY_DATA_CHECK_CODE = 0;
    ArrayList<String> suggestedWords;
    //Text To Speech instance
    private TextToSpeech repeatTTS;
    boolean speech_enabled;
    //////

    //private MyPlayerStateChangeListener myPlayerStateChangeListener;
    //private MyPlaybackEventListener myPlaybackEventListener;
    float heightRatio, widthRatio;
    Paint paint;
    Canvas canvas;
    private List<rowItem> wordToline;


    //////// declaration for custom view of video timeline ////////
    ListView list;
    CustomAdapter adapter;
    public  VideoViewActivity CustomListView = null;
    public  ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<ListModel>();

    ////////////// declarations end//////////



    //////////////////////////////////////////

    // Insert your Video URL
  //  String VideoURL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
    String VideoURL= Environment.getExternalStorageDirectory()+"/video1.mp4";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the layout from video_main.xml
        CustomListView=this;
        setContentView(R.layout.videoview_main);
        // Find your VideoView in your video_main.xml layout
        videoview = (VideoView) findViewById(R.id.VideoView);

        lv = (ListView)findViewById(R.id.listItem);
        gdt = new GestureDetector(this,new GestureListener());
        // Execute StreamVideo AsyncTask
        times_srt=new ArrayList<>();


        listOfTitles= initialise_data_arrays();

        current_title=listOfTitles.get(1);

        // Create a progressbar
        pDialog = new ProgressDialog(VideoViewActivity.this);
        // Set progressbar title
        pDialog.setTitle("Xerox_Player");
       // pDialog.setAnchorView(findViewById(R.id.VideoView));
        // Set progressbar message
       // pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        // Show progressbar
        pDialog.show();

        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(
                    VideoViewActivity.this);
            mediacontroller.setAnchorView(videoview);
            // Get the URL from String VideoURL
           // Uri video = Uri.parse(VideoURL);

            Uri video= Uri.parse(getIntent().getExtras().getString("videoUri"));
            Log.d("uri_video_kushal",video+"");
            if(video!=null)
            {
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);
            }

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        populate_list_view();
        File file = new File(Environment.getExternalStorageDirectory()+"/"+appname,VIDEO);
        if(!file.exists())
        {
            Log.d("making directories","mkdir cdalled");
            file.mkdir();
            File f1= new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO,"clouds");
            File f2= new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO,"keyframes");
            File f3= new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO,"others");
            f1.mkdir();
            f2.mkdir();
            f3.mkdir();
        }
        videoview.requestFocus();
        videoview.setOnPreparedListener(new OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                videoview.start();
            }
        });
        addClickListener();

    }

    public static void deletedir(File dir)
    {
        if (!dir.isDirectory()) {
            Log.d("abc","not directory");
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                deletedir(file);
            }
            else {
                boolean deleted = file.delete();
                if (!deleted) {
                    Log.d("delete","unable to delete"+dir);
                }
            }
        }

        dir.delete();
    }

    public void populate_list_view()
    {

        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listOfTitles);
        Log.d(MainActivity.class.getSimpleName(), "array adapter being set");

        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setItemChecked(1, true);
        lv.setAdapter(arrayAdapter);
        //lv.setItemChecked(1, true);
        //lv.smoothScrollToPosition(1);
        //lv.setSelection(1);
        // lv.setItemChecked(1,true);
        //lv.requestFocus();
        return ;
    }

    @Override
    public void onResume() {
        super.onResume();
        populate_list_view();
        dialogSet=false;
        Log.d(MainActivity.class.getSimpleName(), "resumed main activity");
    }
    @Override
    public void onPause() {
        super.onPause();
        if(dialogSet)
            dialog.dismiss();
        dialogSet=false;
        imageset=false;
        Log.d(MainActivity.class.getSimpleName(), "paused main activity");
    }
    @Override
    public  void onStop()
    {
        super.onStop();
        if(out!=null)
        out.close();
    }

    private void addClickListener() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                // YTPlayer.pause();
                // v.setSelected(true);
                current_title = listOfTitles.get(pos);
                Log.d(MainActivity.class.getSimpleName(), current_title + "clicked/ displaying dialog");
                //  flipview(title);
                showcloud = false;
                displayDialog();

                //   Intent newIntent= new Intent(getApplicationContext(),display_cloud.class);
                // newIntent.putExtra("TOPIC_ID",listOfItems.get(pos));
                // startActivity(newIntent);


                // Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                //Log.d(SearchActivity.class.getSimpleName(),"i m an idiot");
                //Log.d(SearchActivity.class.getSimpleName(),getApplicationContext().toString());
                //Log.d(SearchActivity.class.getSimpleName(),searchResults.get(pos).getId());
                //intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                //startActivity(intent);
            }

        });
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        // ClipDrawable drawable = (ClipDrawable) imageview.getBackground();
        // drawable.setLevel(drawable.getLevel() + 1000); //Line number 21
        if(imageset)
        {
            // Bitmap b = image.getDrawingCache();
            // intrinsicWidth = b.getWidth();
            //intrinsicHeight = b.getHeight();
            //intrinsicHeight = options.outHeight;
            // intrinsicWidth = options.outWidth;
            //  Log.d("intrinsicweightsdialog",intrinsicHeight+"//"+intrinsicWidth);
            Log.d("intrinsicweightsdialog", intrinsicHeight + "//" + intrinsicWidth);
            // setScalefactor();
        }
    }


    public void displayDialog()
    {
        dialog = new Dialog(context);
        //  dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle("Topic"+(listOfTitles.indexOf(current_title)+1)+"/"+listOfTitles.size()+"|| KF 1/1");

        //dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
        int k=0;
        image_id=0;
        imageset=true;
        dialogSet=true;
        // String tmp_title_head=titleHead.get(current_title);
        //String file="/sdcard/"+titleToimagelist.get(tmp_title_head).get(0);
        image = (ImageView) dialog.findViewById(R.id.imageView2);
        final float a, b;
        a = (float) 360 / 401;
        b = (float) 400 / 300;
        //////////////

        String file=Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/"+"keyframes/"+titleToimagelist.get(titleHead.get(current_title)).get(0);
        Log.d("imageshown",Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/"+"keyframes/"+titleToimagelist.get(titleHead.get(current_title)).get(0));
        Bitmap bitmap= BitmapFactory.decodeFile(file);
        image.setImageBitmap(bitmap);

        image.setOnTouchListener(new View.OnTouchListener() {
            boolean flag=false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //  Log.d("valueontouchbegin",x1+"/"+x2+"/"+y1+"/"+y2);
                if(event.getAction()==MotionEvent.ACTION_DOWN)
                {

                    x1 = ((event.getX()*a)/getResources().getDisplayMetrics().density);
                    y1 = ((event.getY()*b)/getResources().getDisplayMetrics().density);
                    //  Log.d("X1/Y1=",x1+"/"+y1);
                    scaledImageOffsetX = x1 - image.getDrawable().getBounds().left;
                    scaledImageOffsetY = y1 - image.getDrawable().getBounds().top;
                    originalImageOffsetX = scaledImageOffsetX * widthRatio;
                    originalImageOffsetY = scaledImageOffsetY * heightRatio;
                    //  Log.d("scaled coords",scaledImageOffsetX+"//"+scaledImageOffsetY);
                    // Log.d("original coords",originalImageOffsetX+"//"+originalImageOffsetY);
                }
                else if(event.getAction()==MotionEvent.ACTION_UP) {
                    x2=event.getX();
                    y2=event.getY();
                    // Log.d("x2/y2=",x2+"//"+y2 );
                }
                if(x2!=0&&x1!=0&&y2!=0&&y1!=0) {
                    flag=true;
                    x1=0.0f;
                    x2=0.0f;
                    y1=0.0f;
                    y2=0.0f;
                }



                if(flag)
                {
                    gdt.onTouchEvent(event);
                }
                return true;
            }
        });

        // image.setBackgroundColor(80000000);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.LEFT;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.show();

    }

    private void showPreviousImage(String title)
    {
        Log.d("FUNCTIONCALLED","showpreviosimage"+image_id);
        String path_to_image=Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/"+"keyframes/";
        if(image_id<0)
        {image_id=0;
            return ;}

        if(image_id< titleToimagelist.get(titleHead.get(title)).size() && image_id >=0)
        {
            path_to_image= path_to_image+titleToimagelist.get(titleHead.get(title)).get(image_id);
            //ImageView image = (ImageView) dialog.findViewById(R.id.imageView2);
            Bitmap bitmap= BitmapFactory.decodeFile(path_to_image);
            image.setImageBitmap(bitmap);
            // intrinsicHeight = options.outHeight;
            // intrinsicWidth = options.outWidth;
            image_id--;
            image_id=image_id>=0? image_id:0 ;
            //  setScalefactor();
            imageset=true;
        }
    }
    private void showNextImage(String title)
    {
        Log.d("FUNCTIONCALLED","shownextimage"+image_id);
        String path_to_image=Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/"+"keyframes/";
        if(image_id >= (titleToimagelist.get(titleHead.get(title)).size()))
            return ;
        if(image_id<titleToimagelist.get(titleHead.get(title)).size() && image_id>=0)
        {
            path_to_image= path_to_image+titleToimagelist.get(titleHead.get(title)).get(image_id);
            //ImageView image = (ImageView) dialog.findViewById(R.id.imageView2);
            Bitmap bitmap= BitmapFactory.decodeFile(path_to_image);
            //  intrinsicHeight = options.outHeight;
            // intrinsicWidth = options.outWidth;
            image.setImageBitmap(bitmap);
            image_id++;
            image_id=image_id==titleToimagelist.get(titleHead.get(title)).size()? image_id-1:image_id ;
            // setScalefactor();
            imageset=true;
        }

    }
    private void showPreviousCloud(String title) {
        Log.d("FUNCTIONCALLED", "showprevioscloud");
        String path_to_image = Environment.getExternalStorageDirectory() + "/" + appname + "/" + VIDEO + "/" + "keyframes/";
        int k = listOfTitles.indexOf(title);
        if (k <= 0)
            return;

        if (k < listOfTitles.size()) {
            if (listOfTitles.get(k - 1) != "") {
                path_to_image = path_to_image + titleToimagelist.get(titleHead.get(listOfTitles.get(k - 1))).get(0);
                //ImageView image = (ImageView) dialog.findViewById(R.id.imageView2);
                Bitmap bitmap = BitmapFactory.decodeFile(path_to_image);
                //intrinsicHeight = options.outHeight;
                //intrinsicWidth = options.outWidth;
                image.setImageBitmap(bitmap);
                current_title = listOfTitles.get(k - 1);
                image_id = 0;
                // lv.setItemChecked(k-1, true);
                // setScalefactor();
                //  lv.smoothScrollToPosition(k-1);
                // lv.setSelection(k-1);
                lv.setItemChecked(k - 1, true);
                dialog.setTitle("Topic" + (listOfTitles.indexOf(current_title) + 1) + "/" + listOfTitles.size() + "|| KF 1/1");
                //lv.requestFocus();
                imageset = true;
            }
        }
    }
    private  void showNextCloud(String title)
    {
        //   String path_to_image="/sdcard/";
        Log.d("FUNCTIONCALLED","shownextcloud");
        String path_to_image=Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/"+"keyframes/";
        int k=listOfTitles.indexOf(title);
        if(k>=(listOfTitles.size()-1))
            return;
        if(k<listOfTitles.size())
        {
            path_to_image= path_to_image+titleToimagelist.get(titleHead.get(listOfTitles.get(k+1))).get(0);
            //ImageView image = (ImageView) dialog.findViewById(R.id.imageView2);
            Bitmap bitmap= BitmapFactory.decodeFile(path_to_image);
            //intrinsicHeight = options.outHeight;
            //intrinsicWidth = options.outWidth;
            image.setImageBitmap(bitmap);
            current_title=listOfTitles.get(k+1);
            image_id=0;
            //setScalefactor();
            //lv.setItemChecked(k+1, true);
            // lv.smoothScrollToPosition(k+1);
            //lv.setSelection(k+1);
            //lv.requestFocus();
            lv.setItemChecked(k + 1, true);
            dialog.setTitle("Topic"+(listOfTitles.indexOf(current_title)+1)+"/"+listOfTitles.size()+"|| KF 1/1");
            imageset=true;
        }
    }

    public void showTagCloud(View v)
    {
        Log.d("button","buttonpressed");
        //YTPlayer.pause();
        showcloud=true;
        ////////////////////
        final float x, y, a, b;
        a = (float) 360 / 401;
        b = (float) 400 / 300;
        dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.tag_cloud);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        // dialog.setTitle("Topic" + (listOfTitles.indexOf(current_title) + 1) + "/" + listOfTitles.size() + "|| KF 1/1");
        ImageView image2= (ImageView)dialog.findViewById(R.id.imageViewCloud);
        Bitmap bitmap_new = Bitmap.createBitmap(401, 300, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap_new);
        canvas.drawColor(Color.WHITE);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        String[] tmparr;
        rectangles= new ArrayList<>();
        //  Log.d("data", 401 / getResources().getDisplayMetrics().density + "..");
        Log.d(current_title+"",Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/clouds/"+titleTofile.get(current_title));
        File file = new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/clouds/"+titleTofile.get(current_title));
        Rect r;
        int k=0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                // Log.d("fileread",line);
                tmparr = line.split(" ");
                //for(String s: tmparr) {
                // Log.d("string",tmparr[0]+"//"+tmparr.length);
                paint.setTextSize((float) Integer.parseInt(tmparr[1]));
                if(tmparr[0].length()>=3) {
                    canvas.drawText(tmparr[0], (float) Integer.parseInt(tmparr[2]) / a, (float) Integer.parseInt(tmparr[3]) / b, paint);
                    // Log.d(tmparr[0], Integer.parseInt(tmparr[2]) + "," + Integer.parseInt(tmparr[3]) + "/" + (Integer.parseInt(tmparr[2]) + paint.measureText(tmparr[0], 0, tmparr[0].length())) + "," + (Integer.parseInt(tmparr[3])) + "/" + Integer.parseInt(tmparr[2]) + "," + (Integer.parseInt(tmparr[3]) + Integer.parseInt(tmparr[1])) + "/" + (Integer.parseInt(tmparr[2]) + paint.measureText(tmparr[0], 0, tmparr[0].length())) + "," + (Integer.parseInt(tmparr[3]) + Integer.parseInt(tmparr[1])));
                    r= new Rect();
                    paint.getTextBounds(tmparr[0], 0, tmparr[0].length(), r);

                    r.offset(Integer.parseInt(tmparr[2]), Integer.parseInt(tmparr[3]));

                    Rect tmprect = new Rect(r.left,r.top-20,r.right,r.bottom+20);
                    //  Log.d(tmparr[0], tmprect.left + "," + tmprect.bottom + "," + tmprect.right + "," + tmprect.top);
                    //  r= new Rect(left,top,right,bottom);               /// these four lines would be used to set and search for words once we get the clicked co-ords
                    rectToword.put(k,tmparr[0]);
                    rectangles.add(tmprect);
                    k+=1;
                }
                else
                {
                    continue;
                }
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
            Log.d("errorkushal","errorreadingfile"+e);
        }

        // paint.setTextSize(16);
        //  canvas.drawText("HelloKushal",10,10,paint);
        //Canvas canvas = new Canvas(bitmap_new);
        //Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //paint.setColor(Color.RED);
        //canvas.drawCircle(50, 50, 10, paint);


        image2.setImageBitmap(bitmap_new);

        image2.setOnTouchListener(new View.OnTouchListener() {
            boolean flag = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //  Log.d("valueontouchbegin",x1+"/"+x2+"/"+y1+"/"+y2);
                if(event.getAction()==MotionEvent.ACTION_DOWN)
                {

                    x1 = ((event.getX()*a)/getResources().getDisplayMetrics().density);
                    y1 = ((event.getY()*b)/getResources().getDisplayMetrics().density);
                    Log.d("X1/Y1=",x1+"/"+y1);
                }
                else if(event.getAction()==MotionEvent.ACTION_UP) {
                    x2=event.getX();
                    y2=event.getY();
                    // Log.d("x2/y2=",x2+"//"+y2 );
                }
                if(x2!=0&&x1!=0&&y2!=0&&y1!=0) {
                    flag=true;
                    x1=0.0f;
                    x2=0.0f;
                    y1=0.0f;
                    y2=0.0f;
                }
                String touched_word="";
                for (int i=0;i<rectangles.size();i++)
                {
                    if(rectangles.get(i).contains((int)x1,(int)y1))
                    {
                        Log.d("Word touched",rectToword.get(i));
                        touched_word=rectToword.get(i);
                        break;
                    }
                }
                if(touched_word!="")
                {
                    dialog.dismiss();
                    displayTimeline(touched_word);
                }
                return true;
            }
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.LEFT;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.show();

        ////////////////////
    }
    ////////////////////////////////////// Speech input and output functions begin //////////

    /*public void onVoiceInput(View v)
    {

        if(speech_enabled)
            listenToSpeech();
        else
            return;

    }

    /**
     * Instruct the app to listen for user speech input
     */
   /* private void listenToSpeech() {

        //start the speech recognition intent passing required data
        Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //indicate package
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        //message to display while listening
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a word!");
        //set speech model
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        //start listening
        startActivityForResult(listenIntent, VR_REQUEST);
    }
    /**
     * onInit fires when TTS initializes
     */
   /* public void onInit(int initStatus) {
        //if successful, set locale
        if (initStatus == TextToSpeech.SUCCESS)
            repeatTTS.setLanguage(Locale.UK);//***choose your own locale here***
    }
    /**
     * onActivityResults handles:
     *  - retrieving results of speech recognition listening
     *  - retrieving result of TTS data check
     */
   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check speech recognition result
        if (requestCode == VR_REQUEST && resultCode == RESULT_OK)
        {
            //store the returned word list as an ArrayList
            suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //set the retrieved list to display in the ListView using an ArrayAdapter
            // wordList.setAdapter(new ArrayAdapter<String> (this, R.layout.word, suggestedWords));
            for (int i=0;i<suggestedWords.size();i++)
            {
                Log.d("suggesstion",suggestedWords.get(i));
                displaySuggestions();
            }

        }

        //tss code here
//returned from TTS data check
        if (requestCode == MY_DATA_CHECK_CODE)
        {
            //we have the data - create a TTS instance
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
                repeatTTS = new TextToSpeech(this, this);
                //data not installed, prompt the user to install it
            else
            {
                //intent will take user to TTS download page in Google Play
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        //call superclass method
        super.onActivityResult(requestCode, resultCode, data);
    }
*/
   /* public void displaySuggestions()
    {
        dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.speech_words);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        //``````````````````````````````````````````````````````````` dialog.setTitle("Topic"+(listOfTitles.indexOf(current_title)+1)+"/"+listOfTitles.size()+"|| KF 1/1");
        ListView list_suggestion= (ListView)dialog.findViewById(R.id.suggestedList);

        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, suggestedWords);
        list_suggestion.setAdapter(adapter);


        list_suggestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  String line=listContent[position];
                dialog.dismiss();
                if (suggestedWords.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        repeatTTS.speak("You said" + suggestedWords.get(position), TextToSpeech.QUEUE_FLUSH, null, null);
                        dialog.dismiss();
                        if(word2time.get(suggestedWords.get(position))!=null)
                            displayTimeline(suggestedWords.get(position));
                    } else {
                        repeatTTS.speak("12 e8", TextToSpeech.QUEUE_FLUSH, null);
                    }

                    Log.d("speak", " not spekaing");
                } else
                    repeatTTS.speak("I am Sorry", TextToSpeech.QUEUE_FLUSH, null);

            }
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.LEFT | Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.show();
    }*/
    /////////////////////////// Speech part ends here /////////////////////////
    public void displayTimeline(final String word)
    {

        Log.d("wordclicked",word);

        dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_list);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        //``````````````````````````````````````````````````````````` dialog.setTitle("Topic"+(listOfTitles.indexOf(current_title)+1)+"/"+listOfTitles.size()+"|| KF 1/1");
        list_dialog= (ListView)dialog.findViewById(R.id.dialoglist);
        wordToline=new ArrayList<>();

        CustomListViewValuesArr = new ArrayList<ListModel>();
        ArrayList<Integer> tmplist= word2time.get(word);

        Collections.sort(tmplist);
        for(int i=1;i<tmplist.size();i++)
        {
            if(i<tmplist.size())
            {
                if(tmplist.get(i-1)==tmplist.get(i))
                    continue;
            }
            final ListModel sched= new ListModel();
            String tmptime="";
            String tmpline="";
            int tmp_integer_time=tmplist.get(i-1);
            tmptime=tmp_integer_time/60+":"+tmp_integer_time%60 ;
            Log.d("tmptime",tmptime);
            int index= Collections.binarySearch(times_srt, tmp_integer_time);
            if(index<0)
                index= -(index+1)-1;
            Log.d("index",index+"");
            if(index<times_srt.size()&& index >=0)
            {
                String l1=time_to_line.get(times_srt.get(index));
                String l2= index-1>=0? time_to_line.get(times_srt.get(index-1)):"";
                String l3= index-2>=0? time_to_line.get(times_srt.get(index-2)):"";
                if(l2.indexOf(word)!=-1)
                    tmpline=l2;
                else if(l3.indexOf(word)!=-1)
                    tmpline=l3;
                else if(l1.indexOf(word)!=-1)
                    tmpline=l1;
                if(tmpline!="") {
                    sched.setTextLine(tmpline);
                    sched.setTimeStamp(tmptime);
                    Log.d(tmptime, tmpline);
                    CustomListViewValuesArr.add(sched);
                }
            }


        }
        Log.d("Customlistviewsize",CustomListViewValuesArr.size()+"");
        Resources res =getResources();
        //list t= ( ListView )findViewById( R.id.list );  // List defined in XML ( See Below )

        /**************** Create Custom Adapter *********/
        adapter=new CustomAdapter( CustomListView, CustomListViewValuesArr,res );
        list_dialog.setAdapter( adapter );



        /*for(int i=0;i<tmplist.size();i++)
        {
            rowItem tmp= new rowItem();
            wordToline.add(tmp);
        }*/


        // updateListfound(word);
       /* ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listContent);
       list_dialog.setAdapter(adapter);*/


      /*  list_dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              //  String line=listContent[position];
                int time= Integer.parseInt(word2time.get(word).get(position));
                Log.d("timeseeked", time + "");
                log_recorder("video seeked from " + YTPlayer.getCurrentTimeMillis() + "ms to" + (time * 1000) + "ms");
                YTPlayer.seekToMillis(time * 1000);
                YTPlayer.play();

                dialog.dismiss();
            }
        });*/ // recent comment after adding custom listview

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.LEFT | Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.show();

    }

    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        ListModel tempValues = ( ListModel ) CustomListViewValuesArr.get(mPosition);


        // SHOW ALERT
        Log.d("debugdemoYT", tempValues.getTextLine() + ".." + tempValues.getTimeStamp());
        String timeSt= tempValues.getTimeStamp();
        String[] tmp =timeSt.trim().split(":");
        int time= Integer.parseInt(tmp[0])*60 + Integer.parseInt(tmp[1]);
       // YTPlayer.seekToMillis(time * 1000);
       // YTPlayer.play();
        videoview.seekTo(time*1000);
        dialog.dismiss();


    }

    private int lasttime=0;
   /* private void updateListfound(final String word) {
        ArrayAdapter<rowItem> adapter = new ArrayAdapter<rowItem>(getApplicationContext(), R.layout.list_view_items, wordToline) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
               // View row = convertView;
                //rowItem holder = null;
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_view_items, parent, false);
                }
                TextView t = (TextView) convertView.findViewById(R.id.timeline);
                TextView lineItem = (TextView) convertView.findViewById(R.id.txtTitle);

               // rowItem display_element=wordToline.get(position);
               // display_element.
                // process the times of the word and the subsequent line here and then set t.setText to time and lineItem.settext to line

                String tmptime=word2time.get(word).get(position) ;
                String tmpline="";
                int tmp_integer_time=Integer.parseInt(tmptime);
                tmptime=tmp_integer_time/60+":"+tmp_integer_time%60 ;
                Log.d("tmptime",tmptime);
                int index= Collections.binarySearch(times_srt, tmp_integer_time);
                if(index<0)
                    index= -(index+1);
                Log.d("index",index+"");
                if(index<times_srt.size())
                {
                    String l1=time_to_line.get(times_srt.get(index));
                    String l2= index-1>=0? time_to_line.get(times_srt.get(index-1)):"";
                    String l3= index-2>=0? time_to_line.get(times_srt.get(index-2)):"";
                    if(l2.indexOf(word)!=-1)
                        tmpline=l2;
                    else if(l3.indexOf(word)!=-1)
                        tmpline=l3;
                    else if(l1.indexOf(word)!=-1)
                        tmpline=l1;
                    if(tmpline!=""&& lasttime<tmp_integer_time) {
                        t.setText(tmptime);
                        lineItem.setText(tmpline);
                        Log.d(tmptime, tmpline);
                        lasttime=tmp_integer_time;
                        }
                }

                return convertView;

            }
        };
        Log.d("settinglist","Kushaladapter");
        list_dialog.setAdapter(adapter);
    }*/

    private ArrayList<String> initialise_data_arrays() {
        // init cloud titles, init title-associated keyframe images
        String[] times = {"50","159","291","502","648","761","950","1039","1251","1351","1465","1651","1766","1863","2023","2240","2321","2691","2756"};
        String[] topics = {"introduction","determine presence","verification;formal verification;testing verification;","the product process;manufacturing process","level of testing;gate level;level functional", "test center operational cost;cost chip;design testability","design for testability;cost test;processes test",
                "basic testing principle;circuit under;golden response",
                "actual of physical defects in;physical failures",
                "functional faults;delay faults;transistor faults",
                "circuit permanently;fault circuit;logical fault",
                "fault equivalence and fault dominance;faults considered;number faults", "single faults possible is 2k;single fault;single faults",
                "and 24 single faults;faults circuit;faults sites",
                "fault equivalence;detect fault;detecting fault",
                "fault collapsing;collapse faults;collapsed fault",
                "nand nor;equivalence rules;fault dominance",
                "detect fault;dominance fault;fault collapsing",
                "tests of;branches called;called checkpoints"
        };

        ///////////////////////////////////////////////////

        ArrayList<String> listoffiles = new ArrayList<>();
        File folder = new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/clouds");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                // Log.d("File ", listOfFiles[i].getName());
                listoffiles.add(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        Collections.sort(listoffiles);
        title_list.add("");
        for (int i=0;i<topics.length;i++)
        {
            title_list.add(topics[i]);
            titleHead.put(topics[i], (i + 1) + ".png");
            titleTotime.put(topics[i], Integer.parseInt(times[i]));
            //   Log.d("File", listoffiles.get(i));
        }


        titleTofile.put(topics[0], "-4XBm5t7_Jg_output_word_cloud_0_3272.txt");
        titleTofile.put(topics[1],"-4XBm5t7_Jg_output_word_cloud_104_54.txt");
        titleTofile.put(topics[2],"-4XBm5t7_Jg_output_word_cloud_225_66.txt");
        titleTofile.put(topics[3],"-4XBm5t7_Jg_output_word_cloud_395_105.txt");
        titleTofile.put(topics[4],"-4XBm5t7_Jg_output_word_cloud_575_73.txt");
        titleTofile.put(topics[5],"-4XBm5t7_Jg_output_word_cloud_704_56.txt");
        titleTofile.put(topics[6],"-4XBm5t7_Jg_output_word_cloud_855_94.txt");
        titleTofile.put(topics[7],"-4XBm5t7_Jg_output_word_cloud_994_44.txt");
        titleTofile.put(topics[8], "-4XBm5t7_Jg_output_word_cloud_1145_106.txt");
        titleTofile.put(topics[9], "-4XBm5t7_Jg_output_word_cloud_1301_50.txt");
        titleTofile.put(topics[10],"-4XBm5t7_Jg_output_word_cloud_1408_57.txt");
        titleTofile.put(topics[11],"-4XBm5t7_Jg_output_word_cloud_1558_93.txt");
        titleTofile.put(topics[12],"-4XBm5t7_Jg_output_word_cloud_1708_57.txt");
        titleTofile.put(topics[13], "-4XBm5t7_Jg_output_word_cloud_1814_48.txt");
        titleTofile.put(topics[14], "-4XBm5t7_Jg_output_word_cloud_1943_80.txt");
        titleTofile.put(topics[15], "-4XBm5t7_Jg_output_word_cloud_2131_108.txt");
        titleTofile.put(topics[16], "-4XBm5t7_Jg_output_word_cloud_2280_40.txt");
        titleTofile.put(topics[17], "-4XBm5t7_Jg_output_word_cloud_2506_185.txt");
        titleTofile.put(topics[18], "-4XBm5t7_Jg_output_word_cloud_2712_32.txt");

        //t1.setLinedef("then the electronic physical design automation which is sometimes");
        ArrayList<String> tmp2= new ArrayList<>();// stores the list of lines corressponding to word ;
        // tmp2.add("In  this course we have so far talked about various aspects of electronic design automation namely");
        tmp2.add("then the electronic physical design automation which is sometimes");
        tmp2.add("called the backend design step in the VLSI design");
        tmp2.add("now we would be talking about another very important aspect of design which is testing");
        tmp2.add("kind of a design you can say design at a conceptual stage, it can be a behavior specification");
        tmp2.add("inished product in the form of a chip ASIC. So once we have a design or a finished product");
        tmp2.add("testability aspects of a design and what are the main you can say the rules and techniques");
        word2Line.put("design",tmp2); // map from word to list of lines where the word occured

        ///////// process the srt file for word to line /////////
        File srt_file=new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/others/"+VIDEO+".srt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(srt_file));
            String line;

            while ((line = br.readLine()) != null) {
                if(line.startsWith("0"))
                {
                    String[] t= line.trim().split("-->");
                    String t_stamp= t[0];
                    String[] time1=t_stamp.split(",")[0].split(":");
                    int time= Integer.parseInt(time1[0])*60*60 + Integer.parseInt(time1[1])*60 + Integer.parseInt(time1[2]);
                    line=br.readLine();
                    times_srt.add(time);
                    time_to_line.put(time, line);
                    Log.d(time + "", line);
                    //Log.d("line=",line);
                }
            }
        }
        catch (IOException e)
        {
            Log.e("error",e+"");
        }
        Log.d("srt processed","word being processed");
        File word_line_file=new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/others/"+VIDEO+"_word.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(word_line_file));
            String line;

            while ((line = br.readLine()) != null) {
                String []tmp= line.trim().split(" ");
                ArrayList<Integer> tmpList= new ArrayList<>();
                for (int i=1;i<tmp.length;i++) {
                    tmpList.add(Integer.parseInt(tmp[i]));
                }
                word2time.put(tmp[0],tmpList);
                // Log.d(tmp[0],tmp[tmp.length-1]);
            }
        }
        catch (IOException e)
        {
            Log.e("error",e+"");
        }

        //////////////////////////////////////////////////////////
        for (int j = 1; j < title_list.size(); j++) {
            String title_head = titleHead.get(title_list.get(j));
            ArrayList<String> tmp_list = new ArrayList<String>();
            tmp_list.add(title_head);
            // tmp_list.add("default.jpg");
               /*for (int i = 0; i < 4; i++) {
                   String image_name = title_head + "_image_" + (i + 1) + ".jpg";
                   tmp_list.add(image_name);
               }*/
            titleToimagelist.put(title_head, tmp_list);
        }

        return title_list;
    }
    private void log_recorder(String prompt)
    {
        prompt=sdf.format(new Date())+"User Id:Arbit,Video Id:"+VIDEO+"User Action event:"+prompt+'\n';
        log= new File(Environment.getExternalStorageDirectory()+"/"+appname+"/"+VIDEO+"/others/"+"log.txt");
        if(!log.exists())
        {
            try{
                log.createNewFile();
                Log.d("filedebug", "new file created");
                out=new PrintWriter(new FileWriter(log,true));
                out.append(prompt);
                Log.d("debug", prompt);
                out.close();
            }
            catch (IOException e)
            {
                Log.e("error",e+"");
            }
        }
        else
        {
            try{
                out=new PrintWriter(new FileWriter(log,true));
                out.append(prompt);
                Log.d("debug", prompt);
                out.close();
            }
            catch (IOException e)
            {
                Log.e("error",e+"");
            }
        }
    }

    private static final int SWIPE_MIN_DISTANCE = 40;
    private static final int SWIPE_THRESHOLD_VELOCITY = 280;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d("SWAP", "RL");
                showNextImage(current_title);
                return false; // Right to left
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d("SWAP", "LR");
                showPreviousImage(current_title);
                return false; // Left to right
            }

            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d("SWAP", "DU");
                showNextCloud(current_title);
                return false; // Bottom to top
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d("SWAP", "UD");
                showPreviousCloud(current_title);
                return false; // Top to bottom
            }
            return false;
        }

        @Override
        public  boolean onDoubleTap(MotionEvent e)
        {
            Log.e("onDoubleTap", "onDoubleTap");
            int time= titleTotime.get(current_title);
            dialog.dismiss();
          //  YTPlayer.seekToMillis(time*1000);
            videoview.seekTo(time*1000);

            return true;
        }
    }


}