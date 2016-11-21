package lt.ru.lexio.ui.words;

import android.content.Context;
import android.database.Cursor;
import android.view.ContextMenu;
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

    Set<Integer> selectedWords = new HashSet<>();
    List<Holder> backInfo = new ArrayList<>();
    int wordProgressFactor;

    public Set<Integer> getSelectedWords() {
        return selectedWords;
    }

    public void setSelectedWords(Set<Integer> selectedWords) {
        this.selectedWords = selectedWords;
    }

    public WordListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int wordProgressFactor) {
        super(context, layout, c, from, to, 0);

        for (int i = 0; i < this.getCount(); i++) {
            backInfo.add(new Holder(false, 0));
        }

        this.wordProgressFactor = wordProgressFactor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);

        return view;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        CheckBox cb = (CheckBox) view.findViewById(R.id.cbWordChecked);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.cbWordChecked);
                backInfo.get(position).checked = cb.isChecked();
                if (cb.isChecked())
                    selectedWords.add(position);
                else
                    selectedWords.remove(position);
            }
        });
        cb.setChecked(backInfo.get(position).checked);

        return view;
    }


    private class Holder {
        public boolean checked;
        public int progress;

        public Holder(boolean checked, int progress) {
            this.checked = checked;
            this.progress = progress;
        }
    }
}
