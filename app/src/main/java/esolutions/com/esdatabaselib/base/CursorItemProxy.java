package esolutions.com.esdatabaselib.base;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Create proxy for cursor item
 * Created by VinhNB on 8/8/2017.
 */

@Deprecated
public abstract class CursorItemProxy {
    private Cursor mCursor;
    private int mIndex;

    public CursorItemProxy(@NonNull Cursor mCursor, int mIndex) {
        this.mCursor = mCursor;
        this.mIndex = mIndex;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getIndex() {
        return mIndex;
    }
}
