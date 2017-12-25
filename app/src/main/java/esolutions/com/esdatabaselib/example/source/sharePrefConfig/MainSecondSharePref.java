package esolutions.com.esdatabaselib.example.source.sharePrefConfig;

import java.util.Set;

import esolutions.com.esdatabaselib.baseSharedPref.anonation.KeyType;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.SharePref;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.TYPE;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Params;

/**
 * Created by VinhNB on 11/9/2017.
 */
@SharePref(name = "MainSecondSharePref")
public class MainSecondSharePref {

    @KeyType(name = "mainSecondString", TYPE = TYPE.STRING)
    public String mainSecondString;

    @KeyType(name = "mainSecondStringSet", TYPE = TYPE.STRING_SET)
    public Set<String> mainSecondStringSet;

    @KeyType(name = "mainSecondInt", TYPE = TYPE.INT)
    public int mainSecondInt;

    @KeyType(name = "mainSecondLong", TYPE = TYPE.LONG)
    public long mainSecondLong;

    @KeyType(name = "mainSecondFloat", TYPE = TYPE.FLOAT)
    public float mainSecondFloat;

    @KeyType(name = "mainSecondBoolean", TYPE = TYPE.BOOLEAN)
    public boolean mainSecondBoolean;

    public MainSecondSharePref(
            @Params(name = "mainSecondString") String mainSecondString,
            @Params(name = "mainSecondStringSet") Set<String> mainSecondStringSet,
            @Params(name = "mainSecondInt") int mainSecondInt,
            @Params(name = "mainSecondLong") long mainSecondLong,
            @Params(name = "mainSecondFloat") float mainSecondFloat,
            @Params(name = "mainSecondBoolean") boolean mainSecondBoolean) {
        this.mainSecondString = mainSecondString;
        this.mainSecondStringSet = mainSecondStringSet;
        this.mainSecondInt = mainSecondInt;
        this.mainSecondLong = mainSecondLong;
        this.mainSecondFloat = mainSecondFloat;
        this.mainSecondBoolean = mainSecondBoolean;
    }

    public MainSecondSharePref() {
    }
}
