package com.example.mikem.finalproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.toilelibre.libe.soundtransform.actions.fluent.FluentClient;
import org.toilelibre.libe.soundtransform.actions.fluent.FluentClientSoundImported;
import org.toilelibre.libe.soundtransform.actions.fluent.FluentClientWithFile;
import org.toilelibre.libe.soundtransform.model.converted.sound.Sound;
import org.toilelibre.libe.soundtransform.model.converted.sound.transform.EightBitsSoundTransform;
import org.toilelibre.libe.soundtransform.model.converted.sound.transform.PitchSoundTransform;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.inputstream.AudioFileHelper;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

import static org.toilelibre.libe.soundtransform.actions.fluent.FluentClient.start;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Menu";
    private static final int READ_REQUEST_CODE = 42;
    SeekBar seekBar;
    MediaPlayer mediaPlayer = null;
    Handler handler;
    Runnable runnable;
    Uri currentAudioURI = null;
    File outputDir;
    File currentFile;



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

        final Button bitify = findViewById(R.id.bitButton);
        bitify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "bitify button clicked");
                if (currentAudioURI != null) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        mediaPlayer.release();
                        if (currentFile != null) {
                            currentFile.delete();
                        }

                        outputDir = getApplicationContext().getCacheDir();
                        currentFile = File.createTempFile("prefix", "extension", outputDir);
                        start().withAudioInputStream(getContentResolver().openInputStream(currentAudioURI)).importToSound().apply(new EightBitsSoundTransform(25)).exportToFile(currentFile);
                        currentAudioURI = Uri.fromFile(currentFile);
                    } catch (Exception e) {
                        Log.d(TAG, "Bitify passed a sound transform exception");
                    }
                }
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(getApplicationContext(), currentAudioURI);
                    mediaPlayer.prepare();
                    seekBar.setMax(mediaPlayer.getDuration());

                }
                catch (IOException ex) {
                    Log.wtf(TAG, "Failure to setDataSource");
                    Log.d(TAG, currentFile.toString());
                }

            }
        });

        final Button save = findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "save button clicked");
            }
        });

        final ImageButton lessPitch = findViewById(R.id.pitchLess);
        lessPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "Less Pitch button clicked");
                if (currentAudioURI != null) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        mediaPlayer.release();
                        if (currentFile != null) {
                            currentFile.delete();
                        }

                        outputDir = getApplicationContext().getCacheDir();
                        currentFile = File.createTempFile("prefix", "extension", outputDir);
                        start().withAudioInputStream(getContentResolver().openInputStream(currentAudioURI)).importToSound().apply(new PitchSoundTransform(95)).exportToFile(currentFile);
                        currentAudioURI = Uri.fromFile(currentFile);
                    } catch (Exception e) {
                        Log.d(TAG, "Pitch less passed a sound transform exception");
                    }
                }
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(getApplicationContext(), currentAudioURI);
                    mediaPlayer.prepare();
                    seekBar.setMax(mediaPlayer.getDuration());

                }
                catch (IOException ex) {
                    Log.wtf(TAG, "Failure to setDataSource");
                    Log.d(TAG, currentFile.toString());
                }

            }
        });

        final ImageButton morePitch = findViewById(R.id.pitchMore);
        morePitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "more pitch button clicked");
                if (currentAudioURI != null) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        mediaPlayer.release();
                        if (currentFile != null) {
                            currentFile.delete();
                        }

                        outputDir = getApplicationContext().getCacheDir();
                        currentFile = File.createTempFile("prefix", "extension", outputDir);
                        start().withAudioInputStream(getContentResolver().openInputStream(currentAudioURI)).importToSound().apply(new PitchSoundTransform(105)).exportToFile(currentFile);
                        currentAudioURI = Uri.fromFile(currentFile);
                    } catch (Exception e) {
                        Log.d(TAG, "Pitch more passed a sound transform exception");
                    }
                }
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(getApplicationContext(), currentAudioURI);
                        mediaPlayer.prepare();
                        seekBar.setMax(mediaPlayer.getDuration());

                    }
                    catch (IOException ex) {
                        Log.wtf(TAG, "Failure to setDataSource");
                        Log.d(TAG, currentFile.toString());
                    }

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
        intent.setType("audio/wav");
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
            if (currentFile != null) {
                currentFile.delete();
            }
            if (outputDir != null) {
                outputDir.delete();
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

    public void convertToWav() {
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                Log.d(TAG, convertedFile.getPath());
                Log.d(TAG, currentFile.getPath());

                // So fast? Love it!
            }
            @Override
            public void onFailure(Exception error) {
                Log.d(TAG, error.toString());
                // Oops! Something went wrong
            }
        };
        AndroidAudioConverter.with(this)
                    // Your current audio file
                .setFile(currentFile)

                            // Your desired audio format
                            .setFormat(AudioFormat.WAV)

                            // An callback to know when conversion is finished
                            .setCallback(callback)

                            // Start conversion
                            .convert();
    }

    public void convertToFile(InputStream stream, File target) {
        try {
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            OutputStream outStream = new FileOutputStream(target);
            outStream.write(buffer);
        } catch (IOException e) {
            Log.d(TAG, "Problem with converting stream to byte[], reading, or writing");
        }
    }

}
