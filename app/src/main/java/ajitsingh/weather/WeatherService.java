package ajitsingh.weather;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import ajitsingh.weather.util.DataFetchListener;
import ajitsingh.weather.util.DataFetcher;

public class WeatherService extends IntentService {
    private JSONObject weatherInfo;
    private Handler handler;

    public WeatherService(String name) {
        super(name);
    }

    public WeatherService() {
        super("Weather service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();
        handler = new Handler();
        if(intent.getStringExtra("networkAvailable?").equals("true")) ping();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    private void sendMessage() {
        Intent intent = new Intent("weatherUpdate");
        intent.putExtra("weatherInfo", weatherInfo.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void ping() {
        try {
            new LongOperation().execute();
            sendMessage();
        } catch (Exception e) {
            Log.e("Error", "In onStartCommand");
            e.printStackTrace();
        }
        scheduleNext();
    }

    private void scheduleNext() {
        int fiveMinutes = 60000 * 5;
        handler.postDelayed(new Runnable() {
            public void run() { ping(); }
        }, fiveMinutes);
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... values) {
            DataFetcher.fetchWeatherInfo(new DataFetchListener() {
                @Override
                public void onDataFetch(JSONObject data) {
                    weatherInfo = data;
                }
            });

            return "success";
        }
    }
}
