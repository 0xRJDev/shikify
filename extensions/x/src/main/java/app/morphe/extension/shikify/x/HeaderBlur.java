package app.morphe.extension.shikify.x;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RecordingCanvas;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;

/**
 * Boost's progressive header blur, for a header that floats over content. Four blur layers of rising
 * strength fade out at different heights, so the blur is strongest at the top and eases off a little
 * below the header with no visible edge.
 */
@android.annotation.TargetApi(31)
final class HeaderBlur extends Drawable implements ViewTreeObserver.OnPreDrawListener {
    private static final int SCALE = 3;
    private static final float[] RADII = {3, 7, 13, 22}, STARTS = {.78f, .6f, .35f, .15f}, ENDS = {1f, .9f, .7f, .5f};
    static final int EXTRA_DP = 16;

    private final RenderNode capture = new RenderNode("ShikifyXHeaderCapture");
    private final RenderNode[] layers = new RenderNode[RADII.length];
    private final Paint[] masks = new Paint[RADII.length];
    private final Paint tint = new Paint();
    private final int[] contentLocation = new int[2], headerLocation = new int[2];
    private final WeakReference<View> content, header;
    private final int color, extra;
    private int maskHeight = -1;
    private boolean recording;
    private ViewTreeObserver observer;

    HeaderBlur(View content, View header, int color) {
        this.content = new WeakReference<>(content);
        this.header = new WeakReference<>(header);
        this.color = color & 0xffffff;
        float density = header.getResources().getDisplayMetrics().density;
        extra = Math.round(EXTRA_DP * density);
        for (int index = 0; index < RADII.length; index++) {
            float radius = RADII[index] * density / SCALE;
            layers[index] = new RenderNode("ShikifyXHeaderBlur" + index);
            layers[index].setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
            masks[index] = new Paint();
            masks[index].setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }
    }

    void start() {
        View view = header.get();
        if (view == null) return;
        stop();
        observer = view.getViewTreeObserver();
        observer.addOnPreDrawListener(this);
    }

    void stop() {
        if (observer != null && observer.isAlive()) observer.removeOnPreDrawListener(this);
        observer = null;
    }

    /** Redraw whenever the content under the header changes, such as while it scrolls. */
    @Override public boolean onPreDraw() {
        View view = content.get();
        if (view != null && view.isDirty()) invalidateSelf();
        return true;
    }

    private void masks(int height) {
        if (height == maskHeight) return;
        maskHeight = height;
        for (int index = 0; index < RADII.length; index++)
            masks[index].setShader(new LinearGradient(0, 0, 0, height, new int[]{0xff000000, 0xff000000, 0, 0}, new float[]{0, STARTS[index], ENDS[index], 1}, Shader.TileMode.CLAMP));
        tint.setShader(new LinearGradient(0, 0, 0, height, new int[]{0x66000000 | color, 0x40000000 | color, 0x26000000 | color, color}, new float[]{0, .6f, .78f, 1}, Shader.TileMode.CLAMP));
    }

    @Override public void draw(Canvas canvas) {
        View source = content.get(), bar = header.get();
        if (recording || source == null || bar == null || !canvas.isHardwareAccelerated()) return;
        int width = getBounds().width(), height = getBounds().height() + extra;
        if (width <= 0 || height <= extra) return;
        masks(height);
        int scaledWidth = Math.max(1, (int) Math.ceil(width / (float) SCALE)), scaledHeight = Math.max(1, (int) Math.ceil(height / (float) SCALE));
        capture.setPosition(0, 0, scaledWidth, scaledHeight);
        for (RenderNode layer : layers) layer.setPosition(0, 0, scaledWidth, scaledHeight);
        RecordingCanvas recorder = capture.beginRecording();
        recording = true;
        try {
            recorder.drawColor(0xff000000 | color);
            recorder.scale(1f / SCALE, 1f / SCALE);
            recorder.clipRect(0, 0, width, height);
            source.getLocationInWindow(contentLocation);
            bar.getLocationInWindow(headerLocation);
            recorder.translate(contentLocation[0] - headerLocation[0], contentLocation[1] - headerLocation[1]);
            source.draw(recorder);
        } catch (RuntimeException ignored) {
        } finally {
            capture.endRecording();
            recording = false;
        }
        for (RenderNode layer : layers) {
            RecordingCanvas blur = layer.beginRecording();
            blur.drawRenderNode(capture);
            layer.endRecording();
        }
        for (int index = 0; index < layers.length; index++) {
            float end = height * ENDS[index];
            int saved = canvas.saveLayer(0, 0, width, end, null);
            int transform = canvas.save();
            canvas.scale(SCALE, SCALE);
            canvas.drawRenderNode(layers[index]);
            canvas.restoreToCount(transform);
            canvas.drawRect(0, 0, width, end, masks[index]);
            canvas.restoreToCount(saved);
        }
        canvas.drawRect(0, 0, width, height, tint);
    }

    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter filter) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
