package bigbird.benchmark;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;

public abstract class RequestGenerator {
    
    private DefaultedHttpParams params;
    private ByteArrayEntity entity;
    private final String url;
    private final String method;
    
    public RequestGenerator(String method,
                            String contentType, byte[] data, String url) {
        super();
        this.method = method;
        this.url = url;
        
        if (contentType != null) {
            entity = new ByteArrayEntity(data);
            entity.setContentType(contentType);
        }
    }

    public HttpRequest generateRequest(int count) {
        BasicHttpEntityEnclosingRequest httppost = 
            new BasicHttpEntityEnclosingRequest(method, url);
        if (entity != null) {
            httppost.setEntity(entity);
        }
        httppost.setParams(params);
        String authorization = "Basic " + new String(Base64.encodeBase64((getUser() + ":password").getBytes()));
        httppost.setHeader(new BasicHeader("Authorization", 
                                           authorization));
        
        return httppost;
    }

    public void setParameters(HttpParams defaultParms) {
        this.params = new DefaultedHttpParams(new BasicHttpParams(), defaultParms);
    }
    
    public abstract String getUser();
}