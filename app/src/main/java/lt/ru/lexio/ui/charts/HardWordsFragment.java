package lt.ru.lexio.ui.charts;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.ContentFragment;

/**
 * Created by lithTech on 13.04.2016.
 */
public class HardWordsFragment extends ContentFragment {

    WordStatisticDAO wordStatisticDAO;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        wordStatisticDAO = new WordStatisticDAO(view.getContext());

        BarChart barChart = (BarChart) view.findViewById(R.id.chart_hard_words);
        barChart.setDrawBarShadow(false);
        barChart.setHorizontalScrollBarEnabled(true);
        barChart.setData(getData());
        barChart.animateXY(1000, 2000);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getViewPortHandler().setMinimumScaleX(3.0f);
        barChart.getXAxis().setLabelRotationAngle(90);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setDrawLabels(false);

        return view;
    }

    public BarData getData() {
        long dictId = mainActivity.getCurrentDictionary().id;
        StringBuilder q = new StringBuilder();
        cookStatQuery(q);

        Cursor c = wordStatisticDAO.query(q.toString(), new String[]{String.valueOf(dictId)});
        Map<String, Integer> xAxis = new HashMap<>();
        List<BarEntry> barIncorrectWords = new ArrayList<>();
        List<BarEntry> barCorrectWords = new ArrayList<>();
        List<IBarDataSet> bars = new ArrayList<>();
        int wordNum = 0;
        while (c.moveToNext()) {
            String word = c.getString(c.getColumnIndex(Db.Common.TITLE));
            int iCount = c.getInt(c.getColumnIndex("I_CNT"));
            int rCount = c.getInt(c.getColumnIndex("R_CNT"));
            Integer index = xAxis.get(word);
            if (index == null) {
                index = wordNum;
                xAxis.put(word, wordNum++);
            }
            BarEntry entry = new BarEntry(iCount, index);
            barIncorrectWords.add(entry);
            entry = new BarEntry(rCount, index);
            barCorrectWords.add(entry);
        }
        BarDataSet barDS = new BarDataSet(barIncorrectWords,
                getResources().getString(R.string.chart_HardWords_IncorrectLabel));
        barDS.setColors(new int[]{getResources().getColor(R.color.colorAccent)});
        bars.add(barDS);

        barDS = new BarDataSet(barCorrectWords,
                getResources().getString(R.string.chart_HardWords_CorrectLabel));
        barDS.setColors(new int[]{getResources().getColor(R.color.correctAnswer)});
        bars.add(barDS);
        return new BarData(new ArrayList<>(xAxis.keySet()), bars);
    }

    private void cookStatQuery(StringBuilder q) {
        q.append("select * from(")
                .append("select ")
                .append("sum(case when IFNULL(s." + Db.WordStatistic.TRAINING_RESULT + ",1)")
                .append(" = 0 then 1 else 0 end) as I_CNT")
                .append(",sum(case when IFNULL(s." + Db.WordStatistic.TRAINING_RESULT + ",0)")
                .append(" = 1 then 1 else 0 end) as R_CNT")
                .append(",w.").append(Db.Common.TITLE)
                .append(",w.").append(Db.Common.ID)
                .append(" from ").append(Db.WordStatistic.TABLE).append(" s ")
                .append("join ").append(Db.Word.TABLE).append(" w ")
                .append("on s.").append(Db.WordStatistic.WORD_ID).append(" = ").append("w.")
                .append(Db.Common.ID)
                .append(" and w.").append(Db.Word.DICTIONARY_ID).append(" = ? ")
                .append("group by w.").append(Db.Common.TITLE).append(",")
                .append("w.").append(Db.Common.ID)
                .append(")")
                .append("where I_CNT > 0 ")
                .append("order by I_CNT desc,R_CNT asc, " + Db.Common.TITLE)
                .append(" limit ").append(40);
    }

}
