package esolutions.com.esdatabaselib.example;


import esolutions.com.esdatabaselib.anonation.Collumn;
import esolutions.com.esdatabaselib.anonation.PrimaryKey;
import esolutions.com.esdatabaselib.anonation.TYPE;
import esolutions.com.esdatabaselib.anonation.Table;

/**
 * Created by VinhNB on 10/10/2017.
 */

@Table(name = "Students")
public class Student {
    @PrimaryKey
    @Collumn(name = "StudentID", type = TYPE.INTEGER, other = "AUTOINCREMENT NOT NULL")
    public int id;

    @Collumn(name = "StudentName", type = TYPE.TEXT, other = "NOT NULL")
    public String name;

    @Collumn(name = "StudentClass")
    public String className;
}

