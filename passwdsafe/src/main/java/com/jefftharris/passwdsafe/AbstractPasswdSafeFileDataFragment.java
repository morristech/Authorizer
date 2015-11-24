/*
 * Copyright (©) 2015 Jeff Harris <jefftharris@gmail.com>
 * All rights reserved. Use of the code is allowed under the
 * Artistic License 2.0 terms, as specified in the LICENSE file
 * distributed with this code, or available from
 * http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.jefftharris.passwdsafe;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.jefftharris.passwdsafe.file.PasswdFileData;
import com.jefftharris.passwdsafe.file.PasswdRecord;
import com.jefftharris.passwdsafe.util.NoPasswdFileDataException;
import com.jefftharris.passwdsafe.view.PasswdLocation;

import org.pwsafe.lib.file.PwsRecord;

/**
 * Base fragment for accessing password file data
 */
public abstract class AbstractPasswdSafeFileDataFragment
        <ListenerT extends AbstractPasswdSafeFileDataFragment.Listener>
        extends Fragment
{
    /**
     * Listener interface for owning activity
     */
    public interface Listener
    {
        /** Get the file data */
        @NonNull PasswdFileData getFileData() throws NoPasswdFileDataException;

        /** Is the navigation drawer open */
        boolean isNavDrawerOpen();
    }

    /**
     * Wrapper class for record information
     */
    protected static class RecordInfo
    {
        public final PwsRecord itsRec;
        public final PasswdRecord itsPasswdRec;
        public final PasswdFileData itsFileData;

        /**
         * Constructor
         */
        public RecordInfo(@NonNull PwsRecord rec,
                          @NonNull PasswdRecord passwdRec,
                          @NonNull PasswdFileData fileData)
        {
            itsRec = rec;
            itsPasswdRec = passwdRec;
            itsFileData = fileData;
        }
    }

    private PasswdLocation itsLocation;
    private ListenerT itsListener;

    /**
     * Create arguments for new instance
     */
    protected static Bundle createArgs(PasswdLocation location)
    {
        Bundle args = new Bundle();
        args.putParcelable("location", location);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            itsLocation = args.getParcelable("location");
        } else {
            itsLocation = new PasswdLocation();
        }
    }

    @Override
    public void onAttach(Context ctx)
    {
        super.onAttach(ctx);
        //noinspection unchecked
        itsListener = (ListenerT)ctx;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        itsListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        if ((itsListener != null) && !itsListener.isNavDrawerOpen()) {
            doOnCreateOptionsMenu(menu, inflater);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Get the password location
     */
    protected final PasswdLocation getLocation()
    {
        return itsLocation;
    }

    /**
     * Get the context listener
     */
    protected final ListenerT getListener()
    {
        return itsListener;
    }

    /**
     * Derived-class create options menu
     */
    protected abstract void doOnCreateOptionsMenu(Menu menu,
                                                  MenuInflater inflater);

    /**
     * Get the file data
     */
    protected final @NonNull PasswdFileData getFileData()
            throws NoPasswdFileDataException
    {
        if (isAdded() && (itsListener != null)) {
            return itsListener.getFileData();
        }
        throw new NoPasswdFileDataException();
    }

    /**
     * Get the record information
     */
    protected final @Nullable
    RecordInfo getRecordInfo()
    {
        try {
            PasswdFileData fileData = getFileData();
            PwsRecord rec = fileData.getRecord(itsLocation.getRecord());
            if (rec != null) {
                PasswdRecord passwdRec = fileData.getPasswdRecord(rec);
                if (passwdRec != null) {
                    return new RecordInfo(rec, passwdRec, fileData);
                }
            }
        } catch (NoPasswdFileDataException e) {
            return null;
        }
        return null;
    }
}
