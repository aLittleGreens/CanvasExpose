package com.example.canvasexpose;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    private ExposeView exposeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        exposeView = new ExposeView(this);
        decorView.addView(exposeView);
    }

    public void reStart(View view) {
        exposeView.reStart();
    }
}
