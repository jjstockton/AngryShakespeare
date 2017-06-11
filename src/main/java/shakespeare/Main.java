package shakespeare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
//
//import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class Main {

    public static void main(String[] args) throws MalformedURLException, TwitterException, InterruptedException, IOException {

        Twitter twitter = authorize();

        URL url = new URL("http://www.pangloss.com/seidel/Shaker/index.html");

        //favourite recent mentions
        ResponseList<twitter4j.Status> mentions = twitter.getMentionsTimeline();

        for (int i = 0; i < mentions.size(); i++) {

            if (!mentions.get(i).isFavorited()) {
                try {
                    twitter.createFavorite(mentions.get(i).getId());
                } catch (TwitterException e) {

                }

            }
        }

        boolean validTweet = false;
        String tweetText = "";

        ArrayList<twitter4j.Status> tweets = searchTweets(searchFilter(), 100, twitter);

        Status tweet = null;
        String tweetTo = null;
        for (int i = 0; tweet == null && i < tweets.size(); i++) {

            if (filterTweet(tweets.get(i))) {

                tweet = tweets.get(i);
                tweetTo = tweets.get(i).getUser().getScreenName();

            } else {
                //System.out.println("@" + tweets.get(i).getUser().getScreenName() + " " + tweets.get(i).getText() + "  //  tweet rejected");
            }
        }

        // Get the input stream through URL Connection
        String insult = null;

        while (!validTweet) {
            validTweet = false;
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();

            //generate insult
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("/font")) {
                    insult = line.replace("</font>", "").replace("[", "").replace("]", "").toLowerCase().replace("<br>", "\n");

                }
            }

            //generating tweet text
            tweetText = "@" + tweetTo + " " + insult;

            if (tweetText.length() <= 140) {

                validTweet = true;
            }
        }

        StatusUpdate status = new StatusUpdate(tweetText);

        status.setInReplyToStatusId(tweet.getId());

        boolean duplicate = false;
        if (twitter.getUserTimeline().size() != 0) {

            if (twitter.getUserTimeline().get(0).getInReplyToScreenName().equals(tweetTo)) {
                duplicate = true;
            }
        }

        if (tweetText.length() <= 140 && !duplicate) {
            twitter.updateStatus(status);

        }

    }

    public static boolean filterTweet(twitter4j.Status tweet) {
        Long[] bannedIDs = {2993594932L};
        ArrayList<Boolean> masterFilter = new ArrayList<>();

        String tweetText = tweet.getText().toLowerCase();
        String screenName = tweet.getUser().getScreenName().toLowerCase();
        Long userID = tweet.getUser().getId();

        Boolean[] filters = {
                tweetText.contains("-"),
                tweetText.contains("\""), //likely to be a shakespeare quote
                tweetText.contains("“"),
                tweetText.contains("william"),
                tweetText.contains("http://"),
                tweetText.contains("https://"),
                screenName.contains("shakespeare"), //likely a Shakespeare twitter account
                Arrays.asList(bannedIDs).contains(userID)

        };

        masterFilter.add(!Arrays.asList(filters).contains(true));

        return !masterFilter.contains(false);

    }

    public static String searchFilter() {

        return "lang:en "
                + "shakespeare hate OR \"fuck shakespeare\" OR \"shakespeare sucks\" "
                + "-filter:retweets";

    }

    public static ArrayList<Status> searchTweets(String searchTerm, int numTweets, twitter4j.Twitter twitter) throws TwitterException {

        ArrayList<twitter4j.Status> tweetsList = new ArrayList<>();

        Query query = new Query(searchTerm);
        //query.setUntil("2015-07-23");

        query.count(100);

        String tweetText;

        while (tweetsList.size() < numTweets) {
            QueryResult tweets = twitter.search(query);

            for (twitter4j.Status status : tweets.getTweets()) {
                tweetText = status.getText();
                if (tweetsList.contains(status)) {
                    numTweets = tweetsList.size();
                }

                if (!tweetText.contains("RT") && !tweetsList.contains(status) && tweetsList.size() < numTweets) {
                    tweetsList.add(status);
                }
            }

        }
        return tweetsList;
    }

    public static Twitter authorize() throws FileNotFoundException, IOException {

        String s = System.getProperty("file.separator");

        Properties prop = new Properties();
        // System.out.println(System.getProperty("user.dir"));

        InputStream input = new FileInputStream("config.properties");

        // load a properties file
        prop.load(input);

        // get the property value
        String consumerKey = prop.getProperty("ConsumerKey");
        String consumerSecret = prop.getProperty("ConsumerSecret");
        String accessToken = prop.getProperty("AccessToken");
        String accessTokenSecret = prop.getProperty("AccessTokenSecret");

        input.close();

        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        cb.setJSONStoreEnabled(true);
        TwitterFactory tf = new TwitterFactory(cb.build());

        return tf.getInstance();

    }

}

