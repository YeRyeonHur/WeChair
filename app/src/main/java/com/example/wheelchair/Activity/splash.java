package com.example.wheelchair.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wheelchair.R;

public class splash extends AppCompatActivity {
    ImageView splash_imv;
    TextView splash_tv;
    Animation anim_imv;
    Animation anim_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash_imv = (ImageView) findViewById(R.id.splash_imv);
        splash_tv = (TextView) findViewById(R.id.splash_tv);
        anim_imv= AnimationUtils.loadAnimation(this,R.anim.imv_anim);
        anim_tv= AnimationUtils.loadAnimation(this,R.anim.tv_anim);
        //splash_imv.setAnimation(anim_imv);
        splash_tv.setAnimation(anim_tv);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}