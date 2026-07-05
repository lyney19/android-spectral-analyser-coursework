package io.lyney.notescope.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.lyney.notescope.R;

import java.util.Arrays;
import java.util.Locale;

public class SpectrogramView extends View {
    private static final int MAX_FREQUENCY = 22050;

    private int visualBins = 256;

    private float minDb = -100f;
    private float maxDb = -20f;
    private boolean interactionEnabled = false;

    private float scaleYFactor = 1f;

    private float minScaleY = 1f;
    private float maxScaleY = 20f;

    private float translateY = 0f;

    private boolean dynamicFrequencyAxis = true;

    private int leftPaddingPx;
    private int rightPaddingPx;
    private int topPaddingPx;
    private int bottomPaddingPx;

    private int backgroundColor;
    private int axisColor;
    private int textColor;
    private int gridColor;

    private float axisStrokeWidth;
    private float gridStrokeWidth;
    private float textSize;

    private boolean showGrid = true;
    private boolean showTimeAxis = true;
    private boolean showFrequencyAxis = true;

    private int buttonBackgroundColor;
    private int buttonIconTint;

    private float buttonCornerRadius;

    private int buttonSizePx;
    private int buttonMarginPx;

    private int frequencyLabelPaddingPx;

    private Bitmap bitmap;

    private int[] pixels;

    private int bitmapWidth;
    private int bitmapHeight;

    private long startTimeSec;
    private long endTimeSec;

    private int validFrames;
    private int historyFrames;

    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Rect dstRect = new Rect();
    private final Rect buttonRect = new Rect();

    private Drawable expandDrawable;
    private Drawable collapseDrawable;

    private ScaleGestureDetector scaleDetector;

    private float lastTouchY;

    private boolean dragging = false;

    private final int[] gradient = new int[]{
            Color.parseColor("#1A237E"),
            Color.parseColor("#283593"),
            Color.parseColor("#3949AB"),
            Color.parseColor("#1E88E5"),
            Color.parseColor("#43A047"),
            Color.parseColor("#FDD835"),
            Color.parseColor("#FB8C00"),
            Color.parseColor("#E53935")
    };

    public SpectrogramView(Context context) {
        super(context);
        init(null);
    }

    public SpectrogramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SpectrogramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {

        backgroundColor = Color.BLACK;

        axisColor = Color.WHITE;
        textColor = Color.WHITE;

        gridColor = Color.argb(70, 255, 255, 255);

        axisStrokeWidth = dp(2);
        gridStrokeWidth = dp(1);

        textSize = sp(14);

        leftPaddingPx = (int) dp(55);
        rightPaddingPx = (int) dp(20);
        topPaddingPx = (int) dp(10);
        bottomPaddingPx = (int) dp(40);

        buttonBackgroundColor = Color.argb(180, 40, 40, 40);
        buttonIconTint = Color.WHITE;
        buttonCornerRadius = dp(12);
        buttonSizePx = (int) dp(52);
        buttonMarginPx = (int) dp(12);

        frequencyLabelPaddingPx = (int) dp(20);

        if (attrs != null) {

            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpectrogramView);

            backgroundColor = a.getColor(R.styleable.SpectrogramView_spectrogramBackgroundColor, backgroundColor);
            axisColor = a.getColor(R.styleable.SpectrogramView_spectrogramAxisColor, axisColor);
            textColor = a.getColor(R.styleable.SpectrogramView_spectrogramTextColor, textColor);
            gridColor = a.getColor(R.styleable.SpectrogramView_spectrogramGridColor, gridColor);
            buttonBackgroundColor = a.getColor(R.styleable.SpectrogramView_spectrogramButtonBackgroundColor, buttonBackgroundColor);
            buttonIconTint = a.getColor(R.styleable.SpectrogramView_spectrogramButtonIconTint, buttonIconTint);

            axisStrokeWidth = a.getDimension(R.styleable.SpectrogramView_spectrogramAxisStrokeWidth, axisStrokeWidth);
            gridStrokeWidth = a.getDimension(R.styleable.SpectrogramView_spectrogramGridStrokeWidth, gridStrokeWidth);
            textSize = a.getDimension(R.styleable.SpectrogramView_spectrogramTextSize, textSize);
            leftPaddingPx = (int) a.getDimension(R.styleable.SpectrogramView_spectrogramLeftPadding, leftPaddingPx);
            rightPaddingPx = (int) a.getDimension(R.styleable.SpectrogramView_spectrogramRightPadding, rightPaddingPx);
            topPaddingPx = (int) a.getDimension(R.styleable.SpectrogramView_spectrogramTopPadding, topPaddingPx);
            bottomPaddingPx = (int) a.getDimension(R.styleable.SpectrogramView_spectrogramBottomPadding, bottomPaddingPx);
            buttonCornerRadius = a.getDimension(R.styleable.SpectrogramView_spectrogramButtonCornerRadius, buttonCornerRadius);
            buttonSizePx = (int) a.getDimension(R.styleable.SpectrogramView_spectrogramButtonSize, buttonSizePx);
            buttonMarginPx = (int) a.getDimension(R.styleable.SpectrogramView_spectrogramButtonMargin, buttonMarginPx);
            dynamicFrequencyAxis = a.getBoolean(R.styleable.SpectrogramView_spectrogramDynamicFrequencyAxis, true);
            frequencyLabelPaddingPx = (int) a.getDimension(R.styleable.SpectrogramView_spectrogramFrequencyLabelPadding, frequencyLabelPaddingPx);

            visualBins = a.getInt(R.styleable.SpectrogramView_spectrogramVisualBins, visualBins);

            minDb = a.getFloat(R.styleable.SpectrogramView_spectrogramMinDb, minDb);
            maxDb = a.getFloat(R.styleable.SpectrogramView_spectrogramMaxDb, maxDb);
            minScaleY = a.getFloat(R.styleable.SpectrogramView_spectrogramMinScaleY, 1f);
            maxScaleY = a.getFloat(R.styleable.SpectrogramView_spectrogramMaxScaleY, 20f);

            showGrid = a.getBoolean(R.styleable.SpectrogramView_spectrogramShowGrid, true);
            showTimeAxis = a.getBoolean(R.styleable.SpectrogramView_spectrogramShowTimeAxis, true);
            showFrequencyAxis = a.getBoolean(R.styleable.SpectrogramView_spectrogramShowFrequencyAxis, true);
            interactionEnabled = a.getBoolean(R.styleable.SpectrogramView_spectrogramInteractionEnabled, false);

            a.recycle();
        }

        initPaints();

        initTouch();
    }

    private void initPaints() {
        setBackgroundColor(backgroundColor);

        axisPaint.setColor(axisColor);
        axisPaint.setStrokeWidth(axisStrokeWidth);

        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);

        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(gridStrokeWidth);

        buttonPaint.setColor(buttonBackgroundColor);
        expandDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_expand_content);
        collapseDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_collapse_content);

        if (expandDrawable != null) {
            expandDrawable.setTint(buttonIconTint);
        }

        if (collapseDrawable != null) {
            collapseDrawable.setTint(buttonIconTint);
        }
    }

    private void initTouch() {
        scaleDetector = new ScaleGestureDetector(
                getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float oldScale = scaleYFactor;

                        scaleYFactor *= detector.getScaleFactor();
                        scaleYFactor = Math.max(minScaleY, Math.min(scaleYFactor, maxScaleY));

                        float focusY = detector.getFocusY();

                        float contentTop = topPaddingPx;
                        float contentBottom = getHeight() - bottomPaddingPx;
                        float contentHeight = contentBottom - contentTop;

                        float normalized = (focusY - contentTop + translateY) / (contentHeight * oldScale);

                        translateY = normalized * contentHeight * scaleYFactor - (focusY - contentTop);

                        clampTranslate();
                        invalidate();

                        return true;
                    }
                }
        );
    }

    public void submit(
            float[] data,
            int bins,
            int history,
            int validFrames,
            long startTimeSec,
            long endTimeSec
    ) {

        if (data == null) return;

        if (bins <= 0 || history <= 0) return;

        if (validFrames < 0) return;

        if (data.length < bins * history) return;

        this.startTimeSec = startTimeSec;
        this.endTimeSec = endTimeSec;

        this.validFrames = validFrames;
        this.historyFrames = history;

        ensureBitmap(history, visualBins);

        int emptyColor = mapColor(minDb);
        Arrays.fill(pixels, emptyColor);

        for (int x = 0; x < validFrames; x++) {

            int sourceFrame = history - validFrames + x;
            int frameOffset = sourceFrame * bins;

            for (int visualY = 0; visualY < visualBins; visualY++) {

                float y0 = (float) visualY / visualBins;
                float y1 = (float) (visualY + 1) / visualBins;

                float freq0 = y0 * MAX_FREQUENCY;
                float freq1 = y1 * MAX_FREQUENCY;

                int startBin = frequencyToBin(freq0, bins);
                int endBin = frequencyToBin(freq1, bins);

                if (startBin < 0) {
                    startBin = 0;
                }

                if (endBin > bins) {
                    endBin = bins;
                }

                if (endBin <= startBin) {
                    endBin = startBin + 1;
                }

                float max = minDb;

                for (int bin = startBin; bin < endBin; bin++) {
                    float value = data[frameOffset + bin];

                    if (value < minDb) {
                        value = minDb;
                    }

                    if (value > maxDb) {
                        value = maxDb;
                    }

                    if (value > max) {
                        max = value;
                    }
                }

                int color = mapColor(max);

                int py = visualBins - 1 - visualY;
                pixels[py * history + x] = color;
            }
        }

        bitmap.setPixels(pixels, 0, history, 0, 0, history, visualBins);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int contentLeft = leftPaddingPx;
        int contentTop = topPaddingPx;
        int contentRight = getWidth() - rightPaddingPx;
        int contentBottom = getHeight() - bottomPaddingPx;

        drawBitmap(canvas, contentLeft, contentTop, contentRight, contentBottom);
        drawAxes(canvas, contentLeft, contentTop, contentRight, contentBottom);
        drawInteractionButton(canvas);
    }

    private void drawBitmap(Canvas canvas, int left, int top, int right, int bottom) {
        if (bitmap == null) return;
        if (historyFrames <= 0) return;
        if (validFrames <= 0) return;

        int contentWidth = right - left;
        float fillRatio = (float) validFrames / historyFrames;

        int spectrogramWidth = (int) (contentWidth * fillRatio);
        float scaledHeight = (bottom - top) * scaleYFactor;

        dstRect.set(
                left,
                (int) (top - translateY),
                left + spectrogramWidth,
                (int) (top + scaledHeight - translateY)
        );

        var srcRect = new Rect(0, 0, validFrames, visualBins);

        canvas.drawBitmap(bitmap, srcRect, dstRect, bitmapPaint);
    }

    private void drawAxes(Canvas canvas, int left, int top, int right, int bottom) {
        canvas.drawLine(left, top, left, bottom, axisPaint);
        canvas.drawLine(left, bottom, right, bottom, axisPaint);

        if (showFrequencyAxis) {
            drawFrequencyAxis(canvas, left, top, right, bottom);
        }

        if (showTimeAxis) {
            drawTimeAxis(canvas, left, right, bottom);
        }
    }

    private void drawFrequencyAxis(Canvas canvas, int left, int top, int right, int bottom) {
        if (!dynamicFrequencyAxis) {
            return;
        }

        float visibleMinFreq = screenToFrequency(bottom, top, bottom);
        float visibleMaxFreq = screenToFrequency(top, top, bottom);
        float visibleRange = visibleMaxFreq - visibleMinFreq;

        float step = chooseFrequencyStep(visibleRange);
        float firstTick = (float) Math.floor(visibleMinFreq / step) * step;

        for (float freq = firstTick; freq <= visibleMaxFreq; freq += step) {
            float y = frequencyToScreenY(freq, top, bottom);

            if (y < top || y > bottom) {
                continue;
            }

            if (showGrid) {
                canvas.drawLine(left, y, right, y, gridPaint);
            }

            canvas.drawLine(left - 10, y, left, y, axisPaint);

            String label;
            if (freq >= 1000) {

                if (freq % 1000 == 0) {
                    label = String.format(Locale.getDefault(), "%.0fk", freq / 1000f);
                } else {
                    label = String.format(Locale.getDefault(), "%.1fk", freq / 1000f);
                }

            } else {
                label = String.format(Locale.getDefault(), "%.0f", freq);
            }

            float labelWidth = textPaint.measureText(label);
            canvas.drawText(label, left - labelWidth - frequencyLabelPaddingPx, y + 10, textPaint);
        }

        float hzWidth = textPaint.measureText("Hz");
        canvas.drawText("Hz", left - hzWidth - frequencyLabelPaddingPx, top + textSize, textPaint);
    }

    private void drawTimeAxis(Canvas canvas, int left, int right, int bottom) {
        int width = right - left;

        float fillRatio = (float) validFrames / historyFrames;

        int visibleWidth = (int) (width * fillRatio);
        int visibleTicks = Math.max(1, (int) Math.ceil(fillRatio * 5f));

        for (int i = 0; i < visibleTicks; i++) {
            float ratio;

            if (visibleTicks == 1) {
                ratio = 1f;
            } else {
                ratio = (float) i / (visibleTicks - 1);
            }

            float x = left + ratio * visibleWidth;

            if (showGrid) {
                canvas.drawLine(x, topPaddingPx, x, bottom, gridPaint);
            }

            canvas.drawLine(x, bottom, x, bottom + 10, axisPaint);

            long time = startTimeSec + (long) ((endTimeSec - startTimeSec) * ratio);
            String label = formatTime(time);

            float textWidth = textPaint.measureText(label);
            float tx = x - textWidth / 2f;

            if (tx < 0) {
                tx = 0;
            }

            if (tx + textWidth > getWidth()) {
                tx = getWidth() - textWidth;
            }

            canvas.drawText(label, tx, bottom + 50, textPaint);
        }

        String axisLabel = "Time";
        float labelWidth = textPaint.measureText(axisLabel);

        canvas.drawText(axisLabel, right - labelWidth, getHeight() - 10, textPaint);
    }

    private void drawInteractionButton(Canvas canvas) {
        updateButtonRect();

        canvas.drawRoundRect(
                buttonRect.left,
                buttonRect.top,
                buttonRect.right,
                buttonRect.bottom,
                buttonCornerRadius,
                buttonCornerRadius,
                buttonPaint
        );

        Drawable drawable =
                interactionEnabled
                        ? collapseDrawable
                        : expandDrawable;

        if (drawable == null) {
            return;
        }

        int iconSize = (int) (buttonSizePx * 0.45f);

        int left = buttonRect.centerX() - iconSize / 2;
        int top = buttonRect.centerY() - iconSize / 2;

        drawable.setBounds(left, top, left + iconSize, top + iconSize);
        drawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (buttonRect.contains((int) event.getX(), (int) event.getY())) {
                toggleInteraction();
                return true;
            }
        }

        if (!interactionEnabled) {
            return true;
        }

        scaleDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.getY();
                dragging = true;
            }

            case MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress() && dragging) {
                    float dy = event.getY() - lastTouchY;
                    translateY -= dy;

                    clampTranslate();
                    lastTouchY = event.getY();

                    invalidate();
                }
            }

            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragging = false;
            }

        }

        return true;
    }

    private void updateButtonRect() {
        int left = getWidth() - buttonSizePx - buttonMarginPx;
        int top = buttonMarginPx;

        buttonRect.set(left, top, left + buttonSizePx, top + buttonSizePx);
    }

    private void toggleInteraction() {
        interactionEnabled = !interactionEnabled;
        if (!interactionEnabled) {
            scaleYFactor = 1f;
            translateY = 0f;
        }

        invalidate();
    }

    private void clampTranslate() {
        float contentHeight = getHeight() - topPaddingPx - bottomPaddingPx;
        float maxTranslate = contentHeight * (scaleYFactor - 1f);

        if (translateY < 0f) {
            translateY = 0f;
        }

        if (translateY > maxTranslate) {
            translateY = maxTranslate;
        }
    }

    private float frequencyToScreenY(float frequency, int top, int bottom) {
        float contentHeight = bottom - top;

        float normalized = frequency / MAX_FREQUENCY;

        float scaledY = (1f - normalized) * contentHeight * scaleYFactor;

        return top + scaledY - translateY;
    }

    private float screenToFrequency(float screenY, int top, int bottom) {
        float contentHeight = bottom - top;

        float normalized = 1f - ((screenY - top + translateY) / (contentHeight * scaleYFactor));

        normalized = Math.max(0f, normalized);
        normalized = Math.min(1f, normalized);

        return normalized * MAX_FREQUENCY;
    }

    private float chooseFrequencyStep(float visibleRange) {
        if (visibleRange <= 1000) {
            return 100;
        }

        if (visibleRange <= 2000) {
            return 200;
        }

        if (visibleRange <= 5000) {
            return 500;
        }

        if (visibleRange <= 10000) {
            return 1000;
        }

        return 5000;
    }

    private int frequencyToBin(float frequency, int bins) {
        float ratio = frequency / MAX_FREQUENCY;

        return (int) (ratio * bins);
    }

    private void ensureBitmap(int width, int height) {
        if (bitmap != null && bitmapWidth == width && bitmapHeight == height) {
            return;
        }

        bitmapWidth = width;
        bitmapHeight = height;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        pixels = new int[width * height];

        int emptyColor = mapColor(minDb);
        Arrays.fill(pixels, emptyColor);
    }

    private int mapColor(float db) {
        float normalized = (db - minDb) / (maxDb - minDb);

        normalized = Math.max(0f, normalized);
        normalized = Math.min(1f, normalized);

        float scaled = normalized * (gradient.length - 1);
        int index = (int) scaled;

        if (index >= gradient.length - 1) {
            return gradient[gradient.length - 1];
        }

        float t = scaled - index;

        return interpolate(gradient[index], gradient[index + 1], t);
    }

    private int interpolate(int c1, int c2, float t) {

        int r = (int) (Color.red(c1) + t * (Color.red(c2) - Color.red(c1)));
        int g = (int) (Color.green(c1) + t * (Color.green(c2) - Color.green(c1)));
        int b = (int) (Color.blue(c1) + t * (Color.blue(c2) - Color.blue(c1)));

        return Color.rgb(r, g, b);
    }

    private String formatTime(long sec) {

        long minutes = sec / 60;
        long seconds = sec % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
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