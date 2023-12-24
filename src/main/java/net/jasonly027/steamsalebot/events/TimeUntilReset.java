package net.jasonly027.steamsalebot.events;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeUntilReset extends ListenerAdapter {

    /**/
    @Override
    public void onReady(@NotNull ReadyEvent event){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {setStatus(minuteConversionToHours(MINUTE_PLACEHOLDER, event));}, 0, 1, TimeUnit.MINUTES);
    }

    public void setStatus(String timeLeft, ReadyEvent event){
        event.getJDA().getPresence().setActivity(Activity.customStatus(timeLeft + " until reset."));
    }

    /*This method takes in an int minutes and converts it into hours and the remaining minutes and returns it as a string.*/
    public static String minuteConversionToHours(int minutes){
        if (minutes < 0){
            return "Invalid Time Input";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        return hours + " hours and " + remainingMinutes + " minutes";
    }

}
