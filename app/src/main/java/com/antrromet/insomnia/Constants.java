package com.antrromet.insomnia;

public class Constants {

    // App preferences stored in this file
    public static final String APP_PREFERENCES = "app_preferences";

    public static final String NINE_GAG_PKG_NAME = "com.ninegag.android.app";

    /**
     * Enum for managing all the Loaders
     */
    public enum Loaders {

        NINE_GAG_FEEDS(100), FACEBOOK_FEEDS(101);

        public final int id;

        Loaders(final int id) {
            this.id = id;
        }
    }

    /**
     * Enum for holding the tags for each Volley Request
     */
    public enum VolleyTags {

        NINE_GAG_FEEDS("nine_gag_feeds"), INSTAGRAM_FEEDS("instagram_feeds");

        public final String tag;

        VolleyTags(String tag) {
            this.tag = tag;
        }
    }

    /**
     * Enum for holding the tags for each Volley Request
     */
    public enum Urls {

        NINE_GAG_FEEDS("http://infinigag.eu01.aws.af.cm/hot/%s"), INSTAGRAM_LOGIN
                ("https://instagram.com/oauth/authorize/?client_id=%s&redirect_uri=%s&response_type=token"),
        INSTAGRAM_FEEDS("https://api" +
                ".instagram.com/v1/users/self/feed?access_token=%s");

        public final String link;

        Urls(String link) {
            this.link = link;
        }
    }

    /**
     * Enum for holding the shared preference keys
     */
    public enum SharedPreferenceKeys {

        NINE_GAG_NEXT_PAGE_ID("nine_gag_next_page_id"), FACEBOOK_AFTER_PAGE_ID
                ("facebook_after_page_id"), INSTAGRAM_ACCESS_TOKEN("instagram_access_token");

        public final String key;

        SharedPreferenceKeys(final String key) {
            this.key = key;
        }

    }

    /**
     * Enum for holding the keys for 9gag API
     */
    public enum ApiKeys {

        DATA("data"), PAGING("paging"), NEXT("next"), ID("id"), CAPTION("caption"), IMAGES
                ("images"), NORMAL("normal"), LARGE("large"), LINK("link"), VOTES("votes"), COUNT
                ("count"), FROM("from"), TO("to"), NAME("name"), MESSAGE("message"), PICTURE
                ("picture"), FULL_PICTURE("full_picture"), TYPE("type"), STATUS_TYPE
                ("status_type"), CREATED_TIME("created_time"), CURSORS("cursors"), AFTER("after");

        public final String key;

        ApiKeys(final String key) {
            this.key = key;
        }

    }

}
