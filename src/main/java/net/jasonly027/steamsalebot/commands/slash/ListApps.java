package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.jasonly027.steamsalebot.util.database.pojos.AppPojo;
import java.util.List;
import static net.jasonly027.steamsalebot.util.database.Database.getAppsTrackedByAServer;

public class ListApps extends SlashCommand {
    private static final ListApps command = new ListApps();

    private ListApps(){
        super("list_apps", "List all the apps currently being tracked.");
    }

    public static ListApps getCommand() {
        return command;
    }

    @Override
    public void doSlashInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long serverId = event.getGuild().getIdLong();
        event.getHook().sendMessageEmbeds(createMessage(serverId)).queue();
    }

    private static MessageEmbed createMessage(long serverId){
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("List of Apps");
        // Loop to addField for each game being tracked
        List<AppPojo> listOfApps = getAppsTrackedByAServer(serverId);
        if (listOfApps.isEmpty()) {
            builder.addField("No Apps Tracked", "No apps are currently being tracked.", false);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            listOfApps.forEach((app) -> stringBuilder.append(app).append('\n'));
            builder.setDescription(stringBuilder.toString());
        }
        return builder.build();
    }

}
