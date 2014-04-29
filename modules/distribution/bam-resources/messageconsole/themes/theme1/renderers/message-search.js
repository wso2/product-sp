var render = function(theme, data, meta, require) {
	var searchResults = require('data/searchResults.json');
	theme('index', {
//		body : [{
//
//			context : {
////				results : searchResults.results,
//                partial : 'message-search',
//                context : data.streams
//			}
//		}],
        body: [
            {
                results : searchResults.results,
                partial: 'message-search',
                context: data.streams
            }
        ],
		header : [{
			partial : 'header',
			context : require('/helpers/header.js').getNavigation(data.header.current)
		}]
	});
}; 