package lt.ru.lexio.ui.training;

/**
 * Created by lithTech on 27.08.2016.
 */

public class EndPageStatistic {

    public long id;
    public String question;
    public String correctAnswer;
    //public String incorrectAnswer;
    public Boolean isCorrect;

    public EndPageStatistic(long id, String question, String correctAnswer, Boolean isCorrect) {
        this.question = question;
        this.id = id;
        this.correctAnswer = correctAnswer;
        //this.incorrectAnswer = incorrectAnswer;
        this.isCorrect = isCorrect;
    }
}
