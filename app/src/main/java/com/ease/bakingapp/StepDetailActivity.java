package com.ease.bakingapp;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ease.bakingapp.model.Step;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

import static com.ease.bakingapp.MainActivity.isThereConnection;

public class StepDetailActivity extends AppCompatActivity {

    private static final String CURRENT_STEP_POSITION = "current step position";
    private ArrayList<Step> steps;
    private int position;
    private TextView stepDescriptionTextView;
    private boolean isPaused;
    private SimpleExoPlayerView mPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private TextView videoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setTheme(R.style.AppTheme_NoActionBar);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_step_detail);
        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_STEP_POSITION)) {
            position = savedInstanceState.getInt(CURRENT_STEP_POSITION);
        } else {
            position = getIntent().getIntExtra(RecipeDetailActivity.STEP_POSITION, -1);

        }
        steps = getIntent().getParcelableArrayListExtra(MainActivity.STEPS_LIST);

        initViews();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setDescription();
            setTitle(steps.get(position).getShortDescription());

        }
        initializePlayer(Uri.parse(steps.get(position).getVideoURL()));


    }

    private void setDescription() {
        stepDescriptionTextView.setText(steps.get(position).getDescription());
    }


    private void initViews() {
        mPlayerView = (SimpleExoPlayerView) findViewById(R.id.videoView);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            stepDescriptionTextView = findViewById(R.id.step_description_text);
        }
        videoTextView = findViewById(R.id.exoplayerTextView);

    }


    public void playPrevious(View view) {
        if (position > 0) {
            position = position - 1;
        } else {
            position = steps.size() - 1;
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setDescription();
        }
        releasePlayer();
        initializePlayer(Uri.parse(steps.get(position).getVideoURL()));
//         playVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }

    }

    public void playNext(View view) {
        if (position < steps.size() - 1) {
            position = position + 1;
        } else {
            position = 0;
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setDescription();
        }
        releasePlayer();
        initializePlayer(Uri.parse(steps.get(position).getVideoURL()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    private void initializePlayer(Uri mediaUri) {
        if (mediaUri != null && !mediaUri.toString().isEmpty()) {
            if (isThereConnection(this)) {
                if (mExoPlayer == null) {
                    // Create an instance of the ExoPlayer.
                    TrackSelector trackSelector = new DefaultTrackSelector();
                    LoadControl loadControl = new DefaultLoadControl();
                    mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

                    mPlayerView.setPlayer(mExoPlayer);
                    // Prepare the MediaSource.
                    String userAgent = Util.getUserAgent(this, "BakingApp");
                    MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(this, userAgent), new DefaultExtractorsFactory(), null, null);
                    mExoPlayer.prepare(mediaSource);
                    mExoPlayer.setPlayWhenReady(true);

                }
                videoTextView.setVisibility(View.GONE);

            } else {
                videoTextView.setVisibility(View.VISIBLE);
                videoTextView.setText(getText(R.string.no_network));

            }
        } else {
            if (steps.get(position).getThumbnailURL().endsWith(".mp4")) {
                initializePlayer(Uri.parse(steps.get(position).getThumbnailURL()));
            } else {
                videoTextView.setVisibility(View.VISIBLE);
                videoTextView.setText(getText(R.string.no_video_available));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_STEP_POSITION, position);

    }

}
