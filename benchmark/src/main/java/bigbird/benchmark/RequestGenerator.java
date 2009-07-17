package bigbird.benchmark;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;

public class RequestGenerator {
    
    private DefaultedHttpParams params;
    private ByteArrayEntity entity;
    private final String authorization;
    private final String url;
    
    public RequestGenerator(String contentType, byte[] data, String url, String username, String password) {
        super();
        this.url = url;
        this.authorization = "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes()));
        entity = new ByteArrayEntity(data);
        entity.setContentType(contentType);
    }

    public HttpRequest generateRequest(int count) {
        BasicHttpEntityEnclosingRequest httppost = 
            new BasicHttpEntityEnclosingRequest("POST", url);
        httppost.setEntity(entity);
        httppost.setParams(params);
        httppost.setHeader(new BasicHeader("Authorization", 
                                           authorization));
        
        return httppost;
    }

    public void setParameters(HttpParams defaultParms) {
        this.params = new DefaultedHttpParams(new BasicHttpParams(), defaultParms);
    }
}