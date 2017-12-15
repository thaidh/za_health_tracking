package za.healthtracking.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by hiepmt on 30/05/2017.
 */

public class PermissionRequirement {
    public interface PermissionRequirementListener {
        void onPermissionGranted();
        void onShouldProvideRationale();
        void onRequestPermission();
        void onPermissionDenied();
    }

    private Activity mActivity;
    private String mPermission;
    private PermissionRequirementListener mListener;
    public PermissionRequirement(Activity activity, String permission, PermissionRequirementListener listener) {
        mActivity = activity;
        mPermission = permission;
        mListener = listener;
    }

    public void execute() {
        if (checkPermissions()) {
            mListener.onPermissionGranted();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(mActivity, mPermission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Should we show an explanation?
        if (shouldProvideRationale) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            mListener.onShouldProvideRationale();
        } else {
            Log.i(TAG, "Requesting permission");
            // No explanation needed, we can request the permission.

            mListener.onRequestPermission();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted, yay! Do the
            mListener.onPermissionGranted();
        } else {

            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            mListener.onPermissionDenied();
        }
    }
}
