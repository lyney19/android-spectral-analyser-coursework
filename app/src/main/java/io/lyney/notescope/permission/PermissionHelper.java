package io.lyney.notescope.permission;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    private static final int REQUEST_AUDIO_RECORD = 1;

    public boolean askAudioRecordPermission(Activity owner) {
        if (ContextCompat.checkSelfPermission(owner, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(
                owner,
                new String[] { Manifest.permission.RECORD_AUDIO },
                REQUEST_AUDIO_RECORD
        );

        return false;
    }
}
