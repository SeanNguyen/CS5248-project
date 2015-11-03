(function(){
    
    var app = angular.module("app", []);
    app.controller("AppController", ["$scope", "$http", AppController]);
    
    function AppController($scope, $http) {
    	$scope.videos = [];
    	$scope.playVideo = playVideo;

    	$http.get('hlsPlayList.php')
    	.then(function(res) {
    		for (var i = res.data.length - 1; i >= 0; i--) {
    			$scope.videos.push({name: getNameFromUrl(res.data[i]), url: res.data[i]});
    		};
    	}, 
    	function() {
    		$scope.videos = [];
    	});

    	function playVideo(url) {
		    
    	}

    	function getNameFromUrl(url) {
    		var startNamePos = url.lastIndexOf("/");
    		var name = url.substring(startNamePos + 1);
    		return name;
    	}
    }
})();