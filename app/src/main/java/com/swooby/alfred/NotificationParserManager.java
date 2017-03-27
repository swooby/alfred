package com.swooby.alfred;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import com.smartfoo.android.core.FooListenerManager;
import com.smartfoo.android.core.FooRun;
import com.smartfoo.android.core.FooString;
import com.smartfoo.android.core.logging.FooLog;
import com.smartfoo.android.core.notification.FooNotificationListenerManager;
import com.smartfoo.android.core.notification.FooNotificationListenerManager.DisabledCause;
import com.smartfoo.android.core.notification.FooNotificationListenerManager.FooNotificationListenerManagerCallbacks;
import com.swooby.alfred.notification.parsers.AbstractNotificationParser;
import com.swooby.alfred.notification.parsers.AbstractNotificationParser.NotificationParseResult;
import com.swooby.alfred.notification.parsers.AbstractNotificationParser.NotificationParserCallbacks;
import com.swooby.alfred.notification.parsers.DownloadManagerNotificationParser;
import com.swooby.alfred.notification.parsers.GoogleCameraNotificationParser;
import com.swooby.alfred.notification.parsers.GoogleDialerNotificationParser;
import com.swooby.alfred.notification.parsers.GoogleHangoutsNotificationParser;
import com.swooby.alfred.notification.parsers.GoogleMyGlassNotificationParser;
import com.swooby.alfred.notification.parsers.GoogleNowNotificationParser;
import com.swooby.alfred.notification.parsers.GooglePhotosNotificationParser;
import com.swooby.alfred.notification.parsers.GooglePlayStoreNotificationParser;
import com.swooby.alfred.notification.parsers.PandoraNotificationParser;
import com.swooby.alfred.notification.parsers.SpotifyNotificationParser;

import java.util.HashMap;
import java.util.Map;

public class NotificationParserManager
{
    private static final String TAG = FooLog.TAG(NotificationParserManager.class);

    public interface NotificationParserManagerConfiguration
    {
        boolean isEnabled();

        @NonNull
        TextToSpeechManager getTextToSpeech();
    }

    public interface NotificationParserManagerCallbacks
    {
        boolean onNotificationAccessSettingConfirmedEnabled();

        void onNotificationAccessSettingDisabled(DisabledCause disabledCause);
    }

    private final Context                                                mContext;
    private final NotificationParserManagerConfiguration                 mConfiguration;
    private final FooListenerManager<NotificationParserManagerCallbacks> mListenerManager;
    private final FooNotificationListenerManager                         mFooNotificationListenerManager;
    private final FooNotificationListenerManagerCallbacks                mFooNotificationListenerManagerCallbacks;
    private final NotificationParserCallbacks                            mNotificationParserCallbacks;
    private final Map<String, AbstractNotificationParser>                mNotificationParsers;

    private boolean mIsInitialized;

    public NotificationParserManager(@NonNull Context context, @NonNull NotificationParserManagerConfiguration configuration)
    {
        FooLog.v(TAG, "+NotificationParserManager(...)");

        FooRun.throwIllegalArgumentExceptionIfNull(context, "context");
        FooRun.throwIllegalArgumentExceptionIfNull(configuration, "configuration");

        mContext = context;
        mConfiguration = configuration;

        mListenerManager = new FooListenerManager<>();

        mFooNotificationListenerManager = FooNotificationListenerManager.getInstance();

        mFooNotificationListenerManagerCallbacks = new FooNotificationListenerManagerCallbacks()
        {
            @Override
            public boolean onNotificationAccessSettingConfirmedEnabled()
            {
                return NotificationParserManager.this.onNotificationAccessSettingConfirmedEnabled();
            }

            @Override
            public void onNotificationAccessSettingDisabled(DisabledCause disabledCause)
            {
                NotificationParserManager.this.onNotificationAccessSettingDisabled(disabledCause);
            }

            @Override
            public void onNotificationPosted(StatusBarNotification sbn)
            {
                NotificationParserManager.this.onNotificationPosted(sbn);
            }

            @Override
            public void onNotificationRemoved(StatusBarNotification sbn)
            {
                NotificationParserManager.this.onNotificationRemoved(sbn);
            }
        };

        mNotificationParserCallbacks = new NotificationParserCallbacks()
        {
            @Override
            public Context getContext()
            {
                return mContext;
            }

            @Override
            public TextToSpeechManager getTextToSpeech()
            {
                return NotificationParserManager.this.getTextToSpeech();
            }
        };

        mNotificationParsers = new HashMap<>();

        FooLog.v(TAG, "-NotificationParserManager(...)");
    }

    @NonNull
    private TextToSpeechManager getTextToSpeech()
    {
        return mConfiguration.getTextToSpeech();
    }

    private boolean isEnabled()
    {
        return mConfiguration.isEnabled();
    }

    public boolean isInitialized()
    {
        return mIsInitialized;
    }

    public boolean isNotificationAccessSettingConfirmedNotEnabled()
    {
        return FooNotificationListenerManager.isNotificationAccessSettingConfirmedNotEnabled(mContext);
    }

    /**
     * @return true if Notification Access is confirmed enabled (ie: FooNotificationListener successfully bound)
     */
    public boolean isNotificationAccessSettingConfirmedEnabled()
    {
        return mFooNotificationListenerManager.isNotificationAccessSettingConfirmedEnabled();
    }

    public void startActivityNotificationListenerSettings()
    {
        FooNotificationListenerManager.startActivityNotificationListenerSettings(mContext);
    }

    public void refresh()
    {
        mFooNotificationListenerManager.initializeActiveNotifications();
    }

    public void attach(NotificationParserManagerCallbacks callbacks)
    {
        mListenerManager.attach(callbacks);

        if (mListenerManager.size() == 1 && mNotificationParsers.size() == 0)
        {
            start();
        }
    }

    public void detach(NotificationParserManagerCallbacks callbacks)
    {
        mListenerManager.detach(callbacks);
    }

    private void addNotificationParser(@NonNull AbstractNotificationParser notificationParser)
    {
        mNotificationParsers.put(notificationParser.getPackageName(), notificationParser);
    }

    private void start()
    {
        // TODO:(pv) In DEBUG, show any parsers that do not have app installed w/ link to install app from Google Play
        // TODO:(pv) Listen for installation/removal of apps (especially ones w/ parsers)
        // TODO:(pv) Future ecosystem to allow installing 3rd-party developed parsers
        // TODO:(pv) Use package reflection to enumerate and load all non-Abstract parsers in parsers package
        // TODO:(pv) Not all of these parsers may be required, and could rely on a decent default implementation that walks and talks all visible text elements
        //addNotificationParser(new AndroidSystemNotificationParser(mNotificationParserCallbacks));
        //addNotificationParser(new FacebookNotificationParser(mNotificationParserCallbacks));
        //addNotificationParser(new GmailNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new DownloadManagerNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new GoogleCameraNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new GoogleDialerNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new GoogleHangoutsNotificationParser(mNotificationParserCallbacks));
        //addNotificationParser(new GoogleMapsNotificationParser(mNotificationParserCallbacks));
        //addNotificationParser(new GoogleMessengerNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new GoogleMyGlassNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new GoogleNowNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new GooglePhotosNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new GooglePlayStoreNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new PandoraNotificationParser(mNotificationParserCallbacks));
        //addNotificationParser(new RedboxNotificationParser(mNotificationParserCallbacks));
        addNotificationParser(new SpotifyNotificationParser(mNotificationParserCallbacks));

        mFooNotificationListenerManager.attach(mContext, mFooNotificationListenerManagerCallbacks);
    }

    private boolean onNotificationAccessSettingConfirmedEnabled()
    {
        FooLog.i(TAG, "onNotificationAccessSettingConfirmedEnabled()");
        mIsInitialized = true;
        boolean handled = false;
        for (NotificationParserManagerCallbacks callbacks : mListenerManager.beginTraversing())
        {
            handled |= callbacks.onNotificationAccessSettingConfirmedEnabled();
        }
        mListenerManager.endTraversing();

        return handled;
    }

    private void onNotificationAccessSettingDisabled(DisabledCause disabledCause)
    {
        FooLog.i(TAG, "onNotificationAccessSettingDisabled(disabledCause=" + disabledCause + ')');
        mIsInitialized = true;
        for (NotificationParserManagerCallbacks callbacks : mListenerManager.beginTraversing())
        {
            callbacks.onNotificationAccessSettingDisabled(disabledCause);
        }
        mListenerManager.endTraversing();
    }

    private void onNotificationPosted(StatusBarNotification sbn)
    {
        if (!isEnabled())
        {
            FooLog.w(TAG, "onNotificationPosted: isEnabled() == false; ignoring");
            return;
        }

        String packageName = AbstractNotificationParser.getPackageName(sbn);
        FooLog.v(TAG, "onNotificationPosted: packageName=" + FooString.quote(packageName));

        NotificationParseResult result;

        AbstractNotificationParser notificationParser = mNotificationParsers.get(packageName);
        if (notificationParser == null)
        {
            result = AbstractNotificationParser.defaultOnNotificationPosted(mContext, sbn, getTextToSpeech());
        }
        else
        {
            result = notificationParser.onNotificationPosted(sbn);
        }

        switch (result)
        {
            case DefaultWithTickerText:
            case DefaultWithoutTickerText:
                break;
            case Unparsable:
                FooLog.w(TAG, "onNotificationPosted: Unparsable StatusBarNotification");
                break;
            case ParsableHandled:
            case ParsableIgnored:
                break;
        }
    }

    private void onNotificationRemoved(StatusBarNotification sbn)
    {
        if (!isEnabled())
        {
            FooLog.w(TAG, "onNotificationRemoved: isEnabled() == false; ignoring");
            return;
        }

        String packageName = AbstractNotificationParser.getPackageName(sbn);
        FooLog.d(TAG, "onNotificationRemoved: packageName=" + FooString.quote(packageName));

        AbstractNotificationParser notificationParser = mNotificationParsers.get(packageName);
        if (notificationParser == null)
        {
            return;
        }

        // TODO:(pv) Reset any cache in the parser...
        notificationParser.onNotificationRemoved(sbn);
    }
}
