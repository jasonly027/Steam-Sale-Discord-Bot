package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.jasonly027.steamsalebot.steam.SearchResult;
import net.jasonly027.steamsalebot.steam.SteamApi;
import net.jasonly027.steamsalebot.util.database.Database;

public class Search extends SlashCommand implements StringSelectMenuInteraction {
    private static final String QUERY = "query";
    private static final String CANCEL_OPTION = "589034812_CANCEL_20913198";

    private final String selectMenuName = "app";

    private static final Search command = new Search();

    private Search() {
        super("search", "Search for an app and add it to the tracking list.");

        OptionData query = new OptionData(OptionType.STRING,
                QUERY, "Search query.", true)
                .setMaxLength(100);
        addOptions(query);
    }

    public static Search getCommand() {
        return command;
    }

    private static MessageEmbed createSearchErrorMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("App Search Results")
                .setDescription("Failed to search. Please try again.");
        return builder.build();
    }

    private static MessageEmbed createCancelMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Cancelled")
                .setDescription("Cancelled adding app.");
        return builder.build();
    }

    private static MessageEmbed createSearchResultsMessage(int count) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("App Search Results")
                .setDescription(count + " results found. Select an app or cancel below.")
                .setFooter("Note: Free apps are included in the search which "
                        + "WILL result in an error if selected.");
        return builder.build();
    }

    private static MessageEmbed createFailureMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Error")
                .setDescription("Failed to add. Check that a free app isn't trying to be added and try again.");
        return builder.build();
    }

    private static MessageEmbed createSuccessMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Success")
                .setDescription("App successfully added.");
        return builder.build();
    }

    @Override
    public void doSlashInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String query = event.getOption(QUERY).getAsString();
        // API Call - Get results matching query
        SearchResult[] results = SteamApi.getSearchResults(query);
        if (results == null) {
            event.getHook().sendMessageEmbeds(createSearchErrorMessage()).queue();
            return;
        }

        // Add results to dropdown + cancel option
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(selectMenuName);
        for (SearchResult result : results) {
            menuBuilder.addOption(result.toString(), String.valueOf(result.getAppId()));
        }
        menuBuilder.addOption("-- Cancel Adding App --", CANCEL_OPTION);

        event.getHook().sendMessageEmbeds(createSearchResultsMessage(results.length))
                .addActionRow(menuBuilder.build()).queue();
    }

    @Override
    public String getSelectMenuName() {
        return selectMenuName;
    }

    @Override
    public void doStringSelectInteraction(StringSelectInteractionEvent event) {
        event.editComponents().queue();

        String selected = event.getValues().get(0);
        // If cancel option was selected
        if (selected.equals(CANCEL_OPTION)) {
            event.getHook().editOriginalEmbeds(createCancelMessage()).queue();
            return;
        }

        // API Call - Check app isn't free and get its name
        long appId = Long.parseLong(selected);
        String appName = SteamApi.getAppName(appId);
        if (appName == null) {
            event.getHook().editOriginalEmbeds(createFailureMessage()).queue();
            return;
        }

        MessageEmbed retMessage;
        // Add app to DB
        long serverId = event.getGuild().getIdLong();
        if (Database.addAppIdToAServer(serverId, appId, appName)) {
            retMessage = createSuccessMessage();
        } else {
            retMessage = createFailureMessage();
        }

        event.getHook().editOriginalEmbeds(retMessage).queue();
    }
}
