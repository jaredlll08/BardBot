package com.blamejared.bot.base;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

public class TrackInfo {
    
    private final String name;
    private final Set<String> artists;
    
    private String urlYoutube;
    private String urlSpotify;
    
    private String thumbnail;
    
    public TrackInfo(String name, Set<String> artists) {
        
        this.name = name;
        this.artists = artists;
    }
    
    public TrackInfo(String name, ArtistSimplified[] artists) {
        
        this.name = name;
        this.artists = Arrays.stream(artists).map(ArtistSimplified::getName).collect(Collectors.toSet());
        
    }
    
    public String getDisplayName() {
        
        return "%s - %s".formatted(name, String.join(", ", artists));
    }
    
    public String getName() {
        
        return name;
    }
    
    public Set<String> getArtists() {
        
        return artists;
    }
    
    public Optional<String> getUrlYoutube() {
        
        return Optional.ofNullable(urlYoutube);
    }
    
    public void setUrlYoutube(String urlYoutube) {
        
        this.urlYoutube = urlYoutube;
    }
    
    public Optional<String> getUrlSpotify() {
        
        return Optional.ofNullable(urlSpotify);
    }
    
    public void setUrlSpotify(String urlSpotify) {
        
        this.urlSpotify = urlSpotify;
    }
    
    
    public EmbedBuilder embed(MessageReceivedEvent event) {
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(getDisplayName());
        getThumbnail().ifPresent(builder::setImage);
        builder.addField("Spotify", getUrlSpotify()
                .map("[Play on Spotify](%s)"::formatted)
                .orElse("No track found!"), true);
        builder.addField("Youtube", getUrlYoutube()
                .map("[Play on YouTube](%s)"::formatted)
                .orElse("No video found!"), true);
        
        builder.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl());
        builder.setTimestamp(event.getMessage().getTimeCreated());
        return builder;
    }
    
    public String getSpotifySearch() {
        
        String name = getName();
        for(String artist : getArtists()) {
            name = name.replaceAll(artist, "");
        }
        return "%s %s".formatted(name, String.join(" ", getArtists()));
    }
    
    public Optional<String> getThumbnail() {
        
        return Optional.ofNullable(thumbnail);
    }
    
    public void setThumbnail(String thumbnail) {
        
        this.thumbnail = thumbnail;
    }
    
}
