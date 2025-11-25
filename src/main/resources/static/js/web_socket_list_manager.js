/**
 * WebSocket utilities for STOMP-based list updates
 * Usage:
 *   const unsubscribe = subscribeToList(listId, (update) => {
 *     console.log("List update received:", update);
 *   });
 *   // Later: unsubscribe();
 */

import {showNotification} from "./notification_banner.js";


let stompClient = null;
const subscriptions = {};

/**
 * Initialize WebSocket connection
 */
export function initWebSocket() {
    return new Promise((resolve, reject) => {
        if (stompClient && stompClient.connected) {
            resolve();
            return;
        }

        // Use SockJS for better browser compatibility
        const socket = new SockJS('/ws/lists');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, 
            (frame) => {
                console.log('Connected:', frame);
                resolve();
            },
            (error) => {
                showNotification("Connection Error: Please refresh the page.");
                console.error('Connection error:', error);
                reject(error);
            }
        );
    });
}

/**
 * Subscribe to updates for a specific list
 * @param {number} listId - The ID of the list to subscribe to
 * @param {function} callback - Function called when updates are received
 * @returns {function} Unsubscribe function
 */
export function subscribeToList(listId, callback) {
    if (!isConnected()) {
        console.error('WebSocket not connected. Call initWebSocket() first.');
        // TODO: Maybe do better error handling here? Throw an error?
        showNotification("Error: WebSocket not connected. Please refresh the page.");
        return () => {};
    }

    const topic = `/topic/list/${listId}`;
    
    // Store subscription to prevent duplicates
    if (subscriptions[listId]) {
        console.warn(`Already subscribed to list ${listId}.`);
    }

    // TODO: Clean this up. If promise based then make rest of function async.
    let subscription = stompClient.subscribe(topic, (message) => {
        try {
            const update = JSON.parse(message.body);
            callback(update);
        } catch (e) {
            console.error('Failed to parse message:', e);
        }
    });

    subscriptions[listId] = subscription;

    // Return unsubscribe function
    return () => {
        if (subscription && subscription.unsubscribe) {
            subscription.unsubscribe()
            console.log('Unsubscribed from:', topic)
            delete subscriptions[listId];
        }
    };
}

/**
 * Unsubscribe from all list topics
 */
export function unsubscribeAll() {
    Object.keys(subscriptions).forEach(listId => {
        if (subscriptions[listId]) {
            const cur_sub = subscriptions[listId];
            delete subscriptions[listId];
            return cur_sub.unsubscribe();
        }
    });
    subscriptions.length = 0;
}

/**
 * Disconnect WebSocket
 */
export function disconnect() {
    if (stompClient && stompClient.connected) {
        unsubscribeAll();
        stompClient.disconnect(() => {
            console.log('Disconnected from WebSockets.');
        });
    }
}

/**
 * Get connection status
 */
export function isConnected() {
    return stompClient && stompClient.connected;
}
