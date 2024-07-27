$(function() {

	UploadWidget.init('/upload/avatar', '/', "#feedback", function(fd) {
	}, function() {
		return true;
	});
	UploadWidget.multiple(false);
	UploadWidget.auto(true);
});