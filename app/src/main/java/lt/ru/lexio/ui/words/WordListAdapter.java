package lt.ru.lexio.ui.words;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lt.ru.lexio.R;

/**
 * Created by lithTech on 22.03.2016.
 */
public class WordListAdapter extends SimpleCursorAdapter{

    List<Holder> data = new ArrayList<>();
    Set<Integer> selectedWords = new HashSet<>();

    public Set<Integer> getSelectedWords() {
        return selectedWords;
    }

    public void setSelectedWords(Set<Integer> selectedWords) {
        this.selectedWords = selectedWords;
    }

    public WordListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to, 0);

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        CheckBox cb = (CheckBox) view.findViewById(R.id.cbWordChecked);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.cbWordChecked);
                if (cb.isChecked())
                    selectedWords.add(position);
                else
                    selectedWords.remove(position);
            }
        });
        cb.setChecked(selectedWords.contains(position));

        return view;
    }

    private class Holder {

    }
}
