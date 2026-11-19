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
    private static final int CARD_TAG = 0x7f5e0001, STATUS_TAG = 0x7f5e0002, GLASS_TAG = 0x7f5e0003, PAD_TAG = 0x7f5e0004, TOP_TAG = 0x7f5e0005, ROW_TAG = 0x7f5e0006, BASE_TAG = 0x7f5e0007, INSET_TAG = 0x7f5e0008;
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
        int listId, rowId, fabId, topTabsId, bottomTabsId, bottomContainerId, appBarId, toolbarId, fleetBarId, fleetHolderId, bottomBarId, slidingId, chromeInnerId, pagerId, replyId, separatorId, fragmentId, connectorTopId, connectorBottomId, dotsId;
        java.lang.ref.WeakReference<View> replyBar = new java.lang.ref.WeakReference<>(null), headerBar = new java.lang.ref.WeakReference<>(null);

        Screen(Activity activity) {
            this.activity = activity;
            listId = id("list"); rowId = id("outer_layout_row_view_tweet"); fabId = id("composer_write");
            topTabsId = id("tab_layout"); bottomTabsId = id("tabs"); bottomContainerId = id("tabsContainer");
            appBarId = id("app_bar"); toolbarId = id("toolbar"); fleetBarId = id("fleetline_app_bar");
            fleetHolderId = id("fleetline_holder"); bottomBarId = id("bottom_bar"); slidingId = id("tabsSlidingContainer");
            chromeInnerId = id("gallery_chrome_inner_root"); pagerId = id("pager"); replyId = id("persistent_reply"); separatorId = id("top_separator"); fragmentId = id("fragment_container");
            connectorTopId = id("tweet_connector_top"); connectorBottomId = id("tweet_connector_bottom"); dotsId = id("connector");
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
            if (id == toolbarId) { surface(group, 0); toolbarGlass(group); }
            if (id != bottomTabsId && isA(group, "com.google.android.material.tabs.TabLayout")) pillTabs(group);
            if (isA(group, "com.google.android.material.appbar.AppBarLayout")) surface(group, palette.surface);
            // The drawer and coordinator paint the status bar area themselves; match it to the header.
            if (isA(group, "androidx.drawerlayout.widget.DrawerLayout") || isA(group, "androidx.coordinatorlayout.widget.CoordinatorLayout")) statusBar(group);
            if (id == chromeInnerId && chromeInnerId != 0) galleryStatus(group);
            if (id == replyId && replyId != 0) replyPill(group);
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
            if (view.getTag(CARD_TAG) == null && view.getTag(GLASS_TAG) == null) { view.setTag(CARD_TAG, Boolean.TRUE); view.setBackgroundColor(color); }
        }

        /** Timeline lists: an M3 surface with each post on its own rounded card. */
        void list(ViewGroup list) {
            if (list.getTag(CARD_TAG) == null) {
                list.setTag(CARD_TAG, Boolean.TRUE);
                list.setBackgroundColor(palette.surface);
                list.setClipToPadding(false);
                list.setPadding(list.getPaddingLeft(), list.getPaddingTop() + dp(list, 4), list.getPaddingRight(), list.getPaddingBottom());
            }
            replyPadding(list);
            headerPadding(list);
            for (int index = 0; index < list.getChildCount(); index++) {
                View child = list.getChildAt(index);
                if (child.getTag(CARD_TAG) != null) continue;
                // Rows sit in a GroupedRowView, which draws the dividers between them.
                boolean grouped = child.getClass().getName().endsWith("GroupedRowView");
                View row = child.getId() == rowId ? child : rowId == 0 ? null : child.findViewById(rowId);
                // Any other grouped row (people, notifications, settings rows) gets a card around its content.
                if (row == null && grouped && child instanceof ViewGroup && ((ViewGroup) child).getChildCount() > 0) row = ((ViewGroup) child).getChildAt(0);
                if (grouped) clearRowPaint(child);
                if (row != null && row.getTag(CARD_TAG) == null) { card(row); child.setTag(CARD_TAG, Boolean.TRUE); child.setTag(ROW_TAG, row); }
            }
            // Threads: a post linked to the next one shares its card. Rows are recycled, so check every frame.
            for (int index = 0; index < list.getChildCount(); index++) {
                Object row = list.getChildAt(index).getTag(ROW_TAG);
                if (row instanceof View) thread((View) row);
            }
        }

        /** Joins a post's card to its neighbours when X draws the thread line through it. */
        void thread(View row) {
            if (!(row.getBackground() instanceof Card)) return;
            Card card = (Card) row.getBackground();
            View top = connectorTopId == 0 ? null : row.findViewById(connectorTopId), bottom = connectorBottomId == 0 ? null : row.findViewById(connectorBottomId);
            boolean up = top != null && top.getVisibility() == View.VISIBLE, down = bottom != null && bottom.getVisibility() == View.VISIBLE;
            // "Show replies" under a thread is its own row with X's thread dots: it finishes the card above.
            boolean tail = top == null && bottom == null && dotsId != 0 && row.findViewById(dotsId) != null;
            if (tail) { up = true; down = false; }
            if (up == card.up && down == card.down) return;
            card.up = up; card.down = down;
            int[] base = (int[]) row.getTag(BASE_TAG);
            // The "Show replies" link gets room under it before the card's rounded bottom.
            int extra = tail ? dp(row, 12) : 0;
            if (base != null) row.setPadding(base[0] + card.side, base[1] + (up ? 0 : card.gap), base[2] + card.side, base[3] + (down ? 0 : card.gap) + extra);
            card.invalidateSelf();
        }

        /** A rounded card that squares and closes the edge it shares with a linked post. */
        static final class Card extends Drawable {
            final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            final android.graphics.Path path = new android.graphics.Path();
            final android.graphics.RectF rect = new android.graphics.RectF();
            final int side, gap; final float radius; boolean up, down;
            Card(int color, int side, int gap, float radius) { paint.setColor(color); this.side = side; this.gap = gap; this.radius = radius; }
            @Override public void draw(android.graphics.Canvas canvas) {
                android.graphics.Rect bounds = getBounds();
                rect.set(bounds.left + side, bounds.top + (up ? 0 : gap), bounds.right - side, bounds.bottom - (down ? 0 : gap));
                float top = up ? 0 : radius, bottom = down ? 0 : radius;
                path.reset();
                path.addRoundRect(rect, new float[]{top, top, top, top, bottom, bottom, bottom, bottom}, android.graphics.Path.Direction.CW);
                canvas.drawPath(path, paint);
            }
            @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
            @Override public void setColorFilter(android.graphics.ColorFilter filter) { paint.setColorFilter(filter); }
            @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
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
            row.setTag(BASE_TAG, new int[]{row.getPaddingLeft(), row.getPaddingTop(), row.getPaddingRight(), row.getPaddingBottom()});
            row.setBackground(new Card(palette.card, side, gap, dp(row, 22)));
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
            tabs.setBackgroundColor(0);
            try {
                Class<?> type = tabs.getClass();
                GradientDrawable pill = rounded(palette.secondaryContainer, dp(tabs, 20));
                // Fixed tabs are wide, so the pill is inset; scrolling tabs hug their label.
                int count = 0;
                try { count = (Integer) type.getMethod("getTabCount").invoke(tabs); } catch (ReflectiveOperationException | RuntimeException ignored) { }
                boolean scrolling = count > 3;
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
            // Boost's frosted glass: the feed behind blurs through the floating bar.
            View source = glassSource(container);
            if (android.os.Build.VERSION.SDK_INT >= 31 && source != null) {
                FeedPillBlur glass = new FeedPillBlur(source, container, palette.container, false, 40, false, 0, 1);
                container.setBackground(glass);
                glass.start();
            }
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

        /** "Post your reply" under a post: a floating glass pill like the bottom bar. */
        void replyPill(ViewGroup reply) {
            if (reply.getTag(CARD_TAG) != null) return;
            reply.setTag(CARD_TAG, Boolean.TRUE);
            View separator = separatorId == 0 ? null : reply.findViewById(separatorId);
            if (separator != null) separator.setVisibility(View.GONE);
            for (int index = 0; index < reply.getChildCount(); index++) reply.getChildAt(index).setBackground(null);
            clearFields(reply);
            if (reply.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) reply.getLayoutParams();
                params.leftMargin = params.rightMargin = dp(reply, 14);
                params.bottomMargin = dp(reply, 12);
                reply.setLayoutParams(params);
            }
            reply.setPadding(reply.getPaddingLeft() + dp(reply, 8), reply.getPaddingTop(), reply.getPaddingRight() + dp(reply, 4), reply.getPaddingBottom());
            reply.setBackground(rounded(palette.container, dp(reply, 28)));
            reply.setClipToOutline(true);
            reply.setElevation(dp(reply, 6));
            replyBar = new java.lang.ref.WeakReference<>(reply);
            if (!(reply.getParent() instanceof ViewGroup)) return;
            ViewGroup parent = (ViewGroup) reply.getParent();
            // The replies stop above the bar; let them run under it so the pill floats over them.
            View source = fragmentId == 0 ? null : parent.findViewById(fragmentId);
            if (source != null && source.getLayoutParams() instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) source.getLayoutParams();
                params.removeRule(android.widget.RelativeLayout.ABOVE);
                params.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
                source.setLayoutParams(params);
            }
            reply.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (bottom - top != oldBottom - oldTop && source instanceof ViewGroup) {
                    View list = source.findViewById(android.R.id.list);
                    if (list != null) replyPadding(list);
                }
            });
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                if (source != null) {
                    FeedPillBlur glass = new FeedPillBlur(source, reply, palette.container, false, 28, false, 0, 1);
                    reply.setBackground(glass);
                    glass.start();
                }
            }
        }

        /** The reply field's underline: the pill is the field's outline. */
        static void clearFields(View view) {
            if (view instanceof android.widget.EditText) view.setBackground(null);
            if (view instanceof ViewGroup) for (int index = 0; index < ((ViewGroup) view).getChildCount(); index++) clearFields(((ViewGroup) view).getChildAt(index));
        }

        /**
         * A post's header: the replies run up under it and it turns to Boost's frosted glass, the same
         * way the reply pill floats over them. Only where the toolbar sits above the replies in a plain layout.
         */
        void toolbarGlass(ViewGroup toolbar) {
            if (android.os.Build.VERSION.SDK_INT < 31 || toolbar.getTag(GLASS_TAG) != null || !(toolbar.getParent() instanceof android.widget.RelativeLayout)) return;
            ViewGroup parent = (ViewGroup) toolbar.getParent();
            View source = fragmentId == 0 ? null : parent.findViewById(fragmentId);
            if (source == null || source.getParent() != parent || !(source.getLayoutParams() instanceof android.widget.RelativeLayout.LayoutParams)) return;
            toolbar.setTag(GLASS_TAG, Boolean.TRUE);
            android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) source.getLayoutParams();
            params.removeRule(android.widget.RelativeLayout.BELOW);
            params.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP);
            source.setLayoutParams(params);
            toolbar.bringToFront();
            headerBar = new java.lang.ref.WeakReference<>(toolbar);
            underStatusBar(toolbar);
            // Boost's progressive blur: strongest at the top, easing off a little below the header.
            parent.setClipChildren(false);
            HeaderBlur blur = new HeaderBlur(source, toolbar, palette.surface);
            toolbar.setBackground(blur);
            blur.start();
            toolbar.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                underStatusBar(toolbar);
                View list = source.findViewById(android.R.id.list);
                if (list != null) headerPadding(list);
            });
        }

        /**
         * The page starts below the status bar, which then shows as a black strip above the blurred header.
         * Move the page up to the top and push only the header's contents down, so the blur covers it.
         */
        void underStatusBar(View toolbar) {
            View content = activity.findViewById(android.R.id.content), page = toolbar;
            while (page != null && page.getParent() != content) page = page.getParent() instanceof View ? (View) page.getParent() : null;
            if (page == null || !(content instanceof ViewGroup)) return;
            int status = page.getTop();
            if (status <= 0) return;
            if (content.getPaddingTop() == status) content.setPadding(content.getPaddingLeft(), 0, content.getPaddingRight(), content.getPaddingBottom());
            else if (page.getLayoutParams() instanceof ViewGroup.MarginLayoutParams && ((ViewGroup.MarginLayoutParams) page.getLayoutParams()).topMargin == status) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) page.getLayoutParams();
                params.topMargin = 0;
                page.setLayoutParams(params);
            } else return;
            Integer applied = (Integer) toolbar.getTag(INSET_TAG);
            if (applied == null || applied != status) {
                int previous = applied == null ? 0 : applied;
                toolbar.setTag(INSET_TAG, status);
                toolbar.setPadding(toolbar.getPaddingLeft(), toolbar.getPaddingTop() - previous + status, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
                ViewGroup.LayoutParams params = toolbar.getLayoutParams();
                if (params.height > 0) { params.height += status - previous; toolbar.setLayoutParams(params); }
            }
        }

        /** A list under the glass header starts below it. */
        void headerPadding(View list) {
            View header = headerBar.get();
            if (header == null || !header.isAttachedToWindow() || header.getHeight() == 0) return;
            View fragment = fragmentId == 0 ? null : header.getRootView().findViewById(fragmentId);
            if (fragment == null || !isDescendant(list, fragment)) return;
            Integer base = (Integer) list.getTag(TOP_TAG);
            if (base == null) { base = list.getPaddingTop(); list.setTag(TOP_TAG, base); }
            int top = base + header.getHeight();
            if (list.getPaddingTop() != top) list.setPadding(list.getPaddingLeft(), top, list.getPaddingRight(), list.getPaddingBottom());
            if (list instanceof ViewGroup) ((ViewGroup) list).setClipToPadding(false);
        }

        /** A list under the reply pill keeps its last row clear of the pill. */
        void replyPadding(View list) {
            View reply = replyBar.get();
            if (reply == null || !reply.isAttachedToWindow() || reply.getHeight() == 0) return;
            View fragment = fragmentId == 0 ? null : reply.getRootView().findViewById(fragmentId);
            if (fragment == null || !isDescendant(list, fragment)) return;
            Integer base = (Integer) list.getTag(PAD_TAG);
            if (base == null) { base = list.getPaddingBottom(); list.setTag(PAD_TAG, base); }
            int bottom = base + reply.getHeight() + dp(reply, 20);
            if (list.getPaddingBottom() != bottom) list.setPadding(list.getPaddingLeft(), list.getPaddingTop(), list.getPaddingRight(), bottom);
            if (list instanceof ViewGroup) ((ViewGroup) list).setClipToPadding(false);
        }

        static boolean isDescendant(View view, View ancestor) {
            for (View current = view; current != null; current = current.getParent() instanceof View ? (View) current.getParent() : null)
                if (current == ancestor) return true;
            return false;
        }

        /** The feed the floating bar sits over: its coordinator's full-size content child. */
        View glassSource(View bar) {
            View coordinator = bar;
            while (coordinator != null && !isA(coordinator, "androidx.coordinatorlayout.widget.CoordinatorLayout"))
                coordinator = coordinator.getParent() instanceof View ? (View) coordinator.getParent() : null;
            if (!(coordinator instanceof ViewGroup)) return null;
            View pager = pagerId == 0 ? null : coordinator.findViewById(pagerId);
            if (pager != null) return pager;
            ViewGroup group = (ViewGroup) coordinator;
            for (int index = 0; index < group.getChildCount(); index++) {
                View child = group.getChildAt(index);
                if (child.getHeight() >= group.getHeight() * 3 / 4 && child.findViewById(bar.getId()) == null) return child;
            }
            return null;
        }

        /**
         * A header over a post list turns into frosted glass: the list slides up under it (Material's
         * header overlap) and is padded by the header's height, so posts scroll behind the blur.
         */
        void headerGlass(ViewGroup coordinator) {
            View bar = null, content = null;
            for (int index = 0; index < coordinator.getChildCount(); index++) {
                View child = coordinator.getChildAt(index);
                if (isA(child, "com.google.android.material.appbar.AppBarLayout")) bar = child;
                else if (behavior(child) != null) content = child;
            }
            if (bar == null || content == null || !(content instanceof ViewGroup) || bar.getHeight() == 0) return;
            // Only the header right above a list: an outer header over another header would hide it.
            if (hasAppBar((ViewGroup) content)) return;
            View list = content.findViewById(android.R.id.list);
            if (!(list instanceof ViewGroup)) return;
            int overlap = bar.getHeight();
            Object behavior = behavior(content);
            try {
                // HeaderScrollingViewBehavior (obfuscated) holds two Rects then verticalLayoutGap and overlayTop.
                java.lang.reflect.Field top = null;
                for (Class<?> type = behavior.getClass(); type != null && top == null; type = type.getSuperclass()) {
                    int rects = 0; java.lang.reflect.Field last = null;
                    for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                        if (field.getType() == android.graphics.Rect.class) rects++;
                        if (field.getType() == int.class && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) last = field;
                    }
                    if (rects == 2 && last != null) top = last;
                }
                if (top == null) return;
                top.setAccessible(true);
                if (top.getInt(behavior) != overlap) { top.setInt(behavior, overlap); content.requestLayout(); }
            } catch (ReflectiveOperationException | RuntimeException error) { return; }
            Integer base = (Integer) list.getTag(PAD_TAG);
            if (base == null) { base = list.getPaddingTop(); list.setTag(PAD_TAG, base); }
            if (list.getPaddingTop() != base + overlap) list.setPadding(list.getPaddingLeft(), base + overlap, list.getPaddingRight(), list.getPaddingBottom());
            ((ViewGroup) list).setClipToPadding(false);
            if (bar.getTag(GLASS_TAG) == null) {
                bar.setTag(GLASS_TAG, Boolean.TRUE);
                FeedPillBlur glass = new FeedPillBlur(content, bar, palette.surface, false, 0, false, 30, 1);
                bar.setBackground(glass);
                bar.setOutlineProvider(null);
                glass.start();
            }
        }

        static Object behavior(View child) {
            Object params = child.getLayoutParams();
            if (params == null) return null;
            for (Class<?> type = params.getClass(); type != null; type = type.getSuperclass())
                for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(params);
                        if (value != null && isA(value, "com.google.android.material.appbar.AppBarLayout$ScrollingViewBehavior")) return value;
                    } catch (ReflectiveOperationException | RuntimeException ignored) { }
                }
            return null;
        }

        static boolean hasAppBar(ViewGroup group) {
            for (int index = 0; index < group.getChildCount(); index++) {
                View child = group.getChildAt(index);
                if (isA(child, "com.google.android.material.appbar.AppBarLayout")) return true;
                if (child instanceof ViewGroup && hasAppBar((ViewGroup) child)) return true;
            }
            return false;
        }

        /**
         * Image viewer: its controls sit on a translucent band that starts below the status bar, so the
         * status bar shows the bare image. Extend the band up behind it; it fades with the controls.
         */
        void galleryStatus(ViewGroup inner) {
            if (inner.getTag(STATUS_TAG) != null || inner.getTop() <= 0) return;
            inner.setTag(STATUS_TAG, Boolean.TRUE);
            int color = 0xb3000000;
            View toolbar = toolbarId == 0 ? null : inner.findViewById(toolbarId);
            if (toolbar != null && toolbar.getBackground() instanceof android.graphics.drawable.ColorDrawable)
                color = ((android.graphics.drawable.ColorDrawable) toolbar.getBackground()).getColor();
            if (inner.getParent() instanceof ViewGroup) ((ViewGroup) inner.getParent()).setClipChildren(false);
            final int band = color;
            Drawable previous = inner.getBackground();
            inner.setBackground(new Drawable() {
                final android.graphics.Paint paint = new android.graphics.Paint();
                @Override public void draw(android.graphics.Canvas canvas) {
                    if (previous != null) { previous.setBounds(getBounds()); previous.draw(canvas); }
                    paint.setColor(band);
                    canvas.drawRect(0, -inner.getTop(), getBounds().width(), 0, paint);
                }
                @Override public void setAlpha(int alpha) { }
                @Override public void setColorFilter(android.graphics.ColorFilter filter) { }
                @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
            });
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
