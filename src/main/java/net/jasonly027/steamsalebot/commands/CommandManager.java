package net.jasonly027.steamsalebot.commands;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jasonly027.steamsalebot.commands.slash.*;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends ListenerAdapter {
    // Add commands here
    private static final SlashCommand[] commands = {
            new SetThreshold(),
            new Bind(),
            new AddApps(),
            new RemoveApps(),
            new Help(),
            new TestDailyCheck(),
            ListApps.getCommand()
    };

    // On bot startup, register commands to all joined guilds
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(commands).queue();
    }

    // On guild join, register commands. This is necessary for guilds that
    // add the bot AFTER bot startup
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getGuild().updateCommands().addCommands(commands).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Find the matching command and call its action
        String commandName = event.getName();
        for (SlashCommand command : commands) {
            if (commandName.equals(command.getName())) {
                command.doInteraction(event);
                break;
            }
        }
    }
}
