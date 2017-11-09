package esolutions.com.esdatabaselib.baseSharedPref;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

import esolutions.com.esdatabaselib.baseSharedPref.anonation.KeyType;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.SharePref;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.TYPE;

import static android.R.attr.mode;
import static esolutions.com.esdatabaselib.utils.EsDatabaseLibCommon.hasPermissionWriteSdcard;

/**
 * Created by VinhNB on 9/19/2016.
 */
public class SharePrefManager<T> {
    private static SharePrefManager ourInstance;
    private static Context mContext;
    private static ArrayList<Class<?>> classes;

    private SharePrefManager(Context context, ArrayList<Class<?>> classzs) throws Exception {
        mContext = context;

        //remove all file
        removeAllFileSharedPref();

        //create files
        for (Class<?> classz : classzs) {

            //check classz is class SharePref config
            Boolean isSharePrefClassConfig = classz.isAnnotationPresent(SharePref.class);
            if (!isSharePrefClassConfig)
                throw new RuntimeException("Class " + classz.getSimpleName() + " not description is class SharePref config!");


            //get name file
            SharePref annSharedPref = classz.getAnnotation(SharePref.class);
            String nameFileSharedPref = annSharedPref.name();


            //create file
            if (checkExistSharePref(nameFileSharedPref))
                throw new RuntimeException("Duplicate class " + classz.getSimpleName() + " initial!");
            createFileSharePref(classz, annSharedPref);
        }
    }

    private void createFileSharePref(Class<?> classz, SharePref annSharedPref) throws IOException {
        //check permission db
        if (!hasPermissionWriteSdcard(mContext))
            throw new SecurityException("Must be permission WRITE_EXTERNAL_STORAGE !");


        //get field
        String nameFileSharedPref = annSharedPref.name();
        Field[] fields = classz.getDeclaredFields();


        //create file shared pref
        SharedPreferences sharePref = mContext.getSharedPreferences(nameFileSharedPref, Context.MODE_PRIVATE);
        sharePref.edit().commit();


        //and create key value of file shared pref
        for (Field field : fields) {
            //break if field is $change or serialVersionUID
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                break;


            //get name column by check annotation
            boolean isKeyTypeSharedPref = field.isAnnotationPresent(KeyType.class);
            if (!isKeyTypeSharedPref)
                throw new RuntimeException("Please config annonation KeyType above field has name " + fieldName + " in class " + nameFileSharedPref);
            KeyType keySPref = field.getAnnotation(KeyType.class);
            String nameKeySPref = keySPref.name();
            if (TextUtils.isEmpty(nameKeySPref))
                throw new RuntimeException("Please config name of annonation KeyType above field has name " + fieldName + " in class " + nameFileSharedPref);
            TYPE typeKeySPref = keySPref.TYPE();


            //create default key value
            createKeyValueSharedPref(sharePref, nameKeySPref, typeKeySPref);
        }
    }

    private void createKeyValueSharedPref(final SharedPreferences sharePref, String nameKeySPref, TYPE typeKeySPref) {
        if (typeKeySPref == TYPE.STRING) {
            sharePref.edit().putString(nameKeySPref, "").commit();
        }
        if (typeKeySPref == TYPE.STRING_SET) {
            sharePref.edit().putStringSet(nameKeySPref, null).commit();
        }
        if (typeKeySPref == TYPE.INT) {
            sharePref.edit().putInt(nameKeySPref, 0).commit();
        }
        if (typeKeySPref == TYPE.LONG) {
            sharePref.edit().putLong(nameKeySPref, 0L).commit();
        }
        if (typeKeySPref == TYPE.FLOAT) {
            sharePref.edit().putFloat(nameKeySPref, 0.0f).commit();
        }
        if (typeKeySPref == TYPE.BOOLEAN) {
            sharePref.edit().putBoolean(nameKeySPref, false).commit();
        }
    }

    private void removeAllFileSharedPref() {
        File sharedPreferenceFile = new File("/data/data/" + mContext.getApplicationContext().getPackageName() + "/shared_prefs/");
        File[] listFiles = sharedPreferenceFile.listFiles();
        for (File file : listFiles) {
            file.delete();
        }
    }

    public synchronized static SharePrefManager getInstance(Context context, ArrayList<Class<?>> classzs) throws Exception{
        //create
        if (ourInstance == null) {
            ourInstance = new SharePrefManager(context, classzs);
        }
        return ourInstance;
    }

    public synchronized static SharePrefManager getInstance() throws Exception {
        if (classes == null)
            throw new NullPointerException("not has file class SharePref config!");
        return ourInstance;
    }

    /**
     * Not used if has file Shared Pref config
     *
     * @param keySharePref
     * @param mode
     * @return
     */
    @Deprecated
    public boolean addSharePref(String keySharePref, int mode) {
        boolean existSharePref = checkExistSharePref(keySharePref);
        if (existSharePref) {
            return false;
        }
        mContext.getSharedPreferences(keySharePref, mode).edit().commit();
        return true;
    }


    public SharedPreferences getSharePref(Class<?> classz) throws RuntimeException {
        //check class is SharedPref Config
        boolean isSharedPrefAnn = classz.isAnnotationPresent(SharePref.class);
        if (!isSharedPrefAnn)
            throw new RuntimeException("Class " + classz.getSimpleName() + " has not config annonation SharePref!");


        //get name file SharedPref Config
        SharePref sharePref = classz.getAnnotation(SharePref.class);
        String nameFileSPref;
        nameFileSPref = sharePref.name();


        boolean existSharePref = checkExistSharePref(nameFileSPref);
        if (existSharePref) {
            SharedPreferences preferences = mContext.getSharedPreferences(nameFileSPref, mode);
            return preferences;
        }


        return null;
    }


    public Object getSharePref(Class<?> classz, Field fieldKeyType) throws RuntimeException {
        //get content file SharePref
        SharedPreferences sharedPreferences = this.getSharePref(classz);

        // fieldKeyType is field SharedPref Config, get name column by check annotation
        String nameKeySPref;
        TYPE typeKeySPref;
        boolean isKeyTypeSharedPref = fieldKeyType.isAnnotationPresent(KeyType.class);
        if (!isKeyTypeSharedPref) {
            throw new RuntimeException("Field has name " + fieldKeyType.getName() + " in class " + sharedPreferences.getClass().getSimpleName() + " has not config annonation KeyType above ");
        }
        KeyType keySPref = fieldKeyType.getAnnotation(KeyType.class);
        nameKeySPref = keySPref.name();
        if (TextUtils.isEmpty(nameKeySPref)) {
            throw new RuntimeException("Field has name " + fieldKeyType.getName() + " in class " + sharedPreferences.getClass().getSimpleName() + " has not config name of annonation KeyType above");
        }
        typeKeySPref = keySPref.TYPE();


        return getSharePrefValue(sharedPreferences, nameKeySPref, typeKeySPref);
    }

    private Object getSharePrefValue(SharedPreferences sharePref, String nameKeySPref, TYPE typeKeySPref) throws RuntimeException {
        Object value = null;


        //check has key  in file shared pref
        boolean isHasKey = sharePref.contains(nameKeySPref);
        if (!isHasKey)
            throw new RuntimeException("File SharedPreferences " + sharePref.getClass().getSimpleName() + " not has key " + nameKeySPref);


        //get value
        if (typeKeySPref == TYPE.STRING) {
            value = sharePref.getString(nameKeySPref, "");
        }
        if (typeKeySPref == TYPE.STRING_SET) {
            value = sharePref.getStringSet(nameKeySPref, new HashSet<String>());
        }
        if (typeKeySPref == TYPE.INT) {
            value = sharePref.getInt(nameKeySPref, 0);
        }
        if (typeKeySPref == TYPE.LONG) {
            value = sharePref.getLong(nameKeySPref, 0L);
        }
        if (typeKeySPref == TYPE.FLOAT) {
            value = sharePref.getFloat(nameKeySPref, 0.0f);
        }
        if (typeKeySPref == TYPE.BOOLEAN) {
            value = sharePref.getBoolean(nameKeySPref, false);
        }


        return value;
    }

    public boolean checkExistSharePref(String keySharePref) {
        File f = new File(
                "/data/data/" + mContext.getApplicationContext().getPackageName() + "/shared_prefs/" + keySharePref + ".xml");
        if (f.exists())
            return true;
        else
            return false;
    }
}
