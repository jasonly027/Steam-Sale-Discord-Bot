package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public abstract class SlashCommand extends CommandDataImpl {
    public SlashCommand(String name, String description) {
        super(name, description);
    }

    abstract public void doSlashInteraction(SlashCommandInteractionEvent event);
}
