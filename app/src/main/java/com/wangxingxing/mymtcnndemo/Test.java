package com.wangxingxing.mymtcnndemo;

public class Test {

    /**
     * AndroidStudio Live Templates
     * Create a singleton
     *
     * private volatile static $class$ instance = null;
     * private $class$(){}
     * public static $class$ getInstance() {
     *     if (instance == null) {
     *         synchronized ($class$.class) {
     *             if (instance == null) {
     *                 instance = new $class$();
     *             }
     *         }
     *     }
     *     return instance;
     * }
     *
     * shortcut key: sig
     */

    private volatile static Test instance = null;
    private Test(){}
    public static Test getInstance() {
        if (instance == null) {
            synchronized (Test.class) {
                if (instance == null) {
                    instance = new Test();
                }
            }
        }
        return instance;
    }
}
