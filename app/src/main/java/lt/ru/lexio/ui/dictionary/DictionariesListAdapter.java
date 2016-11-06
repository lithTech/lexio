package lt.ru.lexio.ui.dictionary;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;

/**
 * Created by lithTech on 20.03.2016.
 */
public class DictionariesListAdapter extends SimpleCursorAdapter {

    Set<Integer> selectedItems = new HashSet<>();
    List<Holder> backInfo = new ArrayList<>();

    public Set<Integer> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(Set<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public List<Holder> getBackInfo() {
        return backInfo;
    }

    public void setBackInfo(List<Holder> backInfo) {
        this.backInfo = backInfo;
    }

    public DictionariesListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to, 0);

        for (int i = 0; i < this.getCount(); i++) {
            backInfo.add(i, new Holder(false, false));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        if (cursor.getInt(cursor.getColumnIndex(Db.Dictionary.ACTIVE)) == 1) {
            backInfo.get(cursor.getPosition()).active = true;
        }

        return view;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        boolean init = convertView == null;
        View view = super.getView(position, convertView, parent);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbDictionary);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.cbDictionary);
                backInfo.get(position).checked = cb.isChecked();
                if (cb.isChecked())
                    selectedItems.add(position);
                else
                    selectedItems.remove(position);
            }
        });
        checkBox.setChecked(backInfo.get(position).checked);

        ImageView iv = (ImageView) view.findViewById(R.id.ivActive);
        iv.setImageDrawable(null);
        if (backInfo.get(position).active)
            iv.setImageResource(R.drawable.ic_menu_flags);

        return view;
    }

    private class Holder {
        public boolean checked;
        public boolean active;

        public Holder(boolean checked, boolean active) {
            this.checked = checked;
            this.active = active;
        }
    }
}
