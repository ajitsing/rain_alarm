package ajitsingh.weather.util;

import org.json.JSONObject;

public interface DataFetchListener {
    public void onDataFetch(JSONObject data);
}
