var stompClient = null;

function connect() {
    var socket = new SockJS('/my-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/admin-updates', function (message) {
            // Handle the received update message
        });
    });
}
