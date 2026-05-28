/*
 * SPDX-FileCopyrightText: 2026 BearAppTH
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.accounts.AccountManager;

import com.google.android.gms.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import org.microg.gms.auth.AuthConstants;

import org.microg.gms.ui.settings.SettingsProvider;

import java.util.Objects;
import java.util.Set;

import static org.microg.gms.ui.settings.SettingsProviderKt.getAllSettingsProviders;

public class MainSettingsActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;

    private NavController getNavController() {
        return ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.navhost))).getNavController();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyStoredDarkMode();
        DynamicColors.applyToActivityIfAvailable(this);
        enableEdgeToEdgeNoContrast();

        Intent intent = getIntent();
        for (SettingsProvider settingsProvider : getAllSettingsProviders(this)) {
            settingsProvider.preProcessSettingsIntent(intent);
        }

        setContentView(R.layout.settings_root_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);

        View rootLayout = findViewById(R.id.root_layout);
        ExtendedFloatingActionButton fab = findViewById(R.id.preference_fab);
        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        final int initialScrollViewPaddingLeft = nestedScrollView.getPaddingLeft();
        final int initialScrollViewPaddingTop = nestedScrollView.getPaddingTop();
        final int initialScrollViewPaddingRight = nestedScrollView.getPaddingRight();
        final int initialScrollViewPaddingBottom = nestedScrollView.getPaddingBottom();

        ViewGroup.MarginLayoutParams fabInitialParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        final int initialFabMarginLeft = fabInitialParams.leftMargin;
        final int initialFabMarginRight = fabInitialParams.rightMargin;
        final int initialFabMarginBottom = fabInitialParams.bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, windowInsets) -> {
            Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            boolean imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
            int bottomInset = imeVisible ? imeInsets.bottom : systemBarsInsets.bottom;

            nestedScrollView.setPadding(
                    initialScrollViewPaddingLeft + systemBarsInsets.left,
                    initialScrollViewPaddingTop,
                    initialScrollViewPaddingRight + systemBarsInsets.right,
                    initialScrollViewPaddingBottom + bottomInset
            );

            ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            fabParams.leftMargin = initialFabMarginLeft + systemBarsInsets.left;
            fabParams.rightMargin = initialFabMarginRight + systemBarsInsets.right;
            fabParams.bottomMargin = initialFabMarginBottom + systemBarsInsets.bottom;
            fab.setLayoutParams(fabParams);

            return windowInsets;
        });

        for (SettingsProvider settingsProvider : getAllSettingsProviders(this)) {
            settingsProvider.extendNavigation(getNavController());
        }

        // Top-level destinations for bottom navigation (no back arrow shown)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.accountManagerFragment,
                R.id.gcmFragment,
                R.id.settingsFragment
        ).build();

        NavigationUI.setupWithNavController(toolbarLayout, toolbar, getNavController(), appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, getNavController());

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                fab.shrink();
            } else {
                fab.extend();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccountBadge();
    }

    private void updateAccountBadge() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav == null) return;
        AccountManager accountManager = AccountManager.get(this);
        int count = accountManager.getAccountsByType(AuthConstants.DEFAULT_ACCOUNT_TYPE).length;
        if (count > 0) {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.accountManagerFragment);
            badge.setNumber(count);
            badge.setVisible(true);
        } else {
            bottomNav.removeBadge(R.id.accountManagerFragment);
        }
    }

    private void applyStoredDarkMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String mode = prefs.getString("pref_dark_mode", "system");
        int nightMode;
        if ("light".equals(mode)) nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        else if ("dark".equals(mode)) nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        else nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private void enableEdgeToEdgeNoContrast() {
        EdgeToEdge.enable(this, SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT));
        getWindow().setNavigationBarContrastEnforced(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(getNavController(), appBarConfiguration) || super.onSupportNavigateUp();
    }
}
