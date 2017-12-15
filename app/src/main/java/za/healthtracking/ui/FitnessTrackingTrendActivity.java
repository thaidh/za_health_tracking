package za.healthtracking.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.zing.pedometer.R;
import za.healthtracking.adapters.HistoryFitnessRecyclerView;
import za.healthtracking.manager.HistoryFitnessManager;
import za.healthtracking.manager.HistoryFitnessPerDateManager;
import za.healthtracking.models.FitnessBucket.BaseFitnessBucket;
import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.models.FitnessDateDetail;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 02/08/2017.
 */

public class FitnessTrackingTrendActivity extends AppCompatActivity {
    final String DAYS = "Ngày";
    final String WEEKS = "Tuần";
    final String MONTHS = "Tháng";
    @BindView(R.id.currentSteps)
    TextView currentSteps;
    @BindView(R.id.txtDate)
    TextView txtDate;
    @BindView(R.id.labelDailySteps)
    TextView labelDailySteps;
    @BindView(R.id.chartDailyStep)
    BarChart chartDailyStep;
    @BindView(R.id.spType)
    Spinner spType;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    DetailListItemViewHolder detailListItemDistanceViewHolder;
    DetailListItemViewHolder detailListItemCaloriesBurnedViewHolder;
    DetailListItemViewHolder detailListItemHealthyPaceViewHolder;

    @BindView(R.id.stepHistoryRecyclerView)
    RecyclerView mFitnessRecyclerView;
    private HistoryFitnessRecyclerView mFitnessAdapter;
    private LinearLayoutManager mFitnessLayoutManager;

    private HistoryFitnessPerDateManager mHistoryFitnessPerDateManager;
    private HistoryFitnessManager mHistoryFitnessManager;

    int mVisibleItems = 8;
    String mSelectedType = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fitness_history);
        ButterKnife.bind(this);

        init();
        initComponents();
    }

    private void init() {
        mHistoryFitnessPerDateManager = new HistoryFitnessPerDateManager();
        mHistoryFitnessManager = new HistoryFitnessManager();
    }

    private void initComponents() {
        // Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("SỐ BƯỚC");
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

        // Detail Item
        detailListItemDistanceViewHolder = new DetailListItemViewHolder(R.id.detail_list_item_distance);
        detailListItemCaloriesBurnedViewHolder = new DetailListItemViewHolder(R.id.detail_list_item_calories_burned);
        detailListItemHealthyPaceViewHolder = new DetailListItemViewHolder(R.id.detail_list_item_healthy_pace);

        // History Fitness RecyclerView
        initRecycleView();
    }

    private void initRecycleView() {
        mFitnessLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        mFitnessRecyclerView.setLayoutManager(mFitnessLayoutManager);
        mFitnessAdapter = new HistoryFitnessRecyclerView(getApplicationContext());
        mFitnessRecyclerView.setAdapter(mFitnessAdapter);

        // Listener on end of recycler view
        mFitnessRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { //Stop scrolling
                    if (mFitnessAdapter.getSelectedItemIndex() != -1) {
                        onUpdateFitnessData();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                int firstPos = mFitnessLayoutManager.findFirstVisibleItemPosition();
                int visible = mFitnessLayoutManager.getChildCount();
                mVisibleItems = visible;

                // Update middle selected item
                int middle = visible / 2 + firstPos - 1;
                mFitnessAdapter.setSelectedItem(middle);

                // Check on bottom
                if (!recyclerView.canScrollHorizontally(-1)) {
                    onScrolledToBottom();
                }
            }
        });

        // Click item
        mFitnessAdapter.setOnItemClickListener(new HistoryFitnessRecyclerView.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (mFitnessAdapter.getItem(position).mIsEnable) {
                    mFitnessAdapter.setSelectedItem(position);
                    scrollToSelectedItem();

                    onUpdateFitnessData();
                }
            }
        });
    }

    private void onUpdateFitnessData() {
        updateFitnessDetailInfo(mFitnessAdapter.getSelectedItem());
        if (DAYS.equals(mSelectedType)) {
            getFitnessStepDetailByDate(mFitnessAdapter.getSelectedItem().mStartDate);
        }

        txtDate.setText(mFitnessAdapter.getSelectedItem().getLiteralDate());
    }

    private void getFitnessStepDetailByDate(Date date) {
        mHistoryFitnessPerDateManager.getData(date, new HistoryFitnessPerDateManager.HistoryFitnessPerDateManagerListener() {
            @Override
            public void onData(FitnessDateDetail data) {
                if (data == null)
                    return;

                updateFitnessStepChart(data.getBuckets());
            }
        });
    }

    private void updateFitnessStepChart(List<FitnessBucket> buckets) {
        if (buckets == null)
            return;

        // Build entries
        List<BarEntry> entries = new ArrayList<>();
        int minutes = 0;
        for (FitnessBucket bucket : buckets) {
            if (bucket.nSteps > 0)
                entries.add(new BarEntry(minutes, bucket.nSteps));
            minutes += 20;
        }

        //
        BarDataSet dataSet = new BarDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        //dataSet.setValueTextColor(...); // styling, ...
        chartDailyStep.getLegend().setEnabled(false);

        // Custom value for x-axis
        // the labels that should be drawn on the XAxis
        final String[] quarters = new String[] { "00:00", "06:00", "12:00", "18:00", "24:00" };
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int hour = (int)value;
                Log.d("HiepIT05", hour + "");
                switch (hour) {
                    case 0:
                        return quarters[0];
                    case 6*60:
                        return quarters[1];
                    case 12*60:
                        return quarters[2];
                    case 18*60:
                        return quarters[3];
                    case 24*60:
                        return quarters[4];
                }
                return "";
            }
        };

        XAxis xAxis = chartDailyStep.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(5, true); // force 5 labels
        xAxis.setAxisMaximum(24*60); // set maximum, default automatically base on data
        xAxis.setAxisMinimum(0); // start at zero, default automatically base on data
        xAxis.setDrawGridLines(false);
//        xAxis.enableGridDashedLine(24*60, 20, 0);
        xAxis.setValueFormatter(formatter);


        IAxisValueFormatter lefYFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value > 0)
                    return String.format("%s", (int)value);
                return ""; // Ignore zero value to be drawn
            }
        };
        YAxis leftAxis = chartDailyStep.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setZeroLineWidth(5);
        leftAxis.setAxisMinimum(0); // start at zero, default automatically base on data
        leftAxis.setLabelCount(2);
        leftAxis.setValueFormatter(lefYFormatter);
//        leftAxis.set

        YAxis rightAxis = chartDailyStep.getAxisRight();
        rightAxis.setEnabled(false);


        BarData barData = new BarData(dataSet);
        barData.setBarWidth(10f); // set bar width in value 10/20
        barData.setDrawValues(false); // Hide values drawn on above bar
        chartDailyStep.setFitBars(true); // add half of the bar -> fully displayed not working??
        chartDailyStep.setData(barData);
        chartDailyStep.invalidate(); // refresh

        // Clear description
        Description description = new Description();
        description.setText("");
        chartDailyStep.setDescription(description);


        //Disable interactive with Chart
        chartDailyStep.setTouchEnabled(false);
        chartDailyStep.setDragEnabled(false);
        chartDailyStep.setScaleEnabled(false);
        chartDailyStep.setScaleXEnabled(false);
        chartDailyStep.setScaleYEnabled(false);
        chartDailyStep.setPinchZoom(false);
        chartDailyStep.setDoubleTapToZoomEnabled(false);
    }

    private void updateFitnessDetailInfo(BaseFitnessBucket data) {
        //
        detailListItemDistanceViewHolder.setValue(data.mDistance);
        detailListItemCaloriesBurnedViewHolder.setValue(data.mCaloriesBurned);
        detailListItemHealthyPaceViewHolder.setValue(0);

        //
        currentSteps.setText(data.nSteps + "");
        txtDate.setText(TimeHelper.getLiteralDate(new Date()));
    }

    private void changeView() {
        switch (mSelectedType) {
            case DAYS:
                chartDailyStep.setVisibility(View.VISIBLE);
                detailListItemDistanceViewHolder.changeName("Khoảng cách", "meters");
                detailListItemCaloriesBurnedViewHolder.changeName("Calo đã tiêu hao", "kcal");
                detailListItemHealthyPaceViewHolder.changeName("Nhịp độ tốt cho sức khỏe", "bước");
                labelDailySteps.setText("Số bước hàng ngày");
                break;
            case WEEKS:
                chartDailyStep.setVisibility(View.GONE);
                detailListItemDistanceViewHolder.changeName("Khoảng cách trung bình", "meters");
                detailListItemCaloriesBurnedViewHolder.changeName("Lượng calo tiêu hao trung bình", "kcal");
                detailListItemHealthyPaceViewHolder.changeName("Số bước tr.bình ở nhịp độ khỏe mạnh", "bước");
                labelDailySteps.setText("Số bước trung bình hàng ngày");
                break;
            case MONTHS:
                chartDailyStep.setVisibility(View.GONE);
                detailListItemDistanceViewHolder.changeName("Khoảng cách trung bình", "meters");
                detailListItemCaloriesBurnedViewHolder.changeName("Lượng calo tiêu hao trung bình", "kcal");
                detailListItemHealthyPaceViewHolder.changeName("Số bước tr.bình ở nhịp độ khỏe mạnh", "bước");
                labelDailySteps.setText("Số bước trung bình hàng ngày");
                break;
        }
    }


    private void onScrolledToBottom() {
        Log.d("Scroll", "On Scrolled To Bottom");

        switch (mSelectedType) {
            case DAYS:
                mHistoryFitnessManager.getMoreFitnessPerDates(new HistoryFitnessManager.HistoryFitnessManagerListener() {
                    @Override
                    public void onSuccess() {
                        mFitnessAdapter.updateData(mHistoryFitnessManager.mFitnessPerDateDataSet);
                    }
                });
                break;
            case WEEKS:
                mHistoryFitnessManager.getMoreFitnessPerWeeks(new HistoryFitnessManager.HistoryFitnessManagerListener() {
                    @Override
                    public void onSuccess() {
                        mFitnessAdapter.updateData(mHistoryFitnessManager.mFitnessPerWeekDataSet);
                    }
                });
                break;
            case MONTHS:
                mHistoryFitnessManager.getMoreFitnessPerMonths(new HistoryFitnessManager.HistoryFitnessManagerListener() {
                    @Override
                    public void onSuccess() {
                        mFitnessAdapter.updateData(mHistoryFitnessManager.mFitnessPerMonthDataSet);
                    }
                });
                break;
        }
    }

    private class DetailListItemViewHolder {
        ViewGroup layout;
        TextView txtName;
        TextView txtValue;
        TextView txtUnit;

        DetailListItemViewHolder(int resource) {
            layout = (ViewGroup) findViewById(resource);
            txtName = (TextView) layout.findViewById(R.id.detail_list_item_name);
            txtValue = (TextView) layout.findViewById(R.id.detail_list_item_value);
            txtUnit = (TextView) layout.findViewById(R.id.detail_list_item_unit);
        }

        void setValue(float value) {
            txtValue.setText(value + "");
        }

        void setValue(int value) {
            txtValue.setText(value + "");
        }

        public void setName(String name) {
            txtName.setText(name);
        }

        void setUnit(String unit) {
            txtUnit.setText(unit);
        }

        void changeName(String name, String unit) {
            setName(name);
            setUnit(unit);
            setValue(0);
        }
    }



    private void scrollToSelectedItem() {
        int selectedItemIndex = mFitnessAdapter.getSelectedItemIndex();
        mFitnessLayoutManager.scrollToPositionWithOffset(selectedItemIndex - mVisibleItems/2, 0);
    }

    private void statisticsByType(String type) {
        mSelectedType = type;
        changeView();
        switch (type) {
            case DAYS:
                mHistoryFitnessManager.getInitFitnessPerDate(new HistoryFitnessManager.HistoryFitnessManagerListener() {
                    @Override
                    public void onSuccess() {
                        mFitnessAdapter.updateData(mHistoryFitnessManager.mFitnessPerDateDataSet);
                        mFitnessAdapter.setSelectedItem(HistoryFitnessManager.DISABLE_PER_DATE_OFFSET); //Select Item
                        scrollToSelectedItem();

                        updateFitnessDetailInfo(mFitnessAdapter.getSelectedItem());
                        getFitnessStepDetailByDate(mFitnessAdapter.getSelectedItem().mStartDate);
                    }
                });
                break;
            case WEEKS:
                mHistoryFitnessManager.getInitFitnessPerWeek(new HistoryFitnessManager.HistoryFitnessManagerListener() {
                    @Override
                    public void onSuccess() {
                        mFitnessAdapter.updateData(mHistoryFitnessManager.mFitnessPerWeekDataSet);
                        mFitnessAdapter.setSelectedItem(HistoryFitnessManager.DISABLE_PER_WEEK_OFFSET); //Select Item
                        scrollToSelectedItem();

                        updateFitnessDetailInfo(mFitnessAdapter.getSelectedItem());
                        onUpdateFitnessData();
                    }
                });
                break;
            case MONTHS:
                mHistoryFitnessManager.getInitFitnessPerMonths(new HistoryFitnessManager.HistoryFitnessManagerListener() {
                    @Override
                    public void onSuccess() {
                        mFitnessAdapter.updateData(mHistoryFitnessManager.mFitnessPerMonthDataSet);
                        mFitnessAdapter.setSelectedItem(HistoryFitnessManager.DISABLE_PER_MONTH_OFFSET); //Select Item
                        scrollToSelectedItem();

                        updateFitnessDetailInfo(mFitnessAdapter.getSelectedItem());
                        onUpdateFitnessData();
                    }
                });
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
