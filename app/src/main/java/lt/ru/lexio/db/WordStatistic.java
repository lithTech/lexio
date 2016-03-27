package lt.ru.lexio.db;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

import java.util.Date;

/**
 * Created by lithTech on 27.03.2016.
 */
@Table(name = Db.WordStatistic.TABLE)
public class WordStatistic extends Entity {

    @Column(name = Db.WordStatistic.TRAINED_ON, unique = false, nullable = false)
    Date trainedOn;

    @Column(name = Db.WordStatistic.TRAINING_RESULT, unique = false, nullable = false)
    int trainingResult;

    @Column(name = Db.WordStatistic.TRAINING_TYPE, unique = false, nullable = false)
    int trainingType;

    @Column(name = Db.WordStatistic.WORD_ID, unique = false, nullable = false)
    Word word;

    @Column(name = Db.WordStatistic.TRAINING_SESSION_ID, unique = false, nullable = true)
    Long sessionId = null;

    public WordStatistic() {
    }

    public Date getTrainedOn() {
        return trainedOn;
    }

    public void setTrainedOn(Date trainedOn) {
        this.trainedOn = trainedOn;
    }

    public int getTrainingResult() {
        return trainingResult;
    }

    public void setTrainingResult(int trainingResult) {
        this.trainingResult = trainingResult;
    }

    public int getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(int trainingType) {
        this.trainingType = trainingType;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
}

