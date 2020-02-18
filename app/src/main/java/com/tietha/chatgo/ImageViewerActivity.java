package com.tietha.chatgo;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gw.swipeback.tools.WxSwipeBackActivityManager;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity{


    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WxSwipeBackActivityManager.getInstance().init(getApplication());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_image_viewer);


        imageView = findViewById(R.id.imageView);
        Intent i = getIntent();
        String url = i.getStringExtra("URL");
        Picasso.get().load(url).into(imageView);
    }

}