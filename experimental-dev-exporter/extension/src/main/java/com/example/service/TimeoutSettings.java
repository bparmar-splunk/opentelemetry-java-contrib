package com.example.service;

/**
 * TimeoutSettings class used to set timeout parameters for every new request created using OKHttp3 Library.
 */
public class TimeoutSettings {

    public static final long NO_TIMEOUT = 0;

    public static final long DEFAULT_CONNECT_TIMEOUT = 3000;
    public static final long DEFAULT_WRITE_TIMEOUT = 10000;
    public static final long DEFAULT_CALL_TIMEOUT = NO_TIMEOUT;
    public static final long DEFAULT_READ_TIMEOUT = 10000;
    public static final long DEFAULT_TERMINATION_TIMEOUT = NO_TIMEOUT;

    public long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    public long callTimeout = DEFAULT_CALL_TIMEOUT;
    public long readTimeout = DEFAULT_READ_TIMEOUT;
    public long writeTimeout = DEFAULT_WRITE_TIMEOUT;
    public long terminationTimeout = DEFAULT_TERMINATION_TIMEOUT;

    public TimeoutSettings() {
    }

    public TimeoutSettings(long connectTimeout, long callTimeout, long readTimeout, long writeTimeout, long terminationTimeout) {
        this.connectTimeout = connectTimeout;
        this.callTimeout = callTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.terminationTimeout = terminationTimeout;
    }
}
