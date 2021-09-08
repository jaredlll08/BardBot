package com.blamejared.bot.events;

import com.blamejared.bot.BardBot;
import com.blamejared.bot.Constants;
import com.blamejared.bot.base.IEvent;
import com.blamejared.bot.base.TrackInfo;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class YouTubePostedEvent implements IEvent<MessageReceivedEvent> {
    
    @Override
    public void accept(MessageReceivedEvent event) {
        
        String contentDisplay = event.getMessage().getContentDisplay();
        if(contentDisplay.contains("https://www.youtube.com/watch?v=")) {
            String videoId = contentDisplay.split("v=")[1].split("[^a-zA-Z0-9\\-_]")[0];
            
            BardBot.INSTANCE.getYouTube().ifPresentOrElse(youTube -> {
                try {
                    YouTube.Videos.List request = youTube.videos().list(Collections.singletonList("snippet"));
                    VideoListResponse response = request.setId(Collections.singletonList(videoId))
                            .setKey(BardBot.INSTANCE.getGoogleToken())
                            .setMaxResults(1L)
                            .execute();
                    if(response.getItems().isEmpty()) {
                        event.getChannel().sendMessage("No videos found from that URL!").queue();
                    } else {
                        Video video = response.getItems().get(0);
                        TrackInfo trackInfo = new TrackInfo(video.getSnippet()
                                .getTitle(), Collections.singleton(video.getSnippet()
                                .getChannelTitle()
                                .replace(" - Topic", "")));
                        
                        trackInfo.setThumbnail(video.getSnippet().getThumbnails().getHigh().getUrl());
                        trackInfo.setUrlYoutube("https://www.youtube.com/watch?v=%s".formatted(video.getId()));
                        
                        BardBot.INSTANCE.getSpotify().ifPresentOrElse(spotify -> {
                            try {
                                Paging<Track> tracks = spotify.searchTracks(trackInfo.getSpotifySearch())
                                        .limit(1)
                                        .build()
                                        .execute();
                                
                                if(tracks.getItems().length > 0) {
                                    trackInfo.setUrlSpotify(tracks.getItems()[0].getExternalUrls()
                                            .getExternalUrls()
                                            .get("spotify"));
                                }
                                
                            } catch(IOException | SpotifyWebApiException | ParseException e) {
                                e.printStackTrace();
                            }
                        }, () -> event.getChannel().sendMessage("Error retrieving Spotify API!").queue());
                        
                        event.getChannel().sendMessageEmbeds(trackInfo.embed(event).build()).queue();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
                
            }, () -> event.getChannel().sendMessage("Error retrieving Youtube API!").queue());
            
        }
        
    }
    
    @Override
    public Set<Class<? extends MessageReceivedEvent>> getEventTypes() {
        
        Set<Class<? extends MessageReceivedEvent>> set = new HashSet<>();
        set.add(MessageReceivedEvent.class);
        return set;
    }
    
    @Override
    public boolean shouldRun(MessageReceivedEvent event) {
        
        return Constants.VALID_CHANNELS.contains(event.getChannel().getId());
    }
    
}
