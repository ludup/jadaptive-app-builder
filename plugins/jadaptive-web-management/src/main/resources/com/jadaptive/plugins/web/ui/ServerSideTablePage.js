$(function() {
	$('.searchTable').click(function(e) {
		e.preventDefault();
		window.location = $(this).attr('href') + "?column=" + $('#searchColumn').val() + "&filter=" + $('#searchValue').val();
	})
});