package com.bignox.testijkplayer;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.ijkplayer_view.video.VideoMediaController;
import com.test.ijkplayer_view.widget.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    protected VideoMediaController mVideoController;

    private IjkVideoView ijkVideoView;
    private RelativeLayout rl_content;
    ViewGroup mParentView;
    private PowerManager.WakeLock wakeLock;
    private String exhibitionUrl = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
    }

    private void initializeView() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);   //启用屏幕常亮功能
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        wakeLock.acquire();

        ijkVideoView = findViewById(R.id.ijk_video_view);
        rl_content = findViewById(R.id.rl_video_content);

        mVideoController = new VideoMediaController();
        mVideoController.setTitle("播放视频");
        mVideoController.setOnVideoControllerClickListener(mOnVideoControllerClickListener);
        ijkVideoView.setMediaController(mVideoController);

        ijkVideoView.setMediaController(mVideoController);
        ijkVideoView.setOnPreparedListener(mOnPreparedListener);
        ijkVideoView.setOnErrorListener(mOnErrorListener);
        ijkVideoView.setOnCompletionListener(mOnCompletionListener);


        prepareToPlay();
    }

    private void prepareToPlay() {
        ijkVideoView.setVideoPath(exhibitionUrl);
        ijkVideoView.requestFocus();
        ijkVideoView.start();
    }

    IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mVideoController.setPreparedState();
            Log.d(TAG, "onPreparedListener");
        }
    };

    IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            mVideoController.setErrorState();
            Log.e(TAG, "framework error + " + i + " impel error + " + i1);
            return false;
        }
    };
    IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mVideoController.setCompleteState();
            Log.d(TAG, "OnCompletionListener");
        }
    };

    VideoMediaController.OnVideoControllerClickListener mOnVideoControllerClickListener = new VideoMediaController.OnVideoControllerClickListener() {
        @Override
        public void clickScreenChange(int screenState) {
            if (screenState == VideoMediaController.FULL_SCREEN) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mParentView = ((ViewGroup) rl_content.getParent());
                mParentView.removeView(rl_content);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                getWindow().addContentView(rl_content, layoutParams);
            } else if (screenState == VideoMediaController.SHRINK_SCREEN) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ((ViewGroup) rl_content.getParent()).removeView(rl_content);
                mParentView.addView(rl_content);
            }
        }

        @Override
        public void back() {            //返回键
            finishThisPage();
        }

        @Override
        public void downloadCache() {   //下载该视频

        }

        @Override
        public void share() {   //分享

        }

        @Override
        public void danmukuStateChange(boolean isShow) {
            if (isShow) {
//                mDanmakuView.show();
            } else {
//                mDanmakuView.hide();
            }
        }

    };

    private void finishThisPage() {
        if (mVideoController != null && mVideoController.onBackPress()) {
            return;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        if (ijkVideoView != null) {
            ijkVideoView.stopPlayback();
            ijkVideoView.release(true);
        }
        if (mVideoController != null) {
            mVideoController.release();
        }
        if (wakeLock != null) {
            wakeLock.release();
        }
        super.onDestroy();
    }
}
