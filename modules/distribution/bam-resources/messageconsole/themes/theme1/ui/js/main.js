function appendProps(selProp){

    var stype = $('#streamName').val();

    $.post('api/messages.jag',
        {operation: "getProperties" ,
            stype: stype},
        function(result){
            var obj = $.parseJSON(result);
             var selected = selProp.find('option:first-child').html();
            for(var i in obj.streamProperties){

                if( obj.streamProperties[i].streamProperty ==  selected) continue;

                selProp.append('<option value="\'' + obj.streamProperties[i].streamID + '\'.\''
                    + obj.streamProperties[i].streamProperty +  '\'">' + obj.streamProperties[i].streamProperty + '</option>');

            }
        });

    selProp.parents('.search-if-row').first().find('select').not('.sel-prop').each(function(){
        var selected = $(this).val();
        $(this).find('option').each(function(){
           if($(this).val().trim() == selected.trim() && typeof $(this).attr('selected') == 'undefined' ) {
               $(this).remove();

           }
        });
    })  ;


  //  selProp.parents('.search-if-row').find('.sel-op').find('option:not([selected])').remove();
}

function populateHiddenProps(){

    var stype = $('#streamName').val();
    var selProp = $('#search-if-row-original').find('.sel-prop');

    $.post('api/messages.jag',
        {operation: "getProperties" ,
            stype: stype},
        function(result){
            var obj = $.parseJSON(result);
           //lll var selected = selProp.find('option:first-child').html();
            for(var i in obj.streamProperties){

               // if( obj.streamProperties[i].streamProperty ==  selected) continue;

                selProp.append('<option value="\'' + obj.streamProperties[i].streamID + '\'.\''
                    + obj.streamProperties[i].streamProperty +  '\'">' + obj.streamProperties[i].streamProperty + '</option>');

            }
        });
}

$(document).ready(function() {
    var id = 0;


	$('#btn-new-search-row').click(function() {

        $('.sel-prop').each(function(){
            $(this).select2("destroy");
        });


		var row = $('#search-if-row-original');

		var newRow = row.clone();
        //$('<div/>').addClass('row search-if-row').append(row.html());
        newRow[0].id = 'newRow_' + id;
        newRow.attr('data-rowId', id);
        newRow.addClass('search-if-row');
        newRow.show();

		row.before(newRow);

        newRow.find('select').select2();

        id++;

        setTimeout(function(){
            $('.sel-prop').select2();
        },100);

	});

	$('#collapse-search').click(function() {
		var container = $('.content-section-wrapper');
		var button = $('#collapse-search-clicked');
		container.slideUp(function() {
			button.show();
		});
	});

	$('body').on('click', '.btn-remove-search-if', function(e) {
		e.preventDefault();
		$(this).closest('.row').fadeOut('fast', function() {

			$(this).remove();
		});
	});

	$('#btn-back').click(function() {
		history.go(-1);
	});

	$('body').on('click', '.btn-show-msg', function(e) {
		e.preventDefault();
		$(this).closest('table').find('.message-result-row').slideToggle();
		
		if ($(this).attr('data-expand') === "true") {
			$(this).attr('data-expand', "false");
			$(this).html('<i class="icon-chevron-down"></i> Expand Message');
		} else {
			$(this).attr('data-expand', "true");
			$(this).html('<i class="icon-chevron-up"></i> Collapse Message');
		}
	});
	
	$('.accordion-toggle').click(function(){
		var icon = $(this).find('i');
		if(icon.hasClass('icon-chevron-down')){
			icon.removeClass().addClass('icon-chevron-up');
		} else {
			icon.removeClass().addClass('icon-chevron-down');
		}
	});

   $('#btn-clear').click(function(e){
       e.preventDefault();
       localStorage['message-search-fields'] = "";
       $('.search-if-row').remove();
   });

    $('#streamName').change(function(){
        $('.sel-prop').each(function(){
            $(this).select2("destroy");
        });
        var stype = $(this).val();

        $.post('api/messages.jag',
            {operation: "getProperties" ,
             stype: stype},
        function(result){
           var obj = $.parseJSON(result);
            $('.sel-prop').html("");
            for(var i in obj.streamProperties){
//                $('.sel-prop').append('<option>' + obj.streams[i].streamID + '</option>');zz
                $('.sel-prop').append('<option value="\'' + obj.streamProperties[i].streamID + '\'.\''
                    + obj.streamProperties[i].streamProperty +  '\'">' + obj.streamProperties[i].streamProperty + '</option>');

//                <option value="{{this.streamID}}">{{this.streamID}}</option>
            }
           setTimeout(function(){
               $('.sel-prop').select2();
           },100);

        });
    });

   // $('.sel-prop').select2();
   //  $('#streamName').select2();

	hljs.initHighlightingOnLoad();

});
