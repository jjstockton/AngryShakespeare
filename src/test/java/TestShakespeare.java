import org.junit.Before;
import org.junit.Test;
import shakespeare.Bot;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static shakespeare.Bot.tweetedAtUser;
import static shakespeare.Bot.validTweet;

// Mostly just acceptance tests
public class TestShakespeare {

    Bot bot;

    @Before
    public void setUp() {
        bot = new Bot();
    }

    @Test
    public void TestShakespeare() throws TwitterException, IOException {

        bot.favouriteRecentMentions();

        for(Status targetTweet : bot.getTweets()) {

            if(!validTweet(targetTweet)) {
                continue;
            }

            ResponseList<Status> myRecentTweets = bot.getRecentTweets(100);
            if(tweetedAtUser(myRecentTweets, targetTweet.getUser().getId())) {
                System.out.println("Skipping tweet with ID " + targetTweet.getId() + ": recently tweeted at " +
                        "user @" + targetTweet.getUser().getScreenName() + ".");
                continue;
            }

            String tweetText = null;

            while(tweetText == null || tweetText.length() > 140) {
                String insult = bot.getInsult();

                assertNotNull(insult);
                assertFalse(insult.isEmpty());
                assertTrue(Pattern.compile("[^<>=]+").matcher(insult).matches()); // Make sure no HTML sneaked in

                tweetText = ".@" + targetTweet.getUser().getScreenName() + " " + insult;
            }

            StatusUpdate status = new StatusUpdate(tweetText);

        }
    }
}
