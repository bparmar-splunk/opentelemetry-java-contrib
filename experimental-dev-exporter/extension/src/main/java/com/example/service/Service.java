package com.example.service;

import com.example.javaagent.CustomHECLogExporter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import io.opentelemetry.sdk.logs.data.LogData;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible to establish connection between client and server.
 * It makes use of pre-defined constants and environment variables to manage connection.
 */
public class Service {
    private static final Gson gson;
    private static final boolean DISABLE_CERTIFICATE_VALIDATION = true;
    private static final String AUTHORIZATION_HEADER_TAG = "Authorization";
    private static final String AUTHORIZATION_HEADER_SCHEME = "Splunk %s";
    private static final String RAW_SERVICE_URL = "/services/collector/raw";

    private static final OkHttpClient httpClient;

    private static TimeoutSettings timeoutSettings = new TimeoutSettings();

    private static final Logger LOGGER = Logger.getLogger(Service.class.getName());

    static {
        gson = new Gson().newBuilder().registerTypeAdapter(LogData.class, (JsonSerializer<LogData>) (src, typeOfSrc, context) -> {

            JsonElement jsonElement = new Gson().toJsonTree(src);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // This format is based on Splunk Date time format.
            String formattedDate = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss.SSS Z"));
            jsonObject.addProperty("time", formattedDate);
            return jsonObject;
        }).create();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(timeoutSettings.connectTimeout, TimeUnit.MILLISECONDS).callTimeout(timeoutSettings.callTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutSettings.readTimeout, TimeUnit.MILLISECONDS).writeTimeout(timeoutSettings.writeTimeout, TimeUnit.MILLISECONDS);


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

                LOGGER.warning("Certificate(s) validation is disabled. Hence all certificates are accepted by default.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
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

        Request.Builder requestBuilder = new Request.Builder().url(CustomHECLogExporter.SPLUNK_BASE_URL + RAW_SERVICE_URL).addHeader(AUTHORIZATION_HEADER_TAG, String.format(AUTHORIZATION_HEADER_SCHEME, CustomHECLogExporter.HEC_TOKEN));

        String body = gson.getAdapter(LogData.class).toJson(logData);

        requestBuilder.post(RequestBody.create(body, MediaType.parse("application/json")));

        httpClient.newCall(requestBuilder.build()).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                LOGGER.info("Response Code: " + response.code() + " -- Message : " + response.message());
                response.close();
            }
        });

    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

}
