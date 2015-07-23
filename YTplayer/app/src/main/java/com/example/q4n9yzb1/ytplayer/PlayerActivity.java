package com.example.q4n9yzb1.ytplayer;

/**
 * Created by q4N9YZB1 on 11-06-2015.
 */

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by q4N9YZB1 on 11-06-2015.
 */
public class PlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView playerView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_player);

         playerView = (YouTubePlayerView)findViewById(R.id.player_view);
         playerView.initialize(YouTubeConnector.KEY, this);
        // YouTubePlayerView youtubeView = new YouTubePlayerView(this);
       // playerView.initialize(YouTubeConnector.KEY, this);
       // FrameLayout youtubeContainer = (FrameLayout) findViewById(R.id.player_view);
        Log.d(PlayerActivity.class.getSimpleName(),"THIS SEEMS NOT TO WORK");
        //youtubeContainer.addView(playerView);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult result) {
        Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean restored) {
        Log.d(PlayerActivity.class.getSimpleName(),"Initialization suceessful method entering kush");
        player.loadVideo(getIntent().getStringExtra("VIDEO_ID"));
        if(!restored){
            player.cueVideo(getIntent().getStringExtra("VIDEO_ID"));
        }
    }
}