package com.blamejared.bot.base;

import com.blamejared.bot.BardBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.Set;
import java.util.function.Consumer;

public interface IEvent<T extends GenericEvent> extends Consumer<T> {
    
    @Override
    void accept(T t);
    
    
    default JDA getClient() {
        
        return BardBot.INSTANCE.getClient();
    }
    
    Set<Class<? extends T>> getEventTypes();
    
    boolean shouldRun(T event);
}
