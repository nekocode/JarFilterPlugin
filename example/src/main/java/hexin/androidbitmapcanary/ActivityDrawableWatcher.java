package hexin.androidbitmapcanary;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ActivityDrawableWatcher {
    private static HashMap<Integer, ActivityDrawableWatcher.DrawableDetectListener> drawableListenerRecord;
    private final ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        public void onActivityCreated(Activity activity, Bundle bundle) {
            if (!(activity instanceof ImgMemoryActivity)) {
                View decorView = activity.getWindow().getDecorView();
                ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
                ActivityDrawableWatcher.DrawableDetectListener drawableDetectListener = new ActivityDrawableWatcher.DrawableDetectListener(decorView);
                viewTreeObserver.addOnGlobalLayoutListener(drawableDetectListener);
                ActivityDrawableWatcher.drawableListenerRecord.put(decorView.hashCode(), drawableDetectListener);
            }
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityResumed(Activity activity) {
            Log.d("new","onActivityResumed:"+activity);
        }

        public void onActivityPaused(Activity activity) {
        }

        public void onActivityStopped(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        public void onActivityDestroyed(Activity activity) {
            View decorView = activity.getWindow().getDecorView();
            ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
            ActivityDrawableWatcher.DrawableDetectListener detectListener = (ActivityDrawableWatcher.DrawableDetectListener)ActivityDrawableWatcher.drawableListenerRecord.get(decorView.hashCode());
            if (detectListener != null) {
                viewTreeObserver.removeOnGlobalLayoutListener(detectListener);
            }

        }
    };
    private final Application application;

    public static void watchDrawable(Application application) {
        (new ActivityDrawableWatcher(application)).startWatch();
    }

    public ActivityDrawableWatcher(Application application) {
        this.application = application;
        drawableListenerRecord = new HashMap();
    }

    public void startWatch() {
        this.stopWatch();
        this.application.registerActivityLifecycleCallbacks(this.lifecycleCallbacks);
    }

    public void stopWatch() {
        this.application.unregisterActivityLifecycleCallbacks(this.lifecycleCallbacks);
    }

    private static class DrawableDetectListener implements OnGlobalLayoutListener {
        private WeakReference<View> rootView;

        public DrawableDetectListener(View rootView) {
            this.rootView = new WeakReference(rootView);
        }

        public void onGlobalLayout() {
            if (this.rootView != null && this.rootView.get() != null && this.rootView.get() instanceof ViewGroup) {
                DrawableDetectUtil.detectDrawableSize((ViewGroup)this.rootView.get());
            }

        }
    }
}
