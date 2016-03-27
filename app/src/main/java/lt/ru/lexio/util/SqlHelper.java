package lt.ru.lexio.util;

import android.database.Cursor;

/**
 * Created by lithTech on 27.03.2016.
 */
public class SqlHelper
{

    public static void dumpCursor(Cursor cursor) {
        int r = 0;
        while (cursor.moveToNext()) {
            System.out.println("-----Row "+(++r)+"------");
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                System.out.println(cursor.getColumnName(i) + " = " + cursor.getString(i));
            }
        }
    }

}
