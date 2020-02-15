package com.example.canvasexpose;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

/**
 * @author LittleGreens <a href="mailto:alittlegreens@foxmail.com">Contact me.</a>
 * @version 1.0
 * @since 2020-02-15 12:40
 */
public class ExposeView extends View {


    //旋转圆的画笔
    private Paint mPaint;
    //扩散圆的画笔
    private Paint mHolePaint;

    //属性动画
    private ValueAnimator mValueAnimator;
    //背景色
    private int mBackgroundColor = Color.WHITE;
    //    每个小圆的颜色
    private int[] mCircleColors;

    //表示旋转圆的中心坐标
    private float mCenterX;
    private float mCenterY;

    //表示斜对角线长度的一半
    private float mDistance;

    //6个小球的半径
    private float mCircleRadius = 18;
    //旋转大圆的半径
    private float mRotateRadius = 90;

    //当前大圆的旋转角度
    private float mCurrentRotateAngle = 0F;
    //当前大圆的半径
    private float mCurrentRotateRadius = mRotateRadius;
    //扩散圆的半径
    private float mCurrentHoleRadius = 0F;
    //表示旋转动画的时长
    private int mRotateDuration = 1000;
    private ExposeState mExposeState;


    public ExposeView(Context context) {
        this(context, null);
    }

    public ExposeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExposeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);

    }

    private void init(Context context) {

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHolePaint.setColor(mBackgroundColor);
        mHolePaint.setStyle(Paint.Style.STROKE);
        mCircleColors = context.getResources().getIntArray(R.array.expose_circle_colors);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        mDistance = (float) (Math.hypot(w, h) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mExposeState == null) {
            mExposeState = new RotateState();
        }
        mExposeState.drawState(canvas);
    }

    private abstract class ExposeState {
        abstract void drawState(Canvas canvas);
    }

    //1、小圆旋转 绘制6个小球，旋转
    private class RotateState extends ExposeState {

        public RotateState() {

            mValueAnimator = ValueAnimator.ofFloat(0, (float) (2 * Math.PI));
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.setRepeatCount(2);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedangle = (float) animation.getAnimatedValue();
                    mCurrentRotateAngle = animatedangle;
                    invalidate();
                }
            });

            mValueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mExposeState = new MerginState();
                }
            });
            mValueAnimator.start();

        }

        @Override
        void drawState(Canvas canvas) {
            drawBackground(canvas);
            drawCircles(canvas);
        }


    }

    private void drawCircles(Canvas canvas) {

        float rotateAngle = (float) (Math.PI * 2 / mCircleColors.length);
        for (int i = 0; i < mCircleColors.length; i++) {
            double angle = rotateAngle * i + mCurrentRotateAngle;
            float cx = (float) (mCenterX + Math.cos(angle) * mCurrentRotateRadius);
            float cy = (float) (mCenterY + Math.sin(angle) * mCurrentRotateRadius);
            mPaint.setColor(mCircleColors[i]);
            canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
        }


    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);
    }

    //扩散、聚合

    private class MerginState extends ExposeState {


        public MerginState() {
            mValueAnimator = ValueAnimator.ofFloat(mCircleRadius, mRotateRadius);
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new OvershootInterpolator(10f));
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentRotateRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mValueAnimator.reverse();//反向开启动画（先放大，再缩小）

            mValueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mExposeState = new Expose2State();
                    invalidate();
                }
            });
        }

        @Override
        void drawState(Canvas canvas) {

            drawBackground(canvas);
            drawCircles(canvas);
        }
    }

//    3 绘制空心圆


    private class Expose2State extends ExposeState {


        public Expose2State() {
            mValueAnimator = ValueAnimator.ofFloat(mCircleRadius, mDistance);
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentHoleRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mValueAnimator.start();
        }

        @Override
        void drawState(Canvas canvas) {


            if(mCurrentHoleRadius == 0){
                drawBackground(canvas);
            }else{
                // 绘制空心圆，改变线宽
                float strokeWidth = mDistance - mCurrentHoleRadius;
                mHolePaint.setStrokeWidth(strokeWidth);
                //假如线宽增加30时，会在圆内增加15，圆外增加15，所以，半径需要增加线宽的一半
                canvas.drawCircle(mCenterX,mCenterY,mCurrentHoleRadius + strokeWidth / 2,mHolePaint);
            }
        }
    }

    public void reStart(){

        if(mValueAnimator.isRunning()){
            return;
        }
         mCurrentRotateAngle = 0F;
        //当前大圆的半径
        mCurrentRotateRadius = mRotateRadius;
        //扩散圆的半径
         mCurrentHoleRadius = 0F;
        mExposeState = new RotateState();
        invalidate();

    }
}
