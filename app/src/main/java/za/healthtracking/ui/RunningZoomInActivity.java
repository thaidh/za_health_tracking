package za.healthtracking.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.zing.pedometer.R;
import za.healthtracking.app.AppController;
import za.healthtracking.models.RunningSession;
import za.healthtracking.utils.PolylineUtils;

public class RunningZoomInActivity extends AppCompatActivity implements OnMapReadyCallback {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_zoom_in);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        // Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("KẾT QUẢ");
        }

        // Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (AppController.runningSession == null)
            return;

        RunningSession session = AppController.runningSession;

        // Draw polyline
        mGoogleMap.addPolyline(PolylineUtils.createPolyline(getResources(), R.color.map_path_outer, getResources().getDimensionPixelSize(R.dimen.map_path_outer), session.getLatLngList()));
        mGoogleMap.addPolyline(PolylineUtils.createPolyline(getResources(), R.color.map_path_inner, getResources().getDimensionPixelSize(R.dimen.map_path_inner), session.getLatLngList()));

        // Move camera to center
        final LatLngBounds.Builder builder = new LatLngBounds.Builder(); // Create boundary
        for (LatLng wayPoint : session.getLatLngList()) {
            builder.include(wayPoint);
        }
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int)getResources().getDimension(R.dimen.running_summary_map_height);
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

        // Add markers
        Marker startMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(session.getLatLngList().get(0))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.track_start_marker))
                .anchor(0.5f,0.5f) // icon center
                .title("Vị trí xuất phát"));

        Marker finishMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(session.getLatLngList().get(session.getLatLngList().size()-1))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.track_finish_marker))
                .anchor(0.5f,0.5f) // icon center
                .title("Vị trí kết thúc"));
    }

    @OnClick(R.id.map_size_switcher_button)
    void onZoomOut() {
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
