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

    public void startTrans() {
        getDB().beginTransaction();
    }

    public void transactionSuccessful() {
        getDB().setTransactionSuccessful();
    }

    public void endTrans() {
        getDB().endTransaction();
    }


    @Override
    public boolean create(Word item) {
        getDB().beginTransaction();
        try {
            boolean r = super.create(item);
            if (r) {
                long dictId = item.getDictionary().id;
                incWords(dictId, "+");
            }
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

    public Cursor getWordsWithProgress(long dictId, String titleFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from (")
                .append("select w._id as _id,")
                .append("w.title as TITLE,")
                .append("w.translation as TRANSLATION,")
                .append("w.transcription as TRANSCRIPTION,")
                .append("w.context as CONTEXT,")
                .append("w.CREATE_DATE as CREATE_DATE,")
                .append("sum(case when IFNULL(ws.training_res,1) = 0 then 1 else 0 end) as I_CNT,")
                .append("sum(case when IFNULL(ws.training_res,0) = 1 then 1 else 0 end) as R_CNT,")
                .append("max(ws.TRAINED_ON_DATE) as last_trained_date ")
                .append("from WORDS w ")
                .append("left join WORD_STAT ws ")
                .append("on w._id = ws.WORD_ID ");
        sql.append("where w.dict_id = ? ");

        String arg[];
        if (titleFilter != null && !titleFilter.isEmpty()) {
            sql.append("and (upper(w.title) like ? or upper(w.translation) like ?) ");
            arg = new String[]{String.valueOf(dictId), titleFilter, titleFilter};
        }
        else
            arg = new String[]{String.valueOf(dictId)};
        sql.append("group by w._id,w.title,w.translation,w.transcription,w.context,w.CREATE_DATE")
                .append(")")
                .append("order by TITLE");

        return execComplexSql(sql.toString(), arg);
    }

    public List<Word> getWordsWithoutIPA(long dictId) {
        Where where = new Where(Db.Word.DICTIONARY_ID, Is.EQUAL, dictId);
        where.and(Db.Word.TRANSCRIPTION, Is.NULL);
        Select<Word> select = select().where(where);

        return readAll(select);
    }

    public Cursor getAllFiltered(long dictId, String titleFilter) {
        Where main = new Where(Db.Word.DICTIONARY_ID, Is.EQUAL, dictId);
        Where where = new Where("UPPER(" + Db.Common.TITLE + ")", Is.LIKE, titleFilter);
        where = where.or("UPPER(" + Db.Word.TRANSLATION + ")", Is.LIKE, titleFilter);

        main.and(where);

        return select().where(main)
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
        if (dictId == oldDict) return;
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
