package esolutions.com.esdatabaselib.example.source;

import esolutions.com.esdatabaselib.anonation.AutoIncrement;
import esolutions.com.esdatabaselib.anonation.Collumn;
import esolutions.com.esdatabaselib.anonation.PrimaryKey;
import esolutions.com.esdatabaselib.anonation.TYPE;
import esolutions.com.esdatabaselib.anonation.Table;

/**
 * Created by VinhNB on 10/10/2017.
 */

@Table(name = "ClassRoom")
public class ClassRoom {
    @PrimaryKey
    @AutoIncrement
    @Collumn(name = "ClassRoomID", type = TYPE.INTEGER, other = "NOT NULL")
    private int id;

    @Collumn(name = "ClassRoomName", type = TYPE.TEXT, other = "NOT NULL")
    private String name;
}

