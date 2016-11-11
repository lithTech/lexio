package lt.ru.lexio.ui.training;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import lt.ru.lexio.R;

/**
 * Created by lithTech on 27.08.2016.
 */
public class EndPageStatAdapter extends ArrayAdapter<EndPageStatistic> {


    List<EndPageStatistic> statistics;

    public EndPageStatAdapter(Context context, int resource, int textViewResourceId, List<EndPageStatistic> objects) {
        super(context, resource, textViewResourceId, objects);

        statistics = objects;
    }

    public List<EndPageStatistic> getStatistics() {
        return statistics;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        EndPageStatistic stat = getItem(position);

        TextView question = (TextView) view.findViewById(R.id.tvQuestion);
        TextView correctAnswer = (TextView) view.findViewById(R.id.tvCorrectAnswer);
        //TextView incorrectAnswer = (TextView) view.findViewById(R.id.tvIncorrectAnswer);
        ImageView status = (ImageView) view.findViewById(R.id.ivAnswerStatus);

        question.setText(stat.question);
        correctAnswer.setText(stat.correctAnswer);
        //incorrectAnswer.setText(stat.incorrectAnswer);

        if (stat.isCorrect != null) {
            if (stat.isCorrect) {
                status.setBackground(view.getResources().getDrawable(R.drawable.ic_menu_check));
            } else {
                status.setBackground(view.getResources().getDrawable(R.drawable.ic_menu_close_2));
            }
        }

        return view;
    }
}
