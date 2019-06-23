package com.mllilek.foodexpiry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionsHelper {
    public static <T> Map<String, T> castMap(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, x->(T)x.getValue()));
    }
}
