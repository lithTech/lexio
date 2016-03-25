package lt.ru.lexio.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;

/**
 * Created by lithTech on 15.03.2016.
 */
public class WordDAO extends EntityManager<Word> {

    public WordDAO(Context ctx) {
        super(Word.class, ctx);
    }

    private void incWords(long dictId, String plusOrMinus) {
        getDB().execSQL("UPDATE " + Db.Dictionary.TABLE + " SET " + Db.Dictionary.WORDS_CNT +
                        " = " + Db.Dictionary.WORDS_CNT + " " + plusOrMinus + " 1 WHERE " + Db.Common.ID + " = ?",
                new Object[]{dictId});
    }

    @Override
    public boolean create(Word item) {
        getDB().beginTransaction();
        try {
            boolean r = super.create(item);
            long dictId = item.getDictionary().id;
            incWords(dictId, "+");
            getDB().setTransactionSuccessful();
            return r;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw  e;
        }
        finally {
            getDB().endTransaction();
        }
    }

    @Override
    public boolean delete(long id) {
        getDB().beginTransaction();
        try {
            Cursor dictCur = select().columns(Db.Word.DICTIONARY_ID).where(Db.Common.ID, Is.EQUAL, id)
                    .execute();
            dictCur.moveToNext();
            long dictId = dictCur.getLong(dictCur.getColumnIndex(Db.Word.DICTIONARY_ID));
            boolean r = super.delete(id);
            incWords(dictId, "-");
            getDB().setTransactionSuccessful();
            return r;
        } finally {
            getDB().endTransaction();
        }
    }

    public Cursor getAll(long dictId) {
        return select().where(Db.Word.DICTIONARY_ID, Is.EQUAL, dictId).orderBy(Db.Common.TITLE, true)
                .execute();
    }
}
