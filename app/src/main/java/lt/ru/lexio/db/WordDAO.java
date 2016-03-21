package lt.ru.lexio.db;

import android.content.Context;

import org.droidparts.persist.sql.EntityManager;

/**
 * Created by lithTech on 15.03.2016.
 */
public class WordDAO extends EntityManager<Word> {

    public WordDAO(Context ctx) {
        super(Word.class, ctx);
    }


}
