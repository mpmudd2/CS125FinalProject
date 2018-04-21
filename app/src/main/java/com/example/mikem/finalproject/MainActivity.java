package com.example.mikem.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Menu";
    private static final int READ_REQUEST_CODE = 42;
    private Uri currentAudioURI = null;
    SeekBar seekBar;
    MediaPlayer mediaPlayer = null;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();


        seekBar = findViewById(R.id.songProgressBar);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "progress bar is changed");
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                    Log.d(TAG, "progress bar is changed and seekTo is called");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        final Button openFile = findViewById(R.id.openFile);
        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "Open file button clicked");
                startOpenFile();
            }
        });

        final ImageButton play = findViewById(R.id.playButton);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "play button clicked");
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                        playCycle();
                    }
                }
            }
        });
    }

    public void playCycle() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());

            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }
    }
    private void startOpenFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "onActivityResult with code " + requestCode + " failed");
            return;
        }

        if (requestCode == READ_REQUEST_CODE) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            Log.w(TAG, "Storing this Audio URI from the upload button");
            currentAudioURI = data.getData();
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(getApplicationContext(), currentAudioURI);
                mediaPlayer.prepare();
                if (seekBar != null) {
                    seekBar.setMax(mediaPlayer.getDuration());
                }

            }
            catch (IOException ex) {
                Log.wtf(TAG, "file does not exist");
            }

        } else {
            Log.d(TAG, "requestCode was not expected: " + requestCode);
        }

        Log.d(TAG, "Audio selection produced URI " + currentAudioURI);
    }
}
