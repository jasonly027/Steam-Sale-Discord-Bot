package net.jasonly027.steamsalebot.commands.setdiscountthreshold;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.jasonly027.steamsalebot.commands.slash.SlashCommand;
import net.jasonly027.steamsalebot.util.database.Database;

public class SetThresholdCommand extends SlashCommand {

    private AuditLogEntry event;
    private final String SERVERID = event.getGuild().getId();
    private final long SERVERIDSTRING = Long.parseLong(SERVERID);
    public SetThresholdCommand(){
        super("setDiscountThreshold", "Set the discount cutoff for notification of sale price.");

        // Command: /setThreshold <threshold>
        OptionData setThreshold = new OptionData(OptionType.INTEGER, "threshold", "The discount cutoff for sale notification.", true);
        addOptions(setThreshold);
    }

    @Override
    public void doInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // get the threshold from the msg the user sent
       OptionMapping thresholdPassed = event.getOption("threshold");
        assert thresholdPassed != null;
        int threshold = thresholdPassed.getAsInt();

        // int rom 1-100 check
        if((threshold <= 0) || (threshold > 100)) {
            event.getHook().sendMessageEmbeds(notInRangeEmbed()).queue();
            // if threshold successfully added to DB
        } else if(Database.updateSaleThresholdOfAServer(SERVERIDSTRING, threshold)) {
            event.getHook().sendMessageEmbeds(setThresholdSuccess()).queue();
            // if threshold couldn't be added to DB
        } else{
            event.getHook().sendMessageEmbeds(setThresholdFailed()).queue();
        }
    }

    public static MessageEmbed notInRangeEmbed(){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Error").setDescription("Discount Threshold must be between 0 and 100.");
        return builder.build();
    }
    /*This embed returns when the DB can't add the threshold, you can specify reason.*/
    public static MessageEmbed setThresholdFailed(){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Error").setDescription("Couldn't set the discount threshold.");
        return builder.build();
    }
    public static MessageEmbed setThresholdSuccess(){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Success").setDescription("Successfully set the discount threshold.");
        return builder.build();
    }
}

