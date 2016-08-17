package com.nexusunsky.liuhao.reformtheactivity.commonlist;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import com.nexusunsky.liuhao.reformtheactivity.R;

import java.util.ArrayList;
import java.util.List;

public class LoadingIndicatorView extends View {
    //indicators
    public static final int BallPulse = 0;
    public static final int BallSpinFadeLoader = 22;

    @IntDef(
            flag = true,
            value = {
                    BallPulse,
                    BallSpinFadeLoader
            })
    public @interface Indicator {
    }

    //Sizes (with defaults in DP)
    public static final int DEFAULT_SIZE = 30;

    //attrs
    int mIndicatorId;
    int mIndicatorColor;

    Paint mPaint;

    RecyclerLoaderAnimator mIndicatorController;

    private boolean mHasAnimation;


    public LoadingIndicatorView(Context context) {
        super(context);
        init(null, 0);
    }

    public LoadingIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LoadingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable
                .LoadingIndicatorView);
        mIndicatorId = a.getInt(R.styleable.LoadingIndicatorView_indicator, BallSpinFadeLoader);
        mIndicatorColor = a.getColor(R.styleable.LoadingIndicatorView_indicator_color, Color
                .WHITE);
        a.recycle();
        mPaint = new Paint();
        mPaint.setColor(mIndicatorColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        applyIndicator();
    }

    public void setIndicatorId(int indicatorId) {
        mIndicatorId = indicatorId;
        applyIndicator();
    }

    public void setIndicatorColor(int color) {
        mIndicatorColor = color;
        mPaint.setColor(mIndicatorColor);
        this.invalidate();
    }

    private void applyIndicator() {
        switch (mIndicatorId) {
            case BallSpinFadeLoader:
                mIndicatorController = new RecyclerLoaderAnimator();
                break;
        }
        mIndicatorController.setTarget(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureDimension(dp2px(DEFAULT_SIZE), widthMeasureSpec);
        int height = measureDimension(dp2px(DEFAULT_SIZE), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureDimension(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicator(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!mHasAnimation) {
            mHasAnimation = true;
            applyAnimation();
        }
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
            if (v == GONE || v == INVISIBLE) {
                mIndicatorController.setAnimationStatus(RecyclerLoaderAnimator.AnimStatus.END);
            } else {
                mIndicatorController.setAnimationStatus(RecyclerLoaderAnimator.AnimStatus.START);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIndicatorController.setAnimationStatus(RecyclerLoaderAnimator.AnimStatus.CANCEL);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIndicatorController.setAnimationStatus(RecyclerLoaderAnimator.AnimStatus.START);
    }

    void drawIndicator(Canvas canvas) {
        mIndicatorController.draw(canvas, mPaint);
    }

    void applyAnimation() {
        mIndicatorController.initAnimation();
    }

    private int dp2px(int dpValue) {
        return (int) getContext().getResources().getDisplayMetrics().density * dpValue;
    }

    public static class RecyclerLoaderAnimator {

        public static final float SCALE = 1.0f;
        public static final int ALPHA = 255;

        float[] scaleFloats = new float[]{
                SCALE,
                SCALE,
                SCALE,
                SCALE,
                SCALE,
                SCALE,
                SCALE,
                SCALE};

        int[] alphas = new int[]{
                ALPHA,
                ALPHA,
                ALPHA,
                ALPHA,
                ALPHA,
                ALPHA,
                ALPHA,
                ALPHA};

        public interface PROGRESSSTYLE {
            int SysProgress = -1;
            int BallSpinFadeLoader = 22;
        }

        private View mTarget;

        private List<Animator> mAnimators;

        public void setTarget(View target) {
            this.mTarget = target;
        }

        public View getTarget() {
            return mTarget;
        }

        public int getWidth() {
            return mTarget.getWidth();
        }

        public int getHeight() {
            return mTarget.getHeight();
        }

        public void postInvalidate() {
            mTarget.postInvalidate();
        }

        public void draw(Canvas canvas, Paint paint) {
            float radius = getWidth() / 10;
            for (int i = 0; i < 8; i++) {
                canvas.save();
                Point point = circleAt(getWidth(), getHeight(), getWidth() / 2 - radius, i *
                        (Math.PI
                                / 4));
                canvas.translate(point.x, point.y);
                canvas.scale(scaleFloats[i], scaleFloats[i]);
                paint.setAlpha(alphas[i]);
                canvas.drawCircle(0, 0, radius, paint);
                canvas.restore();
            }
        }

        /**
         * 圆O的圆心为(a,b),半径为R,点A与到X轴的为角α.
         * 则点A的坐标为(a+R*cosα,b+R*sinα)
         *
         * @param width
         * @param height
         * @param radius
         * @param angle
         * @return
         */
        Point circleAt(int width, int height, float radius, double angle) {
            float x = (float) (width / 2 + radius * (Math.cos(angle)));
            float y = (float) (height / 2 + radius * (Math.sin(angle)));
            return new Point(x, y);
        }

        public List<Animator> createAnimation() {
            List<Animator> animators = new ArrayList<>();
            int[] delays = {0, 120, 240, 360, 480, 600, 720, 780, 840};
            for (int i = 0; i < 8; i++) {
                final int index = i;
                ValueAnimator scaleAnim = ValueAnimator.ofFloat(1, 0.4f, 1);
                scaleAnim.setDuration(1000);
                scaleAnim.setRepeatCount(-1);
                scaleAnim.setStartDelay(delays[i]);
                scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        scaleFloats[index] = (float) animation.getAnimatedValue();
                        postInvalidate();
                    }
                });
                scaleAnim.start();

                ValueAnimator alphaAnim = ValueAnimator.ofInt(255, 77, 255);
                alphaAnim.setDuration(1000);
                alphaAnim.setRepeatCount(-1);
                alphaAnim.setStartDelay(delays[i]);
                alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        alphas[index] = (int) animation.getAnimatedValue();
                        postInvalidate();
                    }
                });
                alphaAnim.start();
                animators.add(scaleAnim);
                animators.add(alphaAnim);
            }
            return animators;
        }

        final class Point {
            public float x;
            public float y;

            public Point(float x, float y) {
                this.x = x;
                this.y = y;
            }
        }

        public void initAnimation() {
            mAnimators = createAnimation();
        }

        /**
         * make animation to start or end when target
         * view was be Visible or Gone or Invisible.
         * make animation to cancel when target view
         * be onDetachedFromWindow.
         *
         * @param animStatus
         */
        public void setAnimationStatus(AnimStatus animStatus) {
            if (mAnimators == null) {
                return;
            }
            int count = mAnimators.size();
            for (int i = 0; i < count; i++) {
                Animator animator = mAnimators.get(i);
                boolean isRunning = animator.isRunning();
                switch (animStatus) {
                    case START:
                        if (!isRunning) {
                            animator.start();
                        }
                        break;
                    case END:
                        if (isRunning) {
                            animator.end();
                        }
                        break;
                    case CANCEL:
                        if (isRunning) {
                            animator.cancel();
                        }
                        break;
                }
            }
        }

        public enum AnimStatus {
            START, END, CANCEL
        }
    }

}
