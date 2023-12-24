package net.jasonly027.steamsalebot;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.jasonly027.steamsalebot.commands.CommandManager;
import net.jasonly027.steamsalebot.events.OnDailyCheck;
import net.jasonly027.steamsalebot.events.OnGuildJoinLeave;
import net.jasonly027.steamsalebot.events.TimeUntilReset;

public class SteamSaleBot {
    private final ShardManager shardManager;

    private static final SteamSaleBot bot = new SteamSaleBot();

    private SteamSaleBot() {
        final String DISCORD_KEY = App.config.get("DISCORD_KEY");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(DISCORD_KEY)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(
                        new CommandManager(),
                        new OnGuildJoinLeave(),
                        new OnDailyCheck(),
                        new TimeUntilReset()
                );

        shardManager = builder.build();
    }

    // Call this in App.java to run the bot
    public static ShardManager getInstance() {
        return bot.shardManager;
    }
}
