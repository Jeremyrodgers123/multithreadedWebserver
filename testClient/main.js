window.onload = main;

let lrgString = `16BitsADFASDFASDFasd16BitsADFASDFASDFasd16BitsADFASDFASDFasd16BitsADFASDFASDFasd16BitsADFASDFASDFasd16BitsADFASDFASDFasd16BitsADFASDFASDFasd`



function main(){
    let messageInput = document.querySelector("#message");
    let sendMsgBtn = document.querySelector("#send");
    let joinStmt = "join msd"
    console.log("working");
    //Open up web socket connection 
    let webSocket = new WebSocket(`ws://${location.host}`);
    webSocket.onopen = (response)=>{
        console.log("websocket open");
        //console.log("response", response);
        webSocket.onmessage = messageHandler;
        webSocket.onerror = handleError;
        webSocket.send(joinStmt);
    }
    


    function messageHandler(e){
        //console.log("message received")
        //console.log(e);
        console.log(e.data);
       // setInterval(webSocket.send(lrgString), 200);
    };
    //on connect send a message
    function handleError(e){
        console.log(e);
    }

    sendMsgBtn.addEventListener('click', (e)=>{
        webSocket.send(messageInput.value);
    })
}