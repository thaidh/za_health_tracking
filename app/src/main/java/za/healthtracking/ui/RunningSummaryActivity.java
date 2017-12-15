package za.healthtracking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
import za.healthtracking.adapters.PacePerKmRecyclerViewAdapter;
import za.healthtracking.app.AppController;
import za.healthtracking.models.RunningSession;
import za.healthtracking.utils.Helper;
import za.healthtracking.utils.PolylineUtils;
import za.healthtracking.utils.TimeHelper;


public class RunningSummaryActivity extends AppCompatActivity implements OnMapReadyCallback {
    @BindView(R.id.txtDateTime)
    TextView txtDateTime;
    @BindView(R.id.txtDuration)
    TextView txtDuration;
    @BindView(R.id.txtDistance)
    TextView txtDistance;
    @BindView(R.id.txtCalories)
    TextView txtCalories;
    @BindView(R.id.txtAvgPace)
    TextView txtAvgPace;
    @BindView(R.id.mapContainer)
    View mapContainer;

    @BindView(R.id.map_size_switcher_button)
    ImageButton map_size_switcher_button;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recyclerViewPacePerKm)
    RecyclerView recyclerViewPacePerKm;
    PacePerKmRecyclerViewAdapter pacePerKmRecyclerViewAdapter;

    GoogleMap mGoogleMap;
    RunningSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_summary);

        ButterKnife.bind(this);

        init();
        bindingData();
    }

    private void bindingData() {
        if (AppController.runningSession == null)
            return;

        session = AppController.runningSession;

        txtDuration.setText(Helper.formatDuration(TimeHelper.MillisToSecond(session.getDuration())));
        txtDistance.setText(Helper.formatDistance(session.getDistance()));
        txtCalories.setText(Helper.formatCalories(session.getCalories()));
        txtAvgPace.setText(Helper.formatPace(session.getAvgPace()));

        pacePerKmRecyclerViewAdapter.updateData(session.getPacePerKmList());

        txtDateTime.setText(Helper.formatDateTime(session.startTime));
    }

    private void init() {
        // Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("CHẠY BỘ");
        }

        // Pacer per km
        pacePerKmRecyclerViewAdapter = new PacePerKmRecyclerViewAdapter(this);
        recyclerViewPacePerKm.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPacePerKm.setAdapter(pacePerKmRecyclerViewAdapter);

        // Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // No wayPoints
        if (session.getLatLngList().size() <= 0) {
            mapContainer.setVisibility(View.GONE);
            return;
        }
        mapContainer.setVisibility(View.VISIBLE);

        // Draw path line
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

        // Disable interaction
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);

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
    void onZoomIn() {
        Intent intent = new Intent(this, RunningZoomInActivity.class);
        startActivity(intent);
    }
}
