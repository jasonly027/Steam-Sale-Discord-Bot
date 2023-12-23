package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.jasonly027.steamsalebot.util.database.Database;

public class SetThreshold extends SlashCommand {
    private static final String THRESHOLD = "threshold";

    public SetThreshold(){
        super("set_discount_threshold",
                "Set the minimum discount percentage warranting an alert of sale price.");

        // Command: /setThreshold <threshold>, threshold must be in the range [1,100]
        OptionData setThreshold = new OptionData(OptionType.INTEGER,
                THRESHOLD, "The discount cutoff for sale notification.", true)
                .setMinValue(1)
                .setMaxValue(100);
        addOptions(setThreshold);
    }

    @Override
    public void doSlashInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // get the threshold from the msg the user sent
        OptionMapping thresholdPassed = event.getOption(THRESHOLD);
        assert thresholdPassed != null;
        int threshold = thresholdPassed.getAsInt();

        MessageEmbed retMessage;
        if (Database.updateSaleThresholdOfAServer(event.getGuild().getIdLong(), threshold)) {
            retMessage = createSuccessMessage();
        } else {
            retMessage = createFailureMessage();
        }

        event.getHook().sendMessageEmbeds(retMessage).queue();
    }

    public static MessageEmbed createFailureMessage(){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Error").setDescription("Failed to set the discount threshold. Please try again.");
        return builder.build();
    }

    public static MessageEmbed createSuccessMessage(){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Success").setDescription("Successfully set the discount threshold.");
        return builder.build();
    }
}

