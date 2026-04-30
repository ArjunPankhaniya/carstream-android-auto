package com.thekirankumar.youtubeauto.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarUiController;
import com.thekirankumar.youtubeauto.R;
import com.thekirankumar.youtubeauto.fragments.CarFragment;
import com.thekirankumar.youtubeauto.fragments.WebViewCarFragment;

import java.util.HashSet;

public class MainCarActivity extends CarActivity {

    private static final String FRAGMENT_MAIN = "main";
    private static final String CURRENT_FRAGMENT_KEY = "app_current_fragment";

    private String mCurrentFragmentTag;

    private final FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks =
            new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentStarted(FragmentManager fm, Fragment f) {
                    updateStatusBarTitle();
                }
            };

    private final HashSet<ActivityCallbacks> activityCallbacks = new HashSet<>();

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        for (ActivityCallbacks next : activityCallbacks) {
            next.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_main);

        CarUiController carUiController = getCarUiController();
        carUiController.getStatusBarController().hideAppHeader();
        carUiController.getStatusBarController().hideConnectivityLevel();

        FragmentManager fragmentManager = getSupportFragmentManager();
        WebViewCarFragment webViewCarFragment = new WebViewCarFragment();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, webViewCarFragment, FRAGMENT_MAIN)
                .detach(webViewCarFragment)
                .commitNow();

        String initialFragmentTag = FRAGMENT_MAIN;
        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_FRAGMENT_KEY)) {
            initialFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_KEY);
        }

        switchToFragment(initialFragmentTag);

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                mFragmentLifecycleCallbacks, false
        );
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_FRAGMENT_KEY, mCurrentFragmentTag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentFragmentTag != null) {
            switchToFragment(mCurrentFragmentTag);
        }
    }

    private void switchToFragment(String tag) {
        if (tag == null || tag.equals(mCurrentFragmentTag)) {
            return;
        }

        FragmentManager manager = getSupportFragmentManager();

        Fragment currentFragment = mCurrentFragmentTag == null
                ? null
                : manager.findFragmentByTag(mCurrentFragmentTag);

        Fragment newFragment = manager.findFragmentByTag(tag);

        if (newFragment == null) return;

        FragmentTransaction transaction = manager.beginTransaction();

        if (currentFragment != null) {
            transaction.detach(currentFragment);
        }

        transaction.attach(newFragment);
        transaction.commit();

        mCurrentFragmentTag = tag;
    }

    private void updateStatusBarTitle() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(mCurrentFragmentTag);
        if (fragment instanceof CarFragment) {
            CarFragment carFragment = (CarFragment) fragment;
            getCarUiController().getStatusBarController().setTitle(carFragment.getTitle());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (ActivityCallbacks next : activityCallbacks) {
            next.onConfigChanged();
        }
    }

    public void addActivityCallback(ActivityCallbacks listener) {
        this.activityCallbacks.add(listener);
    }

    public void removeActivityCallback(ActivityCallbacks listener) {
        this.activityCallbacks.remove(listener);
    }

    public interface ActivityCallbacks {
        void onConfigChanged();
        void onWindowFocusChanged(boolean hasFocus);
    }
}