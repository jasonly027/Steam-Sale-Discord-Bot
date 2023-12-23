package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.jasonly027.steamsalebot.util.database.Database;

public class ClearAppsTracking extends SlashCommand implements StringSelectMenuInteraction {
    private final String selectMenuName = "select";

    private static final ClearAppsTracking command = new ClearAppsTracking();

    private ClearAppsTracking() {
        super("clear_apps", "Clear the list of apps currently being tracked.");
    }

    public static ClearAppsTracking getCommand() {
        return command;
    }

    private static MessageEmbed createWarningMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Warning")
                .setDescription("Are you sure you want to clear the list of apps currently being tracked?");
        return builder.build();
    }

    @Override
    public void doSlashInteraction(SlashCommandInteractionEvent event) {
        event.replyEmbeds(createWarningMessage())
                .addActionRow(
                        StringSelectMenu.create(selectMenuName)
                                .addOption("Yes", "yes")
                                .addOption("No", "no")
                                .build()
                ).queue();
    }

    @Override
    public String getSelectMenuName() {
        return selectMenuName;
    }

    @Override
    public void doStringSelectInteraction(StringSelectInteractionEvent event) {
        event.editComponents().queue();

        MessageEmbed retMessage;
        if (event.getValues().get(0).equals("yes")) {
            long serverId = event.getGuild().getIdLong();
            Database.removeAppIdsFromAServer(serverId,
                    Database.getAppsTrackedByAServer(serverId).stream().map(appPojo -> appPojo.appId).toList());
            retMessage = createYesMessage();
        } else {
            retMessage = createNoMessage();
        }

        event.getHook().editOriginalEmbeds(retMessage).queue();
    }

    private MessageEmbed createYesMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Clear Apps Tracking")
                .setDescription("List has been cleared.");
        return builder.build();
    }

    private MessageEmbed createNoMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Clear Apps Tracking")
                .setDescription("Command has been cancelled.");
        return builder.build();
    }
}
