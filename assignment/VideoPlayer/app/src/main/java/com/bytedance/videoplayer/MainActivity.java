package com.bytedance.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button buttonPlay;
    private Button buttonPause;
    private VideoView videoView;
    private SeekBar seekBar;
    private TextView textView;
    private Button buttonSwitch;
    private FrameLayout frameLayout;
    private RelativeLayout relativeLayout_video;
    private FrameLayout fl;
    private int videowidth=0;
    private int videoheight=0;
    private Handler handler=new Handler();
    //处理视频进度与进度条 进度文字的交互逻辑
    private Runnable runnable=new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            if(videoView.isPlaying()){
                int current=videoView.getCurrentPosition();
                int total=videoView.getDuration();
                float progress=(float) current/total*100;
                seekBar.setProgress((int)progress);

                SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.CHINA);
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                textView.setText("进度"+dateFormat.format(new Date(current))+"/"+dateFormat.format(new Date(total)));
            }
            handler.postDelayed(runnable,1000);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }
    private void initView(){

        fl=findViewById(R.id.fl1);
        //处理进度条
        textView=findViewById(R.id.textView);
        //处理 单击屏幕隐藏按钮进度条等
        frameLayout=findViewById(R.id.frameLayout_tools);
        relativeLayout_video=findViewById(R.id.relativeLayout_video);
        relativeLayout_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(frameLayout.getVisibility()==View.VISIBLE){
                    frameLayout.setVisibility(View.INVISIBLE);
                }
                else frameLayout.setVisibility(View.VISIBLE);
            }
        });
        //暂停按钮
        buttonPause = findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.pause();
            }
        });
        //播放按钮
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.postDelayed(runnable,0);
                videoView.start();
            }
        });
        //videoView初始化
        videoView = findViewById(R.id.videoView);
            //播放结束
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                handler.removeCallbacks(runnable);
                Toast toast=Toast.makeText(MainActivity.this,"视频播放完毕",Toast.LENGTH_SHORT  );
                toast.show();
            }
        });
            //播放开始
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        videowidth = mp.getVideoWidth();
                        // 获取视频资源的高度
                        videoheight = mp.getVideoHeight();
                    }
                });

                handler.postDelayed(runnable,0);
                videoView.start();

            }
        });
        Uri uri = getIntent().getData();
        if(uri!=null) videoView.setVideoURI(uri);
        else videoView.setVideoPath(getVideoPath(R.raw.bytedance));//设置播放路径



        //全屏横屏切换按钮
        buttonSwitch=findViewById(R.id.buttonSwitch);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onClick(View view) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 当前是横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    buttonSwitch.setText("切换为竖屏");
                    getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
                }
            }
        });
        //处理进度条
        seekBar=findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            //滑动过程处理，滑动条与播放进度textview 的交互
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(videoView.isPlaying()){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.CHINA);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                    float seekbar_cur=(float)(seekBar.getProgress()*videoView.getDuration())/100;
                    textView.setText("进度" +dateFormat.format(new Date((int)seekbar_cur))+"/"+dateFormat.format(new Date(videoView.getDuration())));
                }

            }
            //滑动开始时处理
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                    handler.removeCallbacks(runnable);
            }
            //滑动条拖动结束时处理
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress()*videoView.getDuration()/100);
                handler.postDelayed(runnable,0);
                videoView.start();
            }
        });
    }
    private String getVideoPath(int resId) {
        return "android.resource://" + this.getPackageName() + "/" + resId;
    }
    //监测屏幕横竖屏变化，改变切换按钮文字
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            buttonSwitch.setText("切换为竖屏");
            relativeLayout_video.setGravity(Gravity.CENTER);
        }else{
            buttonSwitch.setText("切换为全屏");
            Log.d("121212", "onConfigurationChanged: ");
            relativeLayout_video.setGravity(Gravity.CENTER);
        }
    }
}
