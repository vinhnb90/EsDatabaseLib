package esolutions.com.esdatabaselib.base;

import android.database.Cursor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public abstract class ItemFactory<T> {
    private Class<T> mItemClassType;
    private ArrayList<Annotation> mAnnotationsContructor;
    private Constructor mConstructor;

    public ItemFactory(Class<T> kClass) {
        mItemClassType = kClass;
    }

    public Constructor getsConstructor() {
        return mConstructor;
    }

    public void setsConstructor(Constructor sConstructor) {
        mConstructor = sConstructor;
    }

    public ArrayList<Annotation> getmAnnotationsContructor() {
        return mAnnotationsContructor;
    }

    public void setmAnnotationsContructor(ArrayList<Annotation> mAnnotationsContructor) {
        this.mAnnotationsContructor = mAnnotationsContructor;
    }

    public Class<T> getItemClassType() {
        return mItemClassType;
    }

    abstract T create(Cursor cursor, int index);
}