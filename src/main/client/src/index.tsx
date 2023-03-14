import React from 'react';
import ReactDOM from 'react-dom/client';
import CountbackApp from './CountbackApp';
import { WebWorkerRequest, WebWorkerResponse } from "./types";

let webWorker: Worker | null = null;

const uuid = () => {
  try {
    return crypto.randomUUID();
  } catch (e) {
    return Math.random().toString().slice(2,11)
  }
}

const getWebWorker = (): Promise<Worker> => {
  if (webWorker) {
    return Promise.resolve(webWorker);
  } else {
    const ww = new Worker("/countback.js");
    return new Promise<Worker>((resolve, reject) => {
      const handleEvent = (event: Event) => {
        try {
          if (event instanceof MessageEvent) {
            const data: WebWorkerResponse = JSON.parse(event.data);
            if (data.type === "ready") {
              ww.removeEventListener("message", handleEvent);
              webWorker = ww;
              resolve(ww);
            }
          }
        } catch (e: any) {
          reject(e.toString());
        }
      }
      ww.addEventListener("message", handleEvent);
    });
  }
}

export const sendWwMessage = async (request: WebWorkerRequest): Promise<WebWorkerResponse> => {
  let id = "UNSET";
  try {
    const ww = await getWebWorker();
    id = uuid();
    const promise: Promise<WebWorkerResponse> = new Promise((resolve, reject) => {
      const handleEvent = (event: Event) => {
        try {
          if (event instanceof MessageEvent) {
            const data: WebWorkerResponse = JSON.parse(event.data);
            if (data.id === id) {
              ww.removeEventListener("message", handleEvent);
              resolve(data);
            }
          }
        } catch (e: any) {
          reject(e.toString());
        }
      }
      ww.addEventListener("message", handleEvent);
    });
    ww.postMessage(JSON.stringify({ ...request, id }));
    return promise;
  } catch (e: any) {
    return Promise.reject(`Failed to communicate with Web Worker: ${e.toString()}`);
  }
}

export const sendSwMessageMultipart = async (
  callback: (result: WebWorkerResponse) => void,
  request: WebWorkerRequest
) => {
  let id = "UNSET";
  try {
    const ww = await getWebWorker();
    id = uuid();
    const handleEvent = (event: Event) => {
      try {
        if (event instanceof MessageEvent) {
          const data: WebWorkerResponse = JSON.parse(event.data);
          if (data.id === id) {
            if (
              data.type === "error" ||
              data.type === "end"
            ) {
              ww.removeEventListener("message", handleEvent);
            }
            callback(data);
          }
        }
      } catch (e: any) {
        callback({
          type: "error",
          id,
          message: `Failed to handle response from Wev Worker: ${e.toString()}`,
          stackTrace: null
        })
      }
    }
    ww.addEventListener("message", handleEvent);
    ww.postMessage(JSON.stringify({ ...request, id }));
  } catch (e: any) {
    callback({
      type: "error",
      id,
      message: `Failed to communicate with Web Worker: ${e.toString()}`,
      stackTrace: null
    })
  }
}

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <CountbackApp/>
);