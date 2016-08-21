package lt.ru.lexio.fetcher;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//Class to get access token from Azure Data market
public class AdmAccessToken {

    public String access_token;
    public String token_type;
    public String expires_in;
    public String scope;

    public static AdmAccessToken getAccessToken(String clientId, String clientSecret) {

        AdmAccessToken accessToken = null;
        try {
            String charset = StandardCharsets.UTF_8.name();
            String oauthUrl = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
            String params = "grant_type=client_credentials&scope=http://api.microsofttranslator.com"
                    + "&client_id=" + URLEncoder.encode(clientId, charset)
                    + "&client_secret=" + URLEncoder.encode(clientSecret, charset);
            URL url = new URL(oauthUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + charset);
            connection.setDoOutput(true);
            try (OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())) {
                wr.write(params);
                wr.flush();
            }
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset))) {
                    StringBuffer res = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        res.append(line);
                    }

                    JSONObject obj = new JSONObject(res.toString());
                    AdmAccessToken tmp = new AdmAccessToken();
                    tmp.access_token = obj.getString("access_token");
                    tmp.token_type = obj.getString("token_type");
                    tmp.expires_in = obj.getString("expires_in");
                    tmp.scope = obj.getString("scope");
                    return tmp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }
}