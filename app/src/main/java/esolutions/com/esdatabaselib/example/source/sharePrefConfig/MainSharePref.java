package esolutions.com.esdatabaselib.example.source.sharePrefConfig;

import java.util.Set;

import esolutions.com.esdatabaselib.baseSharedPref.anonation.KeyType;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.SharePref;
import esolutions.com.esdatabaselib.baseSharedPref.anonation.TYPE;
import esolutions.com.esdatabaselib.baseSqlite.anonation.Params;

/**
 * Created by VinhNB on 11/9/2017.
 */
@SharePref(name = "MainSharePref")
public class MainSharePref {
    @KeyType(name = "MainString", TYPE = TYPE.STRING)
    public String mainString;

    @KeyType(name = "MainStringSet", TYPE = TYPE.STRING_SET)
    public Set<String> mainStringSet;

    @KeyType(name = "MainInt", TYPE = TYPE.INT)
    public int mainInt;

    @KeyType(name = "MainLong", TYPE = TYPE.LONG)
    public long mainLong;

    @KeyType(name = "MainFloat", TYPE = TYPE.FLOAT)
    public float mainFloat;

    @KeyType(name = "MainBoolean", TYPE = TYPE.BOOLEAN)
    public boolean mainBoolean;


    public MainSharePref(
            @Params(name = "MainString") String mainString,
            @Params(name = "MainStringSet") Set<String> mainStringSet,
            @Params(name = "MainInt") int mainInt,
            @Params(name = "MainLong") long mainLong,
            @Params(name = "MainFloat") float mainFloat,
            @Params(name = "MainBoolean") boolean mainBoolean) {
        this.mainString = mainString;
        this.mainStringSet = mainStringSet;
        this.mainInt = mainInt;
        this.mainLong = mainLong;
        this.mainFloat = mainFloat;
        this.mainBoolean = mainBoolean;
    }
}
