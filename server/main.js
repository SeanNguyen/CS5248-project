(function(){
    
    var app = angular.module("app", []);
    app.controller("AppController", ["$scope", "$http", AppController]);
    
    function AppController($scope, $http) {
    	$scope.videos = [];

    	$http.get('hlsList.php')
    	.then(function(res) {
    		for (var i = res.data.length - 1; i >= 0; i--) {
    			$scope.videos.push({url: res.data[i]});
    		};
    	}, 
    	function() {
    		$scope.videos = [];
    	});
    }
})();