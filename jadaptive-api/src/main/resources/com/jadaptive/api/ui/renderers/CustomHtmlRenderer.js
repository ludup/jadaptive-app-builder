$(function() {
	var blink_speed = 1000;
	setInterval(function () {
	    var ele = document.getElementById('heading');
	    ele.style.visibility = (ele.style.visibility == 'hidden' ? '' : 'hidden');
	}, blink_speed);
});