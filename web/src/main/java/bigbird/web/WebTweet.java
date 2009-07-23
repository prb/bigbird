package bigbird.web;

import bigbird.Tweet;

public class WebTweet {
    private String href;
    private String text;
    private String date;
    private String user;
    
    public WebTweet(Tweet t) {
        this.date = t.getDate().toString();
        this.href = "/api/" + t.getUser() + "/" + t.getId();
        this.text = t.getText();
        this.user = t.getUser();
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
