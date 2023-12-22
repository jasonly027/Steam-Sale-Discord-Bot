package net.jasonly027.steamsalebot.events;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jasonly027.steamsalebot.util.database.Database;
import org.jetbrains.annotations.NotNull;

public class OnGuildJoinLeave extends ListenerAdapter {
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Database.addServerToDiscordCollection(event.getGuild().getIdLong(),
                event.getGuild().getDefaultChannel().getIdLong());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        Database.removeServerFromDiscordCollection(event.getGuild().getIdLong());
    }

    @Override
    public void onUnavailableGuildLeave(UnavailableGuildLeaveEvent event) {
        Database.removeServerFromDiscordCollection(event.getGuildIdLong());
    }
}
