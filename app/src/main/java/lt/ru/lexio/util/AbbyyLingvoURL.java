package lt.ru.lexio.util;

import java.util.Locale;

/**
 * Created by lithTech on 21.08.2016.
 */

public class AbbyyLingvoURL {

    public static String getUrl(String dictionaryLanguageTag, String word) {
        String defLoc = Locale.getDefault().getLanguage();
        if (dictionaryLanguageTag.contains("-")) {
            dictionaryLanguageTag = dictionaryLanguageTag.substring(0, dictionaryLanguageTag.indexOf("-"));
        }
        String domen = "com";
        if (defLoc.equalsIgnoreCase("ru"))
            domen = "ru";
        return "http://www.lingvo-online." + domen + "/Translate/" + dictionaryLanguageTag +
                "-" + defLoc + "/" + word;
    }

}
