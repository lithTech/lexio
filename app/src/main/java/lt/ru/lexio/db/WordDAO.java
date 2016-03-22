package lt.ru.lexio.db;

import android.content.Context;
import android.database.Cursor;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;

/**
 * Created by lithTech on 15.03.2016.
 */
public class WordDAO extends EntityManager<Word> {

    public WordDAO(Context ctx) {
        super(Word.class, ctx);
    }

    public Cursor getAll(long dictId) {
        return select().where(Db.Word.DICTIONARY_ID, Is.EQUAL, dictId).orderBy(Db.Common.TITLE, true)
                .execute();
    }
}
