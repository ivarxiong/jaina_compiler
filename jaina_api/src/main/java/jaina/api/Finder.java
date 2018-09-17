package jaina.api;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * Created by kuma on 2018/9/17.
 */
public enum Finder {
    VIEW {
        public View findView(Object source, int id) {
            return ((View) source).findViewById(id);
        }

        public Context getContext(Object source) {
            return ((View) source).getContext();
        }
    },
    ACTIVITY {
        public View findView(Object source, int id) {
            return ((Activity) source).findViewById(id);
        }

        public Context getContext(Object source) {
            return (Activity) source;
        }
    },
    DIALOG {
        public View findView(Object source, int id) {
            return ((Dialog) source).findViewById(id);
        }

        public Context getContext(Object source) {
            return ((Dialog) source).getContext();
        }
    };

    public abstract View findView(Object source, int id);

    public abstract Context getContext(Object source);

}