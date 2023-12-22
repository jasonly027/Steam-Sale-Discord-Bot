package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Help extends SlashCommand {
    public Help(){
        super("help", "Show a list of commands and their descriptions.");
    }

    @Override
    public void doInteraction(SlashCommandInteractionEvent event) {
        event.replyEmbeds(createMessage()).queue();
    }

    public static MessageEmbed createMessage(){
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Commands")
                .addField("bind", "Binds the bot to a specific channel.", false)
                .addField("set_threshold", "Set the minimum discount percentage warranting an alert of sale price.", false);
        return builder.build();
    }
}
