package com.example.navi_blind.utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.navi_blind.R;

/**
 * Created by 杨利娟 on 2017/3/27.
 */
public class TitleLayout extends LinearLayout {
    public TitleLayout(Context context, AttributeSet attrs){
        super(context,attrs);
        //通过LayoutInflater的from()方法可以构建一个LayoutInflater对象，然后调用inflate()方法动态加载一个布局文件，
        // inflate()方法接收两个参数，第一个是要加载的布局文件id(R.layout.title_layout),第二个是给加载好的布局再添加一个父布局(TitleLayout即this)
        LayoutInflater.from(context).inflate(R.layout.title_layout,this);
        ImageView imgback = (ImageView)findViewById(R.id.image_back);
        imgback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).finish();
            }
        });

    }
}
