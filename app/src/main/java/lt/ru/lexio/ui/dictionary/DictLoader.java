package lt.ru.lexio.ui.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.ui.GeneralCallback;

public class DictLoader implements Runnable {

        DictionaryDAO dao;
        InputStream sqlInsertsStream;
        long dictId;
        GeneralCallback callback = null;

        public DictLoader(DictionaryDAO dao, InputStream sqlInsertsStream, long dictId) {
            this.dao = dao;
            this.sqlInsertsStream = sqlInsertsStream;
            this.dictId = dictId;
        }

        public DictLoader(DictionaryDAO dao, InputStream sqlInsertsStream, long dictId, GeneralCallback callback) {
            this.dao = dao;
            this.sqlInsertsStream = sqlInsertsStream;
            this.dictId = dictId;
            this.callback = callback;
        }

        @Override
        public void run() {
            int c = 0;
            final int bulk = 500;
            try
            {
                int bulkI = 0;
                BufferedReader reader = new BufferedReader(new InputStreamReader(sqlInsertsStream));
                String line = reader.readLine();
                dao.startTrans();
                while (line != null) {
                    Date date = new Date();
                    dao.importWord(dictId, line, date);
                    line = reader.readLine();
                    c++;
                    bulkI++;
                    if (bulkI >= bulk)
                    {
                        bulkI = 0;
                        dao.transactionSuccessful();
                        dao.endTrans();
                        dao.startTrans();
                    }
                    if (line == null) {
                        dao.endTrans();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Dictionary d = dao.read(dictId);
                d.setWords(c);
                dao.update(d);
                try {
                    sqlInsertsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (callback != null)
                    callback.done(d);
            }
        }
    }