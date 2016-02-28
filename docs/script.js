$('.imgLink').each(function() {
	var link = $(this);  
  var expando = $('<a class="expando">');
  var content = $('<div class="expandoContent">');
  var img = $('<img>').attr('src', link.attr('href'));
  
  img.appendTo(content);
  
  expando
    .on('click', function(e) {
    	expando.toggleClass('expanded');
      content.toggle();
  	})
  	.insertAfter(link);
    
  content.insertAfter(expando);
});
