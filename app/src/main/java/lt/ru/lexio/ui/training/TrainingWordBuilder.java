package lt.ru.lexio.ui.training;

import android.database.Cursor;

import org.droidparts.persist.sql.stmt.Is;

import java.util.ArrayList;
import java.util.List;

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

    public List<Word> build(int count, TrainingWordMethod method, TrainingType trainingType) {
        List<Word> words = new ArrayList<>(count);
        if (method == TrainingWordMethod.UNTRAINING_WORDS) {
            buildForUntrainingWords(count, trainingType, words);
        }
        return words;
    }

    private void buildForUntrainingWords(int count, TrainingType trainingType, List<Word> words) {
        String sql =
                "select * from ("+
                "select w." + Db.Common.ID +
                        ","+Db.Common.TITLE+
                        ",w."+Db.Word.DICTIONARY_ID+
                        ",w."+Db.Word.TRANSLATION+
                        ",w."+Db.Word.CONTEXT+
                        ",sum(case when IFNULL(ws."+Db.WordStatistic.TRAINING_RESULT+",1)"+
                            " = 0 then 1 else 0 end) as I_CNT"+
                        ",sum(case when IFNULL(ws."+Db.WordStatistic.TRAINING_RESULT+",0)"+
                            " = 1 then 1 else 0 end) as R_CNT"+
                " from " + Db.Word.TABLE + " w" +
                " left join " + Db.WordStatistic.TABLE + " ws" +
                    " on w."+Db.Common.ID + " = " + Db.WordStatistic.WORD_ID +
                    " and ws." + Db.WordStatistic.TRAINING_TYPE + " = ?" +
                " where w." + Db.Word.DICTIONARY_ID + " = ?" +
                " group by w."+Db.Common.ID +
                        ",w."+Db.Common.TITLE+
                        ",w."+Db.Word.DICTIONARY_ID+
                        ",w."+Db.Word.TRANSLATION+
                        ",w."+Db.Word.CONTEXT +
                ") " +
                " order by I_CNT - R_CNT desc, I_CNT desc, "+Db.Common.TITLE +
                " limit " + count ;

        try {
            Cursor cur = wordDAO.execComplexSql(sql, new String[]{
                    String.valueOf(trainingType.ordinal()), String.valueOf(dictId)
            });
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

    public List<String> buildRandomAnswers(int wordCount, int answerCount) {
        int total = wordCount * answerCount + answerCount;
        List<String> answers = new ArrayList<>(total);
        Cursor cur = wordDAO.select()
                .columns(Db.Word.TRANSLATION)
                .where(Db.Word.DICTIONARY_ID, Is.EQUAL, dictId)
                .orderBy("RANDOM()", true)
                .limit(total).execute();
        int c = cur.getColumnIndex(Db.Word.TRANSLATION);
        while (cur.moveToNext()) {
            String trans = cur.getString(c);
            answers.add(trans);
        }
        return answers;
    }

}
