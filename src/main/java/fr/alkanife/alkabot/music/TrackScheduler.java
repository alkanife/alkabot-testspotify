package fr.alkanife.alkabot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import fr.alkanife.alkabot.Alkabot;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    public static boolean retrying = false;

    private final AudioPlayer player;
    private BlockingQueue<AlkabotTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public long getQueueDuration() {
        long duration = 0;

        for (AlkabotTrack alkabotTrack : queue)
            if (alkabotTrack.getDuration() < 72000000)
                duration += alkabotTrack.getDuration();

        return duration;
    }

    public void queue(AlkabotTrack track, boolean priority) {
        if (player.getPlayingTrack() == null) {
            Alkabot.getLogger().info("playing track null, playing now");
            MusicLoader.play(track);
            return;
        } else {
            Alkabot.getLogger().info("playing track not null, just adding");
        }

        Alkabot.getLogger().info("Adding to queue");
        if (priority) {
            BlockingQueue<AlkabotTrack> newQueue = new LinkedBlockingQueue<>();
            newQueue.offer(track);
            newQueue.addAll(queue);
            queue = newQueue;
        } else {
            queue.offer(track);
        }
    }

    public void queuePlaylist(AlkabotTrack firstTrack, List<AlkabotTrack> alkabotTrackList, boolean priority) {
        Alkabot.getLogger().info("queing playlist");

        BlockingQueue<AlkabotTrack> newQueue = new LinkedBlockingQueue<>();

        if (!priority)
            newQueue.addAll(queue);

        if (player.getPlayingTrack() == null) {
            Alkabot.getLogger().info("playing track null, playing first track now");
            MusicLoader.play(firstTrack);

            Alkabot.getLogger().info("Adding to queue except first");
            for (AlkabotTrack a : alkabotTrackList)
                if (!a.getIdentifier().equals(firstTrack.getIdentifier()))
                    newQueue.offer(a);
        } else {
            Alkabot.getLogger().info("Adding everything to queue");
            newQueue.addAll(alkabotTrackList);
        }

        if (priority)
            newQueue.addAll(queue);

        queue = newQueue;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Alkabot.getLogger().info("end reason : " + endReason.name() + " - mayplay = " + endReason.mayStartNext);

        if (endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
            Alkabot.getLogger().info("exception onTrackEnd");
            /*if (retrying) {
                retrying = false;
            } else {
                Alkabot.getLogger().info("retrying");
                retrying = true;
                MusicLoader.play(new AlkabotTrack(track));
                return;
            }*/
            return;
        }

        if (endReason.mayStartNext) {
            Alkabot.getLogger().info("go next");
            MusicLoader.play(Alkabot.getTrackScheduler().getQueue().poll());
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Alkabot.getLogger().info("exception onTrackException");
        if (retrying) {
            retrying = false;
        } else {
            Alkabot.getLogger().info("retrying");
            retrying = true;
            MusicLoader.play(new AlkabotTrack(track));
        }

        // say
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Alkabot.getLogger().info("start track " + track.getInfo().title);
    }

    public BlockingQueue<AlkabotTrack> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<AlkabotTrack> queue) {
        this.queue = queue;
    }

    /*



    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        AudioChannel voiceChannel = Alkabot.getGuild().getAudioManager().getConnectedChannel();

        if (voiceChannel != null) {
            if (voiceChannel.getMembers().size() == 1) {
                Music.reset();

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(Alkabot.t("jukebox-playing-error-nomembers-title"));
                embedBuilder.setColor(Colors.BIG_RED);
                embedBuilder.setDescription(Alkabot.t("jukebox-playing-error-nomembers-desc"));

                Alkabot.getLastCommandChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                return;
            }
        }

        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        //Satania.addFailedToPlay();

        if (Alkabot.getLastCommandChannel() != null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(Alkabot.t("jukebox-playing-error-title"));
            embedBuilder.setColor(Colors.BIG_RED);
            embedBuilder.setDescription("[" + track.getInfo().title + "](" + track.getInfo().uri + ")"
                    + " " + Alkabot.t("jukebox-by") + " [" + track.getInfo().author + "](" + track.getInfo().uri + ")\n\n" +
                    Alkabot.t("jukebox-playing-error-message"));
            embedBuilder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/0.jpg");

            Alkabot.getLastCommandChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }*/
}
