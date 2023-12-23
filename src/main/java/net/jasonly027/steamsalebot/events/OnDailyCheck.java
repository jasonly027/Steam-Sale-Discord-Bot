package net.jasonly027.steamsalebot.events;

import com.mongodb.client.MongoCursor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jasonly027.steamsalebot.SteamSaleBot;
import net.jasonly027.steamsalebot.steam.AppInfo;
import net.jasonly027.steamsalebot.steam.SteamApi;
import net.jasonly027.steamsalebot.util.database.Database;
import net.jasonly027.steamsalebot.util.database.pojos.AppPojo;
import net.jasonly027.steamsalebot.util.database.pojos.DiscordPojo;
import net.jasonly027.steamsalebot.util.database.pojos.JunctionPojo;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OnDailyCheck extends ListenerAdapter {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        // Perform a check every 24 hours
        final int CHECK_INTERVAL_IN_MINUTES = 1440;

        scheduler.scheduleAtFixedRate(OnDailyCheck::startDailyCheck,
                getInitialDelay(), CHECK_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);
    }

    // Get the time in minutes until the next 10:05 A.M.
    public static long getInitialDelay() {
        final int HOUR_OF_THE_DAY_TO_CHECK = 10;
        final int MINUTE_OF_THE_HOUR_TO_CHECK = 5;

        LocalTime nowTime = LocalTime.now();
        LocalTime thenTime = LocalTime.of(HOUR_OF_THE_DAY_TO_CHECK, MINUTE_OF_THE_HOUR_TO_CHECK, 0);
        LocalDateTime nextThenDay;
        if (nowTime.isBefore(thenTime)) {
            nextThenDay = LocalDateTime.of(LocalDate.now(), thenTime);
        } else {
            nextThenDay = LocalDateTime.of(LocalDate.now().plusDays(1), thenTime);
        }
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), nextThenDay);
    }

    public static void startDailyCheck() {
        doDailyCheckChunk(Database.getAllAppsCursor());
    }

    /*
     * Since there's an API call limit of 200 per 5 minutes, we segment our check into chunks.
     * The amount of time we wait between calls is specified below.
     * The number of calls in each chunk is specified below.
     */
    public static void doDailyCheckChunk(MongoCursor<AppPojo> appCursor) {
        final int REST_INTERVAL_IN_MINUTES = 5;
        final int CALLS_PER_INTERVAL = 150;

        int calls = 0;
        // Use this cursor until it has been exhausted or the max number of calls
        // specified for a chunk has been reached.
        while (calls < CALLS_PER_INTERVAL && appCursor.hasNext()) {
            AppPojo appPojo = appCursor.next();
            AppInfo appInfo = null;
            // API Call - Skip if call fails
            try {
                appInfo = SteamApi.getAppInfo(appPojo.appId);
            } catch (IOException | InterruptedException ignored) {}
            if (appInfo == null || !appInfo.isSuccess()) {
                ++calls;
                continue;
            }

            boolean isOnSale = appInfo.isFree() || (appInfo.getDiscountPercent() > 0);
            // Iterate through servers watching this app
            // Update their trailingSaleDay field, and send alerts if necessary
            try (MongoCursor<JunctionPojo> junctionCursor = Database.getAllJunctionsWithAppIdOf(appPojo.appId)) {
                while (junctionCursor.hasNext()) {
                    JunctionPojo junctionPojo = junctionCursor.next();

                    // Update trailingSaleDay fields in DB, then
                    // skip if there's no alert to be sent
                    boolean shouldAlert =
                            Database.shouldSendAlertAndUpdateTrailingSaleDay(
                                    junctionPojo.serverId, appPojo.appId,
                                    isOnSale, appInfo.getDiscountPercent());
                    if (!shouldAlert) {
                        continue;
                    }

                    // Get server info entry, skip if read fails
                    DiscordPojo discordPojo = Database.getDiscordPojo(junctionPojo.serverId);
                    if (discordPojo == null) {
                        continue;
                    }

                    // Get server, remove it from DB if it
                    // doesn't exist anymore and skip
                    Guild server = SteamSaleBot.getInstance().getGuildById(discordPojo.serverId);
                    if (server == null) {
                        Database.removeServerFromDiscordCollection(discordPojo.serverId);
                        continue;
                    }

                    // Get channel, update DB with default if the
                    // original doesn't exist anymore.
                    // Skip if default fails
                    TextChannel textChannel = server.getTextChannelById(discordPojo.channelId);
                    if (textChannel == null) {
                        DefaultGuildChannelUnion channel = server.getDefaultChannel();
                        if (channel == null || channel.getType() != ChannelType.TEXT) {
                            continue;
                        }
                        textChannel = channel.asTextChannel();
                        Database.updateChannelIdOfAServer(discordPojo.serverId, textChannel.getIdLong());
                    }

                    // Send alert to this server's text channel
                    textChannel.sendMessageEmbeds(createSaleMessage(appInfo)).queue();
                }
            }
            ++calls;
        }
        // If the cursor was not exhausted, schedule another chunk
        if (appCursor.hasNext()) {
            scheduler.schedule(() -> doDailyCheckChunk(appCursor), REST_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);
        } else {
            appCursor.close();
        }
    }

    public static MessageEmbed createSaleMessage(AppInfo app) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(app.getName() + " is on sale for " + app.getDiscountPercent() + "% off!",
                        app.getStorePageUrl())
                .setColor(getColorBySalePercentage(app.getDiscountPercent()))
                .setImage(app.getBannerUrl())
                .addField("Original Price", app.getOriginalPrice(), true)
                .addField("Sale Price", app.getSalePrice(), true)
                .addField("Reviews", String.valueOf(app.getRecommendationsCount()), true);
        return builder.build();
    }

    public static Color getColorBySalePercentage(int discountPercentage) {
        if ((discountPercentage >= 1) && (discountPercentage <= 5)) {
            return new Color(11, 255, 51);
        }
        else if ((discountPercentage >= 6) && (discountPercentage <= 10)) {
            return new Color(68, 253, 210);
        }
        else if ((discountPercentage >= 11) && (discountPercentage <= 15)) {
            return new Color(68, 253, 253);
        }
        else if ((discountPercentage >= 16) && (discountPercentage <= 20)) {
            return new Color(68, 219, 253);
        }
        else if ((discountPercentage >= 21) && (discountPercentage <= 25)) {
            return new Color(68, 139, 253);
        }
        else if((discountPercentage >= 26) && (discountPercentage <= 30)) {
            return new Color(68, 139, 253);
        }
        else if ((discountPercentage >= 31) && (discountPercentage <= 35)) {
            return new Color(68, 90, 253);
        }
        else if ((discountPercentage >= 36) && (discountPercentage <= 40)) {
            return new Color(133, 68, 253);
        }
        else if((discountPercentage >= 41) && (discountPercentage <= 45)) {
            return new Color(176, 68, 253);
        }
        else if((discountPercentage >= 46) && (discountPercentage <= 50)) {
            return new Color(225, 68, 253);
        }
        else if((discountPercentage >= 51) && (discountPercentage <= 55)) {
            return new Color(253, 68, 222);
        }
        else if((discountPercentage >= 56) && (discountPercentage <=60)){
            return new Color(255, 35, 167);
        }
        else if((discountPercentage >= 61) && (discountPercentage < 100)) {
            return new Color(255, 0, 0);
        }
        else {
            return new Color(255, 255, 255);
        }
    }
}
