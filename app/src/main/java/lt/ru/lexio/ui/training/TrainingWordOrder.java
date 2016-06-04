package lt.ru.lexio.ui.training;

import lt.ru.lexio.R;

/**
 * Created by lithTech on 06.05.2016.
 */
public enum TrainingWordOrder {
    NEW(R.string.training_Order_New, "IFNULL(last_trained_date,0) asc, TITLE"),
    OLD(R.string.training_Order_Old, "IFNULL(last_trained_date, DATE('now','+1 day')) asc, TITLE"),
    HARD(R.string.training_Order_Hard, "R_CNT - I_CNT, TITLE");

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
