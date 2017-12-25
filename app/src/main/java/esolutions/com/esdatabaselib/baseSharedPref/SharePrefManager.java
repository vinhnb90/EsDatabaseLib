package esolutions.com.esdatabaselib.baseSharedPref;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import esolutions.com.esdatabaselib.baseSharedPref.anonation.KeyType;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.SharePref;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.TYPE;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Params;

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
        classes = new ArrayList<>(classzs);
        //remove all file
//        removeAllFileSharedPref();

        //create files
        //check if same annotation name file SharePref
        ArrayList<String> annotationClasses = new ArrayList<>();
        for (Class<?> classz : classzs) {

            //check classz is class SharePref config
            Boolean isSharePrefClassConfig = classz.isAnnotationPresent(SharePref.class);
            if (!isSharePrefClassConfig)
                throw new RuntimeException("Class " + classz.getSimpleName() + " not description is class SharePref config!");


            //get name file
            SharePref annSharedPref = classz.getAnnotation(SharePref.class);
            String nameFileSharedPref = annSharedPref.name();


            //check exist name file and add in annotationClasses
            if (annotationClasses.contains(nameFileSharedPref.toLowerCase()))
                throw new RuntimeException("Duplicate annotation " + nameFileSharedPref + " in serveral class config!");
            annotationClasses.add(nameFileSharedPref.toLowerCase());


            //check exist key if exist then list all key in file and insert and delete if key not map with class config
            //all key map and exist in file then default it
            if (existFileSharePref(nameFileSharedPref)) {
                createKeySharedPref(classz, annSharedPref);
            } else
                createFileSharePref(classz, annSharedPref);
        }
    }

    private void createKeySharedPref(Class<?> classz, SharePref annSharedPref) throws Exception {
        //check permission db
        if (!hasPermissionWriteSdcard(mContext))
            throw new SecurityException("Must be permission WRITE_EXTERNAL_STORAGE !");


        //check exist key if exist then list all key in file and insert and delete if key not map with class config
        //all key map and exist in file then default it
        //get field
        String nameFileSharedPref = annSharedPref.name();
        Field[] fields = classz.getDeclaredFields();


        //create list to get all key name delete, insert, update
        ArrayList<String> listKeyOld = new ArrayList<>();
        ArrayList<String> listKeyNew = new ArrayList<>();
        ArrayList<String> listKeySame = new ArrayList<>();
        ArrayList<String> listKeyDelete = new ArrayList<>();
        ArrayList<String> listKeyInsert = new ArrayList<>();


        //get data SharedPreferences and save a temp List<DataKeySharedPref>
        SharedPreferences sharePref = getSharePref(classz);
        ArrayList<DataKeySharedPref> listDataKeyOld = new ArrayList<>();
        Map<String, ?> allEntries = sharePref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            DataKeySharedPref dataKeySharedPref = getDataKeySharedPref(nameFileSharedPref, entry.getKey(), entry.getValue());
            listDataKeyOld.add(dataKeySharedPref);
            listKeyOld.add(entry.getKey());
        }


        //fillter all field has KeyType ok
        //fillter all same key name
        ArrayList<DataKeySharedPref> listDataKeyNew = new ArrayList<>();
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


            //add data key new
            DataKeySharedPref dataKeySharedPref = new DataKeySharedPref(nameFileSharedPref, nameKeySPref, null, typeKeySPref);
            listDataKeyNew.add(dataKeySharedPref);
            listKeyNew.add(dataKeySharedPref.nameKey);
        }


        //get fillter list keyname
        listKeySame = (ArrayList<String>) listKeyNew.clone();
        listKeySame.retainAll(listKeyOld);
        listKeyInsert = (ArrayList<String>) listKeyNew.clone();
        listKeyInsert.removeAll(listKeySame);
        listKeyDelete = (ArrayList<String>) listKeyOld.clone();
        listKeyDelete.removeAll(listKeySame);


        //insert key
        for (String keyInsert :
                listKeyInsert) {
            for (DataKeySharedPref dataKeyNew :
                    listDataKeyNew) {
                if (dataKeyNew.nameKey.equalsIgnoreCase(keyInsert)) {
                    setValueSharedPref(sharePref, dataKeyNew.nameKey, dataKeyNew.typeKeySPref);
                }
            }
        }


        //delele key
        for (String keyDelete :
                listKeyDelete) {
            for (DataKeySharedPref dataKeyOld :
                    listDataKeyOld) {
                if (dataKeyOld.nameKey.equalsIgnoreCase(keyDelete)) {
                    sharePref.edit().remove(dataKeyOld.nameKey).apply();
                }
            }
        }


        //check key same
        for (String keySame :
                listKeySame) {
            //search type key New
            DataKeySharedPref dataKeyNewSame = null;
            for (DataKeySharedPref dataKeyNew :
                    listDataKeyNew) {
                if (dataKeyNew.nameKey.equalsIgnoreCase(keySame)) {
                    dataKeyNewSame = dataKeyNew;
                    break;
                }
            }


            //search key old, if not same typeKeySPref then remove and create again
            for (DataKeySharedPref dataKeyOld :
                    listDataKeyOld) {
                if (dataKeyOld.nameKey.equalsIgnoreCase(keySame) && dataKeyNewSame.typeKeySPref != dataKeyOld.typeKeySPref) {
                    sharePref.edit().remove(dataKeyOld.nameKey).apply();
                    setValueSharedPref(sharePref, dataKeyNewSame.nameKey, dataKeyNewSame.typeKeySPref);
                }
            }
        }
    }

    private DataKeySharedPref getDataKeySharedPref(String nameFile, String key, Object value) throws Exception {
        DataKeySharedPref result = new DataKeySharedPref();
        result.setNameFile(nameFile);
        result.setNameKey(key);
        result.setValue(value);

        //check value
        if (String.class.isInstance(value)) {
            result.setTypeKeySPref(TYPE.STRING);
        }
        if (new HashSet<String>().getClass().isInstance(value)) {
            result.setTypeKeySPref(TYPE.STRING_SET);
        }
        if (Integer.class.isInstance(value)) {
            result.setTypeKeySPref(TYPE.INT);
        }
        if (Long.class.isInstance(value)) {
            result.setTypeKeySPref(TYPE.LONG);
        }
        if (Float.class.isInstance(value)) {
            result.setTypeKeySPref(TYPE.FLOAT);
        }
        if (Boolean.class.isInstance(value)) {
            result.setTypeKeySPref(TYPE.BOOLEAN);
        }
        return result;
    }


    @Deprecated
    private boolean checkIsTypeOldKeySameTypeNewKeyFileSharePref(DataKeySharedPref dataKeySharedPref, TYPE typeKeySPref) throws Exception {
        //check value
        if (typeKeySPref == TYPE.STRING) {
            return String.class.isInstance(dataKeySharedPref.value);
        }
        if (typeKeySPref == TYPE.STRING_SET) {
            try {
                return new HashSet<String>().getClass().isInstance(dataKeySharedPref.value);
            } catch (Exception e) {
                throw e;
            }
        }
        if (typeKeySPref == TYPE.INT) {
            return Integer.class.isInstance(dataKeySharedPref.value);
        }
        if (typeKeySPref == TYPE.LONG) {
            return Long.class.isInstance(dataKeySharedPref.value);
        }
        if (typeKeySPref == TYPE.FLOAT) {
            return Float.class.isInstance(dataKeySharedPref.value);
        }
        if (typeKeySPref == TYPE.BOOLEAN) {
            return Boolean.class.isInstance(dataKeySharedPref.value);
        }

        return false;
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
            setValueSharedPref(sharePref, nameKeySPref, typeKeySPref);
        }
    }

    private void setValueSharedPref(final SharedPreferences sharePref, String nameKeySPref, TYPE typeKeySPref) {
        if (typeKeySPref == TYPE.STRING) {
            sharePref.edit().putString(nameKeySPref, "").commit();
        }
        if (typeKeySPref == TYPE.STRING_SET) {
            sharePref.edit().putStringSet(nameKeySPref, new HashSet<String>()).commit();
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

    public void removeAllFileSharedPref() {
        File sharedPreferenceFolder = new File("/data/data/" + mContext.getApplicationContext().getPackageName() + File.separator + "shared_prefs");
        if (!sharedPreferenceFolder.exists())
            return;

        File[] listFiles = sharedPreferenceFolder.listFiles();
        for (File file : listFiles) {
            file.delete();
        }
    }

    public synchronized static SharePrefManager getInstance(Context context, ArrayList<Class<?>> classzs) throws Exception {
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
    public boolean addSharePref(String keySharePref, int mode) throws IOException {
        boolean existSharePref = existFileSharePref(keySharePref);
        if (existSharePref) {
            return false;
        }
        mContext.getSharedPreferences(keySharePref, mode).edit().commit();
        return true;
    }


    public <T> SharedPreferences getSharePref(Class<T> classz) throws Exception {
        //check class is SharedPref Config
        boolean isSharedPrefAnn = classz.isAnnotationPresent(SharePref.class);
        if (!isSharedPrefAnn)
            throw new RuntimeException("Class " + classz.getSimpleName() + " has not config annonation SharePref!");


        //get name file SharedPref Config
        SharePref sharePref = classz.getAnnotation(SharePref.class);
        String nameFileSPref;
        nameFileSPref = sharePref.name();


        boolean existSharePref = existFileSharePref(nameFileSPref);
        if (existSharePref) {
            SharedPreferences preferences = mContext.getSharedPreferences(nameFileSPref, Context.MODE_PRIVATE);
            return preferences;
        }


        return null;
    }


    public <T> T getSharePrefObject(Class<T> classz) throws Exception {
        //get content file SharePref
        SharedPreferences sharedPreferences = this.getSharePref(classz);
        T object = null;
        T classResult = null;
        HashMap<String, Object> data = new HashMap<>();


        //get all field
        Field[] fields = classz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                continue;


            // get name and type
            String nameKeySPref;
            TYPE typeKeySPref;
            boolean isKeyTypeSharedPref = field.isAnnotationPresent(KeyType.class);
            if (!isKeyTypeSharedPref) {
                throw new RuntimeException("Field has name " + field.getName() + " in class " + classz.getSimpleName() + " has not config annonation KeyType above ");
            }
            KeyType keySPref = field.getAnnotation(KeyType.class);
            nameKeySPref = keySPref.name();
            if (TextUtils.isEmpty(nameKeySPref)) {
                throw new RuntimeException("Field has name " + field.getName() + " in class " + classz.getSimpleName() + " has not config name of annonation KeyType above");
            }
            typeKeySPref = keySPref.TYPE();


            //check type share and type field
            if (checkIsTypeFieldSameTypeSharePref(field, typeKeySPref)) {
                //reflection object
                field.setAccessible(true);

                Object value = getSharePrefValue(sharedPreferences, nameKeySPref, typeKeySPref);
                data.put(nameKeySPref, value);
            } else
                throw new RuntimeException("Field has name " + field.getName() + " in class " + classz.getSimpleName() + " has not config name of annonation KeyType same type " + typeKeySPref.name() + " above");

        }


        //get all Constructor
        //because create object class by name method getter setter is confuse.
        //then create annonation for contructor
        Constructor[] constructors = classz.getDeclaredConstructors();
        if (constructors.length != 0) {
            ArrayList<Annotation> annotations = new ArrayList<>();
            for (Constructor ctor : constructors) {
                //get all annonation of method constructor
                Annotation[][] annotationss = ctor.getParameterAnnotations();
                int countAnnotation = 0;
                for (Annotation[] annotation1 : annotationss) {
                    for (Annotation annotation : annotation1) {
                        if (annotation instanceof Params) {
                            annotations.add(annotation);
                            countAnnotation++;
                        }
                    }
                }


                //breake if constructor is default not has any params
                int sizeParamsCtor = ctor.getParameterTypes().length;
                if (sizeParamsCtor == 0)
                    continue;

                if(countAnnotation != sizeParamsCtor)
                    continue;

                //create array object
                //if quantum annotations not same quantum contructor
                Object[] objectCtor = new Object[annotations.size()];
                if (annotations.size() != sizeParamsCtor) {
                    continue;
                }


                //get name params contructor and check, set value to a objectCtor
                for (int i = 0; i < annotations.size(); i++) {
                    if (annotations.get(i) instanceof Params) {
                        String key = ((Params) annotations.get(i)).name();
                        if (data.containsKey(key)) {
                            objectCtor[i] = data.get(key);
                        } else
                            throw new RuntimeException("Class " + classz.getSimpleName() + " has contructor params name \"" + key + "\" not same with field name ");
                    }
                }


                //Create object class
                try {
                    classResult = (T) ctor.newInstance(objectCtor);
                } catch (Exception e) {
                    throw new Exception("Class " + classz.getSimpleName() + " declare wrong has contructors with info error is " + e.getMessage());
                }
            }
        }

        return classResult;
    }

    public <T> SharePrefManager writeDataSharePref(Class<T> classz, T data) throws Exception {
        SharedPreferences sharedPreferences = this.getSharePref(classz);


        //get all method of class
        //get all field
        Field[] fields = classz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                break;


            // get name and type
            String nameKeySPref;
            TYPE typeKeySPref;
            boolean isKeyTypeSharedPref = field.isAnnotationPresent(KeyType.class);
            if (!isKeyTypeSharedPref) {
                throw new RuntimeException("Field has name " + field.getName() + " in class " + classz.getSimpleName() + " has not config annonation KeyType above ");
            }
            KeyType keySPref = field.getAnnotation(KeyType.class);
            nameKeySPref = keySPref.name();
            if (TextUtils.isEmpty(nameKeySPref)) {
                throw new RuntimeException("Field has name " + field.getName() + " in class " + classz.getSimpleName() + " has not config name of annonation KeyType above");
            }
            typeKeySPref = keySPref.TYPE();


            //check type share and type field
            Object value = field.get(data);
            if (checkIsTypeFieldSameTypeSharePref(field, typeKeySPref)) {
                writeValue(sharedPreferences, nameKeySPref, value, typeKeySPref);
            }
        }


        return this;
    }

    private void writeValue(SharedPreferences sharedPreferences, String nameKeySharedPref, @Nullable Object value, TYPE typeKeySPref) {
        //check value
        if (value == null)
            return;
        if (typeKeySPref == TYPE.STRING) {
            sharedPreferences.edit().putString(nameKeySharedPref, value.toString()).commit();
        }
        if (typeKeySPref == TYPE.STRING_SET) {
            sharedPreferences.edit().putStringSet(nameKeySharedPref, (Set<String>) value).commit();
        }
        if (typeKeySPref == TYPE.INT) {
            sharedPreferences.edit().putInt(nameKeySharedPref, Integer.parseInt(value.toString())).commit();
        }
        if (typeKeySPref == TYPE.LONG) {
            sharedPreferences.edit().putLong(nameKeySharedPref, Long.parseLong(value.toString())).commit();
        }
        if (typeKeySPref == TYPE.FLOAT) {
            sharedPreferences.edit().putFloat(nameKeySharedPref, Float.parseFloat(value.toString())).commit();
        }
        if (typeKeySPref == TYPE.BOOLEAN) {
            sharedPreferences.edit().putBoolean(nameKeySharedPref, Boolean.parseBoolean(value.toString())).commit();
        }
    }

    private boolean checkIsTypeFieldSameTypeSharePref(Field field, TYPE typeKeySPref) throws Exception {
        //check value
        if (typeKeySPref == TYPE.STRING) {
            return field.getType().isAssignableFrom(String.class);
        }
        if (typeKeySPref == TYPE.STRING_SET) {
            try {
                return field.getType().isAssignableFrom(new HashSet<String>().getClass());
            } catch (Exception e) {
                throw e;
            }
        }
        if (typeKeySPref == TYPE.INT) {
            return field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(int.class);
        }
        if (typeKeySPref == TYPE.LONG) {
            return field.getType().isAssignableFrom(Long.class) || field.getType().isAssignableFrom(long.class);
        }
        if (typeKeySPref == TYPE.FLOAT) {
            return field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(float.class);
        }
        if (typeKeySPref == TYPE.BOOLEAN) {
            return field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(boolean.class);
        }

        return false;
    }

    private Object getSharePrefValue(SharedPreferences sharePref, String nameKeySPref, TYPE typeKeySPref) throws RuntimeException {
        //check has key  in file shared pref
        Object value = null;
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

    public boolean existFileSharePref(String keySharePref) throws IOException {
        //check folder
        File f = new File(
                "/data/data/" + mContext.getApplicationContext().getPackageName() + "/shared_prefs");
        if (!f.exists()) {
            f.mkdir();
            return false;
        }


        //check file
        File xmlFile = new File(f, keySharePref + ".xml");
        if (f.exists())
            return true;
        else {
            xmlFile.createNewFile();
            return false;
        }
    }


    class DataKeySharedPref {
        public String nameFile;
        public String nameKey;
        public Object value;
        public TYPE typeKeySPref;

        public DataKeySharedPref() {
        }

        public DataKeySharedPref(String nameFile, String nameKey, Object value, TYPE typeKeySPref) {
            this.nameFile = nameFile;
            this.nameKey = nameKey;
            this.value = value;
            this.typeKeySPref = typeKeySPref;
        }

        public void setNameFile(String nameFile) {
            this.nameFile = nameFile;
        }

        public void setNameKey(String nameKey) {
            this.nameKey = nameKey;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public void setTypeKeySPref(TYPE typeKeySPref) {
            this.typeKeySPref = typeKeySPref;
        }
    }
}
