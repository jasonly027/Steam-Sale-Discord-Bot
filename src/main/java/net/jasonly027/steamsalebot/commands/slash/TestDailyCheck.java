package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.jasonly027.steamsalebot.events.OnDailyCheck;

public class TestDailyCheck extends SlashCommand {
    private static final TestDailyCheck command = new TestDailyCheck();

    private TestDailyCheck() {
        super("test_daily_check_test", "Test Daily Check");
    }

    public static TestDailyCheck getCommand() {
        return command;
    }

    @Override
    public void doInteraction(SlashCommandInteractionEvent event) {
        event.reply("Testing").queue();
        OnDailyCheck.startDailyCheck();
    }
}
