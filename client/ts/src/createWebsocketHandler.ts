
type HandlerFn = (data: { [key: string]: any }) => void;

interface  WebsocketHandler {
  sendRpc: (msg: { [key: string]: any }) => Promise<any>;
  on: (msgId: string, callback: HandlerFn) => void;
}

let rpcIdGenerator = 1;

const createWebsocketHandler = (
  socket: WebSocket,
  onClose: (didReceiveAtLeastOneMessage: boolean) => void,
): WebsocketHandler => {
  const pendingCallbacks: { [id: string]: HandlerFn } = {};
  const messageHandlers: { [opName: string]: HandlerFn } = {
    rpc: ({ id, ...res }) => {
      const handler = pendingCallbacks[id];
      if (handler) {
        delete pendingCallbacks[id];
        handler(res);
      } else {
        console.warn('Received RPC response for', id, ', expected one of', Object.keys(pendingCallbacks));
      }
    },
  };

  let didReceiveAtLeastOneMessage = false;
  socket.addEventListener('message', function (event) {
    didReceiveAtLeastOneMessage = true;
    // console.log('Message from server ', event.data);
    const parsed = JSON.parse(event.data);
    if (messageHandlers[parsed.type]) {
      messageHandlers[parsed.type](parsed);
    } else {
      console.log('No handler for message', parsed, ', got handlers for', Object.keys(messageHandlers));
    }
  });
  socket.addEventListener('close', () => {
    // Small timeout to reduce risk of it appearing when navigating away
    setTimeout(() => onClose(didReceiveAtLeastOneMessage), 100);
  });

  return {
    on: (id, cb) => messageHandlers[id] = cb,
    sendRpc: (msg) => new Promise(async (res, rej) => {
      const id = rpcIdGenerator++;
      socket.send(JSON.stringify({
        ...msg,
        id,
      }));

      const cleanup = () => delete pendingCallbacks[id];
      pendingCallbacks[id] = ({ error, result }) => {
        cleanup();
        if (error) {
          console.warn('RPC request failed', error);
          rej(error);
        } else {
          res(result);
        }
      };

      setTimeout(() => {
        cleanup();
        rej('Timeout');
      }, 30000);
    }),
  };
};

export { WebsocketHandler }
export default createWebsocketHandler;