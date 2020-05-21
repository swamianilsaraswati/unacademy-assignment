package com.example.assignment;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("FieldCanBeLocal")
public class CircularProgressIndicator extends View {
    private static final int ANGLE_START_PROGRESS_BACKGROUND = 0;
    private static final int ANGLE_END_PROGRESS_BACKGROUND = 360;
    private static final String DEFAULT_PROGRESS_COLOR = "#3F51B5";
    private static final int DEFAULT_STROKE_WIDTH_DP = 8;
    private static final String DEFAULT_PROGRESS_BACKGROUND_COLOR = "#e0e0e0";
    private static final int DEFAULT_ANIMATION_DURATION = 1_000;
    private static final String PROPERTY_ANGLE = "angle";
    private Paint progressPaint;
    private Paint progressBackgroundPaint;
    private Paint dotPaint;
    private int startAngle = 270;
    private int sweepAngle = 0;
    private RectF circleBounds;
    private float radius;
    private boolean shouldDrawDot;
    private double maxProgressValue = 100.0;
    private double progressValue = 0.0;
    private ValueAnimator progressAnimator;
    @NonNull
    private Interpolator animationInterpolator = new AccelerateDecelerateInterpolator();

    public CircularProgressIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public CircularProgressIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgressIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularProgressIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {

        int progressColor = Color.parseColor(DEFAULT_PROGRESS_COLOR);
        int progressBackgroundColor = Color.parseColor(DEFAULT_PROGRESS_BACKGROUND_COLOR);
        int progressStrokeWidth = dp2px(DEFAULT_STROKE_WIDTH_DP);
        int progressBackgroundStrokeWidth = progressStrokeWidth;

        int dotColor = progressColor;
        int dotWidth = progressStrokeWidth;

        progressPaint = new Paint();
        progressPaint.setStrokeWidth(progressStrokeWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);

        progressBackgroundPaint = new Paint();
        progressBackgroundPaint.setStyle(Paint.Style.STROKE);
        progressBackgroundPaint.setStrokeWidth(progressBackgroundStrokeWidth);
        progressBackgroundPaint.setColor(progressBackgroundColor);
        progressBackgroundPaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setStrokeCap(Paint.Cap.ROUND);
        dotPaint.setStrokeWidth(dotWidth);
        dotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        dotPaint.setColor(dotColor);
        dotPaint.setAntiAlias(true);

        circleBounds = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        calculateBounds(w, h);
    }

    private void calculateBounds(int w, int h) {
        radius = w / 2f;

        float dotWidth = dotPaint.getStrokeWidth();
        float progressWidth = progressPaint.getStrokeWidth();
        float progressBackgroundWidth = progressBackgroundPaint.getStrokeWidth();
        float strokeSizeOffset = (shouldDrawDot) ? Math.max(dotWidth, Math.max(progressWidth, progressBackgroundWidth)) : Math.max(progressWidth, progressBackgroundWidth); // to prevent progress or dot from drawing over the bounds
        float halfOffset = strokeSizeOffset / 2f;

        circleBounds.left = halfOffset;
        circleBounds.top = halfOffset;
        circleBounds.right = w - halfOffset;
        circleBounds.bottom = h - halfOffset;

        radius = circleBounds.width() / 2f;

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawProgressBackground(canvas);
        drawProgress(canvas);
        drawDot(canvas);
    }

    private void drawProgressBackground(Canvas canvas) {
        canvas.drawArc(circleBounds, ANGLE_START_PROGRESS_BACKGROUND, ANGLE_END_PROGRESS_BACKGROUND,
                false, progressBackgroundPaint);
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawArc(circleBounds, startAngle, sweepAngle, false, progressPaint);
    }

    private void drawDot(Canvas canvas) {
        double angleRadians = Math.toRadians(startAngle + sweepAngle + 180);
        float cos = (float) Math.cos(angleRadians);
        float sin = (float) Math.sin(angleRadians);
        float x = circleBounds.centerX() - radius * cos;
        float y = circleBounds.centerY() - radius * sin;

        canvas.drawPoint(x, y, dotPaint);
    }

    public void setMaxProgress(double maxProgress) {
        maxProgressValue = maxProgress;
        if (maxProgressValue < progressValue) {
            setCurrentProgress(maxProgress);
        }
        invalidate();
    }

    public void setCurrentProgress(double currentProgress) {
        if (currentProgress > maxProgressValue) {
            maxProgressValue = currentProgress;
        }

        setProgress(currentProgress, maxProgressValue);
    }

    public void setProgress(double current, double max) {
        final double finalAngle;

        finalAngle = current / max * 360;

        double oldCurrentProgress = progressValue;

        maxProgressValue = max;
        progressValue = Math.min(current, max);

        stopProgressAnimation();

        startProgressAnimation(oldCurrentProgress, finalAngle);
    }

    private void startProgressAnimation(double oldCurrentProgress, final double finalAngle) {
        final PropertyValuesHolder angleProperty = PropertyValuesHolder.ofInt(PROPERTY_ANGLE, sweepAngle, (int) finalAngle);

        progressAnimator = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return (startValue + (endValue - startValue) * fraction);
            }
        }, oldCurrentProgress, progressValue);
        progressAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
        progressAnimator.setValues(angleProperty);
        progressAnimator.setInterpolator(animationInterpolator);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sweepAngle = (int) animation.getAnimatedValue(PROPERTY_ANGLE);
                invalidate();
            }
        });
        progressAnimator.addListener(new DefaultAnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
                sweepAngle = (int) finalAngle;
                invalidate();
                progressAnimator = null;
            }
        });
        progressAnimator.start();
    }

    private void stopProgressAnimation() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }

    private int dp2px(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    private int sp2px(float sp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    private void invalidateEverything() {
        calculateBounds(getWidth(), getHeight());
        requestLayout();
        invalidate();
    }

    public void setProgressColor(@ColorInt int color) {
        progressPaint.setColor(color);
        invalidate();
    }

    public void setProgressBackgroundColor(@ColorInt int color) {
        progressBackgroundPaint.setColor(color);
        invalidate();
    }

    public void setProgressStrokeWidthDp(@Dimension int strokeWidth) {
        setProgressStrokeWidthPx(dp2px(strokeWidth));
    }

    public void setProgressStrokeWidthPx(@Dimension int strokeWidth) {
        progressPaint.setStrokeWidth(strokeWidth);

        invalidateEverything();
    }

    public void setProgressBackgroundStrokeWidthDp(@Dimension int strokeWidth) {
        setProgressBackgroundStrokeWidthPx(dp2px(strokeWidth));
    }

    public void setProgressBackgroundStrokeWidthPx(@Dimension int strokeWidth) {
        progressBackgroundPaint.setStrokeWidth(strokeWidth);

        invalidateEverything();
    }


    public void setShouldDrawDot(boolean shouldDrawDot) {
        this.shouldDrawDot = shouldDrawDot;

        if (dotPaint.getStrokeWidth() > progressPaint.getStrokeWidth()) {
            requestLayout();
            return;
        }

        invalidate();
    }

    public void setDotColor(@ColorInt int color) {
        dotPaint.setColor(color);

        invalidate();
    }

    public void setDotWidthDp(@Dimension int width) {
        setDotWidthPx(dp2px(width));
    }

    public void setDotWidthPx(@Dimension int width) {
        dotPaint.setStrokeWidth(width);

        invalidateEverything();
    }

    @ColorInt
    public int getProgressColor() {
        return progressPaint.getColor();
    }

    @ColorInt
    public int getProgressBackgroundColor() {
        return progressBackgroundPaint.getColor();
    }

    @ColorInt
    public int getDotColor() {
        return dotPaint.getColor();
    }

    public float getDotWidth() {
        return dotPaint.getStrokeWidth();
    }


    public double getProgress() {
        return progressValue;
    }

    public double getMaxProgress() {
        return maxProgressValue;
    }
}