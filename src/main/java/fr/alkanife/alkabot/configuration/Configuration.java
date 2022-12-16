package fr.alkanife.alkabot.configuration;

import java.util.List;

public class Configuration {

    private String token;
    private String spotify_client_id;
    private String spotify_client_secret;
    private List<String> administrators_id;
    private String guild_id;
    private Presence presence;

    public Configuration() {}

    public Configuration(String token, String spotify_client_id, String spotify_client_secret, List<String> administrators_id, String guild_id, Presence presence) {
        this.token = token;
        this.spotify_client_id = spotify_client_id;
        this.spotify_client_secret = spotify_client_secret;
        this.administrators_id = administrators_id;
        this.guild_id = guild_id;
        this.presence = presence;
    }

    public String getToken() {
        return token;
    }

    public String getSpotify_client_id() {
        return spotify_client_id;
    }

    public String getSpotify_client_secret() {
        return spotify_client_secret;
    }

    public List<String> getAdministrators_id() {
        return administrators_id;
    }

    public String getGuild_id() {
        return guild_id;
    }

    public Presence getPresence() {
        return presence;
    }

    public static class Presence {
        private String status;
        private Activity activity;

        public Presence() {}

        public Presence(String status, Activity activity) {
            this.status = status;
            this.activity = activity;
        }

        public String getStatus() {
            return status;
        }

        public Activity getActivity() {
            return activity;
        }

        public static class Activity {
            private boolean show;
            private String type;
            private String text;

            public Activity() {}

            public Activity(boolean show, String type, String text) {
                this.show = show;
                this.type = type;
                this.text = text;
            }

            public boolean isShow() {
                return show;
            }

            public String getType() {
                return type;
            }

            public String getText() {
                return text;
            }
        }
    }
}
