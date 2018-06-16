package com.nevercaution.elasticsearch.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;

@Component
public class GsonUtil {

    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .disableHtmlEscaping()
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT)
            .create();

    public static Gson gson() {
        return gson;
    }
}
