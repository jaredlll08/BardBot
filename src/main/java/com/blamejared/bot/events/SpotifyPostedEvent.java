package com.blamejared.bot.events;

import com.blamejared.bot.BardBot;
import com.blamejared.bot.base.IEvent;
import com.blamejared.bot.base.TrackInfo;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Track;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SpotifyPostedEvent implements IEvent<MessageReceivedEvent> {
    
    @Override
    public void accept(MessageReceivedEvent event) {
        
        String contentDisplay = event.getMessage().getContentDisplay();
        if(contentDisplay.contains("https://open.spotify.com/")) {
            String trackId = contentDisplay.split("track/")[1].split("\\b")[0];
            BardBot.INSTANCE.getSpotify().ifPresentOrElse(spotify -> {
                try {
                    Track track = spotify.getTrack(trackId).build().execute();
                    TrackInfo trackInfo = new TrackInfo(track.getName(), track.getArtists());
                    trackInfo.setUrlSpotify(track.getExternalUrls().getExternalUrls().get("spotify"));
                    
                    Optional<YouTube> youTubeApi = BardBot.INSTANCE.getYouTube();
                    youTubeApi.ifPresentOrElse(youTube -> {
                        try {
                            YouTube.Search.List request = youTube.search().list(Collections.singletonList("snippet"));
                            SearchListResponse response = request.setKey(BardBot.INSTANCE.getGoogleToken())
                                    .setMaxResults(1L)
                                    .setQ("%s %s music".formatted(track.getName(), track.getArtists()[0].getName()))
                                    .execute();
                            if(!response.getItems().isEmpty()) {
                                SearchResult video = response.getItems()
                                        .get(0);
                                trackInfo.setUrlYoutube("https://www.youtube.com/watch?v=%s".formatted(video.getId()
                                        .getVideoId()));
                                trackInfo.setThumbnail(video.getSnippet().getThumbnails().getHigh().getUrl());
                            }
                        } catch(IOException e) {
                            e.printStackTrace();
                            event.getChannel().sendMessage("Error while search YouTube!").queue();
                        }
                    }, () -> event.getChannel().sendMessage("Error retrieving Youtube API!").queue());
                    
                    event.getChannel().sendMessageEmbeds(trackInfo.embed(event).build()).queue();
                    return;
                } catch(IOException | SpotifyWebApiException | ParseException e) {
                    e.printStackTrace();
                }
                event.getChannel().sendMessage("Error while getting the track!").queue();
            }, () -> event.getChannel().sendMessage("Error retrieving Spotify API!").queue());
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
    
        return event.getChannel().getName().equals("music");
//        return Constants.VALID_CHANNELS.contains(event.getChannel().getId());
    }
    
}
