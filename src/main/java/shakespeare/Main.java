package shakespeare;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static shakespeare.Bot.*;

public class Main {

    public static void main(String[] args) {

        while (true) {

            try {
                Bot bot = new Bot();

                // Favourite recent mentions
                try {
                    System.out.println("Favouriting recent mentions.");
                    bot.favouriteRecentMentions();
                } catch (TwitterException e) {
                    System.err.println(e.getMessage());
                }

                System.out.println("Attempting to tweet...");
                for (Status targetTweet : bot.getTweets()) {

                    // Check that it's been at least an hour since the last tweet
                    Date currentTime = new Date();
                    long timeSinceLastTweet = currentTime.getTime() - bot.getLastTweet().getCreatedAt().getTime();
                    if (timeSinceLastTweet < 1 * 60 * 60 * 1000) {
                        System.out.println("Minimum elapsed tweet time not reached.");
                        break;
                    }

                    if (!validTweet(targetTweet)) {
                        System.out.println("Skipping tweet with ID " + targetTweet.getId() + ": not valid.");
                        continue;
                    }

                    if (bot.getLastTweet().getInReplyToScreenName().equals(targetTweet.getUser().getScreenName())) {
                        System.out.println("Skipping tweet with ID " + targetTweet.getId() + ": recently tweeted at this user");
                        continue;
                    }

                    String tweetText = null;

                    while (tweetText == null || tweetText.length() > 140) {
                        String insult = getInsult();
                        tweetText = "@" + targetTweet.getUser().getScreenName() + " " + insult;
                    }

                    StatusUpdate status = new StatusUpdate(tweetText);

                    if (bot.reply(status, targetTweet)) {
                        System.out.println("Successfully tweeted at @" + targetTweet.getUser().getScreenName());
                        break;
                    }
                }

                // Sleep the bot for a random time chosen from an exponential distribution, making sure to wait at least
                // an hour.
                // Mean sleep time   = ~3.88 hrs
                // Median sleep time = 3 hrs
                double sleepHrs = -1;
                while(sleepHrs < 1) {
                    sleepHrs = getNextExp(Math.log(2) / 2);
                }

                System.out.println("Sleeping for " + sleepHrs + " hrs.");

                TimeUnit.MILLISECONDS.sleep(Math.round(sleepHrs * 60 * 60 * 1000));

            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                // If we're in here then we've probably hit a fatal error
                // Notify and sleep for 24 hours
                // TODO Send email
                try {
                    System.out.println("Sleeping for 24 hrs");
                    TimeUnit.HOURS.sleep(24);
                } catch (InterruptedException ie) {
                    System.err.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static double getNextExp(double lambda) {
        Random rand = new Random();
        return  Math.log(1-rand.nextDouble())/(-lambda);
    }
}
