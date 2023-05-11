$(function() {

	UploadWidget.init('/upload/public-key', '/app/ui/search/authorizedKeys', "#feedback", function(fd) {
		fd.append("name", $('#name').val());
	}, function() {
		if($('#name').val().trim()  === '') {
			$('#feedback').prepend('<p class="alert alert-danger">A name is required!</p>');
			return false;
		}
		return true;
	});
});