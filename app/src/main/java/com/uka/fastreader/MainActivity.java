package com.uka.fastreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_RESULT = 54321;

    RelativeLayout layout;

    RelativeLayout layoutUI;

    TextView textView;
    TextView tvSpeed;

    Button buttonLoad;

    SeekBar seekBar;

    boolean started = false;

    String[] wordList;

    int wpm = 240;
    int speed = 60 * 1000/wpm;

    int currentWord = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.mainLayout);
        layoutUI = findViewById(R.id.layoutUI);

        buttonLoad = findViewById(R.id.button_load);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        textView = findViewById(R.id.text);
        tvSpeed = findViewById(R.id.speed);

        tvSpeed.setText(wpm + "wpm");
        tvSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(seekBar.getVisibility() == View.GONE)
                    seekBar.setVisibility(View.VISIBLE);
                else
                    seekBar.setVisibility(View.GONE);
            }
        });

        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                wpm = i;
                speed = 60*1000/wpm;
                tvSpeed.setText( wpm + "wpm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //read text and save it to word list
        initializeWordList();

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopReading();
            }
        });
    }

    void showFileChooser(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select file"), FILE_SELECT_RESULT);
        }
        catch (android.content.ActivityNotFoundException e){
            e.printStackTrace();

            Toast.makeText(this, "Cannot find app for this", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case FILE_SELECT_RESULT:
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    try {
                        String path = getPath(this, uri);
                        Log.e(">>>>>>>>", path);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnableCode);
        started = false;
    }

    Handler handler = new Handler();

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            if(currentWord < wordList.length-1)
                currentWord++;
            updateWord();
            handler.postDelayed(runnableCode, speed);
        }
    };

    void updateWord(){
        textView.setText(wordList[currentWord]);
    }

    void initializeWordList(){
        String tmp = getResources().getString(R.string.str_story);
        wordList = tmp.split(" ");
        /*for(String s : wordList)
            android.util.Log.e(">>>>", s);*/
    }

    void startStopReading(){
        if(started){
            layoutUI.setVisibility(View.VISIBLE);
            handler.removeCallbacks(runnableCode);
            started = false;
        }
        else{
            layoutUI.setVisibility(View.GONE);
            handler.postDelayed(runnableCode, speed);
            started = true;
        }
    }
}
