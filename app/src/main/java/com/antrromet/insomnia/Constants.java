package com.antrromet.insomnia;

public class Constants {

    // App preferences stored in this file
    public static final String APP_PREFERENCES = "app_preferences";

    /**
     * Enum for managing all the Loaders
     */
    public enum Loaders {

        NINE_GAG_FEEDS(100),FACEBOOK_FEEDS(101);

        public final int id;

        Loaders(final int id) {
            this.id = id;
        }
    }

    /**
     * Enum for holding the tags for each Volley Request
     */
    public enum VolleyTags {

        NINE_GAG_FEEDS("nine_gag_feeds");

        public final String tag;

        VolleyTags(String tag) {
            this.tag = tag;
        }
    }

    /**
     * Enum for holding the tags for each Volley Request
     */
    public enum Urls {

        NINE_GAG_FEEDS("http://infinigag.eu01.aws.af.cm/hot/%s");

        public final String link;

        Urls(String link) {
            this.link = link;
        }
    }

    /**
     * Enum for holding the shared preference keys
     */
    public enum SharedPreferenceKeys {

        NINE_GAG_NEXT_PAGE_ID("nine_gag_next_page_id");

        public final String key;

        SharedPreferenceKeys(final String key) {
            this.key = key;
        }

    }

    /**
     * Enum for holding the keys for 9gag API
     */
    public enum NineGagKeys {

        DATA("data"), PAGING("paging"), NEXT("next"), ID("id"), CAPTION("caption"), IMAGES
                ("images"), NORMAL("normal"), LARGE("large"), LINK("link"), VOTES("votes"), COUNT("count");

        public final String key;

        NineGagKeys(final String key) {
            this.key = key;
        }

    }

}
