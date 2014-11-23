package ajitsingh.weather;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ajitsingh.weather.util.AppHelper;
import ajitsingh.weather.util.DataFetchListener;
import ajitsingh.weather.util.DataFetcher;
import ajitsingh.weather.util.NotificationUtils;

public class WeatherService extends IntentService {
    private JSONObject weatherInfo;
    private Handler handler;
    private String currentWeather = "";

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
        ping();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    private void sendMessage(String message) {
        Intent intent = new Intent("weatherUpdate");
        intent.putExtra("weather", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void ping() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (AppHelper.isNetworkAvailable(connectivityManager)) {
            new LongOperation().execute();
        } else {
            sendMessage("Internet Unavailable!!");
        }

        scheduleNext();
    }

    private void scheduleNext() {
        int fiveMinutes = 60000 * 5;
        handler.postDelayed(new Runnable() {
            public void run() {
                ping();
            }
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

        @Override
        protected void onPostExecute(String result) {
            int actualId = 0;
            long sunrise = 0;
            long sunset = 0;
            try {
                JSONObject details = weatherInfo.getJSONArray("weather").getJSONObject(0);
                actualId = details.getInt("id");
                sunrise = weatherInfo.getJSONObject("sys").getLong("sunrise") * 1000;
                sunset = weatherInfo.getJSONObject("sys").getLong("sunset") * 1000;
            } catch (JSONException e) {
                e.printStackTrace();
            }


            int id = actualId / 100;
            if (actualId == 800) {
                long currentTime = new Date().getTime();
                if (currentTime >= sunrise && currentTime < sunset) {
                    updateViewAndNotify("SUNNY");
                } else {
                    updateViewAndNotify("CLEAR NIGHT");
                }
            } else {
                switch (id) {
                    case 2:
                        updateViewAndNotify("Its Thundering!");
                        if (!"THUNDER".equals(currentWeather))
                            NotificationUtils.vibrateFor(getBaseContext(), 4000);
                        break;
                    case 3:
                        updateViewAndNotify("Its Drizzling!!");
                        if (!"DRIZZLE".equals(currentWeather))
                            NotificationUtils.vibrateFor(getBaseContext(), 4000);
                        break;
                    case 7:
                        updateViewAndNotify("Weather is Foggy");
                        break;
                    case 8:
                        updateViewAndNotify("Weather is Cloudy");
                        break;
                    case 6:
                        updateViewAndNotify("SNOWY");
                        break;
                    case 5:
                        updateViewAndNotify("Its Raining!!!");
                        if (!"RAINY".equals(currentWeather))
                            NotificationUtils.vibrateFor(getBaseContext(), 5000);
                        break;
                }
            }
        }

        private void updateViewAndNotify(String weather) {
            sendMessage(weather);
            if (!weather.equals(currentWeather)) {
                currentWeather = weather;
                NotificationUtils.sendNotification(getBaseContext(), weather, "");
            }
        }
    }
}