package com.github.lifecycle;

import java.util.List;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public abstract class MonitoredApplication extends Application {
	public final static int APPLICATION_STATE_UNKNOWN = -1;
	
	public final static int APPLICATION_STATE_NORMAL = 1;
	public final static int APPLICATION_STATE_FIRST_RUN = 2;
	public final static int APPLICATION_STATE_FIRST_RUN_AFTER_UPDATE = 3;
	
	private final static String PREFERENCES_VERSION_KEY = "version";
	
	private static MonitoredApplication instance;
	
	private static boolean applicationWasBroughtToBackground = false;
	private static boolean applicationWasClosed = false;
	
	private int _applicationState = APPLICATION_STATE_UNKNOWN;
	
	public MonitoredApplication() {
		instance = this;
	}

	public static Context getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		handleApplicationState(
			determineApplicationState());
	}

	/**
	 * Occurs when the base activity is paused by pressing back.
	 */
	public void onClose() {
		applicationWasClosed = true;
		applicationWasBroughtToBackground = false;
	}

	/**
	 * Occurs when the application detected that it was run for the first time.
	 * 
	 * This is determined by checking if the application version value was previously stored in Shared Preferences.   
	 * If it is not present, then it is assumed that the application is being run for the first time.
	 */
	protected void onApplicationFirstRun() {
		
	}
	
	/**
	 * Occurs when the application detected that it was run for the first time after being updated.
	 * 
	 * This is determined by checking whether the version value stored in Shared Preferences is 
	 * less than the current application version.
	 */
	protected void onApplicationUpdated() {
		
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
		
	private final void handleApplicationState(final int state) {
		switch (state) {
			case APPLICATION_STATE_NORMAL:
			default: 
				break;
				
			case APPLICATION_STATE_FIRST_RUN_AFTER_UPDATE: {
				onApplicationUpdated();
			} break;
			
			case APPLICATION_STATE_FIRST_RUN: {
				onApplicationFirstRun();
			} break;
		}
	}
	
	private final int determineApplicationState() {
		_applicationState = APPLICATION_STATE_UNKNOWN;
		
		boolean isFirstRun = false;
		boolean isFirstRunAfterUpdate = false;
		
		boolean isNormalRun = false;
		
		final SharedPreferences preferences = getSharedPreferences(getPreferencesNamespace(), MODE_PRIVATE);
		
		if (preferences != null) {
			final SharedPreferences.Editor editor = preferences.edit();
			
			final int applicationVersion = getApplicationVersionCode();
			final int preferencesVersion = preferences.getInt(PREFERENCES_VERSION_KEY, 0);
			
			if (preferencesVersion != applicationVersion) {
				if (preferencesVersion == 0) {
					isFirstRun = true;
				} else {
					if (preferencesVersion < applicationVersion) {
						isFirstRunAfterUpdate = true;
					}
				}
				
				editor.putInt(PREFERENCES_VERSION_KEY, applicationVersion);
				editor.commit();
			} else {
				isNormalRun = true;
			}
		}
		
		if (isFirstRun) {
			_applicationState = APPLICATION_STATE_FIRST_RUN;
		} else if (isFirstRunAfterUpdate) {
			_applicationState = APPLICATION_STATE_FIRST_RUN_AFTER_UPDATE;
		} else if (isNormalRun) {
			_applicationState = APPLICATION_STATE_NORMAL;
		}
		
		return _applicationState;
	}
		
	protected final int getApplicationVersionCode() {
		int applicationVersionCode = 0;
		
		PackageInfo packageInfo = null;
		
		try {
			packageInfo = getPackageManager().getPackageInfo(
				getPackageName(), 0);
		} catch (NameNotFoundException e) {
			
		}
		
		if (packageInfo != null) {
			applicationVersionCode = packageInfo.versionCode;
		}
		
		return applicationVersionCode;
	}
	
	public final int getApplicationState() {
		return _applicationState;
	}
	
	public final String getPreferencesNamespace() {
		return getPackageName() + ".preferences";
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
