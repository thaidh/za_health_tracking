package za.healthtracking.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.zing.pedometer.R;
import za.healthtracking.database.entities.RunningActivityLog;
import za.healthtracking.utils.Helper;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by Mai Thanh Hiep on 5/22/2017.
 */

public class RunningActivityRecyclerViewAdapter extends RecyclerView.Adapter<RunningActivityRecyclerViewAdapter.MyViewHolder> {

    private List<RunningActivityLog> mItems = new ArrayList<>();
    private MyClickListener myClickListener;
    private Context mContext;

    public RunningActivityRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_running_trend, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final RunningActivityLog item = getItem(position);

        holder.txtDuration.setText(Helper.formatDuration(TimeHelper.MillisToSecond(item.durationInMillis)));
        holder.txtDistance.setText(Helper.formatDistanceWithUnit(item.distanceInMeters));
        holder.txtStartTime.setText(Helper.formatHourMin(TimeHelper.SecondToMillis(item.startTime)));
    }

    public RunningActivityLog getItem(int pos) {
        if (pos < mItems.size())
            return mItems.get(pos);
        return null;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txtDuration)
        TextView txtDuration;
        @BindView(R.id.txtDistance)
        TextView txtDistance;
        @BindView(R.id.txtStartTime)
        TextView txtStartTime;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (myClickListener != null) {
                myClickListener.onItemClick(getPosition(), v);
            }
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public void updateData(List<RunningActivityLog> items) {
        mItems = items;

        notifyDataSetChanged();
    }
}
