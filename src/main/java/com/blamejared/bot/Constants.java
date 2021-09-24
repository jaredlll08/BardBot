package com.blamejared.bot;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

public class Constants {
    
    public static final String CHANNEL_MM_MUSIC = "885290609609433108";
    public static final String CHANNEL_BLAMEJARED_MUSIC = "884880427494543382";
    
//    public static final Set<String> VALID_CHANNELS = Set.of(Constants.CHANNEL_BLAMEJARED_MUSIC, Constants.CHANNEL_MM_MUSIC);
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
}
