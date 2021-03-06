package com.swooby.alfred.notification.parsers;

import android.app.Notification.Action;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import com.smartfoo.android.core.BuildConfig;
import com.smartfoo.android.core.FooString;
import com.smartfoo.android.core.logging.FooLog;
import com.smartfoo.android.core.platform.FooPlatformUtils;
import com.smartfoo.android.core.texttospeech.FooTextToSpeechBuilder;
import com.swooby.alfred.R;

import java.util.Arrays;
import java.util.Objects;

public class SpotifyNotificationParser
        extends AbstractMediaPlayerNotificiationParser
{
    private static final String TAG = FooLog.TAG(SpotifyNotificationParser.class);

    private boolean      mLastIsPlaying;
    private CharSequence mLastArtist;
    private CharSequence mLastTitle;

    public SpotifyNotificationParser(@NonNull NotificationParserCallbacks callbacks)
    {
        super("#SPOTIFY", callbacks);//, application.getString(R.string.spotify_package_app_spoken_name));
    }

    @Override
    public String getPackageName()
    {
        return "com.spotify.music";
    }

    @Override
    public NotificationParseResult onNotificationPosted(StatusBarNotification sbn)
    {
        FooLog.i(TAG, "---- " + hashtag() + " ----");
        if (BuildConfig.DEBUG)
        {
            super.onNotificationPosted(sbn);
        }

        final String prefix = hashtag("onNotificationPosted");

        Bundle extras = NotificationParserUtils.getExtras(sbn);
        FooLog.v(TAG, prefix + " extras=" + FooPlatformUtils.toString(extras));
        if (extras == null)
        {
            FooLog.w(TAG, prefix + " extras == null; Unparsable");
            return NotificationParseResult.Unparsable;
        }

        CharSequence textTitle = NotificationParserUtils.getAndroidTitle(extras);
        FooLog.v(TAG, prefix + " textTitle=" + FooString.quote(textTitle));
        CharSequence textArtist = NotificationParserUtils.getAndroidText(extras);
        FooLog.v(TAG, prefix + " textArtist=" + FooString.quote(textArtist));

        Context context = getContext();

        MediaController mediaController = getMediaController(context, extras);
        FooLog.v(TAG, prefix + " mediaController=" + mediaController);
        if (mediaController == null)
        {
            FooLog.w(TAG, prefix + " mediaController == null; Unparsable");
            return NotificationParseResult.Unparsable;
        }

        Action[] actions = NotificationParserUtils.getActions(sbn);
        FooLog.v(TAG, prefix + " actions=" + Arrays.toString(actions));
        int[] compactActions = NotificationParserUtils.getCompactActions(extras);
        FooLog.v(TAG, prefix + " compactActions=" + Arrays.toString(compactActions));

        PlaybackState playbackState = mediaController.getPlaybackState();
        if (playbackState == null)
        {
            FooLog.w(TAG, prefix + " mediaSession == null; Unparsable");
            return NotificationParseResult.Unparsable;
        }

        int playbackStateState = playbackState.getState();
        FooLog.v(TAG, prefix + " playbackStateState == " + playbackStateToString(playbackStateState));
        if (playbackStateState != PlaybackState.STATE_PAUSED &&
            playbackStateState != PlaybackState.STATE_PLAYING)
        {
            FooLog.w(TAG, prefix + " playbackStateState != (PAUSED || PLAYING); ParsedIgnored");
            return NotificationParseResult.ParsedIgnored;
        }
        boolean isPlaying = playbackStateState == PlaybackState.STATE_PLAYING;

        // @formatter:off
        // title == non-null commercial/advertisement/company name, artist == null/""
        boolean isCommercial = !FooString.isNullOrEmpty(textTitle) && FooString.isNullOrEmpty(textArtist);
        // Test if *ONLY* the Playing/Paused (middle) Action is enabled (actionIntent != null)
        isCommercial &= actions.length < 1 || actions[0].actionIntent == null; // Thumbs Down (action always present)
        isCommercial &= actions.length < 2 || actions[1].actionIntent == null; // Previous Track (action always present)
        isCommercial &= actions.length < 3 || actions[2].actionIntent != null; // PlayingPause or PausedPlay (action always present)
        isCommercial &= actions.length < 4 || actions[3].actionIntent == null; // Next Track (action always present)
        isCommercial &= actions.length < 5 || actions[4].actionIntent == null; // Thumbs Up (action may be absent/null)
        // @formatter:on
        FooLog.v(TAG, prefix + " isCommercial == " + isCommercial);
        if (isCommercial)
        {
            return onCommercial(prefix);
        }

        onNonCommercial();

        //
        //
        //

        textArtist = NotificationParserUtils.unknownIfNullOrEmpty(context, textArtist);
        textTitle = NotificationParserUtils.unknownIfNullOrEmpty(context, textTitle);

        if (isPlaying == mLastIsPlaying &&
            Objects.equals(textArtist, mLastArtist) &&
            Objects.equals(textTitle, mLastTitle))
        {
            FooLog.w(TAG, prefix + " data unchanged; ParsedIgnored");
            return NotificationParseResult.ParsedIgnored;
        }

        mLastIsPlaying = isPlaying;
        mLastArtist = textArtist;
        mLastTitle = textTitle;

        FooTextToSpeechBuilder builder = new FooTextToSpeechBuilder(getContext(), getPackageAppSpokenName());

        if (isPlaying)
        {
            FooLog.w(TAG, prefix + " playing");

            builder.appendSpeech(R.string.alfred_media_playing);
            builder.appendSilenceWordBreak();
            builder.appendSpeech(R.string.alfred_media_artist, textArtist);
            builder.appendSilenceWordBreak();
            builder.appendSpeech(R.string.alfred_media_title, textTitle);
        }
        else
        {
            FooLog.w(TAG, prefix + " paused");

            builder.appendSpeech(R.string.alfred_media_paused);
        }

        speak(builder);

        return NotificationParseResult.ParsedHandled;
    }
}
