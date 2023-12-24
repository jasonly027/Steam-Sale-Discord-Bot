package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Help extends SlashCommand {
    private static final Help command = new Help();

    private Help(){
        super("help", "Show a list of commands and their descriptions.");
    }

    public static Help getCommand() {
        return command;
    }

    @Override
    public void doSlashInteraction(SlashCommandInteractionEvent event) {
        event.replyEmbeds(createMessage()).queue();
    }

    private static MessageEmbed createMessage(){
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Commands and FAQ")
                .addField("/bind <text_channel>", "Set the channel to where sale alerts are sent. "
                        + "By default, sends to the default channel.", false)
                .addField("/set_discount_threshold <percentage>",
                        "Set the minimum discount percentage warranting an alert of an app sale. "
                                + "By default, the threshold is 1%", false)
                .addField("/add_apps <appId,appId,...>",
                        "Add comma separated app IDs to the tracker.", false)
                .addField("/remove_apps <appId,appId,...>",
                        "Remove comma separated app IDs from the tracker.", false)
                .addField("/search <query>",
                        "Search for an app to add to the tracker.", false)
                .addField("/list_apps",
                        "List all the apps currently being tracked.", false)
                .addField("/clear_apps",
                        "Clear the tracking list.", false)
                .addField("How often does the bot check for sales?",
                        "The bot checks for sales every day at about **10:05 AM (PDT)**.", true)
                .addField("Why aren't alerts showing up?",
                        "Reconfigure your discount threshold in case it is too high. "
                                + "Additionally, try rebinding to a text channel.", true)
                .addField("The app is still on sale but there wasn't an alert.",
                        "Alerts for an app are only sent on the first day of a sale duration "
                                + "or, when added *during* a sale, on the following daily check.", true);
        return builder.build();
    }
}
