package esolutions.com.esdatabaselib.baseSqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import esolutions.com.esdatabaselib.baseSqlite.anonation.AutoIncrement;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Collumn;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Params;
import esolutions.com.esdatabaselib.baseSqlite.anonation.PrimaryKey;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Table;

public class SqlDAO {

    private static final String TAG = SqlDAO.class.getName();
    private Database mDatabase;
    private Context mContext;

    public SqlDAO(SQLiteDatabase database, Context context) {
        mDatabase = new Database(database);
        mContext = context;
    }

    //region select

    /**
     * select all by mode lazy,
     *
     * @param tClass: Class model with full anonations
     * @param cursor: {@link Cursor} nullable, if null default select * from table
     * @param <T>:    Name Class model
     * @return
     */
    public <T> LazyList<T> selectAllLazy(Class<T> tClass, @Nullable Cursor cursor) {
        //check annotation table class
        final Class<?> classz = tClass;
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);

        if (cursor == null) {
            String querySelectAll = "Select * from " + annTable.name();
            cursor = mDatabase.rawQuery(querySelectAll, null);
        }

        ItemFactory<T> item = new ItemFactory<T>(tClass) {
            @Override
            T create(Cursor cursor, int index) {
                T object = null;

                HashMap<String, Object> data = new HashMap<>();
                T classResult = null;
                StringBuilder messsageThrow = new StringBuilder();
                try {
                    object = getItemClassType().newInstance();
                    cursor.moveToPosition(index);

                    //TODO get name column and get value it, then set value it
                    //check annotation table class
                    Class<?> classz = object.getClass();
                    boolean isTableSQL = classz.isAnnotationPresent(Table.class);
                    if (!isTableSQL)
                        throw new RuntimeException("Class not description is table!");
                    Table annTable = classz.getAnnotation(Table.class);

                    //insert object with reflection
                    Field[] fields = object.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        String fieldName = field.getName();
                        if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                            break;

                        //get name column by check annotation
                        boolean isCollumn = field.isAnnotationPresent(Collumn.class);
                        if (!isCollumn)
                            break;
                        Collumn collumn = field.getAnnotation(Collumn.class);

                        //set value reflection
                        field.setAccessible(true);
                        Object value = field.get(object);
                        if (value == null) {
                            int columnIndex = cursor.getColumnIndex(collumn.name());
                            value = cursor.getString(columnIndex);
                        }

                        data.put(fieldName, value);
                    }

                    //get method setter
                    Constructor[] constructors = classz.getDeclaredConstructors();
                    if (constructors.length != 0) {
                        ArrayList<Annotation> annotations = getmAnnotationsContructor();
                        if (super.getmAnnotationsContructor() == null) {
                            for (Constructor ctor : constructors) {
                                Annotation[][] annotationss = ctor.getParameterAnnotations();
                                annotations = new ArrayList<>();
                                for (Annotation[] annotation1 : annotationss) {
                                    for (Annotation annotation : annotation1) {
                                        if (annotation instanceof Params) {
                                            annotations.add(annotation);
                                        }
                                    }
                                }

                                int sizeParamsCtor = ctor.getParameterTypes().length;
                                if (sizeParamsCtor == 0)
                                    continue;

                                Object[] objectCtor = new Object[annotations.size()];
                                if (annotations.size() != sizeParamsCtor) {
                                    continue;
                                }
                                for (int i = 0; i < annotations.size(); i++) {
                                    if (annotations.get(i) instanceof Params) {
                                        String key = ((Params) annotations.get(i)).name();
                                        if (data.containsKey(key)) {
                                            objectCtor[i] = data.get(key);
                                        }
                                    }
                                }
                                super.setsConstructor(ctor);
                                super.setmAnnotationsContructor((ArrayList<Annotation>) annotations.clone());

                                //Create object
                                classResult = (T) ctor.newInstance(objectCtor);
                            }
                        } else {
                            Object[] objectCtor = new Object[annotations.size()];
                            for (int i = 0; i < annotations.size(); i++) {
                                if (annotations.get(i) instanceof Params) {
                                    String key = ((Params) annotations.get(i)).name();
                                    if (data.containsKey(key)) {
                                        objectCtor[i] = data.get(key);
                                    }
                                }
                            }

                            //Create object
                            classResult = (T) super.getsConstructor().newInstance(objectCtor);
                        }

                    }
                } catch (InstantiationException ie) {
                    messsageThrow.append(ie.getMessage());
                    ie.printStackTrace();
                } catch (IllegalAccessException iae) {
                    messsageThrow.append(iae.getMessage());
                    iae.printStackTrace();
                } catch (Exception e) {
                    messsageThrow.append(e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (classResult == null && messsageThrow.length() != 0) {
                        messsageThrow.append("\n").append("Class " + classz.getSimpleName() + " must be has constructor with full param and annotation!");
                        Log.e(TAG, messsageThrow.toString());
                    }
                }

                return classResult;
            }
        };

        LazyList<T> result = new LazyList<T>(cursor, item);
        return result;
    }

    public Cursor getCursor(String query, String[] selectionArgs) throws Exception {
        return mDatabase.rawQuery(query, selectionArgs);
    }

    //endregion

    //region insert
    public <T> Long[] insert(List<T> objects) {
        Long[] indexInserts = new Long[objects.size()];

        int count = 0;
        mDatabase.beginTransaction();
        for (T object : objects) {
            try {
                indexInserts[count] = insert(object);
                count++;
            } catch (Exception e) {
                indexInserts[count] = 0l;
                Log.e(TAG, "insert faild at index " + count);
                e.printStackTrace();
            }
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();

        return indexInserts;
    }

    public <T> long insert(T object) throws Exception {
        //check annotation table class
        Class<?> classz = object.getClass();
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);

        //insert object with reflection
        Field[] fields = object.getClass().getDeclaredFields();
        ContentValues contentValues = new ContentValues();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                break;

            //check annotation collumn
            boolean isCollumn = field.isAnnotationPresent(Collumn.class);
            if (!isCollumn)
                break;
            Collumn collumn = field.getAnnotation(Collumn.class);
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);

            field.setAccessible(true);
            Object value = field.get(object);
            String sValue = (value == null) ? "" : value.toString();

            if (isPrimaryKey && isAutoIncrement) continue;

            contentValues.put(collumn.name(), sValue);
        }

        DatabaseParams.Insert params = new DatabaseParams.Insert(annTable.name(), null, contentValues);
        return mDatabase.insert(params);
    }
    //endregion

    //region delete
    public <T> int deleteAll(Class<T> tClass) throws Exception {
        //check annotation table class
        Class<?> classz = tClass;
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);

        DatabaseParams.Delete paramDelete = new DatabaseParams.Delete(annTable.name(), null, null);
        return mDatabase.delete(paramDelete);
    }
    //endregion

    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public class Database {

        private SQLiteDatabase mDatabase;

        public Database(SQLiteDatabase database) {
            mDatabase = database;
        }

        public int delete(DatabaseParams.Delete params) {
            return mDatabase.delete(params.table, params.whereClause, params.whereArgs);
        }

        public long insert(DatabaseParams.Insert params) {
            return mDatabase.insert(params.table, params.nullColumnHack, params.values);
        }

        public Cursor select(DatabaseParams.Select params) {
            return mDatabase.query(params.distinct, params.table, params.columns, params.selection,
                    params.selectionArgs, params.groupBy, params.having, params.orderBy, params.limit);
        }

        public int update(SQLiteDatabase database, DatabaseParams.Update params) {
            return database.update(params.table, params.values, params.whereClause, params.whereArgs);
        }

        public void beginTransaction() {
            mDatabase.beginTransaction();
        }

        public void endTransaction() {
            mDatabase.endTransaction();
        }

        public void setTransactionSuccessful() {
            mDatabase.setTransactionSuccessful();
        }

        public Cursor rawQuery(String sql, String[] selectionArgs) {
            return mDatabase.rawQuery(sql, selectionArgs);
        }

        public void execSQL(String sql, Object[] bindArgs) throws SQLException {
            mDatabase.execSQL(sql, bindArgs);
        }

        public void execSQL(String sql) throws SQLException {
            mDatabase.execSQL(sql);
        }
    }
}