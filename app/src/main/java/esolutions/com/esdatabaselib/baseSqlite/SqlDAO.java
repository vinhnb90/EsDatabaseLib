package esolutions.com.esdatabaselib.baseSqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import esolutions.com.esdatabaselib.baseSqlite.anonation.AutoIncrement;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Collumn;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Params;
import esolutions.com.esdatabaselib.baseSqlite.anonation.PrimaryKey;
import esolutions.com.esdatabaselib.baseSqlite.anonation.TYPE;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Table;

public class SqlDAO {

    protected static final String TAG = SqlDAO.class.getName();
    protected Database mDatabase;
    protected Context mContext;

    public SqlDAO(SQLiteDatabase database, Context context) {
        mDatabase = new Database(database);
        mContext = context;
    }

    //region select

    /**
     * select all by mode lazy,
     *
     * @param tClass: Class model with full anonations
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
            protected T create(Cursor cursor, int index) {
                T object = null;

                HashMap<String, Object> data = new HashMap<>();
                T classResult = null;
                StringBuilder messsageThrow = new StringBuilder();

                //check cursor
                if (cursor.getCount() == 0) {
                    Toast.makeText(mContext, "Không có dữ liệu trong cursor của table!", Toast.LENGTH_SHORT).show();
                    return classResult;
                }

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
                            continue;

                        //get name column by check annotation
                        boolean isCollumn = field.isAnnotationPresent(Collumn.class);
                        if (!isCollumn)
                            continue;
                        Collumn collumn = field.getAnnotation(Collumn.class);

                        //set value reflection
                        field.setAccessible(true);
                        Object value = field.get(object);
                        int columnIndex = cursor.getColumnIndex(collumn.name());

                        if (collumn.type() == TYPE.INTEGER)
                            value = cursor.getInt(columnIndex);

                        else {
                            if (value == null) {
                                if (collumn.type() == TYPE.TEXT)
                                    value = cursor.getString(columnIndex);

                                if (collumn.type() == TYPE.BLOB)
                                    value = cursor.getString(columnIndex);

                                if (collumn.type() == TYPE.REAL)
                                    value = cursor.getString(columnIndex);
                            }
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
                        Toast.makeText(mContext, messsageThrow.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                return classResult;
            }
        };

        LazyList<T> result = new LazyList<T>(cursor, item);
        return result;
    }

    public <T> LazyList<T> selectCustomLazy(Cursor cursor, ItemFactory<T> itemFactory) {
        return new LazyList<>(cursor, itemFactory);
    }
    //endregion

    //region insert
    public <T> Long[] insert(Class<T> tableClass, List<T> objects) {
        Long[] indexInserts = new Long[objects.size()];

        int count = 0;
        mDatabase.beginTransaction();
        for (T object : objects) {
            try {
                indexInserts[count] = insert(tableClass, object);
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

    public <T> long insert(Class<T> tableClass, T object) throws Exception {
        //check annotation table class
        Class<?> classz = tableClass;
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
                continue;

            //check annotation collumn
            boolean isCollumn = field.isAnnotationPresent(Collumn.class);
            if (!isCollumn)
                continue;
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

    public <T> boolean isExistRows(Class<T> tClass, String[] nameCollumnCheck, String[] valuesCheck) throws Exception {
        //check validate
        if (nameCollumnCheck.length != valuesCheck.length)
            throw new Exception("total collumn not same total value object!");


        //check annotation table class
        Class<?> classz = tClass;
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);


        //lấy tất cả field ngoài $change and serialVersionUID
        //kiểm tra collumn annotations và lấy dữ liệu tạo DatabaseParams.Select và chuỗi điều kiện whereClause và whereArgs
        //nếu giá trị của trường dataCheck = null thì khi isExistRows sẽ bỏ qua trường đó
        Field[] fields = classz.getDeclaredFields();
        DatabaseParams.Select param = new DatabaseParams.Select();
        StringBuilder whereClause = new StringBuilder();
        HashMap<String, Collumn> listCollumn = new HashMap<>();
        ArrayList<String> nameFieldHasAnnotationsCollumn = new ArrayList<>();
        List<String> whereArgs = new ArrayList<>();


        int index = 0;
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                continue;


            //check annotation collumn
            boolean isCollumn = field.isAnnotationPresent(Collumn.class);
            if (!isCollumn)
                continue;
            Collumn collumn = field.getAnnotation(Collumn.class);
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);


            //thêm vào Map cột và tên field cột đó
            //tăng index để loại bỏ "and" nếu index > 0
            listCollumn.put(fieldName, collumn);
            nameFieldHasAnnotationsCollumn.add(fieldName);
//            whereClause.append(collumn.name() + " = ? ").append(" and ");
//            whereArgs.add(value.toString());
            index++;
        }


        //tăng index để loại bỏ "and" nếu index > 0
        if (index == 0)
            throw new RuntimeException("Data isExistRows null all field!");


        //khi có list collumn thì check name collumn trong String[] nameCollumn
        ArrayList<String> nameCollumnListInput = new ArrayList<>(Arrays.asList(nameCollumnCheck));
        ArrayList<String> valuesCheckListInput = new ArrayList<>(Arrays.asList(valuesCheck));


        //nếu có field không trùng tên như trong nameFieldHasAnnotationsCollumn của table thì báo Exceptions
        ArrayList<String> collumnSame = (ArrayList<String>) nameCollumnListInput.clone();
        collumnSame.retainAll(nameFieldHasAnnotationsCollumn);
        ArrayList<String> collumnNotSame = (ArrayList<String>) nameCollumnListInput.clone();
        collumnNotSame.removeAll(collumnSame);


        //check đầu vào
        if (collumnNotSame.size() != 0) {
            StringBuilder message = new StringBuilder("Vui lòng kiểm tra lại các trường input nameCollumn[] không trùng annonations ở bảng " + annTable.name() + " gồm ");
            for (String s : collumnNotSame
                    ) {
                message.append(", " + s);
            }
            throw new Exception(message.toString());
        }


        //create param
        for (int i = 0; i < nameCollumnCheck.length; i++) {
            Collumn collumn = listCollumn.get(nameCollumnCheck[i]);
            whereClause.append(collumn.name() + " = ? ").append(" and ");
            whereArgs.add(valuesCheck[i]);
        }


        //tinh chỉnh câu query
        whereClause.delete(whereClause.length() - (" and ").length(), whereClause.length());


        //select database
        param.table = annTable.name();
        param.columns = nameCollumnCheck;
        param.selection = whereClause.toString();
        param.selectionArgs = whereArgs.toArray(new String[whereArgs.size()]);


        //get Cursor and close db
        Cursor cur = mDatabase.select(param);
        boolean exist = (cur.getCount() > 0);
        cur.close();


        return exist;
    }

    public <T> long updateRows(Class<T> tClass, T dataOld, T dataNew) throws Exception {
        //check validate
        //check annotation table class
        Class<?> classz = tClass;
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);


        //lấy tất cả field ngoài $change and serialVersionUID
        //kiểm tra collumn annotations và lấy dữ liệu tạo DatabaseParams.Select và chuỗi điều kiện whereClause và whereArgs
        //nếu giá trị của trường dataCheck = null thì khi isExistRows sẽ bỏ qua trường đó
        Field[] fields = classz.getDeclaredFields();
        DatabaseParams.Update paramUpdate = new DatabaseParams.Update();
        StringBuilder whereClause = new StringBuilder();
        HashMap<String, Collumn> listCollumn = new HashMap<>();
        HashMap<Field, Collumn> listCollumnPrimaryKey = new HashMap<>();
        ArrayList<String> valuesCheck = new ArrayList<>();
        ContentValues contentValues = new ContentValues();
        int index = 0;
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                continue;


            //check annotation collumn
            boolean isCollumn = field.isAnnotationPresent(Collumn.class);
            if (!isCollumn)
                continue;
            Collumn collumn = field.getAnnotation(Collumn.class);
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);

            if (isPrimaryKey && isAutoIncrement) {
                listCollumnPrimaryKey.put(field, collumn);
                continue;
            }
            if (isPrimaryKey)
                listCollumnPrimaryKey.put(field, collumn);


            //thêm vào Map cột và tên field cột đó
            //tăng index để loại bỏ "and" nếu index > 0
            listCollumn.put(fieldName, collumn);

            //set valueCheck
            field.setAccessible(true);
            contentValues.put(collumn.name(), (field.get(dataNew) == null) ? "" : field.get(dataNew).toString());
            index++;
        }


        //tăng index để loại bỏ "and" nếu index > 0
        if (listCollumnPrimaryKey.size() == 0)
            throw new RuntimeException("class " + annTable.name() + " cần có ít nhất một khóa chính!");

        if (listCollumn.size() == 0)
            throw new RuntimeException("class " + annTable.name() + " cần có ít nhất một cột không phải khóa chính!");

        //create param check
        Set<Field> fieldSet = listCollumnPrimaryKey.keySet();
        ArrayList<String> nameCollumnCheck = new ArrayList<>();
//        ArrayList<String> valuesCheck = new ArrayList<>();

        for (Field field : fieldSet) {
            Collumn collumn = listCollumnPrimaryKey.get(field);
            field.setAccessible(true);
            valuesCheck.add((field.get(dataOld) == null) ? "" : field.get(dataOld).toString());
            nameCollumnCheck.add(collumn.name());
            whereClause.append(collumn.name() + " = ? ").append(" and ");
        }


        //tinh chỉnh câu query
        whereClause.delete(whereClause.length() - (" and ").length(), whereClause.length());


        long rowAffect;
        //select database
        paramUpdate.table = annTable.name();
        paramUpdate.values = contentValues;
        paramUpdate.whereClause = whereClause.toString();
        paramUpdate.whereArgs = valuesCheck.toArray(new String[valuesCheck.size()]);


        //get Cursor and close db
        rowAffect = mDatabase.update(mDatabase.mDatabase, paramUpdate);
        return rowAffect;
    }

    public <T> long updateORInsertRows(Class<T> tClass, T dataOld, T dataNew) throws Exception {
        //check validate
        //check annotation table class
        Class<?> classz = tClass;
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);


        //lấy tất cả field ngoài $change and serialVersionUID
        //kiểm tra collumn annotations và lấy dữ liệu tạo DatabaseParams.Select và chuỗi điều kiện whereClause và whereArgs
        //nếu giá trị của trường dataCheck = null thì khi isExistRows sẽ bỏ qua trường đó
        Field[] fields = classz.getDeclaredFields();
        DatabaseParams.Update paramUpdate = new DatabaseParams.Update();
        DatabaseParams.Insert paramInsert = new DatabaseParams.Insert();
        StringBuilder whereClause = new StringBuilder();
        HashMap<String, Collumn> listCollumn = new HashMap<>();
        HashMap<Field, Collumn> listCollumnPrimaryKey = new HashMap<>();

        ArrayList<String> valuesCheck = new ArrayList<>();
        ContentValues contentValues = new ContentValues();
        int index = 0;
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                continue;


            //check annotation collumn
            boolean isCollumn = field.isAnnotationPresent(Collumn.class);
            if (!isCollumn)
                continue;
            Collumn collumn = field.getAnnotation(Collumn.class);
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);


            if (isPrimaryKey && isAutoIncrement) {
                listCollumnPrimaryKey.put(field, collumn);
                continue;
            }
            if (isPrimaryKey)
                listCollumnPrimaryKey.put(field, collumn);


            //thêm vào Map cột và tên field cột đó
            //tăng index để loại bỏ "and" nếu index > 0
            listCollumn.put(fieldName, collumn);

            //set valueCheck
            field.setAccessible(true);
            contentValues.put(collumn.name(), (field.get(dataNew) == null) ? "" : field.get(dataNew).toString());
            index++;
        }


        //tăng index để loại bỏ "and" nếu index > 0
        if (listCollumnPrimaryKey.size() == 0)
            throw new RuntimeException("class " + annTable.name() + " cần có ít nhất một khóa chính!");

        if (listCollumn.size() == 0)
            throw new RuntimeException("class " + annTable.name() + " cần có ít nhất một cột không phải khóa chính!");

        //create param check
        Set<Field> fieldSet = listCollumnPrimaryKey.keySet();
        ArrayList<String> nameCollumnCheck = new ArrayList<>();
//        ArrayList<String> valuesCheck = new ArrayList<>();

        for (Field field : fieldSet) {
            Collumn collumn = listCollumnPrimaryKey.get(field);
            field.setAccessible(true);
            if(dataOld != null){
                valuesCheck.add((field.get(dataOld) == null) ? "" : field.get(dataOld).toString());
            }
            else {
                valuesCheck.add("");
            }
            nameCollumnCheck.add(collumn.name());
            whereClause.append(collumn.name() + " = ? ").append(" and ");
        }


        //tinh chỉnh câu query
        whereClause.delete(whereClause.length() - (" and ").length(), whereClause.length());


        long rowAffect;
        boolean isHasRow = isExistRows(tClass, nameCollumnCheck.toArray(new String[nameCollumnCheck.size()]), valuesCheck.toArray(new String[valuesCheck.size()]));
        if (isHasRow) {
            //select database
            paramUpdate.table = annTable.name();
            paramUpdate.values = contentValues;
            paramUpdate.whereClause = whereClause.toString();
            paramUpdate.whereArgs = valuesCheck.toArray(new String[valuesCheck.size()]);


            //get Cursor and close db
            rowAffect = mDatabase.update(mDatabase.mDatabase, paramUpdate);
        } else {
            //select database
            paramInsert.table = annTable.name();
            paramInsert.nullColumnHack = null;
            paramInsert.values = contentValues;


            //get Cursor and close db
            rowAffect = mDatabase.insert(paramInsert);
        }
        return rowAffect;
    }

    public <T> int deleteRows(Class<T> tClass, String[] nameCollumnCheck, String[] valuesCheck) throws Exception {
        //check validate
        if (nameCollumnCheck.length != valuesCheck.length)
            throw new Exception("total collumn not same total value object!");


        //check annotation table class
        Class<?> classz = tClass;
        boolean isTableSQL = classz.isAnnotationPresent(Table.class);
        if (!isTableSQL)
            throw new RuntimeException("Class not description is table!");
        Table annTable = classz.getAnnotation(Table.class);


        //lấy tất cả field ngoài $change and serialVersionUID
        //kiểm tra collumn annotations và lấy dữ liệu tạo DatabaseParams.Select và chuỗi điều kiện whereClause và whereArgs
        //nếu giá trị của trường dataCheck = null thì khi isExistRows sẽ bỏ qua trường đó
        Field[] fields = classz.getDeclaredFields();
        DatabaseParams.Delete param = new DatabaseParams.Delete();
        StringBuilder whereClause = new StringBuilder();
        HashMap<String, Collumn> listCollumn = new HashMap<>();
        ArrayList<String> nameFieldHasAnnotationsCollumn = new ArrayList<>();
        List<String> whereArgs = new ArrayList<>();


        int index = 0;
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("$change") || fieldName.equals("serialVersionUID"))
                continue;


            //check annotation collumn
            boolean isCollumn = field.isAnnotationPresent(Collumn.class);
            if (!isCollumn)
                continue;
            Collumn collumn = field.getAnnotation(Collumn.class);
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);


            //thêm vào Map cột và tên field cột đó
            //tăng index để loại bỏ "and" nếu index > 0
            listCollumn.put(fieldName, collumn);
            nameFieldHasAnnotationsCollumn.add(fieldName);
            index++;
        }


        //tăng index để loại bỏ "and" nếu index > 0
        if (index == 0)
            throw new RuntimeException("Data isExistRows null all field!");


        //khi có list collumn thì check name collumn trong String[] nameCollumn
        ArrayList<String> nameCollumnListInput = new ArrayList<>(Arrays.asList(nameCollumnCheck));
        ArrayList<String> valuesCheckListInput = new ArrayList<>(Arrays.asList(valuesCheck));


        //nếu có field không trùng tên như trong nameFieldHasAnnotationsCollumn của table thì báo Exceptions
        ArrayList<String> collumnSame = (ArrayList<String>) nameCollumnListInput.clone();
        collumnSame.retainAll(nameFieldHasAnnotationsCollumn);
        ArrayList<String> collumnNotSame = (ArrayList<String>) nameCollumnListInput.clone();
        collumnNotSame.removeAll(collumnSame);


        //check đầu vào
        if (collumnNotSame.size() != 0) {
            StringBuilder message = new StringBuilder("Vui lòng kiểm tra lại các trường input nameCollumn[] không trùng annonations ở bảng " + annTable.name() + " gồm ");
            for (String s : collumnNotSame
                    ) {
                message.append(", " + s);
            }
            throw new Exception(message.toString());
        }


        //create param
        for (int i = 0; i < nameCollumnCheck.length; i++) {
            Collumn collumn = listCollumn.get(nameCollumnCheck[i]);
            whereClause.append(collumn.name() + " = ? ").append(" and ");
            whereArgs.add(valuesCheck[i]);
        }


        //tinh chỉnh câu query
        whereClause.delete(whereClause.length() - (" and ").length(), whereClause.length());


        //select database
        param.table = annTable.name();
        param.whereClause = whereClause.toString();
        param.whereArgs = valuesCheck;


        //get Cursor and close db
        int rowDelete = mDatabase.delete(param);
        return rowDelete;
    }

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