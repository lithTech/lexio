package lt.ru.lexio.ui.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.ui.GeneralCallback;

public class DictLoader implements Runnable {

    WordDAO dao;
    DictionaryDAO dictionaryDAO;
    InputStream wordList;
    long dictId;
    GeneralCallback callback = null;
    Word word = new Word();
    Date crDate = new Date();

    public DictLoader(WordDAO dao, DictionaryDAO dictionaryDAO, InputStream wordList, long dictId, GeneralCallback callback) {
        this.dao = dao;
        this.dictionaryDAO = dictionaryDAO;
        this.wordList = wordList;
        this.dictId = dictId;
        this.callback = callback;
    }

    private boolean getWord(String string, Word word) {
        String[] parts = string.split("[;|]");
        if (parts.length >= 2) {
            word.id = 0;
            word.setTranscription("");
            word.setCreated(crDate);
            word.setContext("");
            word.setTitle(parts[0].toLowerCase().trim());
            word.setTranslation(parts[1].toLowerCase().trim());

            if (parts.length > 2)
                word.setTranscription(parts[2].toLowerCase().trim());
            if (parts.length > 3)
                word.setContext(parts[3].trim());
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        final int bulk = 500;
        word.setDictionary(dictionaryDAO.read(dictId));

        try {
            int bulkI = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(wordList));
            String line = reader.readLine();
            dao.startTrans();
            while (line != null) {
                if (getWord(line, word))
                {
                    try {
                        dao.create(word);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    bulkI++;
                }
                line = reader.readLine();
                if (bulkI >= bulk) {
                    bulkI = 0;
                    dao.transactionSuccessful();
                    dao.endTrans();
                    dao.startTrans();
                }
            }
            dao.transactionSuccessful();
            dao.endTrans();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Dictionary d = dictionaryDAO.read(dictId);
            d.setWords(dictionaryDAO.getWordCount(dictId));
            dictionaryDAO.update(d);
            try {
                wordList.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (callback != null)
                callback.done(d);
        }
    }
}