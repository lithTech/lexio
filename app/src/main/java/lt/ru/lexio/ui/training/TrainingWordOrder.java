package lt.ru.lexio.ui.training;

import lt.ru.lexio.R;

/**
 * Created by lithTech on 06.05.2016.
 */
public enum TrainingWordOrder {
    //order by: by most recently created words
    NEW(R.string.training_Order_New, "CREATE_DATE desc, TITLE"),
    //order by: by most old trained words, if word haven't trained, then sort by creation (older)
    OLD(R.string.training_Order_Old, "IFNULL(last_trained_date, CREATE_DATE), TITLE"),
    //order by: the most erroneus words, then most old trained word, old created word, and by title
    HARD(R.string.training_Order_Hard, "I_CNT - R_CNT desc, IFNULL(last_trained_date, CREATE_DATE), TITLE");

    private int stringResTitleId;
    private String sqlOrder;

    public int getStringResTitleId() {
        return stringResTitleId;
    }

    public String getSqlOrder() {
        return sqlOrder;
    }

    TrainingWordOrder(int stringResTitleId, String sqlOrderExpression) {
        this.stringResTitleId = stringResTitleId;
        this.sqlOrder = sqlOrderExpression;
    }
}
