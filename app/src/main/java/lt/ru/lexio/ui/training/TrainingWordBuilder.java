package lt.ru.lexio.ui.training;

import android.database.Cursor;

import org.droidparts.persist.sql.stmt.Is;

import java.util.ArrayList;
import java.util.List;

import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatisticDAO;

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

    public List<Word> build(int count, TrainingWordMethod method) {
        List<Word> words = new ArrayList<>(count);
        if (method == TrainingWordMethod.UNTRAINING_WORDS) {
            /*String sql = "select w." + Db.Common.TITLE + ", " +
                    Db.Word.TRANSLATION + "," +
                    Db.Word.DICTIONARY_ID + "," +
                    Db.Word.CONTEXT +

                    " from "+Db.Word.TABLE + " w" +
                    " left join "+Db.WordStatistic.TABLE + " ws" +
                    " on w._id = "+Db.WordStatistic.WORD_ID +
                    " group by w._id";

            wordDAO.execComplexSql()*/
            Cursor cur = wordDAO.getAll(dictId);
            int c = 0;
            while (cur.moveToNext() && (c++) <= count) {
                Word word = wordDAO.readRow(cur);
                words.add(word);
            }
        }
        return words;
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
