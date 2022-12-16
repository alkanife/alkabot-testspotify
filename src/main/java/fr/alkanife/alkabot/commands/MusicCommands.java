package fr.alkanife.alkabot.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.alkanife.alkabot.Alkabot;
import fr.alkanife.alkabot.commands.utils.Command;
import fr.alkanife.alkabot.music.AlkabotTrack;
import fr.alkanife.alkabot.music.Music;
import fr.alkanife.alkabot.music.MusicLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MusicCommands {

    @Command(name = "spotify")
    public void music(SlashCommandInteractionEvent slashCommandEvent) {
        Alkabot.setLastCommandChannel(slashCommandEvent.getChannel());

        String subCommand = slashCommandEvent.getSubcommandName();

        switch (subCommand) {
            case "play" -> {
                slashCommandEvent.deferReply().queue();

                Music.connect(slashCommandEvent.getMember());

                String url = slashCommandEvent.getOption("input").getAsString();

                // spotify
                if (url.toLowerCase().startsWith("https://open.spotify.com/playlist")) {
                    Alkabot.getLogger().info("spotify playlist detected");
                    MusicLoader.loadSpotifyPlaylist(slashCommandEvent, url, false);
                    return;
                }

                if (!Alkabot.isURL(url))
                    url = "ytsearch: " + url;

                MusicLoader.load(slashCommandEvent, url, false);
            }
            case "play_next" -> {
                slashCommandEvent.deferReply().queue();

                Music.connect(slashCommandEvent.getMember());

                String url = slashCommandEvent.getOption("input").getAsString();

                // spotify
                if (url.toLowerCase().startsWith("https://open.spotify.com/playlist")) {
                    Alkabot.getLogger().info("spotify playlist detected");
                    MusicLoader.loadSpotifyPlaylist(slashCommandEvent, url, true);
                    return;
                }

                if (!Alkabot.isURL(url))
                    url = "ytsearch: " + url;

                MusicLoader.load(slashCommandEvent, url, true);
            }
            case "remove" -> {
                if (Alkabot.getAudioPlayer().getPlayingTrack() == null) {
                    slashCommandEvent.reply(Alkabot.t("jukebox-command-no-current")).queue();
                    return;
                }
                OptionMapping removeOption = slashCommandEvent.getOption("input");
                long remove = 1;
                if (removeOption != null) {
                    remove = removeOption.getAsLong();

                    if (remove >= Alkabot.getTrackScheduler().getQueue().size()) {
                        slashCommandEvent.reply(Alkabot.t("jukebox-command-notenough")).queue();
                        return;
                    }
                }
                List<AlkabotTrack> alkabotTracks = new ArrayList<>(Alkabot.getTrackScheduler().getQueue());
                try {
                    AlkabotTrack t = alkabotTracks.get(((int) remove) - 1);

                    alkabotTracks.remove(t);

                    BlockingQueue<AlkabotTrack> newBlockingQueue = new LinkedBlockingQueue<>();

                    for (AlkabotTrack alkabotTrack : alkabotTracks)
                        newBlockingQueue.offer(alkabotTrack);

                    Alkabot.getTrackScheduler().setQueue(newBlockingQueue);

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(Alkabot.t("jukebox-command-remove-title"));
                    embedBuilder.setDescription("[" + t.getTitle() + "](" + t.getUrl() + ")"
                            + " " + Alkabot.t("jukebox-by") + " [" + t.getArtist() + "](" + t.getUrl() + ")");
                    //embedBuilder.setThumbnail("https://img.youtube.com/vi/" + t.getIdentifier() + "/0.jpg"); TODO

                    slashCommandEvent.replyEmbeds(embedBuilder.build()).queue();
                } catch (Exception e) {
                    slashCommandEvent.reply(Alkabot.t("jukebox-command-remove-error")).queue();
                }
            }

            case "skip" -> {
                //Satania.addSkipCommand();

                if (Alkabot.getAudioPlayer().getPlayingTrack() == null) {
                    slashCommandEvent.reply(Alkabot.t("jukebox-command-no-current")).queue();
                    return;
                }
                OptionMapping skipSize = slashCommandEvent.getOption("input");
                int skip = 0;
                if (skipSize != null) {
                    long skipLong = skipSize.getAsLong();

                    if (skipLong >= Alkabot.getTrackScheduler().getQueue().size()) {
                        slashCommandEvent.reply(Alkabot.t("jukebox-command-notenough")).queue();
                        return;
                    }

                    for (skip = 0; skip < skipLong; skip++)
                        Alkabot.getTrackScheduler().getQueue().remove();
                }

                MusicLoader.play(Alkabot.getTrackScheduler().getQueue().poll());

                if (skipSize == null)
                    slashCommandEvent.reply(Alkabot.t("jukebox-command-skip-one")).queue();
                else
                    slashCommandEvent.reply(Alkabot.t("jukebox-command-skip-mult", String.valueOf(skip))).queue();
            }
            case "stop" -> {
                slashCommandEvent.reply(Alkabot.t("jukebox-command-stop")).queue();
                Alkabot.getGuild().getAudioManager().closeAudioConnection();
            }
            case "shuffle" -> {
                //Satania.addShuffleCommand();

                List<AlkabotTrack> alkabotTracks = new ArrayList<>(Alkabot.getTrackScheduler().getQueue());
                Collections.shuffle(alkabotTracks);
                BlockingQueue<AlkabotTrack> blockingQueue = new LinkedBlockingQueue<>();
                for (AlkabotTrack alkabotTrack : alkabotTracks)
                    blockingQueue.offer(alkabotTrack);
                Alkabot.getTrackScheduler().setQueue(blockingQueue);
                slashCommandEvent.reply(Alkabot.t("jukebox-command-shuffle")).queue();
            }
            case "clear" -> {
                //Satania.addClearCommand();

                Alkabot.getTrackScheduler().setQueue(new LinkedBlockingQueue<>());
                slashCommandEvent.reply(Alkabot.t("jukebox-command-clear")).queue();
            }
            case "queue" -> {
                //Satania.addQueueCommand();

                AudioTrack current = Alkabot.getAudioPlayer().getPlayingTrack();
                if (current == null) {
                    slashCommandEvent.reply(Alkabot.t("jukebox-command-no-current")).queue();
                    return;
                }
                List<AlkabotTrack> alkabotTracks = new ArrayList<>(Alkabot.getTrackScheduler().getQueue());
                int tracksSize = alkabotTracks.size();
                int pages = 0;
                if (!endsWithZero(tracksSize)) {
                    for (int i = 0; i < 11; i++) {
                        if (endsWithZero(tracksSize))
                            break;

                        tracksSize++;
                    }
                }
                pages = tracksSize / 10;
                OptionMapping pageOption = slashCommandEvent.getOption("input");
                int page = 0;
                if (pageOption != null)
                    page = ((int) pageOption.getAsLong()) - 1;
                if (page < 0)
                    page = 0;
                if ((page - 1) > pages) { //todo bug
                    slashCommandEvent.reply(Alkabot.t("jukebox-command-queue-outofrange")).queue();
                    return;
                }
                slashCommandEvent.deferReply().queue();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                String desc = "";
                if (alkabotTracks.size() == 0) {
                    embedBuilder.setTitle(Alkabot.t("jukebox-command-queue-now-playing"));
                    embedBuilder.setThumbnail("https://img.youtube.com/vi/" + current.getIdentifier() + "/0.jpg");
                    desc += "**[" + current.getInfo().title + "](" + current.getInfo().uri + ")** " + Alkabot.musicDuration(current.getDuration());
                } else {                                                                           // '~' because String.valueOf don't work?
                    embedBuilder.setTitle(Alkabot.t("jukebox-command-queue-queued-title", "~" + Alkabot.getTrackScheduler().getQueue().size()));
                    embedBuilder.setThumbnail(Alkabot.t("jukebox-command-plgif"));
                    desc = "__" + Alkabot.t("jukebox-command-queue-queued-now-playing") + "__\n" +
                            "**[" + current.getInfo().title + "](" + current.getInfo().uri + ")** " + Alkabot.musicDuration(current.getDuration()) + "\n" +
                            "\n" +
                            "__" + Alkabot.t("jukebox-command-queue-queued-incoming") + "__\n";

                    for (int i = (page * 10); i < ((page * 10) + 10); i++) {
                        try {
                            AlkabotTrack alkabotTrack = alkabotTracks.get(i);
                            desc += "`" + (i + 1) + ".` [" + alkabotTrack.getTitle() + "](" + alkabotTrack.getUrl() + ") " + Alkabot.musicDuration(alkabotTrack.getDuration()) + "\n";
                        } catch (Exception e) {
                            break;
                        }
                    }

                    desc += "\n__" + Alkabot.t("jukebox-command-queue-queued-time") + "__ `" + Alkabot.playlistDuration(Alkabot.getTrackScheduler().getQueueDuration()) + "`\n\n" +
                            "**PAGE " + (page + 1) + " / " + pages + "**\n\n";
                }
                embedBuilder.setDescription(desc);
                slashCommandEvent.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    private boolean endsWithZero(int i) { //what an ugly way
        return Integer.toString(i).endsWith("0");
    }

}
