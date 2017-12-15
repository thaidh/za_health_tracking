package za.healthtracking.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.zing.pedometer.BuildConfig;
import vn.zing.pedometer.R;
import za.healthtracking.service.RunningActivityService;
import za.healthtracking.utils.Helper;
import za.healthtracking.utils.PermissionRequirement;
import za.healthtracking.utils.PolylineUtils;
import za.healthtracking.utils.TimeHelper;

import static vn.zing.pedometer.R.id.map;

public class RunningActivity extends AppCompatActivity implements OnMapReadyCallback {
    private boolean mIsBound;

    @BindView(R.id.layoutRunningActivity)
    View layoutRunningActivity;

    @BindView(R.id.btnPauseResume)
    Button btnPauseResume;

    @BindView(R.id.btnStart)
    Button btnStart;

    @BindView(R.id.btnStop)
    Button btnStop;

    @BindView(R.id.txtDuration)
    TextView txtDuration;
    @BindView(R.id.txtDistance)
    TextView txtDistance;
    @BindView(R.id.txtCalories)
    TextView txtCalories;
    @BindView(R.id.txtAvgPace)
    TextView txtAvgPace;
    @BindView(R.id.txtPacePerKm)
    TextView txtPacePerKm;

    Intent mServiceIntent;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 34;
    PermissionRequirement mLocationPermissionRequirement;

    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    boolean mIsPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        // Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);


        updateState(RunningActivityService.State.NONE);
    }

    /**
     * Prompt user to enable GPS and Location Services
     * @param mGoogleApiClient
     * @param activity
     */
    public static void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequestHighAccuracy = LocationRequest.create();
        locationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequestHighAccuracy);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(RunningActivity.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        mLocationRequest.setNumUpdates(1);
                        if (ContextCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(
                                            location.getLatitude(),
                                            location.getLongitude())).zoom(17).build();
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                }
                            });
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(LocationServices.API)
                .build();

        locationChecker(mGoogleApiClient, RunningActivity.this);

        mGoogleApiClient.connect();
    }


    Polyline mPolylineOuter;
    Polyline mPolylineInner;
    int mLatLngListSize = 0;
    Marker mStartMarker;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (mBoundService != null) {
                txtDuration.setText(Helper.formatDuration(TimeHelper.MillisToSecond(mBoundService.getDuration())));
                txtDistance.setText(Helper.formatDistance(mBoundService.getDistance()));
                txtAvgPace.setText(Helper.formatPace(mBoundService.getAvgPace()));
                txtCalories.setText(Helper.formatCalories(mBoundService.getCalories()));

                txtPacePerKm.setText(Helper.dumbData(mBoundService.getPacePerKmList()));

                // Running Path
                if (mPathUpdateCounter % 1 == 0) {
                    List<LatLng> latLngList = mBoundService.getLatLngList();
                    if (latLngList != null && latLngList.size() > mLatLngListSize) {

                        if (mStartMarker == null) {
                            mStartMarker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(latLngList.get(0))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.track_start_marker))
                                    .anchor(0.5f,0.5f) // icon center
                                    .title("Vị trí xuất phát"));
                        }

                        // Create polyline instance
                        if (mPolylineOuter == null) {
                            mPolylineOuter = mGoogleMap.addPolyline(PolylineUtils.createPolyline(getResources(), R.color.map_path_outer, getResources().getDimensionPixelSize(R.dimen.map_path_outer), latLngList));
                            mPolylineInner = mGoogleMap.addPolyline(PolylineUtils.createPolyline(getResources(), R.color.map_path_inner, getResources().getDimensionPixelSize(R.dimen.map_path_inner), latLngList));
                        } else {
                            mPolylineOuter.setPoints(latLngList);
                            mPolylineInner.setPoints(latLngList);
                        }

                        // Move camera
                        float zoom = mGoogleMap.getCameraPosition().zoom;
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(mBoundService.getLastLatLng()).zoom(zoom).build();
                        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        mLatLngListSize = latLngList.size();
                    }
                }

                mPathUpdateCounter++;
            }
            sendMessageDelayed(Message.obtain(this, 1), 1000);
        }
    };

    int mPathUpdateCounter = 0;

    private RunningActivityService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((RunningActivityService.LocalBinder)service).getService();
            handler.sendMessageDelayed(Message.obtain(handler, 0), 0);

            updateState(mBoundService.getState());

            Log.d("ZHealth", "onServiceConnected!");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.d("ZHealth", "onServiceDisconnected!");
        }
    };


    void updateState(RunningActivityService.State state) {
        switch (state) {
            case RUNNING:
                btnStop.setVisibility(View.VISIBLE);
                btnPauseResume.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
                btnPauseResume.setText("PAUSE");
                break;
            case PAUSE:
                btnStop.setVisibility(View.VISIBLE);
                btnPauseResume.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
                btnPauseResume.setText("RESUME");
                break;
            case NONE:
            case STOP:
                btnStop.setVisibility(View.GONE);
                btnPauseResume.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                break;
        }
    }

    void doBindService() {
        bindService(new Intent(getApplicationContext(), RunningActivityService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ZHealth", "onDestroy!");

        doUnbindService();
    }

    @OnClick(R.id.btnStart)
    void onStartRecordRunning() {
        Log.d("ZHealth", "Started!");

        mServiceIntent = new Intent(getApplicationContext(), RunningActivityService.class);
        startService(mServiceIntent);

        updateState(RunningActivityService.State.RUNNING);

        //
        if (mPolylineOuter != null) {
            mPolylineOuter.remove();
            mPolylineInner.remove();

            mPolylineInner = null;
            mPolylineOuter = null;
        }
        mLatLngListSize = 0;
        mPathUpdateCounter = 0;
        if (mStartMarker != null) {
            mStartMarker.remove();
            mStartMarker = null;
        }
    }

    @OnClick(R.id.btnPauseResume)
    void onPauseResumeRecordRunning() {
        if (mIsPause) { //btn resume
            btnPauseResume.setText("PAUSE");
            if (mBoundService != null) {
                mBoundService.onResumeRecording();
            }
        } else {
            btnPauseResume.setText("RESUME");
            if (mBoundService != null) {
                mBoundService.onPauseRecording();
            }
        }
        mIsPause = !mIsPause;
    }

    @OnClick(R.id.btnStop)
    void onStopRecordRunning() {
        Log.d("ZHealth", "Stopped!");

        if (mBoundService != null) {
            mBoundService.onStopRecording();
        }

        updateState(RunningActivityService.State.STOP);

        // start Summary
        Intent intent = new Intent(this, RunningSummaryActivity.class);
        startActivity(intent);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Location Permission Require
        mLocationPermissionRequirement = new PermissionRequirement(RunningActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, new PermissionRequirement.PermissionRequirementListener() {
            @Override
            public void onPermissionGranted() {
                buildGoogleApiClient();
                if (ActivityCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mGoogleMap.setMyLocationEnabled(true);
            }

            @Override
            public void onShouldProvideRationale() {
                Snackbar.make(
                        layoutRunningActivity,
                        R.string.permission_location_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Request permission
                                ActivityCompat.requestPermissions(RunningActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .show();
            }

            @Override
            public void onRequestPermission() {
                ActivityCompat.requestPermissions(RunningActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }

            @Override
            public void onPermissionDenied() {
                Snackbar.make(
                        layoutRunningActivity,
                        R.string.permission_location_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });
        mLocationPermissionRequirement.execute();


        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
            }
        });

        doBindService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                mLocationPermissionRequirement.onRequestPermissionsResult(permissions, grantResults);
                break;
        }
    }


}
