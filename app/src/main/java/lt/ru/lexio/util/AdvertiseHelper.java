package lt.ru.lexio.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import lt.ru.lexio.BuildConfig;
import lt.ru.lexio.ui.GeneralCallback;

/**
 * Created by lithTech on 26.12.2016.
 */

public class AdvertiseHelper {

    private static AtomicBoolean _cancelShow = new AtomicBoolean(false);
    private static AtomicBoolean _isLoading = new AtomicBoolean(false);

    private static final String FLAVOR_WA = "defaultConfig";

    public static boolean isFlavorWithoutAds() {
        return BuildConfig.FLAVOR.equals(FLAVOR_WA);
    }

    public static void cancelShow() {
        if (_isLoading.get() == true)
            _cancelShow.set(true);
        _isLoading.set(false);
    }

    public static AdView loadAd(final Activity activity,
                                final ViewGroup adParent,
                                final String adId,
                                final GeneralCallback callback) {
        _isLoading.set(true);
        _cancelShow.set(false);
        final AdView adView = new AdView(activity);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adView.setAdSize(AdSize.SMART_BANNER);
                        adView.setAdUnitId(adId);

                        adView.setAdListener(new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                _isLoading.set(false);
                                super.onAdLoaded();
                                if (_cancelShow.get() == false) {
                                    _cancelShow.set(false);
                                    adParent.setVisibility(View.VISIBLE);
                                    if (callback != null)
                                        callback.done(adView);
                                }
                            }
                        });

                        adParent.addView(adView);
                        final AdRequest.Builder adRequest = new AdRequest.Builder()
                                .addKeyword("language");
                        if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.equalsIgnoreCase("Debug"))
                            adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                                    .addTestDevice("4633431AFD4129500546F41EE34F1591");

                        adView.loadAd(adRequest.build());
                    }
                });
            }
        }, 1000);

        return adView;
    }

}
