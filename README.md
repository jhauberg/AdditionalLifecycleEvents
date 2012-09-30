# Additional Lifecycle Events

This example project shows one way to get important application events such as being **brought to the background**, or **brought to the foreground**.

It is not an especially elegant solution, but at least it works. *On my phone*.

## Why

This is useful when you have **application-wide services** that needs to be **paused/resumed** when the application is not actually being used. It also helps with getting more realistic data when tracking user-sessions (time spent in app and such).

Basically, you'll get this:

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

Let's say you were doing the whole Google Analytics thing, then you could do this for more accurate data:

    private GoogleAnalyticsTracker _tracker;

    @Override
    public void onBroughtToForeground() {
        if (_tracker != null) {
            _tracker.startNewSession("**-********-*", 30, this);
        }
    }

    @Override
    public void onBroughtToBackground() {
        if (_tracker != null) {
            _tracker.stopSession();
        }
    }