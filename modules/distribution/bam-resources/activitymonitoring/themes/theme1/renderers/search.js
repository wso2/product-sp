var render = function (theme, data, meta, require) {
    theme('index', {
        body: [
            {
                partial: 'search',
                context: data.activities
            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Search',
                    activities:true,
                    breadcrumb:[
                        {link:'', name:'Search',isLink:false}
                    ]
                }
            }
        ]
    });
};