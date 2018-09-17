package jaina.api;

/**
 * Created by kuma on 2018/9/17.
 */

public interface Bind<T> {

    void bindView(T host, Object object, Finder provider);

}
