package app.morphe.extension.boost;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** Boost's option menus float like the bottom bar; other dialogs stay centred. */
public final class DialogTweaks {
    private DialogTweaks() { }
    public static void onShow(Object instance) {
        if(!(instance instanceof Dialog))return;
        try {
            Dialog dialog=(Dialog)instance;Window window=dialog.getWindow();if(window==null)return;
            Context context=dialog.getContext();
            if(Build.VERSION.SDK_INT>=31 && !SurfaceSettingsActivity.enabled(context,"dialog_blur"))window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            View menuView=find(window.getDecorView());
            if(menuView!=null)ShareHold.attach(dialog,menuView);
            if(menuView!=null && Build.VERSION.SDK_INT>=31 && SurfaceSettingsActivity.enabled(context,"glass_menus",true))glass(window,context);
            if(!SurfaceSettingsActivity.enabled(context,"material_dialogs") || menuView==null)return;
            float density=context.getResources().getDisplayMetrics().density;int margin=Math.round(12*density);
            WindowManager.LayoutParams params=window.getAttributes();
            params.width=Math.min(context.getResources().getDisplayMetrics().widthPixels-margin*2,Math.round(640*density));
            params.height=WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;params.y=margin;
            int animation=context.getResources().getIdentifier("Animation.Material3.BottomSheetDialog","style",context.getPackageName());
            if(animation!=0)params.windowAnimations=animation;
            window.setAttributes(params);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }catch(RuntimeException error){}
    }
    /**
     * Glass menus: instead of blurring the whole screen behind a post or comment menu, the menu
     * itself is a frosted bubble. Android blurs only what's behind the window, clipped to its shape.
     */
    @android.annotation.TargetApi(31)
    private static void glass(Window window,Context context) {
        float density=context.getResources().getDisplayMetrics().density;
        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int base=SurfaceAppearanceTweaks.role(context,true)&0xffffff;
        android.graphics.drawable.GradientDrawable bubble=new android.graphics.drawable.GradientDrawable();
        bubble.setColor(0xb8000000|base);bubble.setCornerRadius(28*density);
        window.setBackgroundDrawable(bubble);
        window.setBackgroundBlurRadius(Math.round(28*density));
        View content=window.getDecorView().findViewById(android.R.id.content);
        // The menu's own card would hide the glass; the window background is the card now.
        if(content instanceof ViewGroup)for(int index=0;index<((ViewGroup)content).getChildCount();index++)((ViewGroup)content).getChildAt(index).setBackground(null);
    }
    static View find(View view) {
        if(view.getClass().getName().endsWith(".menu.MenuView"))return view;
        if(!(view instanceof ViewGroup))return null;
        ViewGroup group=(ViewGroup)view;
        for(int index=0;index<group.getChildCount();index++){View found=find(group.getChildAt(index));if(found!=null)return found;}
        return null;
    }
}
