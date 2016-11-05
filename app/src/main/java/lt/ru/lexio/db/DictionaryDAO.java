package lt.ru.lexio.db;

import android.content.Context;
import android.database.Cursor;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Select;

import java.util.Date;

import lt.ru.lexio.util.SqlHelper;

/**
 * Created by lithTech on 15.03.2016.
 */
public class DictionaryDAO extends EntityManager<Dictionary> {
    public DictionaryDAO(Context ctx) {
        super(Dictionary.class, ctx);
    }

    public Select<Dictionary> getAll() {
        return select().orderBy(Db.Common.TITLE, true);
    }

    public Select<Dictionary> getActive() {
        return select().where(Db.Dictionary.ACTIVE, Is.EQUAL, 1);
    }

    public Dictionary setActive(long id) {
        getDB().beginTransaction();
        Dictionary c = read(id);
        try {
            Dictionary l = null;
            Cursor lastCur = getActive().execute();
            if (lastCur.moveToNext())
                l = read(lastCur.getLong(lastCur.getColumnIndex(Db.Common.ID)));
            c.setActive(1);
            if (l != null) {
                l.setActive(0);
                update(l);
            }
            update(c);
            getDB().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            getDB().endTransaction();
        }
        return c;
    }

    public void importWord(long dictId, String sqlInsert, Date date) {
        getDB().execSQL(sqlInsert, new Object[]{dictId, date});
    }

}
