package za.healthtracking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.zing.pedometer.R;
import za.healthtracking.adapters.HistoryRunningRecyclerViewAdapter;
import za.healthtracking.adapters.RunningActivityRecyclerViewAdapter;
import za.healthtracking.app.AppController;
import za.healthtracking.database.entities.RunningActivityLog;
import za.healthtracking.manager.HistoryFitnessManager;
import za.healthtracking.manager.HistoryRunningManager;
import za.healthtracking.models.RunningBucket.RunningBucket;
import za.healthtracking.models.RunningSession;
import za.healthtracking.utils.Helper;
import za.healthtracking.utils.PolyUtil;
import za.healthtracking.utils.TimeHelper;


public class RunningTrendActivity extends AppCompatActivity {
    final String DAYS = "Ngày";
    final String WEEKS = "Tuần";
    final String MONTHS = "Tháng";
    @BindView(R.id.currentSteps)
    TextView currentSteps;
    @BindView(R.id.txtDate)
    TextView txtDate;
    @BindView(R.id.spType)
    Spinner spType;

    @BindView(R.id.txtNumberOfSessions)
    TextView txtNumberOfSessions;
    @BindView(R.id.txtTotalDistance)
    TextView txtTotalDistance;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.stepHistoryRecyclerView)
    RecyclerView stepHistoryRecyclerView;
    private HistoryRunningRecyclerViewAdapter historyRunningRecyclerViewAdapter;
    private LinearLayoutManager mLayoutManager;

    private HistoryRunningManager mHistoryFitnessManager;

    int mVisibleItems = 8;
    String mSelectedType = "";
    
    //
    @BindView(R.id.runningActivityRecyclerView)
    RecyclerView runningActivityRecyclerView;
    RunningActivityRecyclerViewAdapter runningActivityRecyclerViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_running_trend);
        ButterKnife.bind(this);

        init();
        initComponents();
    }

    private void init() {
        mHistoryFitnessManager = new HistoryRunningManager();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void initComponents() {
        // Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("CHẠY BỘ");
        }

        // Spinner Statistics by Type
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                statisticsByType(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Spinner Drop down elements
        List<String> spStatisticsCycle = new ArrayList<String>();
        spStatisticsCycle.add(DAYS);
        spStatisticsCycle.add(WEEKS);
        spStatisticsCycle.add(MONTHS);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spStatisticsCycle);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spType.setAdapter(dataAdapter);
        // --- End of Spinner Type ---


        // History Fitness RecyclerView
        initRecycleView();

        // Running Activity RecyclerView
        runningActivityRecyclerViewAdapter = new RunningActivityRecyclerViewAdapter(this);
        runningActivityRecyclerViewAdapter.setOnItemClickListener(new RunningActivityRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                RunningActivityLog item = runningActivityRecyclerViewAdapter.getItem(position);
                AppController.runningSession = new RunningSession(item.durationInMillis, item.distanceInMeters, item.calories, item.avgPace, PolyUtil.decode(item.wayPoints), RunningActivityLog.decodePacePerKM(item.pacePerKM), TimeHelper.SecondToMillis(item.startTime));

                Intent intent = new Intent(RunningTrendActivity.this, RunningSummaryActivity.class);
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        runningActivityRecyclerView.setAdapter(runningActivityRecyclerViewAdapter);
        runningActivityRecyclerView.setNestedScrollingEnabled(false);
        runningActivityRecyclerView.setLayoutManager(layoutManager);
        runningActivityRecyclerView.addItemDecoration(new DividerItemDecoration(runningActivityRecyclerView.getContext(),
                layoutManager.getOrientation()));
    }

    private void initRecycleView() {
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        stepHistoryRecyclerView.setLayoutManager(mLayoutManager);

        historyRunningRecyclerViewAdapter = new HistoryRunningRecyclerViewAdapter(getApplicationContext());
        stepHistoryRecyclerView.setAdapter(historyRunningRecyclerViewAdapter);

        // Listener on end of recycler view
        stepHistoryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { //Stop scrolling
                    if (historyRunningRecyclerViewAdapter.getSelectedItemIndex() != -1) {
                        onUpdateFitnessData();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                int firstPos = mLayoutManager.findFirstVisibleItemPosition();
                int visible = mLayoutManager.getChildCount();
                mVisibleItems = visible;

                // Update middle selected item
                int middle = visible / 2 + firstPos - 1;
                historyRunningRecyclerViewAdapter.setSelectedItem(middle);

                // Check on bottom
                if (!recyclerView.canScrollHorizontally(-1)) {
                    onScrolledToBottom();
                }
            }
        });

        // Click item
        historyRunningRecyclerViewAdapter.setOnItemClickListener(new HistoryRunningRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (historyRunningRecyclerViewAdapter.getItem(position).isEnable) {
                    historyRunningRecyclerViewAdapter.setSelectedItem(position);
                    scrollToSelectedItem();

                    onUpdateFitnessData();
                }
            }
        });
    }

    private void onUpdateFitnessData() {
        updateFitnessDetailInfo(historyRunningRecyclerViewAdapter.getSelectedItem());

        txtDate.setText(historyRunningRecyclerViewAdapter.getSelectedItem().getLiteralDate());

        txtTotalDistance.setText(Helper.formatDistanceWithUnit(historyRunningRecyclerViewAdapter.getSelectedItem().getDistance()));
        txtNumberOfSessions.setText(historyRunningRecyclerViewAdapter.getSelectedItem().mLogs.size() + " buổi");
    }

    private void updateFitnessDetailInfo(RunningBucket data) {

        //
        currentSteps.setText(Helper.formatDuration(TimeHelper.MillisToSecond(data.getDuration())));
        txtDate.setText(TimeHelper.getLiteralDate(new Date()));

        //
        runningActivityRecyclerViewAdapter.updateData(data.mLogs);


    }


    private void onScrolledToBottom() {
        Log.d("Scroll", "On Scrolled To Bottom");

        switch (mSelectedType) {
            case DAYS:
                mHistoryFitnessManager.getMoreFitnessPerDates(new HistoryRunningManager.Listener() {
                    @Override
                    public void onSuccess() {
                        historyRunningRecyclerViewAdapter.updateData(mHistoryFitnessManager.mRunningBucketDayList);
                    }
                });
                break;
            case WEEKS:
                mHistoryFitnessManager.getMoreFitnessPerWeeks(new HistoryRunningManager.Listener() {
                    @Override
                    public void onSuccess() {
                        historyRunningRecyclerViewAdapter.updateData(mHistoryFitnessManager.mRunningBucketWeekList);
                    }
                });
                break;
            case MONTHS:
                mHistoryFitnessManager.getMoreFitnessPerMonths(new HistoryRunningManager.Listener() {
                    @Override
                    public void onSuccess() {
                        historyRunningRecyclerViewAdapter.updateData(mHistoryFitnessManager.mRunningBucketMonthList);
                    }
                });
                break;
        }
    }


    private void scrollToSelectedItem() {
        int selectedItem = historyRunningRecyclerViewAdapter.getSelectedItemIndex();
        mLayoutManager.scrollToPositionWithOffset( selectedItem - mVisibleItems/2, 0);
    }

    private void statisticsByType(String type) {
        mSelectedType = type;
        switch (type) {
            case DAYS:
                mHistoryFitnessManager.getInitFitnessPerDate(new HistoryRunningManager.Listener() {
                    @Override
                    public void onSuccess() {
                        historyRunningRecyclerViewAdapter.updateData(mHistoryFitnessManager.mRunningBucketDayList);
                        historyRunningRecyclerViewAdapter.setSelectedItem(HistoryFitnessManager.DISABLE_PER_DATE_OFFSET); //Select Item
                        scrollToSelectedItem();

                        updateFitnessDetailInfo(historyRunningRecyclerViewAdapter.getSelectedItem());
                        onUpdateFitnessData();
                    }
                });
                break;
            case WEEKS:
                mHistoryFitnessManager.getInitFitnessPerWeek(new HistoryRunningManager.Listener() {
                    @Override
                    public void onSuccess() {
                        historyRunningRecyclerViewAdapter.updateData(mHistoryFitnessManager.mRunningBucketWeekList);
                        historyRunningRecyclerViewAdapter.setSelectedItem(HistoryFitnessManager.DISABLE_PER_WEEK_OFFSET); //Select Item
                        scrollToSelectedItem();

                        updateFitnessDetailInfo(historyRunningRecyclerViewAdapter.getSelectedItem());
                        onUpdateFitnessData();
                    }
                });
                break;
            case MONTHS:
                mHistoryFitnessManager.getInitFitnessPerMonths(new HistoryRunningManager.Listener() {
                    @Override
                    public void onSuccess() {
                        historyRunningRecyclerViewAdapter.updateData(mHistoryFitnessManager.mRunningBucketMonthList);
                        historyRunningRecyclerViewAdapter.setSelectedItem(HistoryFitnessManager.DISABLE_PER_MONTH_OFFSET); //Select Item
                        scrollToSelectedItem();

                        updateFitnessDetailInfo(historyRunningRecyclerViewAdapter.getSelectedItem());
                        onUpdateFitnessData();
                    }
                });
                break;
        }
    }
}
