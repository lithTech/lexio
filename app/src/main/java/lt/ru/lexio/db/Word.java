package lt.ru.lexio.db;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

/**
 * Created by lithTech on 15.03.2016.
 */
@Table(name = Db.Word.TABLE)
public class Word extends Entity {

    @Column(name = Db.Word.DICTIONARY_ID, eager = true, nullable = false)
    Dictionary dictionary;

    @Column(name = Db.Common.TITLE, nullable = false, unique = true)
    String title;

    @Column(name = Db.Word.TRANSLATION, nullable = false)
    String translation;

    @Column(name = Db.Word.CONTEXT, nullable = true)
    String context;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null) title = title.trim();
        this.title = title;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
