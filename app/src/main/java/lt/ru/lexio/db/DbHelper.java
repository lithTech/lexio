package lt.ru.lexio.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.droidparts.persist.sql.AbstractDBOpenHelper;

/**
 * Created by User on 15.03.2016.
 */
public class DbHelper extends AbstractDBOpenHelper {

    public DbHelper(Context ctx) {
        super(ctx, "lexio.db", Db.VER);
    }

    public static int getDbVer() {
        return Db.VER;
    }

    @Override
    protected void onCreateTables(SQLiteDatabase db) {
        createTables(db, Dictionary.class);
        createTables(db, Word.class);
        createTables(db, WordStatistic.class);

        createIndex(db, Db.Word.TABLE, false, Db.Word.DICTIONARY_ID);
        //createIndex(db, Db.Word.TABLE, true, "UPPER(" + Db.Common.TITLE + ")");
        //createIndex(db, Db.Word.TABLE, true, "UPPER(" + Db.Word.TRANSLATION + ")");

        createIndex(db, Db.WordStatistic.TABLE, false, Db.WordStatistic.WORD_ID, Db.WordStatistic.TRAINING_TYPE);
        createIndex(db, Db.WordStatistic.TABLE, false, Db.WordStatistic.TRAINING_SESSION_ID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
