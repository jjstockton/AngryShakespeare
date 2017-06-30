package shakespeare;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;

public class Bot {

    private Twitter twitter;
    private URL insultUrl;

    public Bot() {
        String consumerKey = System.getenv("ConsumerKey");
        String consumerSecret = System.getenv("ConsumerSecret");
        String accessToken = System.getenv("AccessToken");
        String accessTokenSecret = System.getenv("AccessTokenSecret");

        if(consumerKey == null || consumerSecret == null || accessToken == null || accessTokenSecret == null) {
            throw new RuntimeException("Authentication variables are missing from the environment.");
        }

        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);

        cb.setJSONStoreEnabled(true);
        TwitterFactory tf = new TwitterFactory(cb.build());

        this.twitter = tf.getInstance();

        try {
            this.insultUrl = new URL(System.getenv("InsultUrl"));
        } catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void favouriteRecentMentions() throws TwitterException {

        ResponseList<Status> mentions;
        try {
            mentions = twitter.getMentionsTimeline();
        } catch (TwitterException e) {
            throw new TwitterException("Failed to get mentions timeline! " + e);
        }

        for (Status m : mentions) {
            try {
                twitter.createFavorite(m.getId());
            } catch(TwitterException e) {
                // There's not much we want to do if creating a favourite fails
                // Sometimes this happens if the user blocked AngryShakespeare :(
                System.err.println("Couldn't favourite tweet: '" + m.getId() + "'. " + e);
            }
        }
    }

    public List<Status> getTweets() throws TwitterException {

        List<Status> tweets;

        Query query = new Query(searchFilter());
        query.count(100);

        tweets = twitter.search(query).getTweets();

        return tweets;
    }

    public static boolean validTweet(Status tweet) {
        Long[] bannedIDs = {2993594932L};

        String tweetText = tweet.getText().toLowerCase();
        String screenName = tweet.getUser().getScreenName().toLowerCase();
        Long userID = tweet.getUser().getId();

        boolean badTweet = tweetText.contains("-") ||
                tweetText.contains("\"") || // Likely to be a shakespeare quote
                tweetText.contains("“") ||
                tweetText.contains("william") ||
                tweetText.contains("http://") ||
                tweetText.contains("https://") ||
                screenName.contains("shakespeare") || // Likely a Shakespeare twitter account
                Arrays.asList(bannedIDs).contains(userID);

        return !badTweet;

    }

    public String getInsult() throws IOException {
        String insult = null;

        URLConnection con = insultUrl.openConnection();
        InputStream is = con.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

        // TODO: Refactor this to be less hacky now that I know a little more about HTML parsing
        while ((line = br.readLine()) != null) {
            if (line.contains("/font")) {
                insult = line.replace("</font>", "")
                        .replace("[", "")
                        .replace("]", "")
                        .toLowerCase()
                        .replace("<br>", "\n");
            }
        }

        return insult;
    }

    private static String searchFilter() {

        return "lang:en "
                + "shakespeare hate OR \"fuck shakespeare\" OR \"shakespeare sucks\" "
                + "-filter:retweets";

    }

    public Status getLastTweet() throws TwitterException {
        return twitter.getUserTimeline().get(0);
    }

    public ResponseList<Status> getRecentTweets(int maxCount) throws TwitterException {
        Paging p = new Paging();
        int count = maxCount;

        while(count > 0) {
            try {
                p.count(count);
                return twitter.getUserTimeline(p);
            } catch (TwitterException e) {
                count--;
            }
        }

        throw new TwitterException("Couldn't fetch user timeline!");
    }

    public static boolean tweetedAtUser(ResponseList<Status> tweets, long userId) {

        for(Status tweet : tweets) {
            if(tweet.getInReplyToUserId() == userId) {
                return true;
            }
        }

        return false;
    }

    public boolean reply(StatusUpdate tweet, Status targetTweet) {

        tweet.setInReplyToStatusId(targetTweet.getId());
        try {
            twitter.updateStatus(tweet);
        } catch(TwitterException e) {
            System.err.println("Failed when tweeting response to ID: '" + targetTweet.getId() + "'. " + e);
            return false;
        }

        return true;
    }
}
