var render = function (theme, data, meta, require) {
    theme('index', {
        body: [
            {
                partial: 'activity-search',
                context: data.activities
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