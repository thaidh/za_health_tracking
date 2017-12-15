package za.healthtracking.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.models.FitnessDateDetail;

/**
 * Created by hiepmt on 25/05/2017.
 */

public class HistoryFitnessPerDateManager {
    private List<FitnessDateDetail> listFitnessDateDetail = new ArrayList<>();
    private HistoryDataManager mHistoryDataManager;

    public interface HistoryFitnessPerDateManagerListener {
        void onData(FitnessDateDetail data);
    }

    public HistoryFitnessPerDateManager() {
        mHistoryDataManager = new HistoryDataManager();
    }

    public void getData(final Date date, final HistoryFitnessPerDateManagerListener listener) {
        for (int i = 0; i < listFitnessDateDetail.size(); i++) {
            if (listFitnessDateDetail.get(i).matchDate(date)) {
                listener.onData(listFitnessDateDetail.get(i));
                return;
            }
        }

        List<FitnessBucket> buckets = mHistoryDataManager.getFitnessBucketsPer20MinForADay(date.getTime());
        FitnessDateDetail fitnessDateDetail = new FitnessDateDetail(date, buckets);
        listFitnessDateDetail.add(fitnessDateDetail);
        listener.onData(fitnessDateDetail);
    }
}
