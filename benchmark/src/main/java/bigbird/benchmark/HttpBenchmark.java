package bigbird.benchmark;
/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * Main program of the HTTP benchmark.
 *
 *
 * @since 4.0
 */
public class HttpBenchmark {

    private HttpParams params = null;
    private HttpHost host = null;
    protected int verbosity = 0;
    protected boolean keepAlive = false;
    protected int requests = 1;
    protected int threads = 1;
    protected URL url = null;
    protected boolean doHeadInsteadOfGet = false;
    private long contentLength = -1;
    protected int socketTimeout = 60000;
    protected boolean useHttp1_0 = false;
    protected RequestGenerator[] requestGenerators;
    
    public void setRequests(int requests) {
        this.requests = requests;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setRequestGenerators(RequestGenerator[] requestGenerators) {
        this.requestGenerators = requestGenerators;
    }

    public void execute() {
        params = getHttpParams(socketTimeout, useHttp1_0);

        for (RequestGenerator g : requestGenerators) {
            g.setParameters(params);
        }
        
        host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());

        ThreadPoolExecutor workerPool = new ThreadPoolExecutor(
            threads, threads, 5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                
                public Thread newThread(Runnable r) {
                    return new Thread(r, "ClientPool");
                }
                
            });
        workerPool.prestartAllCoreThreads();

        BenchmarkWorker[] workers = new BenchmarkWorker[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new BenchmarkWorker(
                    params, 
                    verbosity, 
                    requestGenerators[i], 
                    host, 
                    requests, 
                    keepAlive);
            workerPool.execute(workers[i]);
        }

        while (workerPool.getCompletedTaskCount() < threads) {
            Thread.yield();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }

        workerPool.shutdown();
        ResultProcessor.printResults(workers, host, url.toString(), contentLength);
    }

    private HttpParams getHttpParams(int socketTimeout, boolean useHttp1_0) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(HttpProtocolParams.PROTOCOL_VERSION,
            useHttp1_0 ? HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1)
            .setParameter(HttpProtocolParams.USER_AGENT, "Jakarta-HttpComponents-Bench/1.1")
            .setBooleanParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false)
            .setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, false)
            .setIntParameter(HttpConnectionParams.SO_TIMEOUT, socketTimeout);
        return params;
    }

    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

}
