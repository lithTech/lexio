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

        createIndex(db, Db.Word.TABLE, false, Db.Word.DICTIONARY_ID);
        createIndex(db, Db.Word.TABLE, true, Db.Common.TITLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        addMissingColumns(db, Dictionary.class);
        addMissingColumns(db, Word.class);


        createIndex(db, Db.Word.TABLE, false, Db.Word.DICTIONARY_ID);
        createIndex(db, Db.Word.TABLE, true, Db.Common.TITLE);
    }
}
