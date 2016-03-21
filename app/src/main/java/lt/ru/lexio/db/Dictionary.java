package lt.ru.lexio.db;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

import java.util.Date;

/**
 * Created by lithTech on 15.03.2016.
 */
@Table(name = Db.Dictionary.TABLE)
public class Dictionary extends Entity {

    @Column(name = Db.Common.TITLE, nullable = false, unique = true)
    String title;
    @Column(name = Db.Common.DESC)
    String desc;
    @Column(name = Db.Dictionary.LANG)
    String language;
    @Column(name = Db.Dictionary.WORDS_CNT)
    int words;
    @Column(name = Db.Common.MOD_DATE)
    Date lastModified;

    @Column(name = Db.Dictionary.ACTIVE)
    int active = 0;

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public Dictionary() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null) title = title.trim();
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        if (desc != null) desc = desc.trim();
        this.desc = desc;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

}
