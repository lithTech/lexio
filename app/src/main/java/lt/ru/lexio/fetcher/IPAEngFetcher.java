package lt.ru.lexio.fetcher;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;

/**
 * Created by lithTech on 15.08.2016.
 */

public class IPAEngFetcher extends AsyncTask<List<Word>, String, List<Word>> {

    private String _URL = "http://lingorado.com/ipa/";

    private WordDAO wordDAO;

    private boolean succ = false;

    public IPAEngFetcher(WordDAO wordDAO) {
        this.wordDAO = wordDAO;
    }

    @Override
    protected List<Word> doInBackground(List<Word>... params) {
        if (params[0].isEmpty()) return new ArrayList<>(0);

        String data = fetchFromInternet(params[0]);
        List<Word> result = new ArrayList<>(params[0]);

        if (data != null && !data.isEmpty()) {
            Document doc = Jsoup.parse(data);
            Elements el = doc.select("#transcr_output");
            if (!el.isEmpty()) {
                Elements words = el.select(".transcribed_word");
                if (words.size() != result.size())
                    return new ArrayList<>(0);
                for(int i = 0; i < words.size(); i++) {
                    Element word = words.get(i);
                    result.get(i).setTranscription(word.text());
                }
                succ = true;
            }
        }

        return result;
    }

    private String fetchFromInternet(List<Word> words) {
        String response = "";
        try {
            URL url = new URL(_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            fillConnection(con);

            OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            Iterator<Word> paramIt = words.iterator();
            writer.write("text_to_transcribe=");
            while (paramIt.hasNext()){
                String param = paramIt.next().getTitle();
                writer.write(param);
                if (paramIt.hasNext())
                    writer.write("%0D%0A");
            }
            writer.write("&submit=Show+transcription&output_dialect=am&output_style=only_tr&preBracket=&postBracket=&speech_support=0");

            writer.flush();
            writer.close();
            os.close();
            int responseCode = con.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;

                InputStream dataInput = con.getInputStream();
                if ("gzip".equals(con.getContentEncoding())) {
                    dataInput = new GZIPInputStream(dataInput);
                }

                BufferedReader br=new BufferedReader(new InputStreamReader(dataInput));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";
            }

        } catch (Exception e) {
            return "";
        }
        return response;
    }

    @Override
    protected void onPostExecute(List<Word> words) {

        if (succ)
            wordDAO.update(words);

    }

    private void fillConnection(HttpURLConnection con) throws ProtocolException {
        con.setReadTimeout(15000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);

        con.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
        con.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        con.setRequestProperty("Connection",
                "keep-alive");
        con.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        con.setRequestProperty("Accept-Encoding",
                "gzip, deflate");
        con.setRequestProperty("Accept-Language",
                "ru,en-US;q=0.8,en;q=0.6");
    }
}
