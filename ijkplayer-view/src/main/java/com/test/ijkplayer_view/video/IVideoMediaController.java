package com.test.ijkplayer_view.video;

import com.test.ijkplayer_view.widget.IMediaController;

/**
 * Created by xu.wang
 * Date on  2018/8/15 11:29:39.
 *
 * @Desc
 */

public interface IVideoMediaController extends IMediaController {
    void setPreparedState();    //准备完成的状态

    void setErrorState();       //错误的状态

    void setCompleteState();    //加载完成的状态

    void setLoadingState();     //设置加载中的状态

    boolean onBackPress();         //返回键用于执行全屏,返回true ,说明执行全屏变非全屏逻辑

    void setTitle(String title);
}
