package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.jasonly027.steamsalebot.util.database.Database;

public class Bind extends SlashCommand {
    private static final String CHANNEL = "channel";

    public Bind() {
        super("bind", "Set the channel to where alerts are sent.");

        OptionData channel = new OptionData(OptionType.CHANNEL,
                CHANNEL, "The channel to where alerts are sent", true)
                .setChannelTypes(ChannelType.TEXT);
        addOptions(channel);
    }

    public static MessageEmbed createSuccessMessage(String channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Successfully Updated Channel ID")
                .setDescription("Sale alerts will now be sent to "
                        + channel);
        return builder.build();
    }

    public static MessageEmbed createFailureMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Failed to Update Channel ID")
                .setDescription("Update failed. Please try again.");
        return builder.build();
    }

    @Override
    public void doSlashInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        long channelId = event.getOption(CHANNEL).getAsLong();
        MessageEmbed retMessage;
        if (Database.updateChannelIdOfAServer(event.getGuild().getIdLong(), channelId)) {
             retMessage = createSuccessMessage(event.getChannel().getAsMention());
        } else {
            retMessage = createFailureMessage();
        }

        event.getHook().sendMessageEmbeds(retMessage).queue();
    }
}
