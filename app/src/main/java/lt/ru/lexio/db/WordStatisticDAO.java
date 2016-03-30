package lt.ru.lexio.db;

import android.content.Context;
import android.database.Cursor;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Where;

/**
 * Created by lithTech on 27.03.2016.
 */
public class WordStatisticDAO extends EntityManager<WordStatistic> {
    public WordStatisticDAO(Context ctx) {
        super(WordStatistic.class, ctx);
    }

    public Cursor getTrainSessions() {
        return select().where(Db.WordStatistic.TRAINING_SESSION_ID, Is.NULL, null).execute();
    }

    public Cursor getTrainStatisticsBySession(long sessionId) {
        Where where = new Where(Db.WordStatistic.TRAINING_SESSION_ID, Is.EQUAL, sessionId);
        where = where.or(Db.Common.ID, Is.EQUAL, sessionId);
        return select().where(where).execute();
    }

    public Cursor getTrainStatisticsByWord(long wordId) {
        return select().where(Db.WordStatistic.WORD_ID, Is.EQUAL, wordId).execute();
    }

    public Cursor getTrainStatisticsByWord(long wordId, int trainType) {
        return select().where(Db.WordStatistic.WORD_ID, Is.EQUAL, wordId).execute();
    }

    public void clear() {
        getDB().execSQL("delete from " + Db.WordStatistic.TABLE);
    }

}
