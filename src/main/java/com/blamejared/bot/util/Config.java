package com.blamejared.bot.util;

import com.jsoniter.any.Any;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Config implements JSONUtils {
    
    private final Map<String, String> configMap = new LinkedHashMap<>();
    private final Map<String, Pair<Predicate<String>, String>> configRequirements = new LinkedHashMap<>();
    
    public Config() {
    
    }
    
    public String get(String key) {
        
        if(!configMap.containsKey(key)) {
            throw new RuntimeException("Unknown config: \"%s\"".formatted(key));
        }
        return configMap.get(key);
    }
    
    public void require(String name) {
        
        configMap.put(name, "");
    }
    
    public void require(String name, Pair<Predicate<String>, String> requirements) {
        
        configMap.put(name, "");
        configRequirements.put(name, requirements);
    }
    
    public void require(String name, String defaultValue) {
        
        configMap.put(name, defaultValue);
    }
    
    public void require(String name, String defaultValue, Pair<Predicate<String>, String> requirements) {
        
        configMap.put(name, defaultValue);
        configRequirements.put(name, requirements);
    }
    
    public void generate(String configName) {
        
        try {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(configName))) {
                writer.write(serializePretty(configMap));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public void load(String configName) {
        
        System.out.println(new File(configName).getAbsolutePath());
        try {
            if(!new File(configName).exists()) {
                System.out.println("No config file exists! Generating one now...");
                generate(configName);
                
                System.exit(0);
                return;
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configName));
            Any deserialize = deserialize(bufferedReader.lines().collect(Collectors.joining("\n")));
            for(String key : deserialize.asMap().keySet()) {
                configMap.put(key, deserialize.toString(key));
            }
            
            List<Map.Entry<String, String>> collect = configMap.entrySet()
                    .stream()
                    .filter(entries -> entries.getValue() == null || entries.getValue().isEmpty())
                    .collect(Collectors.toList());
            if(!collect.isEmpty()) {
                collect.stream().map(Map.Entry::getKey).forEach(s -> {
                    System.out.println("No config value provided for: \"%s\"".formatted(s));
                });
                System.out.println("Regenerating config with new values!");
                generate(configName);
                System.exit(0);
            }
            AtomicBoolean valid = new AtomicBoolean(true);
            configRequirements.keySet().forEach(key -> {
                if(configRequirements.get(key).getKey().negate().test(configMap.get(key))) {
                    System.out.printf("Invalid Value for %s%n", key);
                    System.out.println(configRequirements.get(key).getValue());
                    valid.set(false);
                }
            });
            if(!valid.get()) {
                System.exit(0);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}

