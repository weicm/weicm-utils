package cn.weicm.pool;

public interface Opt<T, R> {
        R option(T res) throws Exception;
    }