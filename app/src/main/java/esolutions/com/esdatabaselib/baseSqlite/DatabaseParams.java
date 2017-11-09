package esolutions.com.esdatabaselib.baseSqlite;

import android.content.ContentValues;

public class DatabaseParams {

    public static class Insert {

        public String table;
        public String nullColumnHack;
        public ContentValues values;

        public Insert(String table, String nullColumnHack, ContentValues values) {
            this.table = table;
            this.nullColumnHack = nullColumnHack;
            this.values = values;
        }
    }

    public static class Delete {

        public String table;
        public String whereClause;
        public String[] whereArgs;

        public Delete(String table, String whereClause, String[] whereArgs) {
            this.table = table;
            this.whereClause = whereClause;
            this.whereArgs = whereArgs;
        }
    }

    public static class Select {

        public boolean distinct;
        public String table;
        public String[] columns;
        public String selection;
        public String[] selectionArgs;
        public String groupBy;
        public String having;
        public String orderBy;
        public String limit;
    }

    public static class Update {

        public String table;
        public ContentValues values;
        public String whereClause;
        public String[] whereArgs;
    }
}