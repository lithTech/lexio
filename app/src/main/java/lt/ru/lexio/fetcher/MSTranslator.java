package lt.ru.lexio.fetcher;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lithTech on 21.08.2016.
 */

public class MSTranslator extends AsyncTask<String, Void, String> {

    private static final String CLIENT_ID = "lt-translate-javaprop-111";
    private static final String CLIENT_SECRET = "g6Ckta/ZQIHQchQkKWwVxtkhBQOlVqKLJNOGNlpYhTY=";
    private FetcherCallback callback;
    private static final String httpsTranslateURLTemplate = "https://api.microsofttranslator.com/V2/Ajax.svc/Translate?from=%1s&to=%2s&appid=%3s&text=%4s";

    public static String getAccessToken(String clientId, String clientSecret) {
        return AdmAccessToken.getAccessToken(clientId, clientSecret).access_token;
    }

    public static String translate(String accessToken, String from, String to, String text) {
        try {
            String accessTokenToSend = URLEncoder.encode("Bearer " + accessToken, "UTF-8");
            String textToSend = URLEncoder.encode(text, "UTF-8");
            URL translateUrl = new URL(String.format(httpsTranslateURLTemplate, from, to, accessTokenToSend, textToSend));
            HttpsURLConnection connection = (HttpsURLConnection) translateUrl.openConnection();
            connection.getResponseCode();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(translateUrl.openStream(), StandardCharsets.UTF_8))) {
                StringBuffer res = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    res.append(line);
                }
                return res.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public MSTranslator(FetcherCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length < 3)
        {
            System.out.println("Invalid parameters specified. It Must be " +
                    "fromEncoding, toEncoding, text");
            return null;
        }

        return translate(getAccessToken(CLIENT_ID, CLIENT_SECRET), params[0], params[1], params[2]);
    }

    @Override
    protected void onPostExecute(String s) {
        if (callback != null) {
            s = s.replaceAll("[\"]", "");
            callback.done(s);
        }
    }
}
