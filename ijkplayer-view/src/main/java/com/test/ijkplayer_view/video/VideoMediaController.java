package com.test.ijkplayer_view.video;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.test.ijkplayer_view.R;
import com.test.ijkplayer_view.utils.IjkAnimUtils;

import java.util.Locale;

/**
 * Created by xu.wang
 * Date on  2018/8/13 10:22:44.
 *
 * @Desc 点播 MeidaController
 */

public class VideoMediaController implements IVideoMediaController, View.OnClickListener {
    private static final String TAG = "VideoMediaController";
    private static final int UPDATE_PROGRESS = 110; //刷新进度
    private static final int DELAY_SHOW = 111;      //延时显示
    private static final int DELAY_HIDE = 112;      //延时隐藏

    public static final int FULL_SCREEN = 1111;  //全屏
    public static final int SHRINK_SCREEN = 1112;    //非全屏

    public static final int PLAYING_STATE = 1116;    //点播状态
    public static final int LIVING_STATE = 1117;    //直播状态

    private int mPlayMethod = PLAYING_STATE;
    private View mMediaController;
    private ImageView iv_fullscreen;
    private LinearLayout ll_top, ll_bottom;
    private TextView tv_duration, tv_title;

    private MediaController.MediaPlayerControl mPlayer;
    private boolean isRunning = true;    //点击后,是否过几秒自动隐藏
    private boolean isDrag = false;     //是否正在拖动
    private boolean isShowDanmuku = true;   //是否显示弹幕

    private enum PLAY_QUALITY {
        HIGH, NORMAL
    }

    private PLAY_QUALITY mPlayState = PLAY_QUALITY.HIGH;
    private long mDuration = -1;

    private int mCurScreen = SHRINK_SCREEN;
    private OnVideoControllerClickListener mListener;

    private Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    mHander.removeMessages(UPDATE_PROGRESS);
                    refreshState();
                    if (isRunning) mHander.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    break;
                case DELAY_SHOW:
                    mHander.removeMessages(DELAY_SHOW);
                    show();
                    break;
                case DELAY_HIDE:
                    mHander.removeMessages(DELAY_HIDE);
                    if (!isDrag) hide();
                    break;
            }
        }
    };
    private SeekBar sk_play;
    private RelativeLayout rl_state_content;
    private TextView tv_show;
    private ProgressBar pb_loading;
    private ImageView iv_back;
    private String mTitle;
    private ImageView iv_high, iv_normal, iv_danmaku, iv_download, iv_share;


    private void refreshState() {
        if (mPlayer == null) return;
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (sk_play != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                Log.d(TAG, "refreshState = " + pos);
                sk_play.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            sk_play.setSecondaryProgress(percent * 10);
        }
        mDuration = duration;
        tv_duration.setText(generateTime(position) + "/" + generateTime(mDuration));
    }

    private static String generateTime(long position) {
        int totalSeconds = (int) ((position / 1000.0) + 0.5);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    @Override
    public boolean isShowing() {
        return mMediaController == null ? false :
                (ll_top.getVisibility() == View.VISIBLE && ll_bottom.getVisibility() == View.VISIBLE);
    }

    @Override
    public void setAnchorView(View view) {
        if (mMediaController != null) return;
        mMediaController = LayoutInflater.from(view.getContext()).inflate(R.layout.layout_media_controller_player, (ViewGroup) view, false);
        iv_fullscreen = mMediaController.findViewById(R.id.iv_ijkview_mediacontroller_fullscreen);
        ll_top = mMediaController.findViewById(R.id.ll_media_controller_top);
        ll_bottom = mMediaController.findViewById(R.id.ll_media_controller_bottom);
        tv_duration = mMediaController.findViewById(R.id.tv_ijkview_duration);
        tv_title = mMediaController.findViewById(R.id.tv_ijkview_videoplay_title);
        rl_state_content = mMediaController.findViewById(R.id.rl_ijkview_state_content);
        tv_show = mMediaController.findViewById(R.id.tv_ijkview_mediacontroller_show);
        pb_loading = mMediaController.findViewById(R.id.pb_ijkview_mediacontroller_loading);
        iv_back = mMediaController.findViewById(R.id.iv_ijkview_videoplay_back);
        sk_play = mMediaController.findViewById(R.id.sk_ijkview_play);
        iv_high = mMediaController.findViewById(R.id.iv_ijkview_mediaontroller_high);
        iv_normal = mMediaController.findViewById(R.id.iv_ijkview_mediaontroller_normal);
        iv_danmaku = mMediaController.findViewById(R.id.iv_ijkview_mediacontroller_danmaku);
        iv_download = mMediaController.findViewById(R.id.iv_ijk_media_controller_download);
        iv_share = mMediaController.findViewById(R.id.iv_ijk_media_controller_share);

        sk_play.setMax(1000);
        sk_play.setOnSeekBarChangeListener(mSeekBarChangeListener);
        iv_danmaku.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_normal.setOnClickListener(this);
        iv_high.setOnClickListener(this);
        iv_download.setOnClickListener(this);
        iv_share.setOnClickListener(this);

        if (!TextUtils.isEmpty(mTitle)) tv_title.setText(mTitle);

        setIsLivingOrPlaying(mPlayMethod);
        ((ViewGroup) view).addView(mMediaController);
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override //加载完成状态
    public void setPreparedState() {
        if (mMediaController == null) return;
        rl_state_content.setVisibility(View.GONE);
    }

    @Override //加载失败
    public void setErrorState() {
        if (mMediaController == null) return;
        rl_state_content.setVisibility(View.VISIBLE);
        pb_loading.setVisibility(View.GONE);
        tv_duration.setText("错误 加载失败...");
    }

    @Override
    public void setCompleteState() {
        if (mMediaController == null) return;
        rl_state_content.setVisibility(View.VISIBLE);
        pb_loading.setVisibility(View.GONE);
        tv_duration.setText("播放完成...");
    }

    @Override
    public void setLoadingState() {
        if (mMediaController == null) return;
        rl_state_content.setVisibility(View.VISIBLE);
        pb_loading.setVisibility(View.VISIBLE);
        tv_duration.setText("加载中...");
    }

    @Override
    public boolean onBackPress() {
        if (mCurScreen == FULL_SCREEN) {
            mCurScreen = SHRINK_SCREEN;
            if (mListener != null) mListener.clickScreenChange(mCurScreen);
            return true;
        }
        return false;
    }

    @Override
    public void setTitle(String title) {
        mTitle = title;
        if (mMediaController == null) return;
        tv_title.setText(mTitle);
    }

    SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mDuration <= 0) return;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDrag = true;
            mHander.removeMessages(DELAY_HIDE);
            show();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isDrag = false;
            mHander.sendEmptyMessageDelayed(DELAY_HIDE, 5000);
            Log.d(TAG, "onStopTrackingTouch");
            final long newPosition = (mDuration * seekBar.getProgress()) / 1000;
            tv_duration.setText(generateTime(newPosition) + "/" + generateTime(mDuration));
            mPlayer.seekTo((int) newPosition);
        }
    };


    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        Log.d(TAG, "setMediaPlayer");
        this.mPlayer = player;
        mHander.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
    }

    @Override
    public void show(int timeout) {
        if (mMediaController != null) {
            mHander.removeMessages(DELAY_HIDE);
            mHander.sendEmptyMessageDelayed(DELAY_SHOW, timeout);
        }
    }

    @Override
    public void hide() {
        if (mMediaController != null && isShowing()) {
            mHander.removeMessages(DELAY_SHOW);
//            mMediaController.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隐藏状态栏
            ll_top.setVisibility(View.INVISIBLE);
            ll_bottom.setVisibility(View.INVISIBLE);
            IjkAnimUtils.menuAnim(0, -ll_top.getHeight(), 1.0f, 0.4f, ll_top, ll_top.getContext());
            IjkAnimUtils.menuAnim(0, ll_bottom.getHeight(), 1.0f, 0.4f, ll_bottom, ll_bottom.getContext());
        }
    }

    @Override
    public void show() {
        if (mMediaController != null && !isShowing()) {
//            mMediaController.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);    //显示状态栏
            ll_top.setVisibility(View.VISIBLE);
            ll_bottom.setVisibility(View.VISIBLE);
            IjkAnimUtils.menuAnim(-ll_top.getHeight(), 0, 0.4f, 1.0f, ll_top, ll_top.getContext());
            IjkAnimUtils.menuAnim(ll_bottom.getHeight(), 0, 0.4f, 1.0f, ll_bottom, ll_bottom.getContext());
            mHander.removeMessages(DELAY_HIDE);
            mHander.removeMessages(DELAY_SHOW);
            if (isRunning) mHander.sendEmptyMessageDelayed(DELAY_HIDE, 5000);
        }
    }

    @Override
    public void showOnce(View view) {

    }

    @Override
    public void onClick(View v) {
        if (mMediaController == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.iv_ijkview_mediacontroller_fullscreen) {
            if (mCurScreen == SHRINK_SCREEN) {
                mCurScreen = FULL_SCREEN;
            } else if (mCurScreen == FULL_SCREEN) {
                mCurScreen = SHRINK_SCREEN;
            }

            if (mListener != null) mListener.clickScreenChange(mCurScreen);
        } else if (id == R.id.iv_ijkview_videoplay_back) {
            if (!onBackPress()) {
                if (mListener != null) mListener.back();
            }
        } else if (id == R.id.iv_ijkview_mediaontroller_normal) {
            mPlayState = PLAY_QUALITY.NORMAL;
            refreshPlayQUalityState();
        } else if (id == R.id.iv_ijkview_mediaontroller_high) {
            mPlayState = PLAY_QUALITY.HIGH;
            refreshPlayQUalityState();
        } else if (id == R.id.iv_ijkview_mediacontroller_danmaku) {
            isShowDanmuku = !isShowDanmuku;
            if (isShowDanmuku) {
                iv_danmaku.setImageResource(R.drawable.ic_video_play_danmuku_on);
            } else {
                iv_danmaku.setImageResource(R.drawable.ic_video_play_danmuku);
            }
            if (mListener != null) mListener.danmukuStateChange(isShowDanmuku);
        } else if (id == R.id.iv_ijk_media_controller_download) {
            if (mListener != null) mListener.downloadCache();
        } else if (id == R.id.iv_ijk_media_controller_share) {
            if (mListener != null) mListener.share();
        }
    }

    private void refreshPlayQUalityState() {
        if (mPlayState == PLAY_QUALITY.HIGH) {
            iv_high.setImageResource(R.drawable.ic_video_play_high_quality_on);
            iv_normal.setImageResource(R.drawable.ic_video_play_normal_quality);
        } else if (mPlayState == PLAY_QUALITY.NORMAL) {
            iv_high.setImageResource(R.drawable.ic_video_play_high_quality);
            iv_normal.setImageResource(R.drawable.ic_video_play_normal_quality_on);
        }
    }

    public void release() {
        isRunning = false;
        if (mHander != null) {
            mHander.removeMessages(DELAY_HIDE);
            mHander.removeMessages(DELAY_SHOW);
            mHander.removeMessages(UPDATE_PROGRESS);
        }
    }

    public void setIsLivingOrPlaying(int state) {
        mPlayMethod = state;
        if (state == LIVING_STATE) {
            if (sk_play != null) sk_play.setVisibility(View.INVISIBLE);
            if (tv_duration != null) tv_duration.setVisibility(View.INVISIBLE);
        } else if (state == PLAYING_STATE) {
            if (sk_play != null) sk_play.setVisibility(View.VISIBLE);
            if (tv_duration != null) tv_duration.setVisibility(View.VISIBLE);
        }
    }

    public void setOnVideoControllerClickListener(OnVideoControllerClickListener listener) {
        this.mListener = listener;
    }

    public interface OnVideoControllerClickListener {
        void clickScreenChange(int screenState);    //点击切换全屏

        void back();                                //返回键

        void downloadCache();                       //点击下载缓存

        void share();                               //分享

        void danmukuStateChange(boolean isShow);                  //弹幕状态切换
    }
}
