package com.swooby.alfred;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.smartfoo.android.core.annotations.NonNullNonEmpty;

import java.util.Comparator;

public class Profile
{
    public static abstract class Tokens
    {
        public static final String DISABLED                 = "profile.disabled";
        public static final String HEADPHONES_WIRED         = "profile.headphones_wired";
        public static final String HEADPHONES_BLUETOOTH_ANY = "profile.headphones_bluetooth_any";
        public static final String HEADPHONES_ANY           = "profile.headphones_any";
        public static final String ALWAYS_ON                = "profile.always_on";

        public static boolean isDisabled(String value)
        {
            return value == null || value.equals(DISABLED);
        }

        public static boolean isNotDisabled(String value)
        {
            return value != null && !value.equals(DISABLED);
        }
    }

    public static Comparator<Profile> COMPARATOR = new Comparator<Profile>()
    {
        @Override
        public int compare(Profile lhs, Profile rhs)
        {
            if (lhs.mForcedOrder != Integer.MAX_VALUE)
            {
                int compare = Integer.compare(lhs.mForcedOrder, rhs.mForcedOrder);
                if (compare != 0)
                {
                    return compare;
                }
            }

            return lhs.mName.compareTo(rhs.mName);
        }
    };

    private final int    mForcedOrder;
    @NonNullNonEmpty
    private final String mName;
    @NonNullNonEmpty
    private final String mToken;

    public Profile(@NonNull BluetoothDevice bluetoothDevice)
    {
        this(Integer.MAX_VALUE, bluetoothDevice.getName(), bluetoothDevice.getAddress());
    }

    public Profile(int forcedOrder, @NonNullNonEmpty String name, @NonNullNonEmpty String token)
    {
        mForcedOrder = forcedOrder;
        mName = name;
        mToken = token;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @NonNullNonEmpty
    public String getName()
    {
        return mName;
    }

    @NonNullNonEmpty
    public String getToken()
    {
        return mToken;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Profile && mName.equals(((Profile) obj).mName);
    }
}
