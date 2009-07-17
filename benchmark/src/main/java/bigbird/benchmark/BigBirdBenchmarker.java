package bigbird.benchmark;

import java.net.URL;

public class BigBirdBenchmarker {
    public static void main(String[] args) throws Exception {
        new BigBirdBenchmarker().execute();
    }
    
    public void execute() throws Exception {
        int threads = 10;
        RequestGenerator[] rg = new RequestGenerator[threads];
        for (int i = 0; i < rg.length; i++) {
            rg[i] = new RequestGenerator("application/json", 
                                         "{\"tweet\":\"Hello 1234567890\"}".getBytes(),
                                         "/api/tweet", 
                                         "user" + i, 
                                         "password");
        }
        
        HttpBenchmark benchmark = new HttpBenchmark();
        
        benchmark.setThreads(threads);
        benchmark.setRequests(5);
        
        benchmark.setUrl(new URL("http://localhost:8080"));
        benchmark.setRequestGenerators(rg);
        
        benchmark.execute();
        
        benchmark.setRequests(100);
        benchmark.execute();
    }

}
