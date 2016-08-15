package lt.ru.lexio.db;

/**
 * Created by lithTech on 15.03.2016.
 */
public interface Db {
    public static final int VER = 8;

    public static interface Common {
        public static final String TITLE = "TITLE";
        public static final String DESC = "DESC";
        public static final String MOD_DATE = "MOD_DATE";
        public static final String ID = "_id";
        public static final String CREATE_DATE = "CREATE_DATE";
    }

    public static interface Dictionary{
        public static final String TABLE = "DICTIONARIES";
        public static final String LANG = "LANG";
        public static final String WORDS_CNT = "WORDS_CNT";
        public static final String ACTIVE = "ACTIVE";
    }

    public static interface Word{
        public static final String TABLE = "WORDS";
        public static final String TRANSLATION = "TRANSLATION";
        public static final String DICTIONARY_ID = "DICT_ID";
        public static final String CONTEXT = "CONTEXT";
        public static final String TRANSCRIPTION = "TRANSCRIPTION";
    }

    public static interface WordStatistic{
        public static final String TABLE = "WORD_STAT";
        public static final String WORD_ID = "WORD_ID";
        public static final String TRAINED_ON = "TRAINED_ON_DATE";
        public static final String TRAINING_RESULT = "TRAINING_RES";
        public static final String TRAINING_TYPE = "TRAINING_TYPE";
        public static final String TRAINING_SESSION_ID = "TRAINING_SESS";

    }

}
