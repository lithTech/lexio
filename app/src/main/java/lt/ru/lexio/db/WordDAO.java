package lt.ru.lexio.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Select;
import org.droidparts.persist.sql.stmt.Where;

import java.util.ArrayList;
import java.util.List;

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

    public List<Word> getWordsWithoutIPA(long dictId) {
        Where where = new Where(Db.Word.DICTIONARY_ID, Is.EQUAL, dictId);
        where.and(Db.Word.TRANSCRIPTION, Is.NULL);
        Select<Word> select = select().where(where);

        return readAll(select);
    }

    public Cursor getAllFiltered(long dictId, String titleFilter) {
        Where where = new Where("UPPER(" + Db.Common.TITLE + ")", Is.LIKE, titleFilter);
        where = where.or("UPPER(" + Db.Word.TRANSLATION + ")", Is.LIKE, titleFilter);

        return select().where(where)
                .orderBy(Db.Common.TITLE, true).execute();
    }

    public Cursor execComplexSql(String sql, String[] args) {
        return getDB().rawQuery(sql, args);
    }

    public void copyWord(long dictId, long wordId) {
        Word newWord = this.read(wordId);
        newWord.id = 0;
        Dictionary d = new Dictionary();
        d.id = dictId;
        newWord.setDictionary(d);
        create(newWord);
    }

    public void moveWord(long dictId, long wordId) {
        Word word = read(wordId);
        long oldDict = word.getDictionary().id;
        word.getDictionary().id = dictId;
        getDB().beginTransaction();
        try {
            update(word);
            incWords(oldDict, "-");
            incWords(dictId, "+");

            getDB().setTransactionSuccessful();
        }finally {
            getDB().endTransaction();
        }
    }
}
