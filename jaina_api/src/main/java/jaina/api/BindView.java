package jaina.api;

import android.app.Activity;
import android.view.View;

/**
 * Created by ivar on 2018/9/15.
 */

public class BindView {

    public static void bindView(Object host, Activity activity) {
        bindView(host, activity, Finder.ACTIVITY);
    }

    public static void bindView(Object host, View view) {
        bindView(host, view, Finder.VIEW);
    }

    private static void bindView(Object host, Object object, Finder finder) {
        String className = host.getClass().getName();
        try {
            Class<?> aClass = Class.forName(className + "$$ViewInject");
            Bind bind = (Bind) aClass.newInstance();
            bind.bindView(host, object, finder);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
