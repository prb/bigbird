package bigbird.web;

import bigbird.Tweet;

import java.util.Date;

public class WebTweet {
    private String href;
    private String text;
    private Date date;
    
    public WebTweet(Tweet t) {
        this.date = t.getDate();
        this.href = t.getUser() + "/" + t.getUser() + "/" + t.getId();
        this.text = t.getText();
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
