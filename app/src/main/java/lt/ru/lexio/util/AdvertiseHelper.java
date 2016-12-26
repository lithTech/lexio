package lt.ru.lexio.util;

import android.app.Activity;
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

import lt.ru.lexio.BuildConfig;

/**
 * Created by lithTech on 26.12.2016.
 */

public class AdvertiseHelper {

    private static final String FLAVOR_WA = "withoutAds";

    public static boolean isFlavorWithoutAds() {
        return BuildConfig.FLAVOR.equals(FLAVOR_WA);
    }

    public static AdView loadAd(final Activity activity,
                                final ViewGroup adParent,
                                final String adId) {
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
                                super.onAdLoaded();
                                adParent.setVisibility(View.VISIBLE);
                            }
                        });

                        adParent.addView(adView);
                        final AdRequest.Builder adRequest = new AdRequest.Builder()
                                .addKeyword("language");
                        if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.equalsIgnoreCase("Debug"))
                            adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

                        adView.loadAd(adRequest.build());
                    }
                });
            }
        }, 2000);

        return adView;
    }

}
