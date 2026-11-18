package app.morphe.extension.shikify.x;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * Material 3 for X. Everything is done on the view tree at runtime, by resource name, so a new X
 * build only needs the one startup hook to be found again.
 */
public final class ShikifyX {
    private static final WeakHashMap<Activity, Screen> SCREENS = new WeakHashMap<>();
    private static final int CARD_TAG = 0x7f5e0001, STATUS_TAG = 0x7f5e0002;
    private static boolean installed;

    private ShikifyX() { }

    public static void install(Application application) {
        if (installed) return;
        installed = true;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle state) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) {
                Screen screen = SCREENS.get(activity);
                if (screen == null) { screen = new Screen(activity); SCREENS.put(activity, screen); }
                screen.start();
            }
            @Override public void onActivityPaused(Activity activity) { Screen screen = SCREENS.get(activity); if (screen != null) screen.stop(); }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
            @Override public void onActivityDestroyed(Activity activity) { Screen screen = SCREENS.remove(activity); if (screen != null) screen.stop(); }
        });
    }

    /** Material You colour roles, from the system palette that follows the wallpaper. */
    static final class Palette {
        final int surface, card, container, primary, onPrimaryContainer, primaryContainer, secondaryContainer, onSecondaryContainer, onSurface, onSurfaceVariant, outline;
        Palette(Context context) {
            boolean dark = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            String mode = dark ? "_dark" : "_light";
            surface = system(context, "system_surface_container_lowest" + mode, dark ? 0xff0f0d13 : 0xffffffff);
            card = system(context, "system_surface_container" + mode, dark ? 0xff211f26 : 0xfff3edf7);
            container = system(context, "system_surface_container_high" + mode, dark ? 0xff2b2930 : 0xffece6f0);
            primary = system(context, "system_primary" + mode, dark ? 0xffd0bcff : 0xff6750a4);
            primaryContainer = system(context, "system_primary_container" + mode, dark ? 0xff4f378b : 0xffeaddff);
            onPrimaryContainer = system(context, "system_on_primary_container" + mode, dark ? 0xffeaddff : 0xff21005d);
            secondaryContainer = system(context, "system_secondary_container" + mode, dark ? 0xff4a4458 : 0xffe8def8);
            onSecondaryContainer = system(context, "system_on_secondary_container" + mode, dark ? 0xffe8def8 : 0xff1d192b);
            onSurface = system(context, "system_on_surface" + mode, dark ? 0xffe6e0e9 : 0xff1d1b20);
            onSurfaceVariant = system(context, "system_on_surface_variant" + mode, dark ? 0xffcac4d0 : 0xff49454f);
            outline = system(context, "system_outline_variant" + mode, dark ? 0xff49454f : 0xffcac4d0);
        }
        private static int system(Context context, String name, int fallback) {
            int id = context.getResources().getIdentifier(name, "color", "android");
            if (id == 0) return fallback;
            try { return context.getColor(id); } catch (RuntimeException error) { return fallback; }
        }
    }

    static int dp(View view, float value) { return Math.round(value * view.getResources().getDisplayMetrics().density); }

    static GradientDrawable rounded(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    /** One activity's window. Rows are styled as they appear, so recycled rows stay styled. */
    static final class Screen implements ViewTreeObserver.OnPreDrawListener, ViewTreeObserver.OnGlobalLayoutListener {
        final java.util.Set<ViewGroup> lists = java.util.Collections.newSetFromMap(new WeakHashMap<>());
        final Activity activity;
        Palette palette;
        ViewTreeObserver observer;
        int listId, rowId, fabId, topTabsId, bottomTabsId, bottomContainerId, appBarId, toolbarId, fleetBarId, fleetHolderId, bottomBarId, slidingId;

        Screen(Activity activity) {
            this.activity = activity;
            listId = id("list"); rowId = id("outer_layout_row_view_tweet"); fabId = id("composer_write");
            topTabsId = id("tab_layout"); bottomTabsId = id("tabs"); bottomContainerId = id("tabsContainer");
            appBarId = id("app_bar"); toolbarId = id("toolbar"); fleetBarId = id("fleetline_app_bar");
            fleetHolderId = id("fleetline_holder"); bottomBarId = id("bottom_bar"); slidingId = id("tabsSlidingContainer");
        }
        int id(String name) { return activity.getResources().getIdentifier(name, "id", activity.getPackageName()); }

        void start() {
            palette = new Palette(activity);
            try {
                activity.getWindow().setStatusBarColor(palette.surface);
                // The status bar shows the window background above the content. Repaint it only when it is
                // an opaque colour, so see-through windows such as the media viewer keep what is behind them.
                Drawable window = activity.getWindow().getDecorView().getBackground();
                if (window instanceof android.graphics.drawable.ColorDrawable && ((android.graphics.drawable.ColorDrawable) window).getAlpha() == 255)
                    activity.getWindow().getDecorView().setBackgroundColor(palette.surface);
            } catch (RuntimeException ignored) { }
            View decor = activity.getWindow().getDecorView();
            stop();
            observer = decor.getViewTreeObserver();
            observer.addOnPreDrawListener(this);
            observer.addOnGlobalLayoutListener(this);
            onGlobalLayout();
        }
        void stop() {
            if (observer != null && observer.isAlive()) { observer.removeOnPreDrawListener(this); observer.removeOnGlobalLayoutListener(this); }
            observer = null;
        }

        /** The whole window is walked only when its layout changes. */
        @Override public void onGlobalLayout() {
            try { style(activity.getWindow().getDecorView()); } catch (RuntimeException ignored) { }
        }

        /** Scrolling brings in rows without a layout pass, so known lists are checked every frame. */
        @Override public boolean onPreDraw() {
            try { for (ViewGroup list : lists) if (list.isAttachedToWindow()) list(list); } catch (RuntimeException ignored) { }
            return true;
        }

        void style(View root) {
            if (!(root instanceof ViewGroup)) return;
            ViewGroup group = (ViewGroup) root;
            int id = group.getId();
            if ((id == android.R.id.list || id == listId && listId != 0) && group.getClass().getName().contains("RecyclerView")) { lists.add(group); list(group); return; }
            if (id == appBarId || id == toolbarId || id == fleetBarId) surface(group, palette.surface);
            if (id != bottomTabsId && isA(group, "com.google.android.material.tabs.TabLayout")) pillTabs(group);
            if (isA(group, "com.google.android.material.appbar.AppBarLayout")) surface(group, palette.surface);
            // The drawer and coordinator paint the status bar area themselves; match it to the header.
            if (isA(group, "androidx.drawerlayout.widget.DrawerLayout") || isA(group, "androidx.coordinatorlayout.widget.CoordinatorLayout")) statusBar(group);
            if (id == bottomContainerId && bottomContainerId != 0) bottomBar(group);
            if (id == fleetHolderId && fleetHolderId != 0) flat(group);
            if (group.getClass().getName().endsWith("SwipeRefreshLayout")) refresh(group);
            for (int index = 0; index < group.getChildCount(); index++) {
                View child = group.getChildAt(index);
                if (child.getId() == fabId && fabId != 0) fab(child);
                else style(child);
            }
        }

        static boolean isA(Object object, String name) {
            for (Class<?> type = object.getClass(); type != null; type = type.getSuperclass()) if (type.getName().equals(name)) return true;
            return false;
        }

        void surface(View view, int color) {
            Drawable background = view.getBackground();
            if (background instanceof GradientDrawable && view.getTag(CARD_TAG) != null) return;
            if (view.getTag(CARD_TAG) == null) { view.setTag(CARD_TAG, Boolean.TRUE); view.setBackgroundColor(color); }
        }

        /** Timeline lists: an M3 surface with each post on its own rounded card. */
        void list(ViewGroup list) {
            if (list.getTag(CARD_TAG) == null) {
                list.setTag(CARD_TAG, Boolean.TRUE);
                list.setBackgroundColor(palette.surface);
                list.setClipToPadding(false);
                list.setPadding(list.getPaddingLeft(), list.getPaddingTop() + dp(list, 4), list.getPaddingRight(), list.getPaddingBottom());
            }
            for (int index = 0; index < list.getChildCount(); index++) {
                View child = list.getChildAt(index);
                if (child.getTag(CARD_TAG) != null) continue;
                // Rows sit in a GroupedRowView, which draws the dividers between them.
                boolean grouped = child.getClass().getName().endsWith("GroupedRowView");
                View row = child.getId() == rowId ? child : rowId == 0 ? null : child.findViewById(rowId);
                // Any other grouped row (people, notifications, settings rows) gets a card around its content.
                if (row == null && grouped && child instanceof ViewGroup && ((ViewGroup) child).getChildCount() > 0) row = ((ViewGroup) child).getChildAt(0);
                if (grouped) clearRowPaint(child);
                if (row != null && row.getTag(CARD_TAG) == null) { card(row); child.setTag(CARD_TAG, Boolean.TRUE); }
            }
        }

        /** GroupedRowView fills its row and draws dividers from colours it holds per row; cards replace both. */
        static void clearRowPaint(View row) {
            for (String name : new String[]{"c", "d"}) {
                try { java.lang.reflect.Field field = row.getClass().getDeclaredField(name); if (field.getType() != int.class) continue; field.setAccessible(true); field.setInt(row, 0); }
                catch (ReflectiveOperationException | RuntimeException ignored) { }
            }
            row.setBackground(null);
            row.invalidate();
        }

        void card(View row) {
            row.setTag(CARD_TAG, Boolean.TRUE);
            int side = dp(row, 10), gap = dp(row, 5);
            GradientDrawable shape = rounded(palette.card, dp(row, 22));
            row.setBackground(new InsetDrawable(shape, side, gap, side, gap));
            row.setPadding(row.getPaddingLeft() + side, row.getPaddingTop() + gap, row.getPaddingRight() + side, row.getPaddingBottom() + gap);
            // The post itself paints a flat background and a full-width press highlight.
            int inner = row.getResources().getIdentifier("row", "id", row.getContext().getPackageName());
            View tweet = inner == 0 ? null : row.findViewById(inner);
            if (tweet != null) {
                tweet.setBackground(new RippleDrawable(ColorStateList.valueOf(palette.onSurface & 0x1fffffff), null, rounded(0xff000000, dp(row, 22))));
                tweet.setClipToOutline(false);
            }
        }

        /** Top tabs such as For you / Following: a filled pill under the selected tab. */
        void pillTabs(ViewGroup tabs) {
            if (tabs.getTag(CARD_TAG) != null) return;
            tabs.setTag(CARD_TAG, Boolean.TRUE);
            tabs.setBackgroundColor(palette.surface);
            try {
                Class<?> type = tabs.getClass();
                GradientDrawable pill = rounded(palette.secondaryContainer, dp(tabs, 20));
                // Fixed tabs are wide, so the pill is inset; scrolling tabs hug their label.
                boolean scrolling = false;
                try { scrolling = (Integer) type.getMethod("getTabMode").invoke(tabs) == 0; } catch (ReflectiveOperationException | RuntimeException ignored) { }
                int side = dp(tabs, scrolling ? 2 : 24);
                call(type, tabs, "setSelectedTabIndicator", Drawable.class, new InsetDrawable(pill, side, dp(tabs, 8), side, dp(tabs, 8)));
                call(type, tabs, "setSelectedTabIndicatorColor", int.class, palette.secondaryContainer);
                call(type, tabs, "setSelectedTabIndicatorGravity", int.class, 3);
                call(type, tabs, "setTabIndicatorFullWidth", boolean.class, true);
                call(type, tabs, "setTabRippleColor", ColorStateList.class, ColorStateList.valueOf(0));
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }

        /** Bottom bar: a floating rounded M3 bar, like Boost's, with a pill behind the selected icon. */
        void bottomBar(ViewGroup container) {
            if (container.getTag(CARD_TAG) != null) return;
            container.setTag(CARD_TAG, Boolean.TRUE);
            for (View parent = (View) container.getParent(); parent != null; parent = parent.getParent() instanceof View ? (View) parent.getParent() : null) {
                if (parent.getId() != slidingId && parent.getId() != bottomBarId) break;
                parent.setBackground(null);
                if (parent instanceof ViewGroup) { ((ViewGroup) parent).setClipChildren(false); ((ViewGroup) parent).setClipToPadding(false); }
            }
            if (container.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                params.leftMargin = params.rightMargin = dp(container, 22);
                params.bottomMargin = dp(container, 14);
                params.topMargin = dp(container, 6);
                container.setLayoutParams(params);
            }
            container.setBackground(rounded(palette.container, dp(container, 40)));
            container.setClipToOutline(true);
            container.setElevation(dp(container, 6));
            View tabs = bottomTabsId == 0 ? null : container.findViewById(bottomTabsId);
            if (tabs == null) return;
            tabs.setBackgroundColor(0);
            try {
                Class<?> type = tabs.getClass();
                GradientDrawable pill = rounded(palette.secondaryContainer, dp(tabs, 16));
                call(type, tabs, "setSelectedTabIndicator", Drawable.class, new InsetDrawable(pill, dp(tabs, 10), dp(tabs, 10), dp(tabs, 10), dp(tabs, 10)));
                call(type, tabs, "setSelectedTabIndicatorColor", int.class, palette.secondaryContainer);
                call(type, tabs, "setSelectedTabIndicatorGravity", int.class, 3);
                call(type, tabs, "setTabIndicatorFullWidth", boolean.class, true);
                call(type, tabs, "setTabRippleColor", ColorStateList.class, ColorStateList.valueOf(0));
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }

        void statusBar(ViewGroup layout) {
            if (layout.getTag(STATUS_TAG) != null) return;
            layout.setTag(STATUS_TAG, Boolean.TRUE);
            try { call(layout.getClass(), layout, "setStatusBarBackgroundColor", int.class, palette.surface); } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }

        /** The hairline under the top tabs: the M3 header has no divider. */
        void flat(ViewGroup holder) {
            holder.setBackgroundColor(palette.surface);
            for (int index = 0; index < holder.getChildCount(); index++) holder.getChildAt(index).setBackground(null);
        }

        /** Pull to refresh in Material You colours. */
        void refresh(ViewGroup layout) {
            if (layout.getTag(CARD_TAG) != null) return;
            layout.setTag(CARD_TAG, Boolean.TRUE);
            try {
                layout.getClass().getMethod("setColorSchemeColors", int[].class).invoke(layout, (Object) new int[]{palette.primary});
                call(layout.getClass(), layout, "setProgressBackgroundColorSchemeColor", int.class, palette.container);
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }

        /** Post button: an M3 floating action button, a rounded square in the primary container colour. */
        void fab(View button) {
            if (button.getTag(CARD_TAG) != null) return;
            button.setTag(CARD_TAG, Boolean.TRUE);
            // A Material FloatingActionButton keeps its own background, so use its colour and shape setters.
            button.setBackgroundTintList(ColorStateList.valueOf(palette.primaryContainer));
            if (button instanceof ImageView) ((ImageView) button).setImageTintList(ColorStateList.valueOf(palette.onPrimaryContainer));
            try {
                Class<?> type = button.getClass();
                call(type, button, "setRippleColor", ColorStateList.class, ColorStateList.valueOf(palette.onPrimaryContainer & 0x33ffffff));
                // Shape model -> builder, set every corner, build. Obfuscated, so matched by signature.
                Method getter = type.getMethod("getShapeAppearanceModel");
                Object shape = getter.invoke(button), builder = null, corners = null;
                for (Method method : shape.getClass().getMethods())
                    if (method.getParameterTypes().length == 0 && method.getReturnType().getName().equals(shape.getClass().getName() + "$a")) { builder = method.invoke(shape); break; }
                if (builder != null) {
                    for (Method method : builder.getClass().getMethods())
                        if (method.getReturnType() == void.class && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == float.class) method.invoke(builder, (float) dp(button, 16));
                    for (Method method : builder.getClass().getMethods())
                        if (method.getParameterTypes().length == 0 && method.getReturnType() == shape.getClass()) { corners = method.invoke(builder); break; }
                }
                if (corners != null) type.getMethod("setShapeAppearanceModel", getter.getReturnType()).invoke(button, corners);
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }

        static void call(Class<?> type, Object target, String name, Class<?> parameter, Object value) throws ReflectiveOperationException {
            Method method = type.getMethod(name, parameter);
            method.invoke(target, value);
        }
    }
}
