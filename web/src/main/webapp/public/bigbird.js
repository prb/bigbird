$(document).ready(function(){
	refreshTweets();
});

function home() {
	// TODO: restore/hide tweet box
	refreshTweets();
}
function refreshTweets() {
	$("#tweets").empty();
	$.getJSON("/api/friendsTimeline?start=0&count=20",
			  function (data) {
			      $.each(data, addTweet);
	          }
	);
}

function addTweet(i, item) {
    var tweetDiv = $("<div class=\"tweet\"/>");
    var user = $("<span class=\"tweet-user\">")
      .appendTo(tweetDiv);
    var link = $("<a href=\"#users/" + item.user + "\" onclick=\"viewUser(\'" + item.user + "')\"/>")
    	 .text(item.user).appendTo(user);
    $("<span class=\"tweet-text\"/>").text(": " + item.text).appendTo(tweetDiv);
    $("<div class=\"tweet-date\"/>").text(item.date).appendTo(tweetDiv);
    tweetDiv.appendTo("#tweets");
}

function tweet() {
	var tweet = new Object();
	tweet.tweet = tweetBox.value;
	
	 $.ajax({
		   type: "POST",
		   url: "/api/tweet",
		   contentType: "application/json",
		   data: "{ \"tweet\" : \"" + tweetBox.value + "\" }",
		   success: function(msg){
		     refreshTweets();
		   },
		   error: function (XMLHttpRequest, textStatus, errorThrown) {
			   // typically only one of textStatus or errorThrown 
			   // will have info
			   alert(textStatus + " " + errorThrown);
			 }
		 });
	 
}

function viewUser(user) {
	$("#tweets").empty();
	$.getJSON("/api/users/" + user + "?start=0&count=20",
			  function (data) {
			      $.each(data, addTweet);
	          }
	);
}