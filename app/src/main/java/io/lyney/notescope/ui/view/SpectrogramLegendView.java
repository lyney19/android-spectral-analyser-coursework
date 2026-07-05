package io.lyney.notescope.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import io.lyney.notescope.R;

public class SpectrogramLegendView extends View {

    private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();

    private String title;
    private String minLabel;
    private String maxLabel;

    private float cornerRadius;
    private float barHeight;

    private int textColor;
    private int titleColor;

    private int borderColor;
    private float borderWidth;

    private int[] gradientColors = {
            Color.parseColor("#1A237E"),
            Color.parseColor("#283593"),
            Color.parseColor("#3949AB"),
            Color.parseColor("#1E88E5"),
            Color.parseColor("#43A047"),
            Color.parseColor("#FDD835"),
            Color.parseColor("#FB8C00"),
            Color.parseColor("#E53935")
    };

    public SpectrogramLegendView(Context context) {
        super(context);
        init(null);
    }

    public SpectrogramLegendView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SpectrogramLegendView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        title = "Signal Intensity";
        minLabel = "-120 dB";
        maxLabel = "0 dB";

        cornerRadius = dp(12);
        barHeight = dp(18);

        textColor = Color.parseColor("#B0BEC5");
        titleColor = Color.WHITE;

        borderColor = Color.parseColor("#80FFFFFF");
        borderWidth = dp(1);

        if (attrs != null) {

            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpectrogramLegendView);

            title = a.getString(R.styleable.SpectrogramLegendView_legendTitle) != null
                    ? a.getString(R.styleable.SpectrogramLegendView_legendTitle)
                    : title;

            minLabel = a.getString(R.styleable.SpectrogramLegendView_minLabel) != null
                    ? a.getString(R.styleable.SpectrogramLegendView_minLabel)
                    : minLabel;

            maxLabel = a.getString(R.styleable.SpectrogramLegendView_maxLabel) != null
                    ? a.getString(R.styleable.SpectrogramLegendView_maxLabel)
                    : maxLabel;

            cornerRadius = a.getDimension(R.styleable.SpectrogramLegendView_legendCornerRadius, cornerRadius);
            barHeight = a.getDimension(R.styleable.SpectrogramLegendView_legendBarHeight, barHeight);
            borderWidth = a.getDimension(R.styleable.SpectrogramLegendView_legendBorderWidth, borderWidth);

            textColor = a.getColor(R.styleable.SpectrogramLegendView_legendTextColor, textColor);

            titleColor = a.getColor(R.styleable.SpectrogramLegendView_legendTitleColor, titleColor);
            borderColor = a.getColor(R.styleable.SpectrogramLegendView_legendBorderColor, borderColor);

            a.recycle();
        }

        initPaints();
    }

    private void initPaints() {
        labelPaint.setColor(textColor);
        labelPaint.setTextSize(sp(12));

        titlePaint.setColor(titleColor);
        titlePaint.setTextSize(sp(14));
        titlePaint.setFakeBoldText(true);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float left = getPaddingLeft();
        float right = w - getPaddingRight();

        Paint.FontMetrics titleMetrics = titlePaint.getFontMetrics();

        float top = getPaddingTop() + (titleMetrics.bottom - titleMetrics.top) + dp(8);

        barRect.set(left, top, right, top + barHeight);

        LinearGradient gradient = new LinearGradient(
                barRect.left,
                0,
                barRect.right,
                0,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
        );

        gradientPaint.setShader(gradient);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Paint.FontMetrics titleMetrics = titlePaint.getFontMetrics();
        Paint.FontMetrics labelMetrics = labelPaint.getFontMetrics();

        float titleHeight = titleMetrics.bottom - titleMetrics.top;
        float labelHeight = labelMetrics.bottom - labelMetrics.top;

        int desiredHeight = (int) (
                getPaddingTop()
                        + titleHeight
                        + dp(8)
                        + barHeight
                        + dp(12)
                        + labelHeight
                        + getPaddingBottom()
        );

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint.FontMetrics titleMetrics = titlePaint.getFontMetrics();

        float titleBaseline = getPaddingTop() - titleMetrics.top;
        float titleWidth = titlePaint.measureText(title);

        canvas.drawText(title, (getWidth() - titleWidth) / 2f, titleBaseline, titlePaint);
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, gradientPaint);
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, borderPaint);

        Paint.FontMetrics labelMetrics = labelPaint.getFontMetrics();

        float labelsBaseline = barRect.bottom + dp(12) - labelMetrics.ascent;
        canvas.drawText(minLabel, barRect.left, labelsBaseline, labelPaint);

        float maxWidth = labelPaint.measureText(maxLabel);
        canvas.drawText(maxLabel, barRect.right - maxWidth, labelsBaseline, labelPaint);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private float sp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}