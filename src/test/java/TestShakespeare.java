import org.junit.Before;
import org.junit.Test;
import shakespeare.Bot;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

// Mostly just acceptance tests
public class TestShakespeare {

    Bot bot;

    @Before
    public void setUp() {
        bot = new Bot();
    }

    @Test
    public void TestInsult() throws TwitterException, IOException {

        String insult = bot.getInsult();

        assertNotNull(insult);
        assertFalse(insult.isEmpty());
        assertTrue(Pattern.compile("[^<>=]+").matcher(insult).matches()); // Check that no HTML sneaked in
    }

    @Test
    public void TestRun() throws TwitterException, IOException {
        bot.run(false);
    }
}
