package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.jasonly027.steamsalebot.steam.SteamApi;
import net.jasonly027.steamsalebot.util.database.Database;
import net.jasonly027.steamsalebot.util.database.pojos.AppPojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AddApps extends SlashCommand {
    private final String APP_IDS = "app_ids";

    private static final AddApps command = new AddApps();

    private AddApps() {
        super("add_apps", "Add apps by their app ID to the tracker.");

        OptionData appId = new OptionData(OptionType.STRING, APP_IDS,
                "The app IDs of apps to be added, separated by commas. E.g., 400,440,1868140", true)
                .setMaxLength(150);
        addOptions(appId);
    }

    public static AddApps getCommand() {
        return command;
    }

    // Create message indicating which apps were successfully/unsuccessfully added
    private static MessageEmbed createSuccessMessage(List<AppPojo> successfulApps, List<Long> badApps) {
        int totalApps = successfulApps.size() + badApps.size();

        // Create a field listing all the successfully added apps
        StringBuilder successfulAppsStringBuilder = new StringBuilder();
        successfulApps.forEach(app -> successfulAppsStringBuilder.append(app).append('\n'));
        MessageEmbed.Field successfulAppsField = new MessageEmbed.Field(
                "Successfully Added",
                successfulAppsStringBuilder.toString(),
                false);

        // Create a field listing all the unsuccessfully added apps
        StringBuilder unsuccessfulAppsStringBuilder = new StringBuilder();
        badApps.forEach(id -> unsuccessfulAppsStringBuilder.append(id).append('\n'));
        MessageEmbed.Field unsuccessfulAppsField = new MessageEmbed.Field(
                "Unsuccessfully Added (Check and try again)",
                unsuccessfulAppsStringBuilder.toString(),
                false);

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Added " + successfulApps.size() + " out of " + totalApps + " apps");
        if (!successfulAppsStringBuilder.isEmpty()) {
            builder.addField(successfulAppsField);
        }
        if (!unsuccessfulAppsStringBuilder.isEmpty()) {
            builder.addField(unsuccessfulAppsField)
                    .setFooter("Note: Free apps cannot be added");
        }
        return builder.build();
    }

    // Create message for when any one of the inputted app IDs contains a non-integer
    private static MessageEmbed createInvalidAppIdsMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Failed to add app IDs")
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
        long[] appIdsAsLongs;
        try {
            appIdsAsLongs = Arrays.stream(appIds.split(","))
                    .mapToLong(Long::parseLong).toArray();
        } catch (NumberFormatException e) {
            event.getHook().sendMessageEmbeds(createInvalidAppIdsMessage()).queue();
            return;
        }

        /*
         * Check which app IDs are valid by trying to use the ID to get the app's name
         * on steam from an API call. Apps with a retrieved name are put on the valid list.
         * IDs that couldn't retrieve a name or had connection issues are on the bad list.
         */
        final int INITIAL_CAPACITY = 10;
        List<AppPojo> validAppIds = new ArrayList<>(INITIAL_CAPACITY);
        List<Long> badAppIds = new ArrayList<>(INITIAL_CAPACITY);
        for (long appId : appIdsAsLongs) {
            String appName = null;
            try {
                appName = SteamApi.getAppName(appId);
            } catch (IOException | InterruptedException ignored) {}

            if (appName != null) {
                validAppIds.add(new AppPojo(appId, appName));
            } else {
                badAppIds.add(appId);
            }
        }

        long serverId = event.getGuild().getIdLong();
        AppPojo firstUnsuccessfulApp = new AppPojo();
        Iterator<AppPojo> validAppsIterator = validAppIds.iterator();
        List<AppPojo> successfulApps = Database.addAppIdsToAServer(serverId, validAppsIterator, firstUnsuccessfulApp);

        // Record apps that failed to be inserted.
        if (firstUnsuccessfulApp.appId != -1) {
            badAppIds.add(firstUnsuccessfulApp.appId);
        }
        validAppsIterator.forEachRemaining(badApp -> badAppIds.add(badApp.appId));

        event.getHook().sendMessageEmbeds(createSuccessMessage(successfulApps, badAppIds)).queue();
    }
}
