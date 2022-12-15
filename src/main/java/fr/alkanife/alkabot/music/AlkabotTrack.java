package fr.alkanife.alkabot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AlkabotTrack {

    private String url;
    private String provider;
    private String title;
    private String artist;
    private long duration;

    public AlkabotTrack () {}

    public AlkabotTrack(String url, String provider, String title, String artist, long duration) {
        this.url = url;
        this.provider = provider;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public AlkabotTrack(AudioTrack audioTrack) {
        this.url = audioTrack.getInfo().uri;
        this.title = audioTrack.getInfo().title;
        this.artist = audioTrack.getInfo().author;
        this.provider = "YT";
        this.duration = audioTrack.getDuration();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
