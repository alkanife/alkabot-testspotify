package fr.alkanife.alkabot.events;

import fr.alkanife.alkabot.Alkabot;
import fr.alkanife.alkabot.Colors;
import fr.alkanife.alkabot.music.Music;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Events extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent readyEvent) {
        Alkabot.getLogger().info("Checking for Discord environment");

        try {
            // Check for guild
            Guild guild = readyEvent.getJDA().getGuildById(Alkabot.getConfig().getGuild_id());
            if (guild == null) {
                Alkabot.getLogger().error("The Discord guild '" + Alkabot.getConfig().getGuild_id() + "' was not found");
                readyEvent.getJDA().shutdownNow();
                System.exit(0);
            }
            Alkabot.setGuild(guild);

            Alkabot.getLogger().info("Updating commands");
            updateCommands();

            Alkabot.getLogger().info("Initializing music");
            Music.initialize();

            Alkabot.getLogger().info("Ready!");
        } catch (Exception exception) {
            exception.printStackTrace();
            readyEvent.getJDA().shutdownNow();
            System.exit(0);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashCommandInteractionEvent) {
        Alkabot.getCommandHandler().handleSlash(slashCommandInteractionEvent);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent messageReceivedEvent) {
        // If in DM, handle admin commands
        if (!messageReceivedEvent.getChannelType().equals(ChannelType.PRIVATE))
            return;

        // Deny if not administrator
        if (!Alkabot.getConfig().getAdministrators_id().contains(messageReceivedEvent.getAuthor().getId()))
            return;

        Alkabot.getCommandHandler().handleAdmin(messageReceivedEvent);
    }

    private void updateCommands() {
        SlashCommandData jukebox = Commands.slash("spotify", Alkabot.t("jukebox-command-description"));

        SubcommandData jukeboxPlay = new SubcommandData("play", Alkabot.t("jukebox-command-play-description"))
                .addOption(OptionType.STRING, "input", Alkabot.t("jukebox-command-play-input-description"), true);

        SubcommandData jukeboxPlaynext = new SubcommandData("play_next", Alkabot.t("jukebox-command-play-priority-description"))
                .addOption(OptionType.STRING, "input", Alkabot.t("jukebox-command-play-input-description"), true);

        SubcommandData jukeboxSkip = new SubcommandData("skip", Alkabot.t("jukebox-command-skip-description"))
                .addOption(OptionType.INTEGER, "input", Alkabot.t("jukebox-command-skip-input-description"), false);

        SubcommandData jukeboxRemove = new SubcommandData("remove", Alkabot.t("jukebox-command-remove-description"))
                .addOption(OptionType.INTEGER, "input", Alkabot.t("jukebox-command-remove-input-description"), false);

        SubcommandData jukeboxQueue = new SubcommandData("queue", Alkabot.t("jukebox-command-queue-description"))
                .addOption(OptionType.INTEGER, "input", Alkabot.t("jukebox-command-queue-input-description"), false);

        SubcommandData jukeboxShuffle = new SubcommandData("shuffle", Alkabot.t("jukebox-command-shuffle-description"));
        SubcommandData jukeboxStop = new SubcommandData("stop", Alkabot.t("jukebox-command-stop-description"));
        SubcommandData jukeboxClear = new SubcommandData("clear", Alkabot.t("jukebox-command-clear-description"));
        jukebox.addSubcommands(jukeboxPlay, jukeboxPlaynext, jukeboxSkip, jukeboxShuffle, jukeboxQueue, jukeboxStop, jukeboxClear, jukeboxRemove);

        Collection<CommandData> commandDataCollection = new ArrayList<>();

        commandDataCollection.add(jukebox);

        Alkabot.getGuild().updateCommands().addCommands(commandDataCollection).queue();
    }

}
