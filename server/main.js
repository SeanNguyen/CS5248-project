(function(){
    var url = "video_repo/SampleVideo_1080x720_5mb/SampleVideo_1080x720_5mb.mpd";
    var context = new Dash.di.DashContext();
    var player = new MediaPlayer(context);
    player.startup();
    player.attachView(document.querySelector("#videoPlayer"));
    player.attachSource(url);
})();