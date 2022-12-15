package fr.alkanife.alkabot.commands;

import fr.alkanife.alkabot.Alkabot;
import fr.alkanife.alkabot.MemoryUtils;
import fr.alkanife.alkabot.commands.utils.Command;
import fr.alkanife.alkabot.configuration.Configuration;
import fr.alkanife.alkabot.configuration.ConfigurationLoader;
import fr.alkanife.alkabot.lang.TranslationsLoader;
import fr.alkanife.alkabot.music.Music;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.Presence;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Locale;

public class AdminCommands {

    public static void help(MessageReceivedEvent messageReceivedEvent) {
        messageReceivedEvent.getMessage().reply("Administrative commands:\n" +
                "- `stop`: Shutdown the bot\n" +
                "- `config`: Displays the current configuration\n" +
                "- `status`: Displays bot uptime and system memory usage\n" +
                "- `reload translations`: Reload the lang.yml file\n" +
                "- `reload configuration`: Reload the configuration from file\n" +
                "- `reload music`: Reset music player").queue();
    }

    @Command(name = "test", administrative = true)
    public void test(MessageReceivedEvent messageReceivedEvent) {

    }

    @Command(name = "status", administrative = true)
    public void status(MessageReceivedEvent messageReceivedEvent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("```yaml\n[STATUS]\n\n");

        SelfUser selfUser = messageReceivedEvent.getJDA().getSelfUser();
        stringBuilder.append("Client: ").append(selfUser.getAsTag()).append(" (").append(selfUser.getId()).append(")\n");

        Duration duration = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        String formattedElapsedTime = String.format("%d days, %02d hours, %02d minutes, %02d seconds",
                duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        stringBuilder.append("Uptime: ").append(formattedElapsedTime).append("\n\n");

        stringBuilder.append("Memory usage:\n")
                .append(" - Max: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getMaxMemory())).append("\n")
                .append(" - Used: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getUsedMemory())).append("\n")
                .append(" - Total: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getTotalMemory())).append("\n")
                .append(" - Free: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getFreeMemory()));

        stringBuilder.append("\n```");

        messageReceivedEvent.getMessage().reply(stringBuilder.toString()).queue();
    }

    @Command(name = "reload", administrative = true)
    public void reload(MessageReceivedEvent messageReceivedEvent) {
        String content = messageReceivedEvent.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);

        String[] args = content.split(" ");

        if (args.length == 0)
            return;

        switch (args[1]) {
            case "configuration":
                messageReceivedEvent.getMessage().reply("Reloading configuration").queue(message -> {
                    try {
                        ConfigurationLoader configurationLoader = new ConfigurationLoader(true);

                        if (configurationLoader.getConfiguration() == null)
                            return;

                        Alkabot.setConfig(configurationLoader.getConfiguration());

                        Presence presence = Alkabot.getJDA().getPresence();

                        presence.setStatus(OnlineStatus.valueOf(Alkabot.getConfig().getPresence().getStatus()));
                        if (Alkabot.getConfig().getPresence().getActivity().isShow())
                            presence.setActivity(Alkabot.buildActivity());

                        message.reply("The configuration was successfully reloaded").queue();
                    } catch (IOException e) {
                        e.printStackTrace();
                        message.reply("Failed to reload configuration, check logs").queue();
                    }
                });
                break;

            case "translations":
                messageReceivedEvent.getMessage().reply("Reloading translations").queue(message -> {
                    try {
                        TranslationsLoader translationsLoader = new TranslationsLoader(false);

                        if (translationsLoader.getTranslations() == null)
                            return;

                        Alkabot.setTranslations(translationsLoader.getTranslations());

                        message.reply("Success, " + Alkabot.getTranslations().size() + " loaded translations").queue();
                    } catch (IOException e) {
                        e.printStackTrace();
                        message.reply("Failed to reload translations, check logs").queue();
                    }
                });
                break;

            case "music":
                Music.reset();
                messageReceivedEvent.getMessage().reply("OK").queue();
                break;

            default:
                messageReceivedEvent.getMessage().reply("Reload: `configuration`, `translations`, `music`, `playlists`").queue();
                break;

        }

    }

    @Command(name = "stop", administrative = true)
    public void stop(MessageReceivedEvent messageReceivedEvent) {
        messageReceivedEvent.getMessage().reply("Stopping (may take a moment!)").queue(message -> {
            // Shutdown
            messageReceivedEvent.getJDA().shutdown();
        });
    }

    @Command(name = "config", administrative = true)
    public void config(MessageReceivedEvent messageReceivedEvent) {
        Configuration configuration = Alkabot.getConfig();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("```yaml\n[CONFIGURATION - ALKABOT TEST SPOTIFY]\n\n");

        stringBuilder.append("Administrators: ").append(configuration.getAdministrators_id().size());
        if (configuration.getAdministrators_id().size() > 0)
            for (String admin : Alkabot.getConfig().getAdministrators_id())
                stringBuilder.append("\n - <@").append(admin).append(">");

        stringBuilder.append("\nGuild: ").append(Alkabot.getGuild().getName()).append(" [").append(Alkabot.getGuild().getId()).append("]\n");

        Configuration.Presence presence = configuration.getPresence();
        stringBuilder.append("Presence:\n").append(" - Status: ").append(presence.getStatus()).append("\n");
        stringBuilder.append(" - Activity:\n").append("    - Show: ").append(b(presence.getActivity().isShow())).append("\n");
        stringBuilder.append("    - Type: ").append(presence.getActivity().getType()).append("\n");
        stringBuilder.append("    - Text: \"").append(presence.getActivity().getText()).append("\"\n");
        stringBuilder.append("\n```");

        messageReceivedEvent.getMessage().reply(stringBuilder.toString()).queue();
    }

    private String b(boolean boo) {
        return boo ? "yes" : "no";
    }
}
