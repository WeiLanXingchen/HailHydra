package com.yeqiu.androidlibrary.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yeqiu.androiddome.R;
import com.yeqiu.androidlibrary.utils.StatusBarUtils;

/**
 * @project：AndroidLbrary
 * @author：小卷子
 * @date 2018/7/9
 * @describe：
 * @fix：
 */
public class ImmersionImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immersion_image);

        StatusBarUtils.with(this)
                .init();

    }

}