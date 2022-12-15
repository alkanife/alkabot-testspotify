package fr.alkanife.alkabot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import fr.alkanife.alkabot.commands.AdminCommands;
import fr.alkanife.alkabot.commands.MusicCommands;
import fr.alkanife.alkabot.commands.utils.CommandHandler;
import fr.alkanife.alkabot.configuration.Configuration;
import fr.alkanife.alkabot.configuration.ConfigurationLoader;
import fr.alkanife.alkabot.events.Events;
import fr.alkanife.alkabot.lang.TranslationsLoader;
import fr.alkanife.alkabot.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Alkabot {

    private static String ABSOLUTE_PATH = ""; // Path where the .jar is located
    private static Logger LOGGER;
    private static Configuration CONFIGURATION;
    private static CommandHandler COMMAND_HANDLER;
    private static HashMap<String, Object> TRANSLATIONS = new HashMap<>();
    private static JDA JDA;
    private static Guild GUILD;
    private static MessageChannelUnion LAST_COMMAND_CHANNEL;
    private static AudioPlayerManager AUDIO_PLAYER_MANAGER;
    private static AudioPlayer AUDIO_PLAYER;
    private static TrackScheduler TRACK_SCHEDULER;

    public static void main(String[] args) {
        try {
            //
            // Moving old 'latest.log' file to the logs/ folder
            //
            ABSOLUTE_PATH = Paths.get("").toAbsolutePath().toString();
            System.out.println("ABSOLUTE_PATH: " + ABSOLUTE_PATH);

            File latestLogs = new File(ABSOLUTE_PATH + "/latest.log");

            if (latestLogs.exists()) {
                System.out.println("latest.log file existing");
                System.out.println("Cleaning logs...");

                File logsFolder = new File(ABSOLUTE_PATH + "/logs");

                if (logsFolder.exists()) {
                    System.out.println("logs/ folder already existing");
                    if (!logsFolder.isDirectory()) {
                        System.out.println(ABSOLUTE_PATH + "/logs is not a directory");
                        return;
                    }
                } else {
                    System.out.println("No logs/ directory found, creating one");
                    logsFolder.mkdir();
                }

                String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                String newPath = ABSOLUTE_PATH + "/logs/before-" + date + ".log";

                System.out.println("Moving latest.log file to " + newPath);
                Files.move(latestLogs.toPath(), Paths.get(newPath));
            } else {
                System.out.println("No latest.log file");
            }

            System.out.println("Creating logger");
            LOGGER = LoggerFactory.getLogger(Alkabot.class);

            //
            // Initializing configuration
            //
            ConfigurationLoader configurationLoader = new ConfigurationLoader(false);

            if (configurationLoader.getConfiguration() == null)
                return;

            CONFIGURATION = configurationLoader.getConfiguration();

            //
            // Initializing commands
            //
            getLogger().info("Setting up commands");

            COMMAND_HANDLER = new CommandHandler();
            getCommandHandler().registerCommands(new AdminCommands(), new MusicCommands());

            getLogger().info(COMMAND_HANDLER.getCommands().size() + " commands ready");

            //
            // Initializing translations
            //
            getLogger().info("Reading translations");

            TranslationsLoader translationsLoader = new TranslationsLoader(false);

            if (translationsLoader.getTranslations() == null)
                return;

            TRANSLATIONS = translationsLoader.getTranslations();

            //
            // Building JDA
            //
            getLogger().info("Building JDA...");

            JDABuilder jdaBuilder = JDABuilder.createDefault(getConfig().getToken());
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.setStatus(OnlineStatus.valueOf(getConfig().getPresence().getStatus()));
            if (getConfig().getPresence().getActivity().isShow())
                jdaBuilder.setActivity(buildActivity());

            jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_BANS,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT);
            jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
            jdaBuilder.addEventListeners(new Events());

            getLogger().info("Starting JDA");
            JDA = jdaBuilder.build();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String absolutePath() {
        return ABSOLUTE_PATH;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Configuration getConfig() {
        return CONFIGURATION;
    }

    public static void setConfig(Configuration CONFIGURATION) {
        Alkabot.CONFIGURATION = CONFIGURATION;
    }

    public static CommandHandler getCommandHandler() {
        return COMMAND_HANDLER;
    }

    public static HashMap<String, Object> getTranslations() {
        return TRANSLATIONS;
    }

    public static void setTranslations(HashMap<String, Object> TRANSLATIONS) {
        Alkabot.TRANSLATIONS = TRANSLATIONS;
    }

    public static JDA getJDA() {
        return JDA;
    }

    public static void setGuild(Guild guild) {
        GUILD = guild;
    }

    public static Guild getGuild() {
        return GUILD;
    }

    public static MessageChannelUnion getLastCommandChannel() {
        return LAST_COMMAND_CHANNEL;
    }

    public static void setLastCommandChannel(MessageChannelUnion lastCommandChannel) {
        LAST_COMMAND_CHANNEL = lastCommandChannel;
    }

    public static AudioPlayerManager getAudioPlayerManager() {
        return AUDIO_PLAYER_MANAGER;
    }

    public static void setAudioPlayerManager(AudioPlayerManager audioPlayerManager) {
        AUDIO_PLAYER_MANAGER = audioPlayerManager;
    }

    public static AudioPlayer getAudioPlayer() {
        return AUDIO_PLAYER;
    }

    public static void setAudioPlayer(AudioPlayer audioPlayer) {
        AUDIO_PLAYER = audioPlayer;
    }

    public static TrackScheduler getTrackScheduler() {
        return TRACK_SCHEDULER;
    }

    public static void setTrackScheduler(TrackScheduler trackScheduler) {
        TRACK_SCHEDULER = trackScheduler;
    }

    public static String t(String key, String... values) {
        if (TRANSLATIONS.containsKey(key)) {
            MessageFormat messageFormat = new MessageFormat(String.valueOf(TRANSLATIONS.get(key)));
            return messageFormat.format(values);
        } else return "{MISSING TRANSLATION @ " + key + "}";
    }

    public static String limitString(String value, int length) {
        StringBuilder buf = new StringBuilder(value);
        if (buf.length() > length) {
            buf.setLength(length - 5);
            buf.append("`...`");
        }

        return buf.toString();
    }

    public static String musicDuration(long duration) {
        if (duration >= 72000000)
            return "";

        if (duration >= 3600000) {
            return "[" + String.format("%02d:%02d:%02d",  TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1)) + "]";
        } else {
            return "[" + String.format("%02d:%02d",  TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1)) + "]";
        }
    }

    public static String playlistDuration(long duration) {
        if (duration >= 3600000) {
            return String.format("%02d:%02d:%02d",  TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
        } else {
            return String.format("%02d:%02d",  TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
        }
    }

    public static boolean isURL(String s) {
        return s.toLowerCase(Locale.ROOT).startsWith("http");
    }

    public static Activity buildActivity() {
        getLogger().info("Building activity");
        Activity.ActivityType activityType = Activity.ActivityType.valueOf(getConfig().getPresence().getActivity().getType());
        return Activity.of(activityType, getConfig().getPresence().getActivity().getText());
    }
}
