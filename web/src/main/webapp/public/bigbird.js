$(document).ready(function(){
	home();
	$("#find_user").mousedown(clearFindUser);
	$("#find_user").keyup(findUser);
});

var tweetIndex = 0;
var clearUser = true;
var followers;
var following;
var currentUser;

function home() {
	$("#tweetHeader").show();
	$("#userHeader").hide();
	$("#find_user").val("Find user...");
	clearError();
	
	refreshTweets();
	
	$("#following").empty();
	$("#followers").empty();
	updateUsers("./api/following", addFollowing, true, true);
	updateUsers("./api/followers", addFollowers, true, false);
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
	 $.ajax({
		   type: "POST",
		   url: "./api/tweet",
		   contentType: "application/json",
		   data: "{ \"tweet\" : \"" + $("#tweetBox").val() + "\" }",
		   success: function(msg){
		     refreshTweets();
		     $("#tweetBox").val("");
		   },
		   error: function (XMLHttpRequest, textStatus, errorThrown) {
			   // typically only one of textStatus or errorThrown 
			   // will have info
			   alert(textStatus + " " + errorThrown);
			 }
		 });
	 
}

function viewUser(user) {
	var viewUserHandler = function (data) {
		currentUser = user;
		$("#tweetHeader").hide();
		$("#userHeader").show();
		$("#user").text(user);
		$("#tweets").empty();
		$("#following").empty();
		$("#followers").empty();
		$("#find_user").val("Find user...");
		
		if (contains(following,user)) {
			$("#follow").text("Unfollow");
		} else {
			$("#follow").text("Follow");
		}
		
		clearUser = true;
	    $.each(data, addTweet);
		updateUsers("./api/following?user=" + user, addFollowing);
		updateUsers("./api/followers?user=" + user, addFollowers);
    }
	
	var errorHandler = function(data) {
		showError("Could not find user " + user + "!");
	}

	 $.ajax({
		   type: "GET",
		   url: "./api/users/" + user + "?start=0&count=20",
		   success: viewUserHandler,
		   error: errorHandler,
		   dataType: "json"
		 });
}


function showMessage(text) {
	$("#message").text(text);
}

function clearError() {
	$("#error").hide();
	$("#error").text("");
}

function showError(text) {
	$("#error").show();
	$("#error").text(text);
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

function updateUsers(url, addFunction, isLoggedInUser, isFollowingResult) {
	$.getJSON(url, function (data) {
		               // store the data so we can reuse it
		               if (isLoggedInUser) {
			               if (isFollowingResult) {
			            	   following = data;
			               } else {
			            	   followers = data;
			               }
		               }
				       $.each(data, addFunction);
		           }
	);
}

function clearFindUser() {
	if (clearUser) {
	  $("#find_user").val("");
	  clearUser = false;
	}
}

function findUser(e) {
	if (e.keyCode == 13) {
		viewUser($("#find_user").val())
	}
}

// Follow or unfollow the current user depending on the current state.
function followOrUnfollow() {
	if (contains(following,user)) {
	} else {
	}
	
	$.ajax({
		   type: "POST",
		   url: "./api/startFollowing",
		   contentType: "application/json",
		   data: "{ \"user\" : \"" + currentUser + "\" }",
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
function contains(arr,obj) {
	for (var i = 0; i < arr.length; i++) {
		if (obj == arr[i]) return true;
	}
	return false;
}