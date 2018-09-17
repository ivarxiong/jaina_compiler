package jaina.compiler.dispose;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kuma on 2018/9/14.
 */
public class ViewElement {

    private static final Pattern pattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);

    public String id;
    public String fieldType;

    public ViewElement(String sourceId, String type) {
        final Matcher matcher = pattern.matcher(sourceId);
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2);
        }
        this.fieldType = type;
    }

    public String getId() {
        return id;
    }

    public String getFieldName() {
        String[] names = id.split("_");
        StringBuilder builder = new StringBuilder();
        builder.append("m");
        for(String str: names) {
            builder.append(disposeString(str));
        }
        return builder.toString();
    }

    public String getFieldType() {
        if(fieldType.contains(".")) {
            return fieldType;
        }else if("View".equals(fieldType)) {
            return "android.view.View";
        } else {
            return "android.widget." + fieldType;
        }
    }

    public static String disposeString(String str) {
        if(str == null || "".equals(str)) return "";
        return str.substring(0, 1).toUpperCase(Locale.CHINA) + str.substring(1);
    }

}
