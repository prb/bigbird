$(document).ready(function(){
	refreshTweets();
});

function refreshTweets() {
	$("#tweets").empty();
	$.getJSON("/api/friendsTimeline?start=0&count=20",
			  function (data) {
			      $.each(data, function(i, item) {
			    	               var tweetDiv = $("<div class=\"tweet\"/>");
			                       $("<span class=\"tweet-user\"/>").text(item.user + ": ").appendTo(tweetDiv);
			                       $("<span class=\"tweet-text\"/>").text(item.text).appendTo(tweetDiv);
			                       $("<div class=\"tweet-date\"/>").text(item.date).appendTo(tweetDiv);
			                       tweetDiv.appendTo("#tweets");
			                   });
	          }
	);
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