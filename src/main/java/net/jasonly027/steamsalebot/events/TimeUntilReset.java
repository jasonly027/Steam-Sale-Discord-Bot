package net.jasonly027.steamsalebot.events;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jasonly027.steamsalebot.util.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TimeUntilReset extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event){
        ScheduledExecutorService scheduler = Scheduler.getScheduler();
        Runnable updateStatus = () -> event.getJDA().getPresence()
                .setActivity(Activity.customStatus(
                        convertTime(OnDailyCheck.getTimeTillNextCheckInMinutes()) + " until reset"));
        scheduler.scheduleAtFixedRate(updateStatus, 0, 1, TimeUnit.MINUTES);
    }

    /*This method takes in an int minutes and converts it into hours and the remaining minutes and returns it as a string.*/
    private static String convertTime(long minutes){
        if (minutes < 0){
            return "Invalid Time Input";
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        return hours + " hours and " + remainingMinutes + " minutes";
    }
}
