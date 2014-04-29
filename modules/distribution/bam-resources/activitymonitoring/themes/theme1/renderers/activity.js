var render = function (theme, data, meta, require) {
    theme('index', {
        body: [
            {
                partial: 'activity',
                context: data.activity
            }
        ],
        header: [
            {
                partial: 'header',
                context:{
                    title:'Activity',
                    activities:true,
                    breadcrumb:[
                        {link:'/search.jag', name:'Search',isLink:true},
                        {link:'/activities.jag', name:'Activities',isLink:true},
                        {link:'', name:'Events',isLink:false}
                    ]
                }
            }
        ]
    });
};