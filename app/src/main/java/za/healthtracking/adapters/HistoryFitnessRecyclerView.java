package za.healthtracking.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vn.zing.pedometer.R;
import za.healthtracking.models.FitnessBucket.BaseFitnessBucket;

/**
 * Created by hiepmt on 23/05/2017.
 */

public class HistoryFitnessRecyclerView extends RecyclerView.Adapter<HistoryFitnessRecyclerView.DataObjectHolder> {
    private List<BaseFitnessBucket> mItems = new ArrayList<>();
    private MyClickListener mMyClickListener;
    private Context mContext;
    private int mSelectedItemIndex = -1;

    public void setSelectedItem(int selectedItem) {
        if (mSelectedItemIndex != selectedItem) {
            mSelectedItemIndex = selectedItem;
            notifyDataSetChanged();
        }
    }

     class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout stepHighLayout;
        TextView label;
        View viewCircleBackground;

        DataObjectHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.label);
            stepHighLayout = (LinearLayout) itemView.findViewById(R.id.stepHigh);
            viewCircleBackground = itemView.findViewById(R.id.viewCircleBackground);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mMyClickListener != null)
                mMyClickListener.onItemClick(getPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        mMyClickListener = myClickListener;
    }

    public HistoryFitnessRecyclerView(Context context) {
        mContext = context;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_steps, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        BaseFitnessBucket item = getItem(position);

        // Change label
        holder.label.setText(item.getLabel());

        // Change height
        int MAX_HEIGHT = 200; //dp
        int heightDp = (int)((float)item.nSteps/ BaseFitnessBucket.MAX_STEP * MAX_HEIGHT);

        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, mContext.getResources().getDisplayMetrics());
        ViewGroup.LayoutParams params = holder.stepHighLayout.getLayoutParams();
        params.height = heightPx;
        holder.stepHighLayout.setLayoutParams(params);

        // Change label text color
        if (position == mSelectedItemIndex) {
            holder.label.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
            holder.viewCircleBackground.setVisibility(View.VISIBLE);
        } else {
            holder.viewCircleBackground.setVisibility(View.INVISIBLE);

            // Check disable
            if (item.mIsEnable) {
                holder.label.setTextColor(ContextCompat.getColor(mContext, R.color.primaryText));
            } else {
                holder.label.setTextColor(ContextCompat.getColor(mContext, R.color.stepHistoryDisableLabel));
            }
        }
    }

    public BaseFitnessBucket getItem(int pos) {
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

    public void updateData(List<BaseFitnessBucket> items) {
        mItems = items;
        notifyDataSetChanged();
    }
    public int getSelectedItemIndex() {
        return mSelectedItemIndex;
    }

    public BaseFitnessBucket getSelectedItem() {
        return mItems.get(mSelectedItemIndex);
    }
}
