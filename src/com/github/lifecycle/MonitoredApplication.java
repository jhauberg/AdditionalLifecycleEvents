package com.github.lifecycle;

import java.util.List;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;

public abstract class MonitoredApplication extends Application {
	private static MonitoredApplication instance;
	
	private static boolean applicationWasBroughtToBackground = false;
	private static boolean applicationWasClosed = false;
	
	public MonitoredApplication() {
		instance = this;
	}

	public static Context getInstance() {
		return instance;
	}

	/**
	 * Occurs when the base activity is paused by pressing back.
	 */
	public void onClose() {
		applicationWasClosed = true;
		applicationWasBroughtToBackground = false;
	}

	/**
	 * Occurs when an activity is paused, usually due to a press on the home button.
	 */
	public void onBroughtToBackground() {
		
	}
	
	/**
	 * Occurs when an activity is resumed.
	 */
	public void onBroughtToForeground() {
		applicationWasClosed = false;
	}
	
	/**
     * Determines whether the application is in the background or not (i.e behind another application's activity).
     * 
     * @param context
     * @return true if another application is above this one.
     */
    public final static boolean isApplicationBroughtToBackground(final Context context) {
    	final List<RunningTaskInfo> tasks = getRunningTasks(context, 1);
    	
        if (tasks != null && !tasks.isEmpty()) {
            final ComponentName topActivity = tasks.get(0).topActivity;
            
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }
    
	public static void handleApplicationBroughtToBackground(final Context context) {
		if (isApplicationBroughtToBackground(context)) {
			applicationWasBroughtToBackground = true;
			
			((MonitoredApplication)context.getApplicationContext())
				.onBroughtToBackground();
		}
	}

	public static void handleApplicationBroughtToForeground(final Context context) {
		if (applicationWasBroughtToBackground || applicationWasClosed) {
			applicationWasBroughtToBackground = false;

			((MonitoredApplication)context.getApplicationContext())
				.onBroughtToForeground();
		}
	}

	/**
	 * Calls `onClose()` manually if necessary. 
	 * 
	 * If the activity history is traversed by pressing back, eventually the application's
	 * base activity will be reached. This method then determines if the next activity will
	 * be the launcher/homescreen, and if so, explicitly triggers the onClose event.
	 * 
	 * Note that activities have to call this manually in onKeyDown/onBackPressed! 
	 * See `com.github.activity.MonitoredActivity` for details.
	 * 
	 * @param context
	 */
	public static void handleApplicationClosing(final Context context) {
		final List<RunningTaskInfo> tasks = getRunningTasks(context, 2);

		if (tasks != null && tasks.size() > 1) {
			final RunningTaskInfo currentTask = tasks.get(0);
			final RunningTaskInfo nextTask = tasks.get(1);
	
			final boolean launcherWillAppearNext = 
				nextTask.baseActivity.getPackageName().indexOf("launcher") != -1;
			
			if (currentTask.topActivity.equals(currentTask.baseActivity) && 
				launcherWillAppearNext) {
				((MonitoredApplication)context.getApplicationContext())
					.onClose();
			}
		}
	}
	
	private final static List<RunningTaskInfo> getRunningTasks(final Context context, final int limit) {
		List<RunningTaskInfo> tasks = null;
		
        final ActivityManager activityManager = 
            	(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            
        if (activityManager != null) {
	        try {
	        	tasks = activityManager.getRunningTasks(limit);
	        } catch (SecurityException e) {
	        	throw e;
	        	// Oops, looks like someone forgot to declare 
	        	// <uses-permission android:name="android.permission.GET_TASKS" /> in the manifest.
	        }
        }

        return tasks;		
	}
}
