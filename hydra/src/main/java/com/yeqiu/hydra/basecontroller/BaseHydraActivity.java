package com.yeqiu.hydra.basecontroller;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gyf.barlibrary.ImmersionBar;
import com.yeqiu.hydra.net.OkGoManager;
import com.yeqiu.hydra.utils.ActivityManager;
import com.yeqiu.hydra.utils.KeybordUtils;
import com.yeqiu.hydra.utils.LogUtils;
import com.yeqiu.hydra.utils.ResourceUtil;
import com.yeqiu.hydra.utils.ScreenUtils;
import com.yeqiu.hydra.utils.net.NetWorkUtils;
import com.yeqiu.hydra.widget.statuslayout.OnStatusClickListener;
import com.yeqiu.hydra.widget.statuslayout.StatusLayout;
import com.yeqiu.hydrautils.R;

import org.greenrobot.eventbus.EventBus;

/**
 * @project：HailHydra
 * @author：小卷子
 * @date 2018/9/15
 * @describe：
 * @fix：activity
 */
public abstract class BaseHydraActivity extends SwipeBackActivity implements View
        .OnClickListener, OnStatusClickListener {

    protected LinearLayout llBaseRoot;
    protected StatusLayout statusLayout;
    protected LinearLayout llHeadLayoutRoot;
    protected ImageView ivHeadBack;
    protected TextView tvHeadClose;
    protected TextView headerTitle;
    protected TextView tvheaderRight;
    protected ImageView ivheaderRight;
    protected View headLine;
    protected ImmersionBar imersionBar;
    protected RelativeLayout rlCommonHead;
    protected int openEnterAnimation = R.anim.slide_right_to_left_in;
    protected int openExitAnimation = R.anim.slide_right_to_left_out;
    protected int closeEnterAnimation = R.anim.slide_left_to_right_in;
    protected int closeExitAnimation = R.anim.slide_left_to_right_out;
    private Activity context;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setActivityAnimation(openEnterAnimation,openExitAnimation,closeEnterAnimation,closeExitAnimation);
        showActivityAnimation(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        context = this;
        //添加到activity管理器
        ActivityManager.getAppManager().addActivity(this);
        ///隐藏ActionBar
        isShowActionBar();
        setSwipeBackEnable(isSwipeBack());
        init();

    }

    /**
     * activty跳转动画
     *
     * @param isCreate
     */
    private void showActivityAnimation(boolean isCreate) {

        if (!isShowActivityAnimation()) {
            return;
        }

        if (isCreate) {
            overridePendingTransition(openEnterAnimation, openExitAnimation);
        } else {
            overridePendingTransition(closeEnterAnimation, closeExitAnimation);
        }

    }


    private void isShowActionBar() {

        if (removeActionBar()) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        }

    }


    private void init() {
        llBaseRoot = (LinearLayout) findViewById(R.id.ll_base_root);
        statusLayout = (StatusLayout) findViewById(R.id.base_status_layout);
        statusLayout.setBackgroundColor(ResourceUtil.getColor(R.color.color_white));
        statusLayout.setContentView(getContentView());
        statusLayout.showContentView();
        statusLayout.setOnStatusClickListener(this);
        initImmersionBar();
        registerEventBus();
        initHead();
        initView();
        initData();
        initListener();
    }


    /**
     * 初始化head标题栏
     * 标题栏中包括 返回 标题 右侧文字
     */
    private void initHead() {
        llHeadLayoutRoot = (LinearLayout) findViewById(R.id.ll_common_header_root);
        ivHeadBack = (ImageView) findViewById(R.id.iv_common_head_back);
        tvHeadClose = (TextView) findViewById(R.id.tv_common_head_close);
        headerTitle = (TextView) findViewById(R.id.tv_common_head_title);
        tvheaderRight = (TextView) findViewById(R.id.tv_common_head_title_right);
        ivheaderRight = (ImageView) findViewById(R.id.iv_common_head_title_right);
        rlCommonHead = (RelativeLayout) findViewById(R.id.rl_common_head);
        headLine = findViewById(R.id.head_line);
        //默认隐藏右侧的图片和文字
        tvheaderRight.setVisibility(View.GONE);
        ivheaderRight.setVisibility(View.GONE);
        tvHeadClose.setVisibility(View.GONE);
        ivHeadBack.setOnClickListener(onClickListener);
        tvheaderRight.setOnClickListener(onClickListener);
        ivheaderRight.setOnClickListener(onClickListener);
        tvHeadClose.setOnClickListener(onClickListener);
        ivHeadBack.setImageResource(getDefHeadBackImgId());

        addStatusViewWithColor(getStatusColorId());
    }


    /**
     * 添加状态栏占位视图
     * 如果页面顶部是图片，可以重新此方法 不添加任何占位
     *
     * @param colorId
     */
    protected void addStatusViewWithColor(int colorId) {

        View statusBarView = new View(getActivity());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ScreenUtils.getStatusHeight());
        statusBarView.setBackgroundColor(ResourceUtil.getColor(colorId));

        llBaseRoot.addView(statusBarView, 0, lp);

    }


    /**
     * 初始化沉浸式
     */
    protected void initImmersionBar() {

        if (isImmersionBarEnabled()) {
            imersionBar = ImmersionBar.with(this)
                    .keyboardEnable(true)
                    .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION);
            imersionBar.init();

            setStatusBarDarkFont(isStatusBarDarkFont());
        }
    }


    /**
     * 注册EventBus
     */
    private void registerEventBus() {

        if (isRegisterEventBus()) {

            EventBus.getDefault().register(this);
        }

    }


    /**
     * 标题栏返回点击
     */
    protected void onBackClick() {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getAppManager().finishActivity(this);

        if (isImmersionBarEnabled()) {
            ImmersionBar.with(this).destroy();
        }

        if (isRegisterEventBus()) {
            EventBus.getDefault().unregister(this);
        }

        //取消网络请求
        OkGoManager.getInstance().cancelRequest(this);
    }


    @Override
    public void finish() {
        //关闭本页的输入法
        KeybordUtils.closeKeybord(this);
        super.finish();
        showActivityAnimation(false);
    }


    //    --------- 抽象方法  ---------


    /**
     * 获取view
     *
     * @return
     */
    protected abstract Object getContentView();


    /**
     * 初始化view
     */
    protected abstract void initView();

    /**
     * 获取数据
     */
    protected abstract void initData();


    /**
     * 初始化监听器
     */
    protected abstract void initListener();

    /**
     * 设置返回键图片
     *
     * @return
     */
    protected abstract int getDefHeadBackImgId();


    //    --------- 以下方法供子类使用  ---------

    /**
     * 设置activity跳转动画
     *
     * @param openEnterAnimation  A1启动A2，A2出现在屏幕上的动画
     * @param openExitAnimation   A1启动A2，A1从屏幕上消失的动画
     * @param closeEnterAnimation A2退回A1 A1出现在屏幕上的动画
     * @param closeExitAnimation  A2退回A1 A2从屏幕上消失的动画
     */
    protected void setActivityAnimation(int openEnterAnimation, int openExitAnimation,
                                        int closeEnterAnimation, int closeExitAnimation) {
        this.openEnterAnimation = openEnterAnimation;
        this.openExitAnimation = openExitAnimation;
        this.closeEnterAnimation = closeEnterAnimation;
        this.closeExitAnimation = closeExitAnimation;
    }


    /**
     * 删除ActionBar
     */
    protected boolean removeActionBar() {

        return true;
    }

    /**
     * 设置标题栏颜色
     *
     * @param colorId
     * @param backSrcId
     */
    protected void setHeadLayoutColor(int colorId, int backSrcId) {
        llHeadLayoutRoot.setBackgroundColor(ResourceUtil.getColor(colorId));
        ivHeadBack.setImageResource(backSrcId);
    }

    /**
     * 状态栏字体颜色 亮色或深色，默认深色
     */
    protected void setStatusBarDarkFont(boolean isrDark) {

        if (isrDark) {
            if (ImmersionBar.isSupportStatusBarDarkFont()) {
                imersionBar.statusBarDarkFont(true).init();
            } else {
                LogUtils.i("当前设备不支持状态栏字体变色");
            }
        } else {
            imersionBar.statusBarDarkFont(false).init();
        }

    }


    /**
     * 获取当前网络状态
     * 0:无网络
     * 1:网络断开或关闭
     * 2:以太网网络
     * 3:wifi网络
     * 4:移动数据连接
     */
    protected int getNetStatus() {
        return NetWorkUtils.getNetworkType();
    }


    /**
     * 是否设置activity跳转动画
     *
     * @return
     */
    protected boolean isShowActivityAnimation() {
        return true;
    }

    /**
     * 是否可以使用沉浸式
     *
     * @return
     */
    protected boolean isImmersionBarEnabled() {
        return true;
    }


    /**
     * 状态栏是否设置深色
     *
     * @return
     */
    protected boolean isStatusBarDarkFont() {
        return true;
    }


    /**
     * 是否注册EventBus
     *
     * @return
     */
    protected boolean isRegisterEventBus() {

        return false;
    }


    /**
     * 状态栏占位的颜色
     *
     * @return
     */
    protected int getStatusColorId() {
        return R.color.color_white;
    }

    /**
     * 返回本身实例
     *
     * @return
     */
    protected Activity getActivity() {
        return context;
    }

    /**
     * 是否使用侧滑返回
     *
     * @return
     */
    protected boolean isSwipeBack() {
        return false;
    }


    /**
     * 设置head的标题
     * 自动显示标题栏
     */
    protected void setHeaderTitle(String title) {

        if (!TextUtils.isEmpty(title)) {
            llHeadLayoutRoot.setVisibility(View.VISIBLE);
            headerTitle.setText(title);
        }

    }

    /**
     * 设置左上角返回图片
     *
     * @param bakImg
     */
    protected void setHeadBackImg(int bakImg) {


        ivHeadBack.setImageResource(bakImg);
    }

    /**
     * 显示标题栏右侧图片
     */
    protected void showHeaderRightImageview(int imaId) {
        ivheaderRight.setVisibility(View.VISIBLE);
        ivheaderRight.setImageResource(imaId);
    }

    /**
     * 显示标题栏右侧文字
     */
    protected void showHeaderRightTextview(String headTitle) {
        tvheaderRight.setVisibility(View.VISIBLE);
        tvheaderRight.setText(headTitle);

    }

    /**
     * 隐藏head底部的横线
     */
    protected void hideHeadLine() {
        headLine.setVisibility(View.GONE);
    }

    /**
     * 是否显示标题栏
     *
     * @param show
     */
    protected void showHeadLayout(boolean show) {
        llHeadLayoutRoot.setVisibility(show ? View.VISIBLE : View.GONE);

    }


    /**
     * 设置窗口透明度
     *
     * @param f
     */
    protected void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }


    /**
     * 是否有网络
     */
    protected boolean hasNet() {
        return NetWorkUtils.hasNetwork(this);
    }


    /**
     * 隐藏键盘
     */
    protected void hideKeyBoard() {
        KeybordUtils.closeKeybord(context);
    }


    /**
     * 显示正常数据页面
     */
    protected void showContentView() {
        statusLayout.showContentView();

    }


    /**
     * 显示错误数据页面
     */
    protected void showErrorView() {
        statusLayout.showErrorView();

    }


    /**
     * 显示空数据页面
     */
    protected void showEmptyView() {
        statusLayout.showEmptyView();

    }


    /**
     * 显示加载数据页面
     */
    protected void showLoadingView() {
        statusLayout.showLoadingView();

    }

    /**
     * 显示自定义数据页面
     */
    protected void showCustomView() {
        statusLayout.showCustomView();

    }


    //    --------- 以下是空方法 子类选择实现  ---------


    /**
     * 状态布局的点击
     *
     * @param view
     */
    @Override
    public void onStatusClick(View view) {

    }

    /**
     * 标题右边的字点击事件
     */
    protected void onTvRightClick() {
    }

    /**
     * 标题右边的图标点击事件
     */
    protected void onIvRightClick() {
    }

    /**
     * 标题栏关闭点击事件
     */
    protected void onTvCloseClick() {
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.iv_common_head_back) {
                //左上角返回按钮统一处理
                onBackClick();
            } else if (id == R.id.tv_common_head_title_right) {
                onTvRightClick();
            } else if (id == R.id.iv_common_head_title_right) {
                onIvRightClick();
            } else if (id == R.id.tv_common_head_close) {
                onTvCloseClick();
            }
        }
    };


}