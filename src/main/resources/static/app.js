let stompClient = null;

function connect() {
    let socket = new SockJS('/my-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/admin/info', function (message) {
            handleUpdate(JSON.parse(message.body));
        });
    });
}

function handleUpdate(adminObj) {
    let top10SearchesElement = document.getElementById("top10Searches");
    let barrelsElement = document.getElementById("barrels");
    let downloadersElement = document.getElementById("downloaders");
    top10SearchesElement.innerText = adminObj.top10Searches;
    barrelsElement.innerText = adminObj.barrels;
    downloadersElement.innerText = adminObj.downloaders;
}

window.addEventListener('load', function () {
    connect();
});
