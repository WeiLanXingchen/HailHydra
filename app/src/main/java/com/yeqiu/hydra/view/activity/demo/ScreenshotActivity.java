package com.yeqiu.hydra.view.activity.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.yeqiu.hydra.R;
import com.yeqiu.hydra.constant.Url;
import com.yeqiu.hydra.utils.JumpUtils;
import com.yeqiu.hydra.utils.ScreenUtils;
import com.yeqiu.hydra.view.activity.BaseActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @project：HailHydra
 * @author：小卷子
 * @date 2019/7/17
 * @describe：
 * @fix：
 */
public class ScreenshotActivity extends BaseActivity {

    TextView tvScreenshotWebView;
    TextView tvScreenshotScreen;
    TextView tvScreenshotView;
    TextView tvScreenshotList;
    RecyclerView rvScreenshot;

    @Override
    protected Object getContentView() {
        return R.layout.activity_screenshot;
    }

    @Override
    protected void initView() {
        setHeaderTitle("截图");
        tvScreenshotWebView = (TextView) findViewById(R.id.tv_screenshot_web_view);
        tvScreenshotScreen = (TextView) findViewById(R.id.tv_screenshot_screen);
        tvScreenshotView = (TextView) findViewById(R.id.tv_screenshot_view);
        tvScreenshotList = (TextView) findViewById(R.id.tv_screenshot_list);
        rvScreenshot = (RecyclerView) findViewById(R.id.rv_screenshot);
        rvScreenshot.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    protected void initData() {


        List<String> datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("测试数据 " + i);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(R.layout.item_sticky, datas);
        rvScreenshot.setAdapter(simpleAdapter);


    }

    @Override
    protected void initListener() {
        tvScreenshotWebView.setOnClickListener(this);
        tvScreenshotScreen.setOnClickListener(this);
        tvScreenshotView.setOnClickListener(this);
        tvScreenshotList.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_screenshot_web_view:
                webScreenshot();
                break;
            case R.id.tv_screenshot_screen:
                screenshot();
                break;
            case R.id.tv_screenshot_view:
                viewScreenshot();
                break;
            case R.id.tv_screenshot_list:
                listScreenshot();
                break;

            default:
                break;
        }
    }

    private void webScreenshot() {

        Intent intent = new Intent(getContext(), ScreenshotWebActivity.class);
        intent.putExtra("url", Url.HailHydraUrl);
        JumpUtils.jumpToActivityByIntent(intent);
    }

    private void screenshot() {

        Bitmap screenShotWithoutStatusBar = ScreenUtils.getScreenShotWithoutStatusBar(getContext());
        Intent intenWithBitMp = getIntenWithBitMp(screenShotWithoutStatusBar);
        JumpUtils.jumpToActivityByIntent(intenWithBitMp);

    }

    private void viewScreenshot() {

        Bitmap bitmap = screenShotView(tvScreenshotView);
        Intent intenWithBitMp = getIntenWithBitMp(bitmap);
        JumpUtils.jumpToActivityByIntent(intenWithBitMp);
    }

    /**
     * 获取View在屏幕可见区域的截图 在可视区域外的部分无法截取到
     */
    private Bitmap screenShotView(View view) {
        //开启缓存功能
        view.setDrawingCacheEnabled(true);
        //创建缓存
        view.buildDrawingCache();
        //获取缓存Bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        return bitmap;
    }


    private void listScreenshot() {


        Bitmap bitmap = shotRecyclerView(rvScreenshot);
        Intent intenWithBitMp = getIntenWithBitMp(bitmap);
        JumpUtils.jumpToActivityByIntent(intenWithBitMp);

    }


    public static Bitmap shotRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(),
                        holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            Drawable lBackground = view.getBackground();
            if (lBackground instanceof ColorDrawable) {
                ColorDrawable lColorDrawable = (ColorDrawable) lBackground;
                int lColor = lColorDrawable.getColor();
                bigCanvas.drawColor(lColor);
            }

            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }
        }
        return bigBitmap;
    }




    /**
     * 将bitmap转成数组传递到activity，项目中尽量不要使用intent传递bitmap
     *
     * @param bitmap
     * @return
     */
    private Intent getIntenWithBitMp(Bitmap bitmap) {
        Intent intent = new Intent(this, BigPicActivity.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bitmapByte = baos.toByteArray();
        intent.putExtra("bitmap", bitmapByte);

        return intent;

    }


    class SimpleAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public SimpleAdapter(int layoutResId, @Nullable List<String> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {

            helper.setText(R.id.tv_item_sticky, item);
        }
    }

}