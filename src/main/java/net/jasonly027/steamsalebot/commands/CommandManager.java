package net.jasonly027.steamsalebot.commands;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jasonly027.steamsalebot.commands.slash.*;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends ListenerAdapter {
    // Add commands here
    private static final SlashCommand[] commands = {
            SetThreshold.getCommand(),
            Bind.getCommand(),
            AddApps.getCommand(),
            RemoveApps.getCommand(),
            Help.getCommand(),
            ClearAppsTracking.getCommand(),
            ListApps.getCommand(),
            TestDailyCheck.getCommand(),
            Search.getCommand()
    };

    // If a command uses string select menu, add it here
    private static final StringSelectMenuInteraction[] stringSelectMenuCommands = {
            ClearAppsTracking.getCommand(),
            Search.getCommand()
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
                command.doSlashInteraction(event);
                break;
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentName = event.getComponentId();
        for (StringSelectMenuInteraction command : stringSelectMenuCommands) {
            if (componentName.equals(command.getSelectMenuName())) {
                command.doStringSelectInteraction(event);
                break;
            }
        }
    }
}
