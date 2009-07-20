$(document).ready(function(){
	home();
});

var tweetIndex = 0;

function home() {
	$("#tweetHeader").show();
	$("#userHeader").hide();
	refreshTweets();
	
	$("#following").empty();
	$("#followers").empty();
	updateUsers("./api/following", addFollowing);
	updateUsers("./api/followers", addFollowers);
}
function refreshTweets() {
	$("#tweets").empty();
	tweetIndex = 0;
	getMoreTweets();
}

function getMoreTweets() {
	$.getJSON("./api/friendsTimeline?start=" + tweetIndex + "&count=20",
			  function (data) {
			      $.each(data, addTweet);
			      tweetIndex += data.length;
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
		   url: "./api/tweet",
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
	$("#tweetHeader").hide();
	$("#userHeader").show();
	$("#user").text(user);
	$("#tweets").empty();
	$("#following").empty();
	$("#followers").empty();
	
	$.getJSON("./api/users/" + user + "?start=0&count=20",
			  function (data) {
			      $.each(data, addTweet);
	          }
	);
	updateUsers("./api/following", addFollowing);
	updateUsers("./api/followers", addFollowers);
}


function addFollowing(i,item) {
	addUser(item, $("#following"));
}

function addFollowers(i,item) {
	addUser(item, $("#followers"));
}

function addUser(user, element) {
	var div = $("<div class=\"user\"/>");
	var link = $("<a href=\"#users/" + user + "\" onclick=\"viewUser(\'" + user + "')\"/>")
	 .text(user).appendTo(div);
	div.appendTo(element);
}

function updateUsers(url, addFunction) {
	$.getJSON(url, function (data) {
			      $.each(data, addFunction);
	          }
	);
}

