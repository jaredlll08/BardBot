package com.blamejared.bot;


import com.blamejared.bot.base.IEvent;
import com.blamejared.bot.util.Config;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.youtube.YouTube;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BardBot extends ListenerAdapter {
    
    public static final BardBot INSTANCE = new BardBot();
    private final Config config = new Config();
    
    private final List<IEvent> events = new ArrayList<>();
    
    private JDA client;
    
    private static SpotifyApi spotify;
    private static long spotifyExpires;
    
    public static void main(String... args) throws LoginException {
        
        INSTANCE.init();
    }
    
    
    public void init() throws LoginException {
        
        System.out.println("Starting");
        initConfig();
        
        collect();
        client = JDABuilder.create(config.get("discordToken"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .addEventListeners(INSTANCE)
                .build();
        
        System.out.println("Started");
    }
    
    private void initConfig() {
        
        config.require("discordToken");
        config.require("spotifyToken");
        config.require("spotifyClientID");
        config.require("googleToken");
        config.load("data/config.json");
    }
    
    private void collect() {
        
        Reflections reflections = new Reflections("com.blamejared.bot");
        Set<Class<? extends IEvent>> eventClasses = reflections.getSubTypesOf(IEvent.class);
        for(Class<? extends IEvent> eventClass : eventClasses) {
            try {
                getEvents().add(eventClass.getConstructor().newInstance());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        
        super.onGenericEvent(event);
        getEvents().stream()
                .filter(iEvent -> iEvent.getEventTypes().contains(event.getClass()))
                .filter(iEvent -> iEvent.shouldRun(event))
                .forEach(iEvent -> iEvent.accept(event));
    }
    
    public Config getConfig() {
        
        return config;
    }
    
    public JDA getClient() {
        
        return client;
    }
    
    public List<IEvent> getEvents() {
        
        return events;
    }
    
    public Optional<SpotifyApi> getSpotify() {
        
        try {
            if(spotify == null) {
                spotify = new SpotifyApi.Builder()
                        .setClientId(config.get("spotifyClientID"))
                        .setClientSecret(config.get("spotifyToken"))
                        .build();
            }
            // refresh the token every 45 minutes
            if(spotifyExpires - 900_000 <= System.currentTimeMillis()) {
                ClientCredentialsRequest clientCredentialsRequest = spotify.clientCredentials().build();
                final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyExpires = System.currentTimeMillis() + (clientCredentials.getExpiresIn() * 1000);
                spotify.setAccessToken(clientCredentials.getAccessToken());
                System.out.println("Refreshed spotify api");
            }
            return Optional.of(spotify);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
        
    }
    
    public Optional<YouTube> getYouTube() {
        
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            YouTube youtube = new YouTube.Builder(httpTransport, Constants.JSON_FACTORY, null)
                    .setApplicationName("Bard Bot")
                    .build();
            return Optional.of(youtube);
        } catch(GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    public String getGoogleToken() {
        
        return config.get("googleToken");
    }
    
}