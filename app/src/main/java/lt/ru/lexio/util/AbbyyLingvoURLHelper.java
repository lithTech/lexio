package lt.ru.lexio.util;

import java.util.Locale;

/**
 * Created by lithTech on 21.08.2016.
 */

public class AbbyyLingvoURLHelper {

    public static String getUrl(String dictionaryLanguageTag, String word) {
        String defLoc = Locale.getDefault().getLanguage();
        if (dictionaryLanguageTag.contains("-")) {
            dictionaryLanguageTag = dictionaryLanguageTag.substring(0, dictionaryLanguageTag.indexOf("-"));
        }
        String lang = "en-en";
        if (defLoc.equalsIgnoreCase("ru"))
            lang = "ru-ru";
        return "http://www.lingvolive.com/"+lang+"/translate/" + dictionaryLanguageTag +
                "-" + defLoc + "/" + word;
    }

}
