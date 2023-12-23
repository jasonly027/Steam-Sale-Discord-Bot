package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.jasonly027.steamsalebot.util.database.Database;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveApps extends SlashCommand {
    private final String APP_IDS = "app_ids";

    public RemoveApps() {
        super("remove_apps", "Remove apps by their app ID from the tracker.");

        OptionData appId = new OptionData(OptionType.STRING, APP_IDS,
                "The app IDs of apps to be removed, separated by commas. E.g., 400,440,1868140", true)
                .setMaxLength(150);
        addOptions(appId);
    }

    public static MessageEmbed createSuccessMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Successfully removed app IDs")
                .setDescription("Specified apps have been removed if they were being tracked.");
        return builder.build();
    }

    public static MessageEmbed createFailureMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Failed to remove app IDs")
                .setDescription("Removal unexpectedly failed. Please try again.");
        return builder.build();
    }

    // Create message for when any one of the inputted app IDs contains a non-integer
    public static MessageEmbed createInvalidAppIdsMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Failed to remove app IDs")
                .setDescription("One or more of the app IDs contains non-integers. "
                        + "Please check your input.");
        return builder.build();
    }

    @Override
    public void doSlashInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String appIds = event.getOption(APP_IDS).getAsString();
        // Try to convert comma-separated app IDs to longs
        // Abort and send an error message if any one of the conversions failed
        List<Long> appIdsAsLongs;
        try {
            appIdsAsLongs = Arrays.stream(appIds.split(","))
                    .map(Long::valueOf).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            event.getHook().sendMessageEmbeds(createInvalidAppIdsMessage()).queue();
            return;
        }

        MessageEmbed retMessage;
        if (Database.removeAppIdsFromAServer(event.getGuild().getIdLong(), appIdsAsLongs)) {
            retMessage = createSuccessMessage();
        }
        else {
            retMessage = createFailureMessage();
        }

        event.getHook().sendMessageEmbeds(retMessage).queue();
    }
}
