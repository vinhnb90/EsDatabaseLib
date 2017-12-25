package esolutions.com.esdatabaselib.baseSharedPref.anonation;

/**
 * Created by VinhNB on 10/10/2017.
 */

public enum TYPE {
    STRING("STRING"),
    STRING_SET("STRING_SET"),
    INT("INT"),
    LONG("LONG"),
    FLOAT("FLOAT"),
    BOOLEAN("BOOLEAN")
    ;

    private String contentType;

    TYPE(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public static TYPE findByContent(String content) {
        for (TYPE v : values()) {
            if (v.getContentType().equalsIgnoreCase(content)){
                return v;
            }
        }
        return null;
    }
}
