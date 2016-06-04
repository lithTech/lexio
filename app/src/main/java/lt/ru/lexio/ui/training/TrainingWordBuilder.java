package lt.ru.lexio.ui.training;

import android.database.Cursor;

import org.droidparts.persist.sql.stmt.Is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.util.SqlHelper;

/**
 * Created by lithTech on 27.03.2016.
 */
public class TrainingWordBuilder {

    WordDAO wordDAO;
    long dictId;

    public TrainingWordBuilder(WordDAO wordDAO, long dictId) {
        this.wordDAO = wordDAO;
        this.dictId = dictId;
    }

    public List<Word> build(int wordCount,
                            int page,
                            Date toDate,
                            TrainingWordOrder wordOrder,
                            TrainingType trainingType) {
        List<Word> words = new ArrayList<>(wordCount);
        build(wordCount, page, toDate, trainingType, wordOrder, words);
        return words;
    }

    private StringBuilder generateSql(TrainingType trainingType, Date toDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from (")
                .append("select w._id as _id,")
                .append("w.title as TITLE,")
                .append("w.translation as TRANSLATION,")
                .append("w.context as CONTEXT,")
                .append("sum(case when IFNULL(ws.training_res,1) = 0 then 1 else 0 end) as I_CNT,")
                .append("sum(case when IFNULL(ws.training_res,0) = 1 then 1 else 0 end) as R_CNT,")
                .append("max(ws.TRAINED_ON_DATE) as last_trained_date ")
                .append("from WORDS w ")
                .append("left join WORD_STAT ws ")
                .append("on w._id = ws.WORD_ID ");
        if (trainingType != null)
            sql.append("and (ws.TRAINING_TYPE = ? OR ws.TRAINING_TYPE IS NULL)");
        sql.append("where w.dict_id = ? ");
        if (toDate != null)
            sql.append("and (ws.TRAINED_ON_DATE < ? OR ws.TRAINED_ON_DATE is NULL)");
        sql.append("group by w._id,w.title,w.translation,w.context")
                .append(")");
        return sql;
    }

    private void build(int count,
                       int page,
                       Date toDate,
                       TrainingType trainingType,
                       TrainingWordOrder order,
                       List<Word> words) {
        List<String> arg = new ArrayList<>();
        StringBuilder sql = generateSql(trainingType, toDate);
        if (trainingType != null)
            arg.add(String.valueOf(trainingType.ordinal()));
        arg.add(String.valueOf(dictId));
        if (toDate != null)
            arg.add(String.valueOf(toDate.getTime()));
        int limit = count;
        int offset = limit * (page - 1);
        sql.append(" order by ")
                .append(order.getSqlOrder())
                .append(" limit ").append(limit)
                .append(" offset ").append(offset);

        try {
            Cursor cur = wordDAO.execComplexSql(sql.toString(), arg.toArray(new String[0]));
            SqlHelper.dumpCursor(cur);
            cur = wordDAO.execComplexSql(sql.toString(), arg.toArray(new String[0]));
            int c = 0;
            while (cur.moveToNext() && (c++) <= count) {
                Word word = wordDAO.readRow(cur);
                words.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<String> buildRandomAnswers(int wordCount, int answerCount, String dbField) {
        int total = wordCount * answerCount + answerCount;
        List<String> answers = new ArrayList<>(total);
        Cursor cur = wordDAO.select()
                .columns(dbField)
                .where(Db.Word.DICTIONARY_ID, Is.EQUAL, dictId)
                .orderBy("RANDOM()", true)
                .limit(total).execute();
        int c = cur.getColumnIndex(dbField);
        while (cur.moveToNext()) {
            String trans = cur.getString(c);
            answers.add(trans);
        }
        return answers;
    }

}
