package lt.ru.lexio.ui.words;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

/**
 * Created by lithTech on 22.03.2016.
 */
public class WordListAdapter extends SimpleCursorAdapter{

    public WordListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to, 0);
    }


}
