package ajitsingh.weather.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataFetcher {
    public static void fetchWeatherInfo(DataFetchListener listener){
        String url = "http://api.openweathermap.org/data/2.5/weather?q=Chennai&units=metric";
        String data = getJSON(url);
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listener.onDataFetch(jsonObject);
    }

    private static String getJSON(String address) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        httpGet.setHeader("Content-type", "application/json");

        try {
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }
}
