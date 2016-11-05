package lt.ru.lexio.db;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

import java.util.Date;

/**
 * Created by lithTech on 15.03.2016.
 */
@Table(name = Db.Word.TABLE)
public class Word extends Entity {

    @Column(name = Db.Word.DICTIONARY_ID, eager = true, nullable = false)
    Dictionary dictionary;

    @Column(name = Db.Common.TITLE, nullable = false, unique = false)
    String title;

    @Column(name = Db.Word.TRANSCRIPTION, nullable = true, unique = false)
    String transcription;

    @Column(name = Db.Word.TRANSLATION, nullable = false)
    String translation;

    @Column(name = Db.Word.CONTEXT, nullable = true)
    String context;

    @Column(name = Db.Common.CREATE_DATE, nullable = false)
    Date created;

    @Column(name = Db.Common.MOD_DATE, nullable = true)
    Date lastModified;

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

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
        if (title != null){
            title = title.trim();
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }

        this.title = title;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        if (translation != null) {
            translation = translation.trim().toLowerCase();
        }

        this.translation = translation;
    }
}
