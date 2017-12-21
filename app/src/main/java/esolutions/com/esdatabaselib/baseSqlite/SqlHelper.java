package esolutions.com.esdatabaselib.baseSqlite;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import esolutions.com.esdatabaselib.baseSqlite.anonation.AutoIncrement;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Collumn;
import esolutions.com.esdatabaselib.baseSqlite.anonation.DBConfig;
import esolutions.com.esdatabaselib.baseSqlite.anonation.EnumNameCollumn;
import esolutions.com.esdatabaselib.baseSqlite.anonation.PrimaryKey;
import esolutions.com.esdatabaselib.baseSqlite.anonation.TYPE;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Table;
import esolutions.com.esdatabaselib.example.activity.DatabaseActivity;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static android.util.Log.d;
import static android.util.Log.e;
import static android.util.Log.i;
import static esolutions.com.esdatabaselib.utils.EsDatabaseLibCommon.createFileIfNotExist;
import static esolutions.com.esdatabaselib.utils.EsDatabaseLibCommon.hasPermissionWriteSdcard;


/**
 * class input data include
 * {@link DBConfig}
 * {@link Table}
 * {@link Collumn}
 * in every collumn has property
 * {@link PrimaryKey}
 * {@link TYPE}
 * Created by VinhNB on 8/8/2017.
 */

public class SqlHelper extends SQLiteOpenHelper {
    private static final String TAG = "SQLiteOpenHelper";
    private static final int REQUEST_CODE_PERMISSION = 5555;
    private static SqlHelper sIntance;
    private static ObjectDbConfig sConfigData;
    private static int mOpenCounter;
    private static SQLiteDatabase mSqLiteDatabase;
    private static Class<?> sClassDBConfig;
    private static Class<?>[] sClassDBTable;
    private int oldVer;
    private int newVer;
    private String message;

    private SqlHelper() throws Exception {
        super(sConfigData.getContext(), sConfigData.getNameDB() + ".s3db", null, sConfigData.getVersion());
    }

    private SqlHelper(String nameFolder) throws Exception {

        super(sConfigData.getContext(), Environment.getExternalStorageDirectory() + File.separator + nameFolder + File.separator + sConfigData.getNameDB() + ".s3db", null, sConfigData.getVersion());
        SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + nameFolder + File.separator + sConfigData.getNameDB() + ".s3db", null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        this.oldVer = oldVer;
        this.newVer = newVer;
        if (newVer > oldVer) {
            for (Class<?> classz : sClassDBTable) {
                sqLiteDatabase.execSQL(getQueryDropTable(classz));
            }
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //overrider onDowgrare and throw message
        try{
            super.onDowngrade(db, oldVersion, newVersion);
        }catch (Exception e)
        {
            throw new RuntimeException(message);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            for (Class<?> classz : sClassDBTable) {
                String create = getQueryCreateTable(classz);
                sqLiteDatabase.execSQL(create);
            }
        } catch (Exception e) {
            //reset version old
            message = e.getMessage();
            Toast.makeText(sConfigData.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            this.onDowngrade(sqLiteDatabase, oldVer, newVer);
            sqLiteDatabase.execSQL("pragma user_version = " + this.oldVer);
        }
    }

    /**
     * get DB
     *
     * @return
     * @throws Exception
     */
    public synchronized static SqlHelper getIntance() throws Exception {
        if (sConfigData == null)
            throw new NullPointerException("resouce config database is Empty!");
        return sIntance;
    }

    /**
     * setup DB
     *
     * @param context
     * @param classDBConfig {@link DBConfig}
     * @param classDBTable  {@link Table}
     * @return
     * @throws Exception
     */
    public synchronized static SqlHelper setupDB(Context context, Class<?> classDBConfig, Class<?>[] classDBTable) throws Exception {
        if (context == null || !(context instanceof Context))
            throw new ClassCastException("value param context must be not null and is Context class!");

        if (classDBConfig == null)
            throw new NullPointerException("dataconfig database is Empty!");

        //check info classDBConfig
        boolean isDataConfig = classDBConfig.isAnnotationPresent(DBConfig.class);
        if (!isDataConfig)
            throw new RuntimeException("class must be format annotation ObjectDbConfig!");

        //check info array class classDBTable
        boolean hasClassNotTable = false;
        for (Class<?> classz :
                classDBTable) {
            boolean isDataTable = classz.isAnnotationPresent(Table.class);
            if (!isDataTable) {
                hasClassNotTable = true;
                break;
            }
        }

        if (hasClassNotTable)
            throw new RuntimeException("Has class not be format annotation Table!");

        //check permission db
        if (!hasPermissionWriteSdcard(context))
            throw new SecurityException("Must be permission WRITE_EXTERNAL_STORAGE !");

        //setup
        sClassDBConfig = classDBConfig;
        sClassDBTable = classDBTable;

        //set up object data config
        DBConfig annDBConfig = classDBConfig.getAnnotation(DBConfig.class);
        sConfigData = new ObjectDbConfig();
        sConfigData.setContext(context);
        sConfigData.setNameDB(annDBConfig.name());
        sConfigData.setNameFolder(annDBConfig.folderName());
        sConfigData.setVersion(annDBConfig.version());

        //check folder db
        createFolderFileDB(sConfigData.getNameFolder(), sConfigData.getNameDB());

        //create
        if (sIntance == null) {

            if (!TextUtils.isEmpty(sConfigData.getNameFolder())) {
                createFileIfNotExist(context, Environment.getExternalStorageDirectory() + File.separator + sConfigData.getNameFolder() + File.separator + sConfigData.getNameDB() + ".s3db");
                sIntance = new SqlHelper(sConfigData.getNameFolder());
            } else
                sIntance = new SqlHelper();

            sIntance.getWritableDatabase();
        }
        return sIntance;
    }

    public synchronized SQLiteDatabase openDB() {
        Log.i(TAG, "openDB: ");
        if (mOpenCounter == 0) {
            mSqLiteDatabase = sIntance.getWritableDatabase();
        }
        mOpenCounter++;
        return mSqLiteDatabase;
    }

    public synchronized SQLiteDatabase closeDB() {
        Log.i(TAG, "closeDB: ");
        if (mOpenCounter <= 0)
            return mSqLiteDatabase;
        if (mOpenCounter == 0)
            mSqLiteDatabase.close();
        mOpenCounter--;
        return mSqLiteDatabase;
    }

    public ObjectDbConfig getConfigData() {
        return sConfigData;
    }

    private String getQueryCreateTable(Class<?> classz) throws Exception {
        //check table
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);


        //check has EnumNameCollumn
        Class<?>[] methods = classz.getClasses();
        ArrayList<String> elementEnumNameCollumn = new ArrayList<>();
        for (Class<?> aClass : methods) {
            if (!aClass.isEnum())
                continue;


            boolean isEnumConstant = aClass.isAnnotationPresent(EnumNameCollumn.class);
            if (!isEnumConstant)
                continue;


            if (elementEnumNameCollumn.size() != 0)
                throw new Exception("EnumNameCollumn of enum " + aClass.getSimpleName() + " must be only declare 1 time!");


            Object[] consts = aClass.getEnumConstants();
            for (Object o : consts) {
                String s = o.toString();
                elementEnumNameCollumn.add(s);
            }


            //kiểm tra việc ghi method static getName trả về tên bảng
            Method[] methodsEnum = aClass.getDeclaredMethods();
            Method getNameMethod = null;
            boolean isHasMethodGetName = false;
            for (Method method : methodsEnum
                    ) {

                //check public
                boolean isPublic = Modifier.isPublic(method.getModifiers());
                //check static
                boolean isStatic = Modifier.isStatic(method.getModifiers());
                //check type return
                boolean isReturnString = method.getReturnType().newInstance() instanceof String;
                //check name
                boolean isGetName = method.getName().equalsIgnoreCase("getName");
                //not params
                boolean isNotParam = (method.getParameterTypes().length == 0);
                if (isPublic
                        && isStatic
                        && isReturnString
                        && isGetName
                        && isNotParam) {
                    isHasMethodGetName = true;
                    getNameMethod = method;
                    break;
                }
            }


            //nếu không có method getName như yêu cầu thì thông báo
            if (!isHasMethodGetName)
                throw new Exception("Trong class " + classz.getSimpleName() + " ở enum có @EnumNameCollumn hãy khai báo một method \"public static String getName()\" và return tên bảng sql phục vụ việc lấy tên bảng!");


            //lên giá trị table trả về và so sánh với annTable name
            getNameMethod.setAccessible(true);
            String nameTable = String.valueOf(getNameMethod.invoke(null, new Object[]{}));
            if (!nameTable.equalsIgnoreCase(annTable.name()))
                throw new Exception("Trong class " + classz.getSimpleName() + "giá trị return tên bảng trong method \"public static String getName()\" không trùng với name trong Annotations Table của bảng!");
        }


        //tạo query
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS "
                + annTable.name()
                + "(");
        Field[] fields = classz.getDeclaredFields();
        ArrayList<String> annonationClass = new ArrayList<>();


        for (int i = 0; i < fields.length; i++) {

            boolean isCollumn = fields[i].isAnnotationPresent(Collumn.class);
            if (!isCollumn)
                continue;


            Collumn collumn = fields[i].getAnnotation(Collumn.class);
            boolean isPrimaryKey = fields[i].isAnnotationPresent(PrimaryKey.class);
            boolean isAutoIncrement = fields[i].isAnnotationPresent(AutoIncrement.class);


            query.append(" "
                    + collumn.name()
                    + " "
                    + collumn.type().getContentType()
                    + (isPrimaryKey ? " PRIMARY KEY" : "")
                    + (isAutoIncrement ? " AUTOINCREMENT" : "")
                    + " "
                    + collumn.other());
            query.append(",");


            annonationClass.add(collumn.name());
        }


        //xóa kí tự cuối
        query.replace(query.length() - ",".length(), query.length(), "");
        query.append(");");


        //check các trường khai báo trong enum có trùng với các trường annotations ở các field không
        ArrayList<String> same = (ArrayList<String>) elementEnumNameCollumn.clone();
        same.retainAll(annonationClass);
        ArrayList<String> elementEnumNameCollumnNotSame = (ArrayList<String>) elementEnumNameCollumn.clone();
        elementEnumNameCollumnNotSame.removeAll(same);
        StringBuilder sElementEnumNameCollumnNotSame = new StringBuilder();
        if (elementEnumNameCollumnNotSame.size() != 0) {
            for (String s :
                    elementEnumNameCollumnNotSame) {
                sElementEnumNameCollumnNotSame.append(s).append(",");
            }
        }


        ArrayList<String> annonationClassNotSame = (ArrayList<String>) annonationClass.clone();
        annonationClassNotSame.removeAll(same);

        StringBuilder sAnnonationClassNotSame = new StringBuilder();
        if (annonationClassNotSame.size() != 0) {
            for (String s :
                    annonationClassNotSame) {
                sAnnonationClassNotSame.append(s).append(",");
            }
        }


        if (elementEnumNameCollumnNotSame.size() != 0 || annonationClassNotSame.size() != 0) {
            Log.e(TAG, "Trong class " + classz.getSimpleName() + " hãy khai báo trường tên của các trường enum có @EnumNameCollumn bao gồm các tên cột được khai báo như trên các field của class! Như sau:");
            Log.e(TAG, "EnumNameCollumn: " + sElementEnumNameCollumnNotSame.toString());
            Log.e(TAG, "annonationClass Field:  " + sAnnonationClassNotSame.toString());
            throw new Exception("Trong class " + classz.getSimpleName() + " hãy khai báo trường tên của các trường enum có @EnumNameCollumn bao gồm các tên cột được khai báo như trên các field của class!");
        }


        return query.toString();
    }

    private String getQueryDropTable(Class<?> classz) {
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");

        Table annTable = classz.getAnnotation(Table.class);

        StringBuilder query = new StringBuilder("DROP TABLE IF EXISTS "
                + annTable.name()
                + ";");

        return query.toString();
    }

    /**
     * Kiểm tra tồn tại database
     *
     * @param nameFolder
     * @param nameDB
     * @return
     */
    public static void createFolderFileDB(String nameFolder, String nameDB) throws Exception {
//        File fileParent = new File(Environment.getExternalStorageDirectory(), nameFolder);
        File fileParent = new File(Environment.getExternalStorageDirectory() + File.separator + sConfigData.getNameFolder() + File.separator);
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        } else {
            if (!fileParent.isDirectory()) {
                fileParent.delete();
                fileParent.mkdirs();
            }
        }

        showFolderSdcard(fileParent);

        File file = new File(fileParent, nameDB + ".s3db");
        if (!file.exists()) {
            file.createNewFile();
        } else {
            if (file.isDirectory()) {
                file.delete();
                file.createNewFile();
            }
        }
        showFolderSdcard(file.getParentFile());
    }

    private static void showFolderSdcard(File folderParent) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] allFilesRoot = folderParent.list();
            for (int i = 0; i < allFilesRoot.length; i++) {
                allFilesRoot[i] = folderParent + allFilesRoot[i];
            }
            if (allFilesRoot != null)
                MediaScannerConnection.scanFile(sConfigData.getContext(), allFilesRoot, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                d("ExternalStorage", "Scanned " + path + ":");
                                d("ExternalStorage", "uri=" + uri);
                            }
                        });
        } else {
            sConfigData.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
                    .parse("file://" + folderParent.getPath())));
        }
    }

    public static String getDatabasePath() throws Exception {
        if (sConfigData == null)
            throw new Exception("must be setup source data base!");
        return Environment.getExternalStorageDirectory() + File.separator + sConfigData.getNameFolder() + File.separator + sConfigData.getNameDB() + ".s3db";
    }

    static class ObjectDbConfig {
        private String mNameFolder;
        private String mNameDB;
        private int mVersion;
        private Context mContext;

        public String getNameFolder() {
            return mNameFolder;
        }

        public void setNameFolder(String mNameFolder) {
            this.mNameFolder = mNameFolder;
        }

        public String getNameDB() {
            return mNameDB;
        }

        public void setNameDB(String mNameDB) {
            this.mNameDB = mNameDB;
        }

        public int getVersion() {
            return mVersion;
        }

        public void setVersion(int mVersion) {
            this.mVersion = mVersion;
        }

        public Context getContext() {
            return mContext;
        }

        public void setContext(Context mContext) {
            this.mContext = mContext;
        }
    }
}
