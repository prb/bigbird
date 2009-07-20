package bigbird.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class StatsServlet extends HttpServlet {

    private WebApplicationContext ctx;
    private ThreadPoolTaskExecutor commandExecutor;
    private ThreadPoolTaskExecutor tweetServiceExecutor;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
        IOException {
        PrintWriter writer = resp.getWriter();
        
        writer.write("command.queueDepth:" + commandExecutor.getThreadPoolExecutor().getQueue().size());
        writer.write(" command.activeThreads:" + commandExecutor.getThreadPoolExecutor().getActiveCount());
        writer.write(" command.poolSize:" + commandExecutor.getThreadPoolExecutor().getPoolSize());
        
        writer.write(" tweet.queueDepth:" + tweetServiceExecutor.getThreadPoolExecutor().getQueue().size());
        writer.write(" tweet.activeThreads:" + tweetServiceExecutor.getThreadPoolExecutor().getActiveCount());
        writer.write(" tweet.poolSize:" + tweetServiceExecutor.getThreadPoolExecutor().getPoolSize());
        
        writer.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        
        commandExecutor = (ThreadPoolTaskExecutor) ctx.getBean("commandExecutor");
        tweetServiceExecutor = (ThreadPoolTaskExecutor) ctx.getBean("tweetServiceExecutor");
    }
}
