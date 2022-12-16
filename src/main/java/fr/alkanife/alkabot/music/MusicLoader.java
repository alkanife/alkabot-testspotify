package fr.alkanife.alkabot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.alkanife.alkabot.Alkabot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicLoader {

    public static boolean loadRetrying = false;
    public static boolean playLoadRetrying = false;

    // From command
    public static void load(SlashCommandInteractionEvent slashCommandInteractionEvent, final String url, boolean priority) {
        Alkabot.getLogger().info("LOAD ->> " + url + " - " + priority);
        Alkabot.getAudioPlayerManager().loadItemOrdered(Alkabot.getAudioPlayer(), url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                loadRetrying = false;

                Alkabot.getLogger().info("track loaded ");

                Alkabot.getTrackScheduler().queue(new AlkabotTrack(track), priority);

                slashCommandInteractionEvent.getHook().sendMessage("load de `" + track.getInfo().title + "` OK").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                loadRetrying = false;

                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null)
                    firstTrack = playlist.getTracks().get(0);

                if (url.startsWith("ytsearch")) {
                    Alkabot.getLogger().info("playlist loaded - search yt ");

                    Alkabot.getTrackScheduler().queue(new AlkabotTrack(firstTrack), priority);

                    slashCommandInteractionEvent.getHook().sendMessage("load de `" + firstTrack.getInfo().title + "` OK (recherche youtube)").queue();
                } else {
                    Alkabot.getLogger().info("playlist loaded - playlist");

                    List<AlkabotTrack> alkabotTrackList = new ArrayList<>();

                    for (AudioTrack audioTrack : playlist.getTracks())
                        alkabotTrackList.add(new AlkabotTrack(audioTrack));

                    Alkabot.getTrackScheduler().queuePlaylist(new AlkabotTrack(firstTrack), alkabotTrackList, priority);

                    slashCommandInteractionEvent.getHook().sendMessage("load de `" + playlist.getName() + "` OK (playlist youtube)").queue();
                }
            }

            @Override
            public void noMatches() {
                loadRetrying = false;

                Alkabot.getLogger().info("no matches");
                slashCommandInteractionEvent.getHook().sendMessage("load fail : aucun résulat").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Alkabot.getLogger().info("Load fail - retry = " + loadRetrying);
                if (loadRetrying) {
                    slashCommandInteractionEvent.getHook().sendMessage("load fail : FriendlyException (retry = 1 je me suis pas chié dessus c toi le probleme)").queue();
                    loadRetrying = false;
                } else {
                    loadRetrying = true;
                    MusicLoader.load(slashCommandInteractionEvent, url, priority);
                }
            }
        });
    }

    public static void loadSpotifyPlaylist(SlashCommandInteractionEvent slashCommandInteractionEvent, final String url, boolean priority) {
        Alkabot.getLogger().info("LOAD ->> " + url + " - " + priority);

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(Alkabot.getConfig().getSpotify_client_id())
                .setClientSecret(Alkabot.getConfig().getSpotify_client_secret())
                .build();

        // access token

        String access = "";

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();

        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            access = clientCredentials.getAccessToken();

            System.out.println(access);
            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            slashCommandInteractionEvent.getHook().sendMessage("error clientCredentialsRequest").queue();
        }
        spotifyApi.setAccessToken(access);

        // tracks playlists

        String id = url.replaceAll("https://open.spotify.com/playlist/", "");

        Alkabot.getLogger().info(id);

        GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                .getPlaylistsItems(id)
//          .fields("description")
//          .limit(10)
//          .offset(0)
//          .market(CountryCode.SE)
//          .additionalTypes("track,episode")
                .build();

        try {
            final Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();

            List<AlkabotTrack> alkabotTrackList = new ArrayList<>();

            for (PlaylistTrack playlistTrack : playlistTrackPaging.getItems()) {
                Track track = (Track) playlistTrack.getTrack();
                alkabotTrackList.add(new AlkabotTrack(track));
            }

            Alkabot.getTrackScheduler().queuePlaylist(alkabotTrackList.get(0), alkabotTrackList, priority);

            slashCommandInteractionEvent.getHook().sendMessage("load OK (playlist spotify) --- " + alkabotTrackList.size() + " entrées").queue();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            slashCommandInteractionEvent.getHook().sendMessage("error getPlaylistsItemsRequest").queue();
        }
    }

    // From the queue
    public static void play(AlkabotTrack alkabotTrack) {
        if (alkabotTrack == null) {
            Alkabot.getLogger().info("track null");
            return;
        }

        if (alkabotTrack.getUrl() == null)
            alkabotTrack.setUrl("ytsearch: " + alkabotTrack.getTitle() + " " + alkabotTrack.getArtist());

        Alkabot.getLogger().info("PLAY ->> " + alkabotTrack.getUrl() + " - " + alkabotTrack.getTitle());

        Alkabot.getAudioPlayerManager().loadItemOrdered(Alkabot.getAudioPlayer(), alkabotTrack.getUrl(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                playLoadRetrying = false;
                Alkabot.getLogger().info("loaded");
                Alkabot.getLastCommandChannel().sendMessage("playing `" + alkabotTrack.getTitle() + "`").queue();
                Alkabot.getTrackScheduler().getPlayer().startTrack(track, false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // spotify, likely
                playLoadRetrying = false;

                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null)
                    firstTrack = playlist.getTracks().get(0);

                if (alkabotTrack.getUrl().startsWith("ytsearch")) {
                    Alkabot.getLogger().info("spotify track loaded - search yt ");

                    Alkabot.getTrackScheduler().getPlayer().startTrack(firstTrack, false);

                    Alkabot.getLastCommandChannel().sendMessage("playing `" + alkabotTrack.getUrl() + "` (recherche youtube)").queue();
                }
            }

            @Override
            public void noMatches() {
                Alkabot.getLogger().info("play fail = no matches");
                Alkabot.getLastCommandChannel().sendMessage("play load fail : no matches, go next").queue();
                MusicLoader.play(Alkabot.getTrackScheduler().getQueue().poll());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Alkabot.getLogger().info("Loadplay fail - retry = " + playLoadRetrying);
                if (playLoadRetrying) {
                    Alkabot.getLastCommandChannel().sendMessage("play load fail : FriendlyException (retry = 1 je me suis pas chié dessus c toi le probleme)").queue();
                    playLoadRetrying = false;
                    MusicLoader.play(Alkabot.getTrackScheduler().getQueue().poll());
                } else {
                    playLoadRetrying = true;
                    MusicLoader.play(alkabotTrack);
                }
            }
        });
    }
}
