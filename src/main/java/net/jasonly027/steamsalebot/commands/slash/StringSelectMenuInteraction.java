package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public interface StringSelectMenuInteraction {
    String getSelectMenuName();
    void doStringSelectInteraction(StringSelectInteractionEvent event);
}
