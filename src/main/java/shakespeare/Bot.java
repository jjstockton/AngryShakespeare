package shakespeare;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

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

    Twitter twitter;

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

    }

    public void favouriteRecentMentions() throws TwitterException {
        ResponseList<Status> mentions = twitter.getMentionsTimeline();

        for (Status m : mentions) {
            try {
                twitter.createFavorite(m.getId());
            } catch(TwitterException e) {
                // There's not much we can do if creating a favourite fails
                // Sometimes this happens if the user blocked AngryShakespeare :(
            }
        }
    }

    public List<Status> getTweets() {

        List<Status> tweets;

        Query query = new Query(searchFilter());
        query.count(100);

        try {
            tweets = twitter.search(query).getTweets();
        } catch(TwitterException e) {
            throw new RuntimeException(e);
        }

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

    public static String getInsult() {
        String insult = null;
        try {
            URL url = new URL("http://www.pangloss.com/seidel/Shaker/index.html");

            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("/font")) {
                    insult = line.replace("</font>", "")
                            .replace("[", "")
                            .replace("]", "")
                            .toLowerCase()
                            .replace("<br>", "\n");
                }
            }

        } catch(IOException e) {
            throw new RuntimeException(e);
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

    public boolean reply(StatusUpdate tweet, Status targetTweet) {

        tweet.setInReplyToStatusId(targetTweet.getId());
        try {
            twitter.updateStatus(tweet);
        } catch(TwitterException e) {
            return false;
        }

        return true;
    }
}
