package lt.ru.lexio.ui.training;

import lt.ru.lexio.R;

/**
 * Created by lithTech on 06.05.2016.
 */
public enum TrainingWordOrder {
    //order by: by most recently created words
    NEW(R.string.training_Order_New, R.string.training_Order_New_Descr, "CREATE_DATE desc, TITLE"),
    //order by: by most old trained words, if word haven't trained, then sort by creation (older)
    OLD(R.string.training_Order_Old, R.string.training_Order_Old_Descr, "IFNULL(last_trained_date, CREATE_DATE), TITLE"),
    //order by: the most erroneus words, then most old trained word, old created word, and by title
    HARD(R.string.training_Order_Hard, R.string.training_Order_Hard_Descr, "I_CNT - R_CNT desc, IFNULL(last_trained_date, CREATE_DATE), TITLE"),
    //order by: the most erroneus words, then most old trained word, old created word, and by title
    RANDOMIZE(R.string.training_Order_Random, R.string.training_Order_Random_Descr, "RANDOM(), TITLE");

    private int stringResTitleId;
    private int stringResDescrId;
    private String sqlOrder;

    public int getStringResTitleId() {
        return stringResTitleId;
    }


    public int getStringResDescrId() {
        return stringResDescrId;
    }

    public String getSqlOrder() {
        return sqlOrder;
    }

    TrainingWordOrder(int stringResTitleId, int stringResDescrId, String sqlOrderExpression) {
        this.stringResTitleId = stringResTitleId;
        this.sqlOrder = sqlOrderExpression;
        this.stringResDescrId = stringResDescrId;
    }
}
