RecipeTracker.GetRecipeItems = function() {
    $.ajax({
        url: '/services',
        dataType: 'json',
        success : function(data) {
            for (var i = 0, len = data.length; i < len; i++) {
                RecipeTracker.recipesController.addItem(RecipeTracker.Recipe.create({
                    name: data[i]['name'],
                    ip: data[i]['ip'],
                    description: data[i]['description']
                }));
            }
        } });
};
