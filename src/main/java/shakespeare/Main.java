package shakespeare;

import twitter4j.TwitterException;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void main(String[] args) {

        boolean isRealRun;
        if(args.length == 1) {
            isRealRun = Boolean.parseBoolean(args[0]);
        } else {
            throw new RuntimeException("Unable to parse arguments: expected 1 argument but got " + args.length + ".");
        }

        if(!isRealRun) {
            System.out.println("Running as test.");
        }

        while (true) {

            Bot bot = new Bot();

            try {

                bot.run(isRealRun);

                if(!isRealRun) {
                    return;
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
                throw new RuntimeException(e);
            } catch (TwitterException | IOException e) {
                System.err.println("Fatal error: " + e);
                // If we're in here then we've probably hit a fatal error
                // Notify and sleep for 24 hours
                // TODO Send email
                try {
                    System.out.println("Sleeping for 24 hrs.");
                    TimeUnit.HOURS.sleep(24);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
        }
    }

    public static double getNextExp(double lambda) {
        Random rand = new Random();
        return  Math.log(1-rand.nextDouble())/(-lambda);
    }
}
