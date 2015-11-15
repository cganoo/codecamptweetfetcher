package tweetfetcher;

import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by cganoo on 06/11/15.
 */
public class TwitterConnection implements Callable {
    private static final Logger log = LoggerFactory.getLogger(TwitterConnection.class.getName());
    private static final URI TWITTER_URI = URI.create("https://stream.twitter.com/1.1/statuses/sample.json");

    private final S3Service s3Service;

    public TwitterConnection(final S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public String call() {
        try {
            fetchTweets();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "done";
    }

    private void fetchTweets() throws IOException, InterruptedException {
        final HttpURLConnection httpURLConnection = connectToTwitter();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
            processTweets(reader.lines());
        } catch (Exception e) {
            log.info("Exception Thrown: {}\nRetrying....\n", e.getMessage());
            retryConnection();
        }
    }

    void processTweets(final Stream<String> stream) {
        stream.filter(this::isNotDeleteEvent)
                .forEach(this::persist);
    }

    private boolean isNotDeleteEvent(String tweet) {
        return !tweet.startsWith("{\"delete\"");
    }

    private void persist(String tweet) {
        log.info(tweet);
        try {
            s3Service.putObject("codecamp15", new S3Object(UUID.randomUUID().toString(), tweet));
        } catch (Exception e) {
            //its ok
            log.error(e.toString());
        }
    }

    private void retryConnection() throws IOException, InterruptedException {
        Thread.sleep(2000);
        fetchTweets();
    }

    private HttpURLConnection connectToTwitter() throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) TWITTER_URI.toURL().openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("GET");

        TwitterOAuth oauth = new TwitterOAuth();
        String signature = oauth.generateSignature();
        httpURLConnection.addRequestProperty("Authorization", "OAuth " + oauth.getAuthParams()
                + ", oauth_signature=\"" + signature + "\"");
        return httpURLConnection;
    }
}
