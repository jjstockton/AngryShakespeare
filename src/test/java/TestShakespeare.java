import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import shakespeare.Bot;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static shakespeare.Bot.getInsult;
import static shakespeare.Bot.validTweet;

// Mostly just acceptance tests
public class TestShakespeare {

    Bot bot;

    @Before
    public void setUp() {
        bot = new Bot();
    }

    @Test
    public void TestShakespeare() throws TwitterException {

        bot.favouriteRecentMentions();

        for(Status targetTweet : bot.getTweets()) {

            if(!validTweet(targetTweet)) {
                continue;
            }

            if(bot.getLastTweet().getInReplyToScreenName().equals(targetTweet.getUser().getScreenName())) {
                continue;
            }

            String tweetText = null;

            while(tweetText == null || tweetText.length() > 140) {
                String insult = getInsult();

                assertNotNull(insult);
                assertFalse(insult.isEmpty());
                assertTrue(Pattern.compile("[^<>=]+").matcher(insult).matches()); // Make sure no HTML sneaked in

                tweetText = ".@" + targetTweet.getUser().getScreenName() + " " + insult;
            }

            StatusUpdate status = new StatusUpdate(tweetText);

        }
    }
}
