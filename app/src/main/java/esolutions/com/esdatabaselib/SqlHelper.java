package esolutions.com.esdatabaselib;

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
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;

import esolutions.com.esdatabaselib.anonation.Collumn;
import esolutions.com.esdatabaselib.anonation.DBConfig;
import esolutions.com.esdatabaselib.anonation.PrimaryKey;
import esolutions.com.esdatabaselib.anonation.TYPE;
import esolutions.com.esdatabaselib.anonation.Table;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static android.util.Log.d;


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

    private SqlHelper() throws Exception {
        super(sConfigData.getContext(), Environment.getExternalStorageDirectory() + File.separator + sConfigData.getNameFolder() + File.separator + sConfigData.getNameDB() + ".s3db", null, sConfigData.getVersion());
        SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + sConfigData.getNameFolder() + File.separator + sConfigData.getNameDB() + ".s3db", null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        if (newVer > oldVer) {
            for (Class<?> classz : sClassDBTable) {
                sqLiteDatabase.execSQL(getQueryDropTable(classz));
            }
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (Class<?> classz : sClassDBTable) {
            sqLiteDatabase.execSQL(getQueryCreateTable(classz));
        }
    }

    /**
     * get DB
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
     * @param classDBTable {@link Table}
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
        if (!hasPermission(context))
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
            sIntance = new SqlHelper();
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

    private String getQueryCreateTable(Class<?> classz) {
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");

        Table annTable = classz.getAnnotation(Table.class);

        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS "
                + annTable.name()
                + "(");

        Field[] fields = classz.getFields();
        for (int i = 0; i < fields.length; i++) {
            boolean isPrimaryKey = fields[i].isAnnotationPresent(PrimaryKey.class);
            boolean isCollumn = fields[i].isAnnotationPresent(Collumn.class);
            Collumn collumn = fields[i].getAnnotation(Collumn.class);

            if (!isCollumn)
                break;

            if (i != 0 && i != fields.length - 1)
                query.append(",");

            query.append(" "
                    + collumn.name()
                    + " "
                    + collumn.type().getContentType()
                    + (isPrimaryKey ? " PRIMARY KEY" : "")
                    + " "
                    + collumn.other());
        }

        query.append(");");
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

    public static boolean hasPermission(Context context) {
        boolean hasPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(context.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
            } else {
                hasPermission = true;
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                hasPermission = false;
            else
                hasPermission = true;
        }
        return hasPermission;
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
