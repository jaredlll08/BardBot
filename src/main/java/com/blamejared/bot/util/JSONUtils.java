package com.blamejared.bot.util;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.extra.GsonCompatibilityMode;
import com.jsoniter.output.JsonStream;

public interface JSONUtils {
    
    GsonCompatibilityMode config = new GsonCompatibilityMode.Builder().build();
    GsonCompatibilityMode configPretty = new GsonCompatibilityMode.Builder().setPrettyPrinting().build();
    
    default Any deserialize(String json) {
        
        return JsonIterator.deserialize(config, json);
    }
    
    default String serialize(Object obj) {
        
        return JsonStream.serialize(config, obj);
    }
    
    default String serializePretty(Object obj) {
        
        return JsonStream.serialize(configPretty, obj);
    }
    
}
