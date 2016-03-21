package lt.ru.lexio;

import android.content.Context;

import org.droidparts.AbstractDependencyProvider;
import org.droidparts.net.image.ImageFetcher;
import org.droidparts.persist.sql.AbstractDBOpenHelper;

import lt.ru.lexio.db.DbHelper;

/**
 * Created by lithTech on 15.03.2016.
 */
public class DependencyProvider extends AbstractDependencyProvider {

    private final DbHelper dbOpenHelper;
    private ImageFetcher imageFetcher;

    public DependencyProvider(Context ctx) {
        super(ctx);
        dbOpenHelper = new DbHelper(ctx);
    }

    @Override
    public AbstractDBOpenHelper getDBOpenHelper() {
        return dbOpenHelper;
    }

    public ImageFetcher getImageFetcher(Context ctx) {
        if (imageFetcher == null) {
            imageFetcher = new ImageFetcher(ctx);
            imageFetcher.clearCacheOlderThan(48);
        }
        return imageFetcher;
    }


}