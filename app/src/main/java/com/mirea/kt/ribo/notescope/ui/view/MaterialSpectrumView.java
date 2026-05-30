package com.mirea.kt.ribo.notescope.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.mirea.kt.ribo.notescope.R;

import java.util.Locale;

public class MaterialSpectrumView extends View {

    private float[] audioData;

    private Paint spectrumPaint;
    private Paint gridPaint;
    private Paint axisPaint;
    private Paint textPaint;
    private Paint cardPaint;
    private Paint borderPaint;

    private final RectF cardRect = new RectF();

    private float displayCenter = -25f;
    private float displayRange = 40f;

    private float sampleRate;
    private int frameSize;

    private float padding;
    private float cornerRadius;
    private float chartPaddingTop;
    private float chartPaddingBottom;
    private float chartPaddingStart;
    private float chartPaddingEnd;

    private int spectrumColor;
    private int gridColor;
    private int axisColor;
    private int textColor;
    private int cardColor;
    private int borderColor;

    private float spectrumStrokeWidth;
    private float gridStrokeWidth;
    private float axisStrokeWidth;
    private float labelTextSize;

    private boolean showGrid = true;
    private boolean showXAxisLabels = true;
    private boolean showCenterLine = true;

    public MaterialSpectrumView(Context context) {
        super(context);
        init(null);
    }

    public MaterialSpectrumView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialSpectrumView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    private void init(AttributeSet attrs) {

        spectrumColor = Color.parseColor("#4FC3F7");
        gridColor = Color.parseColor("#33FFFFFF");
        axisColor = Color.parseColor("#66FFFFFF");
        textColor = Color.parseColor("#DEFFFFFF");
        cardColor = Color.parseColor("#0B1220");

        sampleRate = 44100f;
        frameSize = 4096;

        spectrumStrokeWidth = dp(1.4f);
        gridStrokeWidth = dp(1f);
        axisStrokeWidth = dp(1.2f);

        labelTextSize = sp(12);

        cornerRadius = dp(24);
        padding = dp(16);

        chartPaddingTop = dp(24);
        chartPaddingBottom = dp(36);
        chartPaddingStart = dp(52);
        chartPaddingEnd = dp(20);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSpectrumView);

            spectrumColor = a.getColor(R.styleable.MaterialSpectrumView_spectrumColor, spectrumColor);
            gridColor = a.getColor(R.styleable.MaterialSpectrumView_spectrumGridColor, gridColor);
            axisColor = a.getColor(R.styleable.MaterialSpectrumView_spectrumAxisColor, axisColor);
            textColor = a.getColor(R.styleable.MaterialSpectrumView_spectrumTextColor, textColor);
            cardColor = a.getColor(R.styleable.MaterialSpectrumView_spectrumCardColor, cardColor);
            borderColor = a.getColor(R.styleable.MaterialSpectrumView_spectrumBorderColor, borderColor);

            spectrumStrokeWidth = a.getDimension(R.styleable.MaterialSpectrumView_spectrumStrokeWidth, spectrumStrokeWidth);
            labelTextSize = a.getDimension(R.styleable.MaterialSpectrumView_spectrumLabelTextSize, labelTextSize);
            cornerRadius = a.getDimension(R.styleable.MaterialSpectrumView_spectrumCornerRadius, cornerRadius);

            sampleRate = a.getFloat(R.styleable.MaterialSpectrumView_spectrumSampleRate, sampleRate);
            frameSize = a.getInt(R.styleable.MaterialSpectrumView_spectrumFrameSize, frameSize);

            showGrid = a.getBoolean(R.styleable.MaterialSpectrumView_spectrumShowGrid, true);
            showXAxisLabels = a.getBoolean(R.styleable.MaterialSpectrumView_spectrumShowXAxisLabels, true);
            showCenterLine = a.getBoolean(R.styleable.MaterialSpectrumView_spectrumShowCenterLine, true);

            a.recycle();
        }

        initPaints();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void initPaints() {

        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(cardColor);
        cardPaint.setStyle(Paint.Style.FILL);
        cardPaint.setShadowLayer(dp(8), 0, dp(2), Color.argb(90, 0, 0, 0));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        spectrumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        spectrumPaint.setStyle(Paint.Style.STROKE);
        spectrumPaint.setStrokeWidth(spectrumStrokeWidth);
        spectrumPaint.setStrokeCap(Paint.Cap.ROUND);
        spectrumPaint.setStrokeJoin(Paint.Join.ROUND);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(gridStrokeWidth);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(axisColor);
        axisPaint.setStrokeWidth(axisStrokeWidth);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(labelTextSize);
    }

    public void setSpectrum(float[] data) {
        this.audioData = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);

        ChartBounds b = calculateChartBounds();

        drawGrid(canvas, b);
        drawAxes(canvas, b);
        drawLabels(canvas, b);

        if (audioData != null && audioData.length > 0) {
            updateAnimation();
            drawSpectrum(canvas, b);
        }
    }

    private void drawBackground(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        cardRect.set(padding, padding, w - padding, h - padding);
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint);
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint);
    }

    private static class ChartBounds {
        float left, top, right, bottom;
        float width, height;
    }

    private ChartBounds calculateChartBounds() {
        var b = new ChartBounds();

        b.left = cardRect.left + chartPaddingStart;
        b.top = cardRect.top + chartPaddingTop;
        b.right = cardRect.right - chartPaddingEnd;
        b.bottom = cardRect.bottom - chartPaddingBottom;

        b.width = b.right - b.left;
        b.height = b.bottom - b.top;

        return b;
    }

    private void drawGrid(Canvas canvas, ChartBounds b) {
        if (!showGrid) return;

        float[] yGrid = getYGridValues();

        for (float y : yGrid) {
            float py = mapValueToY(y, b);
            canvas.drawLine(b.left, py, b.right, py, gridPaint);
        }

        float[] freqs = {10, 100, 1000, 10000, 20000};

        for (float freq : freqs) {
            float x = frequencyToX(freq, b);
            canvas.drawLine(x, b.top, x, b.bottom, gridPaint);
        }
    }

    private float[] getYGridValues() {
        float min = displayCenter - displayRange;
        float max = displayCenter + displayRange;

        return new float[]{
                max,
                displayCenter + displayRange / 2f,
                displayCenter,
                displayCenter - displayRange / 2f,
                min
        };
    }

    private void drawAxes(Canvas canvas, ChartBounds b) {
        if (!showCenterLine) return;

        float y = mapValueToY(displayCenter, b);
        canvas.drawLine(b.left, y, b.right, y, axisPaint);
    }

    private void drawLabels(Canvas canvas, ChartBounds b) {

        float[] vals = getYGridValues();

        float x = b.left - dp(44);

        for (float v : vals) {
            float y = mapValueToY(v, b);
            canvas.drawText(
                    String.format(Locale.getDefault(), "%.1f", v),
                    x,
                    y + dp(4),
                    textPaint
            );
        }

        if (showXAxisLabels) {
            String[] labels = {"10", "100", "1k", "10k", "20k"};
            float[] freqs = {10, 100, 1000, 10000, 20000};

            for (int i = 0; i < labels.length; i++) {
                x = frequencyToX(freqs[i], b);

                canvas.drawText(
                        labels[i],
                        x - textPaint.measureText(labels[i]) / 2f,
                        b.bottom + dp(24),
                        textPaint
                );
            }
        }
    }

    private void drawSpectrum(Canvas canvas, ChartBounds b) {

        LinearGradient gradient = new LinearGradient(
                b.left, b.top,
                b.right, b.bottom,
                new int[]{spectrumColor, Color.WHITE},
                null,
                Shader.TileMode.CLAMP
        );

        spectrumPaint.setShader(gradient);

        Path path = new Path();

        boolean first = true;

        for (int i = 0; i < audioData.length; i++) {

            float v = audioData[i];
            if (Float.isNaN(v) || Float.isInfinite(v)) continue;

            float x = b.left + ((float) i / (audioData.length - 1)) * b.width;
            float y = mapValueToY(v, b);

            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }

        canvas.drawPath(path, spectrumPaint);
    }

    private void updateAnimation() {

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for (float v : audioData) {
            if (Float.isNaN(v) || Float.isInfinite(v)) continue;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }

        if (min == Float.MAX_VALUE) return;

        float targetCenter = (max + min) * 0.5f;
        float targetRange = (max - min) * 0.5f;

        targetRange = Math.max(targetRange, 10f) * 1.15f;

        displayCenter += (targetCenter - displayCenter) * 0.08f;

        float attack = 0.5f;
        float release = 0.02f;

        if (targetRange > displayRange) {
            displayRange += (targetRange - displayRange) * attack;
        } else {
            displayRange += (targetRange - displayRange) * release;
        }
    }

    private float mapValueToY(float value, ChartBounds b) {
        float min = displayCenter - displayRange;
        float max = displayCenter + displayRange;

        value = Math.max(min, Math.min(max, value));
        float n = (value - min) / (max - min);

        return b.top + b.height * (1f - n);
    }

    private float frequencyToX(float freq, ChartBounds b) {
        float minFreq = 10f;
        float maxFreq = 20000f;

        float logMin = (float) Math.log10(minFreq);
        float logMax = (float) Math.log10(maxFreq);

        float fraction =
                ((float) Math.log10(freq) - logMin) /
                        (logMax - logMin);

        return b.left + fraction * b.width;
    }

    private float dp(float v) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                v,
                getResources().getDisplayMetrics()
        );
    }

    private float sp(float v) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                v,
                getResources().getDisplayMetrics()
        );
    }
}