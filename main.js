window.onload = main;

function main(){
    let form = document.querySelector("#joinForm");
    let username = form.elements.username;
    let chatRoom = form.elements.chatRoom;
    let messageList = document.querySelector('#messageList');
    let socket;

    form.addEventListener('submit', (e)=>{
        e.preventDefault();
        username = form.elements.username;
        chatRoom = form.elements.chatRoom;
        // form validation. Only submit if boxes aren't null.
        if(username.value == "" || chatRoom.value == ""){
            let validation = document.querySelector('#validation');
            validation.innerHTML = "Cannot have blank value for username or chat room";
            username.addEventListener("change", ()=>{
                validation.innerHTML = "";
            });
            chatRoom.addEventListener("change", ()=>{
                validation.innerHTML = "";
            });
            return;
        }
       //connect web socket on click
       let mySocket = new WebSocket(`ws://${location.host}`);
        mySocket.onopen = (e)=>{
            let request = `join ${chatRoom.value.toLowerCase()}`;
            mySocket.send(request);
            socket = mySocket;
            makeAjaxRequest();
            mySocket.onmessage = messageHandler;
           
        }
       
      
    });

    function makeAjaxRequest(){
        let xhr = new XMLHttpRequest();
        xhr.open('GET', '/chatroom.html');
        xhr.addEventListener("load", (e)=>{
            let chatRoomPg = e.currentTarget.response;
            swapPages(chatRoomPg);

        })
        xhr.send();
    };

    function swapPages(page){
        //console.log("running swapPages")
        let row = document.querySelector('#topRow');
        row.innerHTML = page;
        editChatroomHTML();
    };

    function editChatroomHTML(){
        console.log("running edit Chat Room");
        console.log("socket :", socket);
        messageList.style.display = "block";
        let usernameChat = document.querySelector("#usernameChat");
        usernameChat.innerHTML = username.value;
        let messageForm = document.querySelector('#messageForm');
        let messageTextElem = messageForm.elements.message;
       
        messageForm.addEventListener('submit', (e)=>{
            e.preventDefault();
            console.log("sending Value");
            messageTextElem.innerHTML = "";
            socket.send(`${username.value} ${messageTextElem.value}`);
        })
    }

    function messageHandler(e){
       
        console.log("message received");
        console.log(e);
        console.log("data returned");
        console.log(e.data);
        //Stackoverflow
        let data = e.data.replace(/\n/g, "\\n").replace(/\r/g, "\\r").replace(/\t/g, "\\t")
                                 
        console.log(data);
        data = JSON.parse(data);
        buildMessageUI(data);
    };

    function buildMessageUI(data){
        let divElem = document.createElement('div');
        let messageBody = document.querySelector("#messageForm #message");
        divElem.classList.add("message");
        
        if(username.value === data.userName){
            divElem.classList.add("currentUserMessage");
        }else{
            divElem.classList.add("otherUserMessage");
        }
        divElem.innerHTML = `
            <div class = "username">
                ${data.userName}:
            </div>
            <div class = "messageText">
                ${data.message}
            <div>` ;
        
        if (messageList != null) {
            messageList.prepend(divElem);
            messageBody.value = "";
        }
    }
}