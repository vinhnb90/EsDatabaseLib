package esolutions.com.esdatabaselib.example.source.sqliteConfig;


import esolutions.com.esdatabaselib.baseSqlite.anonation.AutoIncrement;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Collumn;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Params;
import esolutions.com.esdatabaselib.baseSqlite.anonation.PrimaryKey;
import esolutions.com.esdatabaselib.baseSqlite.anonation.TYPE;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Table;

/**
 * Created by VinhNB on 10/10/2017.
 */

@Table(name = "Students")
public class Student {
    @PrimaryKey
    @AutoIncrement
    @Collumn(name = "StudentID", type = TYPE.INTEGER, other = "NOT NULL")
    private int id;

    @Collumn(name = "StudentName", type = TYPE.TEXT, other = "NOT NULL")
    private String mName;

    @Collumn(name = "StudentClass")
    private String className;


    public Student() {
    }

    public Student(@Params(name = "id") int id, @Params(name = "mName") String mName, @Params(name = "className") String className) {
        this.id = id;
        this.mName = mName;
        this.className = className;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}

