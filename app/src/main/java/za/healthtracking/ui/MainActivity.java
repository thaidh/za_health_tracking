package za.healthtracking.ui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.zing.pedometer.R;
import za.healthtracking.app.Settings;
import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.service.PedometerService;
import za.healthtracking.sleepdetectionlib.engine.EstimatedSleepItem;
import za.healthtracking.sleepdetectionlib.main.SleepDetection;
import za.healthtracking.sleepdetectionlib.main.SleepEstimationManager;
import za.healthtracking.sleepdetectionlib.service.SleepTrackerServiceManager;
import za.healthtracking.utils.Helper;
import za.healthtracking.utils.TimeHelper;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.txtSteps)
    TextView txtSteps;
    @BindView(R.id.txtDistance)
    TextView txtDistance;
    @BindView(R.id.txtCalories)
    TextView txtCalories;
    @BindView(R.id.chartDailyStep)
    CombinedChart chartDailyStep;

    //
    public static TextView txtUserState;
    public static TextView txtStepLength;
    public static TextView txtStepFrequency;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    boolean mIsBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        // Toolbar
        setSupportActionBar(toolbar);

        //
        txtUserState = (TextView) findViewById(R.id.txtUserState);
        txtStepLength = (TextView) findViewById(R.id.txtStepLength);
        txtStepFrequency = (TextView) findViewById(R.id.txtStepFrequency);

        // Check weight
        if (Settings.getUserProfileWeight() < 5) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        }

        // Bind UI with service
        doBindService();
    }


    private PedometerService mBoundService;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mBoundService != null) {
                txtDistance.setText(Helper.formatDistance(mBoundService.getDistance()));
                txtCalories.setText(Helper.formatCalories(mBoundService.getCalories()));
                txtSteps.setText(Helper.formatStep(mBoundService.getSteps()));

                // update chart
                updateChart(mBoundService.readHistoryFitnessPer20MinutesToDay());
            }

            sendMessageDelayed(Message.obtain(this, 1), Settings.UI_UPDATE_DELAY_IN_MS);
        }
    };

    private void updateChart(List<FitnessBucket> buckets) {
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
        chartDailyStep.getLegend().setEnabled(false);

        // Custom value for x-axis
        // the labels that should be drawn on the XAxis
        final String[] quarters = new String[] { "00:00", "06:00", "12:00", "18:00", "24:00" };
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int hour = (int)value;
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
        xAxis.setGridColor(Color.rgb(117, 117, 117));
        xAxis.setTextColor(Color.rgb(117, 117, 117));


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
        leftAxis.setEnabled(false);
        leftAxis.setGridColor(Color.rgb(117, 117, 117));
        leftAxis.setTextColor(Color.rgb(117, 117, 117));

        YAxis rightAxis = chartDailyStep.getAxisRight();
        rightAxis.setEnabled(false);


        // Set data
        CombinedData data = new CombinedData();
        data.setData(generateBarData(dataSet));

        // Current line
        int maxSteps = 0;
        for (FitnessBucket bucket : buckets) {
            if (maxSteps < bucket.nSteps)
                maxSteps = bucket.nSteps;
        }
        data.setData(generateLineData(maxSteps));

        // Bubble data
        data.setData(generateBubbleData());


        // Set combine data
        chartDailyStep.setData(data);

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

        //        chartDailyStep.setFitBars(true); // add half of the bar -> fully displayed not working??
//        chartDailyStep.setData(barData);

        chartDailyStep.invalidate(); // refresh
    }

    private BarData generateBarData(BarDataSet dataSet) {
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(10f); // set bar width in value 10/20
        barData.setDrawValues(false); // Hide values drawn on above bar

        return barData;
    }

    private LineData generateLineData(int maxSteps) {
        if (maxSteps < 50)
            maxSteps = 50;

        long now = Calendar.getInstance().getTimeInMillis();
        int nowInMinute = (int)((now - TimeHelper.getStartTimeOnThisDayTimestamp(now))/1000/60);

        LineData lineData = new LineData();

        // Current Time line
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(nowInMinute, 0));
        entries.add(new Entry(nowInMinute, maxSteps * 1.15f));

        LineDataSet currentTimeLine = new LineDataSet(entries, "Line DataSet");
        currentTimeLine.setCircleColorHole(Color.rgb(0, 0, 0));
        currentTimeLine.setColor(Color.rgb(0, 0, 0));
        currentTimeLine.setLineWidth(0.5f);
        currentTimeLine.setCircleColor(Color.rgb(0, 0, 0));
        currentTimeLine.setCircleRadius(2f);
        currentTimeLine.setMode(LineDataSet.Mode.LINEAR);
        currentTimeLine.setDrawValues(false);
        currentTimeLine.setAxisDependency(YAxis.AxisDependency.LEFT);

        // Add lines
        lineData.addDataSet(currentTimeLine);

        return lineData;
    }

    private BubbleData generateBubbleData() {
        BubbleData bd = new BubbleData();

        ArrayList<BubbleEntry> entries = new ArrayList<BubbleEntry>();

        for (int index = 0; index < 5; index++) {
            entries.add(new BubbleEntry(index*6 * 60, 10, 50));
        }

        BubbleDataSet set = new BubbleDataSet(entries, "Bubble DataSet");
        set.setColors(Color.rgb(0,0,0));
        set.setDrawValues(false);
        set.setHighlightCircleWidth(10f);
        set.setFormLineWidth(10f);
        bd.addDataSet(set);

        return bd;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((PedometerService.LocalBinder)service).getService();

            Intent serviceIntent = new Intent(getApplicationContext(), PedometerService.class);
            startService(serviceIntent);

            startService(new Intent(getApplicationContext(), SleepTrackerServiceManager.class));

            // Update UI
            handler.sendMessageDelayed(Message.obtain(handler, 0), 0);
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

    void doBindService() {
        bindService(new Intent(getApplicationContext(), PedometerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @OnClick(R.id.btnRunningActivity)
    void onStartIntentRunningActivity() {
//        Intent intent = new Intent(this, RunningActivity.class);
//        startActivity(intent);
//        SleepDetection.updateSleepTime();

        SleepEstimationManager.getInstance().startSleepEstimation();
        EstimatedSleepItem estimatedSleepItem = SleepEstimationManager.getInstance().getEstimatedSleepItem(System.currentTimeMillis());
//        if (estimatedSleepItem != null) {
//            Log.i("ZHEALTH", "Bed time: " + estimatedSleepItem.getBedTime() + "\nWake time: " + estimatedSleepItem.getWakeUpTime());
//        }
    }

    @OnClick(R.id.chartDailyStepContainer)
    void onFitnessHistory() {
        Intent intent = new Intent(this, FitnessTrackingTrendActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miSetting: {
                Intent intent = new Intent(this, UserProfileActivity.class);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    @OnClick(R.id.runningTrend)
    void onRunningTrend() {
        Intent intent = new Intent(this, RunningTrendActivity.class);
        startActivity(intent);
    }
}
