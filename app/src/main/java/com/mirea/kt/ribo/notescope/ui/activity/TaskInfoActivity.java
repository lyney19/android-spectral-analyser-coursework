package com.mirea.kt.ribo.notescope.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.mirea.kt.ribo.notescope.model.Task;
import com.mirea.kt.ribo.notescope.R;

public class TaskInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_task_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        var task = (Task) getIntent().getSerializableExtra("task");

        TextView title = findViewById(R.id.tvTitle);
        TextView variant = findViewById(R.id.tvVariant);
        TextView description = findViewById(R.id.tvDescription);

        title.setText(task.title());
        variant.setText(String.format(getString(R.string.variant), task.variant()));
        description.setText(task.task());

        findViewById(R.id.btnCopy).setOnClickListener(view -> {
            var clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            var clipData = ClipData.newPlainText("Task", String.valueOf(task));
            clipboard.setPrimaryClip(clipData);

            Toast.makeText(this, R.string.copied, Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnShare).setOnClickListener(view -> {
            var intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(task));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.no_situable_apps, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnTransfer).setOnClickListener(view -> {
            var intent = new Intent(this, SpectrumActivity.class);
            startActivity(intent);
        });

        NestedScrollView scrollDescription = findViewById(R.id.scrollDescription);
        var fadeView = findViewById(R.id.fadeBottom);

        scrollDescription.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            View child = scrollDescription.getChildAt(0);
            boolean canScroll = child.getHeight() > scrollDescription.getHeight();

            fadeView.setVisibility(canScroll ? View.VISIBLE : View.GONE);
        });

        scrollDescription.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener)
                        (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            View child = v.getChildAt(0);
            boolean reachedBottom = scrollY + v.getHeight() >= child.getHeight();

            if (reachedBottom) {
                fadeView.animate()
                        .alpha(0f)
                        .setDuration(150)
                        .start();
            } else {
                fadeView.setVisibility(View.VISIBLE);
                fadeView.animate()
                        .alpha(1f)
                        .setDuration(150)
                        .start();
            }
        });
    }
}