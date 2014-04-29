var getMessages = function(searchResults, messages) {

	for(var i in searchResults){
		if(searchResults[i].type == "HL7"){
			searchResults[i].message = messages[0];
		} else {
			searchResults[i].message = messages[1];
		}
		
		
	}
	return {results:searchResults};
}
