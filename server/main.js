(function(){
    
    var app = angular.module("app", []);
    app.controller("AppController", ["$scope", "$http", AppController]);
    
    function AppController($scope, $http) {
    	$scope.currentVideo = "SampleVideo_1080x720_5mb.mpd";
    	$scope.videos = [];
    	$scope.playVideo = playVideo;

    	$http.get('/mpdList.php')
    	.then(function(res) {
    		for (var i = res.data.length - 1; i >= 0; i--) {
    			$scope.videos.push({name: getNameFromUrl(res.data[i]), url: res.data[i]});
    		};
    	}, 
    	function() {
    		$scope.videos = [];
    	});

    	function playVideo(url) {
		    var context = new Dash.di.DashContext();
		    var player = new MediaPlayer(context);
		    player.startup();
		    player.attachView(document.querySelector("#videoPlayer"));
		    player.attachSource(url);	
    	}

    	function getNameFromUrl(url) {
    		var startNamePos = url.lastIndexOf("/");
    		var name = url.substring(startNamePos + 1);
    		return name;
    	}
    }
})();