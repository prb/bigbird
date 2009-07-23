package bigbird.benchmark;

import java.net.URL;
import java.util.Random;

public class BigBirdBenchmarker {
    private int threads = 10;
    private int requests = 10;
    private double percentage = 0.10;

    public static void main(String[] args) throws Exception {
        BigBirdBenchmarker benchmarker = new BigBirdBenchmarker();
        if (args.length > 0) {
            benchmarker.setThreads(Integer.valueOf(args[0]));
        }
        if (args.length > 1) {
            benchmarker.setRequests(Integer.valueOf(args[1]));
        }
        if (args.length > 2) {
            benchmarker.setTweetPercentage(Double.valueOf(args[2]));
        }
        benchmarker.execute();
    }

    private void setTweetPercentage(double percentage) {
        this.percentage = percentage;
    }

    private void setThreads(int threads) {
        this.threads = threads;

    }

    private void setRequests(int requests) {
        this.requests = requests;
    }

    public void execute() throws Exception {
        System.out.println("Starting benchmark");
        RequestGenerator[] rg = new RequestGenerator[threads];

        int tweetThreads = (int)((double)percentage * (double)threads);
        // int tweetThreads = 1;
        System.out.println("Tweet threads: " + tweetThreads);
        System.out.println("Get friends timeline threads: " + (threads - tweetThreads));

        final Random random = new Random();

        for (int i = 0; i < tweetThreads; i++) {
            rg[i] = new RequestGenerator("POST", "application/json", "{\"tweet\":\"Hello 1234567890\"}"
                .getBytes(), "/api/tweet") {

                @Override
                public String getUser() {
                    return "user" + random.nextInt(1000);
                }

            };
        }

        for (int i = tweetThreads; i < threads; i++) {
            rg[i] = new RequestGenerator("GET", null, null, "/api/friendsTimeline?start=0&count=20") {

                @Override
                public String getUser() {
                    return "user" + random.nextInt(1000);
                }

            };
        }

        HttpBenchmark benchmark = new HttpBenchmark();

        benchmark.setThreads(threads);
        benchmark.setRequests(requests);
        // benchmark.setVerbosity(3);
        benchmark.setUrl(new URL("http://tat1.datapr0n.com:8080"));
        benchmark.setRequestGenerators(rg);

        benchmark.execute();
    }

}
