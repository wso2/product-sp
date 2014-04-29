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
                context : require('/helpers/header.js').getNavigation(data.header.current)
            }
        ]
    });
};