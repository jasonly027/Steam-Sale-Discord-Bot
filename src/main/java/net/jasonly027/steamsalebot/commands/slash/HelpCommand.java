package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.jasonly027.steamsalebot.util.database.Database;

public class HelpCommand extends SlashCommand {
    private final String HELP = "help";

    public HelpCommand(){
        super("help", "Returns list of bot commands and their desciprtions.");
    }

    @Override
    public void doInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        MessageEmbed retMessage = createHelpEmbed();
        event.getHook().sendMessageEmbeds(retMessage).setEphemeral(true).queue();
    }

    public static MessageEmbed createHelpEmbed(){
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Bot Commands")
                .setDescription("List of possible commands and their corresponding functions.")
                .addField("bind", "Binds the bot to a specific channel.", false)
                .addField("set_threshold", "Set the minimum discount percentage warranting an alert of sale price.", false);
        return builder.build();
    }
}
