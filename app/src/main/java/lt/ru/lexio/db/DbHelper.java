package lt.ru.lexio.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.droidparts.persist.sql.AbstractDBOpenHelper;
import org.droidparts.persist.sql.EntityManager;

import java.util.Date;

/**
 * Created by User on 15.03.2016.
 */
public class DbHelper extends AbstractDBOpenHelper {

    public static final int DB_VER = 3;

    public DbHelper(Context ctx) {
        super(ctx, "lexio.db", DB_VER);
    }

    public static int getDbVer() {
        return DB_VER;
    }

    @Override
    protected void onCreateTables(SQLiteDatabase db) {
        createTables(db, Dictionary.class);
        createTables(db, Word.class);

        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table';", null);
        while (cursor.moveToNext()) {
            String[] columnNames = cursor.getColumnNames();
            for (int i = 0; i < columnNames.length; i++) {
                String val = cursor.getString(i);
                System.out.println(val);
            }

        }
        System.out.println(cursor);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        addMissingColumns(db, Dictionary.class);
        addMissingColumns(db, Word.class);

        createIndex(db, Db.Word.TABLE, false, Db.Word.DICTIONARY_ID);
        createIndex(db, Db.Word.TABLE, true, Db.Common.TITLE);
    }
}
