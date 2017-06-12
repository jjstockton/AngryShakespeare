package shakespeare;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

import java.util.concurrent.TimeUnit;

import static shakespeare.Bot.*;

public class Main {

    public static void main(String[] args) {

        while (true) {

            try {
                Bot bot = new Bot();

                // Favourite recent mentions
                try {
                    bot.favouriteRecentMentions();
                } catch (TwitterException e) {
                    // TODO: Send email
                }

                for (Status targetTweet : bot.getTweets()) {

                    if (!validTweet(targetTweet)) {
                        continue;
                    }

                    if (bot.getLastTweet().getInReplyToScreenName().equals(targetTweet.getUser().getScreenName())) {
                        continue;
                    }

                    // TODO: Check that minimum tweet time has elapsed

                    String tweetText = null;

                    while (tweetText == null || tweetText.length() > 140) {
                        String insult = getInsult();
                        tweetText = ".@" + targetTweet.getUser().getScreenName() + " " + insult;
                    }

                    StatusUpdate status = new StatusUpdate(tweetText);

                    if (bot.reply(status, targetTweet)) {
                        break;
                    }
                }

                // TODO: Sleep

            } catch (Exception e) {
                // If we're in here then we've probably hit a fatal error
                // Notify and sleep for 24 hours
                // TODO Send email
                try {
                    TimeUnit.HOURS.sleep(24);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
