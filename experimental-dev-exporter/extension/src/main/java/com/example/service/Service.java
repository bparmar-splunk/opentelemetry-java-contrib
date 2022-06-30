package com.example.service;

import com.example.javaagent.CustomHECLogExporter;
import com.google.gson.*;
import io.opentelemetry.sdk.logs.data.LogData;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class Service {
    private static final Gson gson;
    private static final boolean DISABLE_CERTIFICATE_VALIDATION = true;
    private static final String AUTHORIZATION_HEADER_TAG = "Authorization";
    private static final String AUTHORIZATION_HEADER_SCHEME = "Splunk %s";
    private static final String RAW_SERVICE_URL = "/services/collector/raw";

    private static final OkHttpClient httpClient;

    private static TimeoutSettings timeoutSettings = new TimeoutSettings();

    static {
        gson = new Gson().newBuilder().registerTypeAdapter(LogData.class, new JsonSerializer<LogData>() {
            @Override
            public JsonElement serialize(LogData src, Type typeOfSrc, JsonSerializationContext context) {

                JsonElement jsonElement = new Gson().toJsonTree(src);
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                // This format is based on Splunk Date time format.
                String formattedDate = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss.SSS Z"));
                jsonObject.addProperty("time", formattedDate);
                return jsonObject;
            }
        }).create();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(timeoutSettings.connectTimeout, TimeUnit.MILLISECONDS).callTimeout(timeoutSettings.callTimeout, TimeUnit.MILLISECONDS).readTimeout(timeoutSettings.readTimeout, TimeUnit.MILLISECONDS).writeTimeout(timeoutSettings.writeTimeout, TimeUnit.MILLISECONDS);


        if (DISABLE_CERTIFICATE_VALIDATION) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }};
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        httpClient = builder.build();
    }

    /**
     * This method is responsible to send the events to Splunk server.
     *
     * @param logData
     */
    public void sendEvents(LogData logData) throws Exception {

        Request.Builder requestBuilder = new Request.Builder().url(CustomHECLogExporter.SPLUNK_BASE_URL + RAW_SERVICE_URL)
                .addHeader(AUTHORIZATION_HEADER_TAG, String.format(AUTHORIZATION_HEADER_SCHEME, CustomHECLogExporter.HEC_TOKEN));

        String body = gson.getAdapter(LogData.class).toJson(logData);

        requestBuilder.post(RequestBody.create(body, MediaType.parse("application/json")));

        httpClient.newCall(requestBuilder.build()).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                System.out.println("Response Code : " + response.code());
                System.out.println("Message : " + response.message());
                response.close();
            }
        });

    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public static class TimeoutSettings {
        public static final long DEFAULT_CONNECT_TIMEOUT = 3000;
        public static final long DEFAULT_WRITE_TIMEOUT = 10000; // 0 means no timeout
        public static final long DEFAULT_CALL_TIMEOUT = 0;
        public static final long DEFAULT_READ_TIMEOUT = 10000;
        public static final long DEFAULT_TERMINATION_TIMEOUT = 0;

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
}
