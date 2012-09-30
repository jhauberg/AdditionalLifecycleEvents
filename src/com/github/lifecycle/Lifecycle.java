package com.github.lifecycle;

public final class Lifecycle extends MonitoredApplication {
	@Override
	public void onBroughtToBackground() {
		/*
		 * An activity from the application was brought to the background, 
		 * usually due to a press on the home button.
		 * 
		 * Pause any on-going tasks that should not happen in the background.
		 */
	}
	
	@Override
	public void onBroughtToForeground() {
		/*
		 * An activity was brought from the background to the foreground.
		 * 
		 * Resume any tasks that were paused.
		 */
	}
	
	@Override
	public void onClose() {
		/*
		 * Back was pressed from the base activity.
		 * 
		 * Some applications might want to handle this scenario specifically, though
		 * this could just as well be considered as being brought to the background.
		 * 
		 * (Note that onBroughtToBackground will not be called in this case)
		 */
	}
}
