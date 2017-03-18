package com.swooby.alfred;

import android.content.Context;
import android.media.AudioManager;

import com.smartfoo.android.core.FooString;
import com.smartfoo.android.core.content.FooPreferences;
import com.swooby.alfred.Profile.Tokens;

public class AppPreferences
        extends FooPreferences
{
    public static final String DEFAULT_VOICE_NAME = "en-GB-language";

    private static final String KEY_USER_VOICE_NAME              = "pref_user_tts_voice_name";
    private static final String KEY_USER_VOICE_AUDIO_STREAM_TYPE = "pref_user_tts_voice_audio_stream_type";
    private static final String KEY_USER_PROFILE_TOKEN           = "pref_user_profile_token";
    private static final String KEY_USER_KEYPHRASE               = "pref_user_keyphrase";

    public AppPreferences(Context applicationContext)
    {
        super(applicationContext);
    }

    public String getTextToSpeechVoiceName()
    {
        return getString(FILE_NAME_USER, KEY_USER_VOICE_NAME, DEFAULT_VOICE_NAME);
    }

    public void setTextToSpeechVoiceName(String value)
    {
        setString(FILE_NAME_USER, KEY_USER_VOICE_NAME, !FooString.isNullOrEmpty(value) ? value : DEFAULT_VOICE_NAME);
    }

    public int getTextToSpeechAudioStreamType()
    {
        return getInt(FILE_NAME_USER, KEY_USER_VOICE_AUDIO_STREAM_TYPE, AudioManager.STREAM_MUSIC);
    }

    public void setTextToSpeechAudioStreamType(int value)
    {
        setInt(FILE_NAME_USER, KEY_USER_VOICE_AUDIO_STREAM_TYPE, value);
    }

    public String getProfileToken()
    {
        return getString(FILE_NAME_USER, KEY_USER_PROFILE_TOKEN, Tokens.HEADPHONES_ONLY);
    }

    public void setProfileToken(String value)
    {
        setString(FILE_NAME_USER, KEY_USER_PROFILE_TOKEN, value);
    }

    public String getKeyphrase()
    {
        return getString(FILE_NAME_USER, KEY_USER_KEYPHRASE, "alfred");
    }

    public void setKeyphrase(String value)
    {
        setString(FILE_NAME_USER, KEY_USER_KEYPHRASE, value);
    }
}
