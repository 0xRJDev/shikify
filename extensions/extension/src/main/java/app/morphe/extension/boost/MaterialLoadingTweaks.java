package app.morphe.extension.boost;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.WeakHashMap;

/** Retains native gesture/refresh state, animations, callbacks, measurement and insets. */
public final class MaterialLoadingTweaks {
    private static final WeakHashMap<Object,Refresh> REFRESH=new WeakHashMap<>();
    private static final WeakHashMap<View,Wheel> WHEELS=new WeakHashMap<>();
    private MaterialLoadingTweaks() { }
    public static void onProgressCreated(Object instance) {
        View view = (View) instance;
        view.setBackground(new MaterialLoadingDrawable(view.getContext()));
        view.setVisibility(View.GONE);
        int label = view.getResources().getIdentifier("m3_loading_indicator_content_description",
                "string", view.getContext().getPackageName());
        view.setContentDescription(view.getContext().getString(label));
        view.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host,
                    AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(ProgressBar.class.getName());
            }
        });
    }
    public static void startProgress(Object instance) {
        View view = (View) instance;
        View parent = view.getParent() instanceof View ? (View) view.getParent() : view;
        int refreshId = view.getResources().getIdentifier("swipe_refresh_layout", "id",
                view.getContext().getPackageName());
        View refresh = parent.findViewById(refreshId);
        if (refresh != null && Boolean.TRUE.equals(CommentsChromeTweaks.field(refresh, "c"))) {
            ((Animatable) view.getBackground()).stop();
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.getBackground().setVisible(view.isAttachedToWindow() && view.isShown()
                && view.getWindowVisibility() == View.VISIBLE, false);
        ((Animatable) view.getBackground()).start();
        place(view, parent);
        view.post(() -> place(view, parent));
    }
    /**
     * An empty screen keeps the indicator centred. When posts are already showing, more are loading
     * below them, so the indicator moves to the bottom, clear of the floating navigation bar.
     */
    private static void place(View view, View parent) {
        View list = SurfaceAppearanceTweaks.find(parent, "recyclerview");
        boolean content = list instanceof android.view.ViewGroup && ((android.view.ViewGroup) list).getChildCount() > 0;
        if (!content || view.getHeight() == 0 || parent.getHeight() == 0) {
            view.setTranslationY(0);
            return;
        }
        int[] location = new int[2];
        parent.getLocationInWindow(location);
        float limit = location[1] + parent.getHeight();
        View root = view.getRootView();
        android.view.WindowInsets insets = root.getRootWindowInsets();
        if (insets != null) limit = Math.min(limit, root.getHeight() - insets.getSystemWindowInsetBottom());
        View nav = SurfaceAppearanceTweaks.find(root, "bottom_navigation_view");
        if (nav == null) nav = SurfaceAppearanceTweaks.find(root, "bottom_navigation");
        if (nav != null && nav.isShown() && nav.getHeight() > 0) {
            nav.getLocationInWindow(location);
            if (location[1] > 0) limit = Math.min(limit, location[1]);
        }
        float gap = 16 * view.getResources().getDisplayMetrics().density;
        view.getLocationInWindow(location);
        float restingTop = location[1] - view.getTranslationY();
        view.setTranslationY(limit - gap - view.getHeight() - restingTop);
    }
    public static void stopProgress(Object instance) {
        View view = (View) instance;
        ((Animatable) view.getBackground()).stop();
        view.setVisibility(View.GONE);
    }
    public static void onDialogShown(Object instance) {
        if (!(instance instanceof Dialog)) {
            return;
        }
        Dialog dialog = (Dialog) instance;
        View view = dialog.findViewById(android.R.id.progress);
        if (!(view instanceof ProgressBar)) {
            return;
        }
        ProgressBar bar = (ProgressBar) view;
        Drawable drawable = bar.getIndeterminateDrawable();
        if (bar.isIndeterminate() && drawable != null && drawable.getClass().getName().equals(
                "me.zhanghai.android.materialprogressbar.IndeterminateCircularProgressDrawable")) {
            bar.setIndeterminateDrawable(new MaterialLoadingDrawable(bar.getContext()));
        }
    }
    public static void onRefreshCreated(Object owner) {
        if(!(owner instanceof View))return;
        try {
            Object icon=CommentsChromeTweaks.field(owner,"N"),drawable=CommentsChromeTweaks.field(owner,"T");
            if(!(icon instanceof ImageView) || !(drawable instanceof Drawable))return;
            Refresh state=new Refresh((View)owner,(ImageView)icon);
            REFRESH.put(drawable,state);state.theme();
        }catch(ReflectiveOperationException | RuntimeException ignored) { }
    }
    public static void onPull(Object owner,float distance) {
        Object drawable=CommentsChromeTweaks.field(owner,"T");Refresh state=REFRESH.get(drawable);
        if(state==null)return;
        try {float threshold=state.threshold.getFloat(owner),before=state.progress;state.progress=Math.max(0,Math.min(1,distance/Math.max(1,threshold)));if(before<1 && state.progress>=1 && owner instanceof View)Haptics.tick((View)owner);}
        catch(IllegalAccessException ignored) { }
    }
    public static void onRefreshStopped(Object drawable) {
        Refresh state=REFRESH.get(drawable);if(state!=null){state.indicator.reset();state.progress=0;}
    }
    public static boolean drawRefresh(Object object,Canvas canvas) {
        Refresh state=REFRESH.get(object);if(state==null || !(object instanceof Drawable))return false;
        View owner=state.owner.get();ImageView icon=state.icon.get();if(owner==null || icon==null)return false;
        try {
            Drawable drawable=(Drawable)object;Rect bounds=drawable.getBounds();
            boolean loading=state.refreshing.getBoolean(owner) || ((Animatable)drawable).isRunning();
            state.theme();
            // Native CircularProgressDrawable owns the frame driver. No second timer,
            // overlay, pre-draw scan or refresh listener is installed.
            state.indicator.draw(canvas,bounds.exactCenterX(),bounds.exactCenterY(),Math.min(bounds.width(),bounds.height()),
                drawable.getAlpha(),loading,state.progress,scale(icon.getContext(),state.legacyScale),System.nanoTime());
            return true;
        }catch(IllegalAccessException ignored) {return false;}
    }
    public static boolean drawWheel(Object object,Canvas canvas) {
        if(!(object instanceof View))return false;View view=(View)object;
        Wheel state=WHEELS.get(view);
        if(state==null) {
            try {state=new Wheel(view);WHEELS.put(view,state);}
            catch(ReflectiveOperationException | RuntimeException ignored){return false;}
        }
        try {
            if(!state.spinning.getBoolean(view)){state.indicator.reset();return false;}
            float size=Math.min(view.getWidth()-view.getPaddingLeft()-view.getPaddingRight(),view.getHeight()-view.getPaddingTop()-view.getPaddingBottom());
            size=Math.min(size,48*view.getResources().getDisplayMetrics().density);
            float scale=scale(view.getContext(),state.legacyScale);
            state.indicator.draw(canvas,view.getPaddingLeft()+(view.getWidth()-view.getPaddingLeft()-view.getPaddingRight())/2f,
                view.getPaddingTop()+(view.getHeight()-view.getPaddingTop()-view.getPaddingBottom())/2f,size,255,true,1,scale,System.nanoTime());
            if(scale>0 && view.isAttachedToWindow() && view.isShown() && view.getWindowVisibility()==View.VISIBLE)view.postInvalidateOnAnimation();
            return true;
        }catch(IllegalAccessException ignored){return false;}
    }
    private static Field mapped(Class<?> type,String name) throws NoSuchFieldException {
        for(Class<?> current=type;current!=null;current=current.getSuperclass()) {
            try {Field field=current.getDeclaredField(name);field.setAccessible(true);return field;}
            catch(NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }
    private static float legacyScale(Context context) {
        if(Build.VERSION.SDK_INT>=33)return 1;
        try{return Math.max(0,Settings.Global.getFloat(context.getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1));}
        catch(RuntimeException ignored){return 1;}
    }
    private static float scale(Context context,float fallback) {
        return Build.VERSION.SDK_INT>=33 ? ValueAnimator.getDurationScale():fallback;
    }
    private static final class Refresh {
        final WeakReference<View> owner;
        final WeakReference<ImageView> icon;
        final Field refreshing,threshold;
        final MaterialLoadingIndicator indicator;
        final int container;
        final float legacyScale;
        Drawable background;
        float progress;
        Refresh(View view,ImageView image) throws ReflectiveOperationException {
            owner=new WeakReference<>(view);icon=new WeakReference<>(image);
            refreshing=mapped(view.getClass(),"c");threshold=mapped(view.getClass(),"z");
            container=MaterialYouTweaks.color(view.getContext(),"colorPrimaryContainer",android.R.attr.colorBackground);
            indicator=new MaterialLoadingIndicator(MaterialYouTweaks.color(view.getContext(),"colorOnPrimaryContainer",android.R.attr.colorAccent));
            legacyScale=legacyScale(view.getContext());
        }
        void theme() {
            ImageView view=icon.get();if(view==null)return;
            // Boost changes the original circle colour while configuring its theme.
            // Colour only the existing ShapeDrawable; retain its alpha/elevation.
            Drawable current=view.getBackground();
            if(current instanceof android.graphics.drawable.ShapeDrawable) {
                android.graphics.Paint paint=((android.graphics.drawable.ShapeDrawable)current).getPaint();
                if((paint.getColor()&0xffffff)!=(container&0xffffff)){int alpha=paint.getAlpha();paint.setColor(container);paint.setAlpha(alpha);current.invalidateSelf();}
            } else if(background!=current) {view.setBackgroundColor(container);}
            background=view.getBackground();
        }
    }
    private static final class Wheel {
        final Field spinning;
        final MaterialLoadingIndicator indicator;
        final float legacyScale;
        Wheel(View view) throws ReflectiveOperationException {
            spinning=mapped(view.getClass(),"O");
            indicator=new MaterialLoadingIndicator(MaterialYouTweaks.color(view.getContext(),"colorPrimary",android.R.attr.colorAccent));
            legacyScale=legacyScale(view.getContext());
        }
    }
}
