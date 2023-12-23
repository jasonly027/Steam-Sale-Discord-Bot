package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.jasonly027.steamsalebot.util.database.pojos.AppPojo;
import java.util.List;
import static net.jasonly027.steamsalebot.util.database.Database.getAppsTrackedByAServer;

public class ListApps extends SlashCommand {

    public ListApps(){
        super("list_apps", "List all the Steam apps you are keeping track of.");
    }

    @Override
    public void doInteraction(SlashCommandInteractionEvent event) {
        long serverId = event.getGuild().getIdLong();
        event.replyEmbeds(createMessage(serverId)).queue();
    }

    public static MessageEmbed createMessage(long serverId){
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("List of Apps")
                .setDescription("List of apps you are keeping track of.");

        // Loop to addField for each game being tracked
        List<AppPojo> listOfApps = getAppsTrackedByAServer(serverId);
        if(listOfApps.isEmpty()) {
            builder.addField("No Apps Tracked", "No apps are currently being tracked.", true);
        } else {
            for (AppPojo app : listOfApps) {
                builder.addField(app.appName, "App Id: " + app.appId, true);
            }
        }
        return builder.build();
    }

}
