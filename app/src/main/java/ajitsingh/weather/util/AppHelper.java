package ajitsingh.weather.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AppHelper {
    public static boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
