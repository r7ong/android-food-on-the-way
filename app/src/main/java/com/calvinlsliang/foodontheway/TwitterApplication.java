package com.calvinlsliang.foodontheway;


import android.content.Context;

import com.calvinlsliang.foodontheway.TwitterClient;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     TwitterClient client = TwitterApplication.getRestClient();
 *     // use client to send requests to API
 *
 */
public class TwitterApplication  {
    private static Context context;

    public static TwitterClient getRestClient() {
        return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, TwitterApplication.context);
    }
}

