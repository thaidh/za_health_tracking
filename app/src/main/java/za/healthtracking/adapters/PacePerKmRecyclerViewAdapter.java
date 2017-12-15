package za.healthtracking.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.zing.pedometer.R;
import za.healthtracking.models.PacePerKm;
import za.healthtracking.utils.Helper;

/**
 * Created by Mai Thanh Hiep on 5/22/2017.
 */

public class PacePerKmRecyclerViewAdapter extends RecyclerView.Adapter<PacePerKmRecyclerViewAdapter.MyViewHolder> {

    private List<PacePerKm> mItems = new ArrayList<>();
    private MyClickListener myClickListener;
    private Context mContext;
    float MAX_PACE = 0;

    public PacePerKmRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pace_per_km, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final PacePerKm item = getItem(position);

        holder.txtDistance.setText(Helper.formatPacePerKmDistanceClean(item.distanceInMeters /1000f + position));
        holder.txtPace.setText(Helper.formatPace(item.getPace()));

        ViewTreeObserver vto = holder.shapePaceContainer.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.shapePaceContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int MAX_WIDTH  = holder.shapePaceContainer.getMeasuredWidth();
                int widthPx = (int)((item.getPace()/MAX_PACE) * MAX_WIDTH);
                ViewGroup.LayoutParams params = holder.shapePace.getLayoutParams();
                params.width = widthPx;
                holder.shapePace.setLayoutParams(params);
            }
        });
    }

    public PacePerKm getItem(int pos) {
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
        @BindView(R.id.txtDistance)
        TextView txtDistance;
        @BindView(R.id.txtPace)
        TextView txtPace;
        @BindView(R.id.shapePace)
        View shapePace;
        @BindView(R.id.shapePaceContainer)
        View shapePaceContainer;

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

    public void updateData(List<PacePerKm> items) {
        mItems = items;

        for (PacePerKm pacePerKm : items) {
            if (pacePerKm.getPace() > MAX_PACE)
                MAX_PACE = pacePerKm.getPace();
        }
        MAX_PACE /= 0.85;

        notifyDataSetChanged();
    }
}
