package lt.ru.lexio.ui.words;

import android.content.Context;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Word;

/**
 * Created by lithTech on 22.03.2016.
 */
public class WordListAdapter extends SimpleCursorAdapter{

    Set<Long> selectedWords = new HashSet<>();
    List<Holder> backInfo = new ArrayList<>();
    int wordProgressFactor;

    public Set<Long> getSelectedWords() {
        return selectedWords;
    }

    public void setSelectedWords(Set<Long> selectedWords) {
        this.selectedWords = selectedWords;
    }

    public WordListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int wordProgressFactor) {
        super(context, layout, c, from, to, 0);

        for (int i = 0; i < this.getCount(); i++) {
            backInfo.add(new Holder(false, -1));
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
                Cursor c = (Cursor) getItem(position);
                long id = c.getLong(c.getColumnIndex(Db.Common.ID));
                if (cb.isChecked())
                    selectedWords.add(id);
                else
                    selectedWords.remove(id);
            }
        });
        cb.setChecked(backInfo.get(position).checked);

        int progress = backInfo.get(position).progress;
        ProgressBar pg = (ProgressBar) view.findViewById(R.id.pbWordProgress);
        if (progress == -1) {
            Cursor cur = getCursor();
            int correct = cur.getInt(cur.getColumnIndex("R_CNT"));
            int incorrect = cur.getInt(cur.getColumnIndex("I_CNT"));
            if (correct > incorrect)
                progress = (int)Math.round(((double) (correct - incorrect) / wordProgressFactor) * 100.0);
            if (progress > 100) progress = 100;
            backInfo.get(position).progress = progress;
        }
        pg.setProgress(progress);

        return view;
    }

    public long getIdByPosition(int pos) {
        Cursor c = (Cursor) getItem(pos);
        return c.getLong(c.getColumnIndex(Db.Common.ID));
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
