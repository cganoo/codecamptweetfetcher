package tweetfetcher; /**
 * Created by cganoo on 06/11/15.
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.jets3t.service.S3Service;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class Application implements RequestHandler<Request, String> {
    private static final Logger log = LoggerFactory.getLogger("Application");

    private String awsAccessKey;
    private String awsSecretAccessKey;

    @Override
    public String handleRequest(Request request, Context context) {
        try {

            /**
             * Normally to access other AWS services from your Lambda function,
             * you simply have to adjust the granted role.
             *
             * For example, see: http://docs.aws.amazon.com/lambda/latest/dg/intro-permission-model.html
             *
             * However in this case we are using JetS3t library for S3 interactions
             * and hence we need to supply it with appropriate IAM credentials.
             */
            loadAWSAccessKeys();
            final S3Service s3Service = new RestS3Service(new AWSCredentials(awsAccessKey, awsSecretAccessKey));
            final TwitterConnection twitterConnection = new TwitterConnection(s3Service);
            Future<String> tweetTimer =  Executors.newSingleThreadExecutor().submit(twitterConnection);
            try {
                tweetTimer.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                // Cancel the job after timeout
                tweetTimer.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "OK";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAWSAccessKeys() {
        Map<String, String> prop = System.getenv();
        awsAccessKey = prop.get("AWS_ACCESS_KEY");
        awsSecretAccessKey = prop.get("AWS_SECRET_ACCESS_KEY");
        if (awsAccessKey == null || awsSecretAccessKey == null) {
            /**
             * If there aren't any environment variables,
             * look for the properties file or decrypt bundled secrets if available.
             *
             * See README for details.
             */
            throw new RuntimeException("You must define the AWS access keys either as environment " +
                    "variables or through some other way");
        }
    }
}
