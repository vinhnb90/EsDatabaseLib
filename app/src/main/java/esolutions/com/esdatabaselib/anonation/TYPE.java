package esolutions.com.esdatabaselib.anonation;

/**
 * Created by VinhNB on 10/10/2017.
 */

public enum TYPE {
    NULL("NULL"),
    INTEGER("INTEGER"),
    REAL("REAL"),
    TEXT("TEXT"),
    BLOB("BLOB")
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
