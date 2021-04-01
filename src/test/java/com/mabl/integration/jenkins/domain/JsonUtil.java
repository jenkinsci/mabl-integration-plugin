package com.mabl.integration.jenkins.domain;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class JsonUtil {

    static final Gson gson =
            new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    static <T> T deserialize(String filename, Class<T> object) throws FileNotFoundException {
        return gson.fromJson(new FileReader(System.getProperty("user.dir") + "/src/test/resources/__files/" + filename), object);
    }


}
