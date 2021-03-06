package com.yeqiu.hydra.widget.keyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yeqiu.hydrautils.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ye
 * @date 2018/4/4
 * @desc GridView自定义键盘
 */
public class NumberKeyBoardView extends RelativeLayout {

    private List<String> numbers = new ArrayList<>();
    private Context context;
    private ImageView ivkeyboardBack;
    private GridView gvKeybord;
    private OnKeyboardListener onKeyboardListener;
    private Animation enterAnim;
    private Animation exitAnim;
    private KeyBoardAdapter keyBoardAdapter;
    private int backSrc = -1;
    private int delSrc = R.drawable.keyboard_del;
    private RelativeLayout backLayout;
    private boolean showPoint;


    public NumberKeyBoardView(Context context) {
        this(context, null);
    }

    public NumberKeyBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public NumberKeyBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        readAttribute(attrs);
        init();
    }

    private void init() {
        initView();
        initData();
        initlistener();
        initAnim();
    }

    private void readAttribute(AttributeSet attrs) {

        if (attrs == null) {
            return;
        }
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable
                .NumberKeyBoardView);

        backSrc = typedArray.getResourceId(R.styleable.NumberKeyBoardView_keyBoardBackSrc,
                -1);
        delSrc = typedArray.getResourceId(R.styleable.NumberKeyBoardView_keyBoardDelSrc,
                R.drawable.keyboard_del);
        showPoint = typedArray.getBoolean(R.styleable.NumberKeyBoardView_keyBoardShowPoint,
                false);

    }


    private void initlistener() {
        ivkeyboardBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
            }
        });


        gvKeybord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = numbers.get(position);
                if (TextUtils.equals("del", s)) {
                    //删除键
                    if (onKeyboardListener != null) {
                        onKeyboardListener.onDeleteKeyEvent();
                    }

                } else {
                    //普通键盘
                    if (onKeyboardListener != null) {
                        onKeyboardListener.onInsertKeyEvent(s);
                    }
                }

            }
        });

    }


    private void initAnim() {
        enterAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_in);
        exitAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_out);
    }


    private void initView() {
        View view = View.inflate(context, R.layout.layout_number_keyboard_view, null);
        ivkeyboardBack = (ImageView) view.findViewById(R.id.imgBack);
        backLayout = (RelativeLayout) view.findViewById(R.id.layoutBack);
        gvKeybord = (GridView) view.findViewById(R.id.gv_keybord);


        if (backSrc == -1) {
            backLayout.setVisibility(GONE);
        } else {
            backLayout.setVisibility(VISIBLE);
            ivkeyboardBack.setImageResource(backSrc);
        }

        addView(view);

    }


    private void initData() {
        for (int i = 1; i < 10; i++) {
            numbers.add(String.valueOf(i));
        }
        numbers.add(".");
        numbers.add("0");
        numbers.add("del");

        keyBoardAdapter = new KeyBoardAdapter(context, numbers);
        keyBoardAdapter.showPoit(showPoint);
        gvKeybord.setAdapter(keyBoardAdapter);
    }


    private class KeyBoardAdapter extends BaseAdapter {

        private Context context;
        private List<String> datas;
        private boolean isShowPoit = false;

        public KeyBoardAdapter(Context context, List<String> datas) {
            this.context = context;
            this.datas = datas;
        }

        public void showPoit(boolean isShowPoit) {

            this.isShowPoit = isShowPoit;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public String getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_keyboard, null);
                holder = new ViewHolder();
                holder.keyboardKey = (TextView) convertView.findViewById(R.id.tv_keyboard_key);
                holder.keyboardDelete = (RelativeLayout) convertView.findViewById(R.id
                        .img_keyboard_delete);
                holder.ivDel = (ImageView) convertView.findViewById(R.id
                        .iv_del);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String key = getItem(position);
            if (TextUtils.equals("del", key)) {
                //删除键
                holder.keyboardKey.setVisibility(GONE);
                holder.keyboardDelete.setVisibility(VISIBLE);
                holder.ivDel.setImageResource(delSrc);
            } else {
                //普通键盘

                if (TextUtils.equals(".", key)) {
                    //点 是否需要显示
                    if (isShowPoit) {
                        holder.keyboardKey.setVisibility(VISIBLE);
                    } else {
                        holder.keyboardKey.setVisibility(GONE);
                    }
                }

                holder.keyboardDelete.setVisibility(GONE);
                holder.keyboardKey.setText(key);


            }

            return convertView;
        }


        public final class ViewHolder {
            public TextView keyboardKey;
            public RelativeLayout keyboardDelete;
            public ImageView ivDel;
        }
    }


    /**
     * 设置键盘的监听事件。
     *
     * @param listener 监听事件
     */
    public void setOnKeyboardListener(OnKeyboardListener listener) {
        this.onKeyboardListener = listener;
    }


    /**
     * 打开键盘
     */
    public void closeKeyboard() {
        if (this.getVisibility() == VISIBLE) {
            //当前正在显示，可以关闭
            this.startAnimation(exitAnim);
            this.setVisibility(View.GONE);
        }

    }

    /**
     * 关闭键盘
     */
    public void openKeyboard() {
        if (this.getVisibility() == GONE || this.getVisibility() == INVISIBLE) {
            //当前没有显示，可以显示
            this.startAnimation(enterAnim);
            this.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 是否显示小数点
     */
    public void showPoit(boolean isShowPoit) {

        if (keyBoardAdapter != null) {
            keyBoardAdapter.showPoit(isShowPoit);
        }
    }


}
