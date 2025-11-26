// src/main/resources/static/js/list_detail.js

const { h } = preact;
const { useState, useEffect } = preactHooks;
import { Modal } from "./modal.js";
import { subscribeToList, disconnect, isConnected } from "websocket";

const html = htm.bind(h);

const ListDetailPage = ({ list, navigate }) => {
    const [items, setItems] = useState([]);
    const [newItemName, setNewItemName] = useState("");
    const [editingItemId, setEditingItemId] = useState(null);
    const [editingItemName, setEditingItemName] = useState("");
    const [deleteTarget, setDeleteTarget] = useState(null);

    const fetchItems = () => {
        fetch(`/list/${list.id}`)
            .then(res => res.json())
            .then(data => setItems(data.listItems || []));
    };

    useEffect(fetchItems, [list.id]);

    // This is used for websocket connection. It calls the returned callback when the component is unmounted.
    useEffect(() => {

        // TODO Better initialization handling ... retries.
        if( !isConnected() ) return console.log(
            'Websockets not connected. Please refresh the page to reconnect.'
        )

        // Subscribe to the list's websocket channel. The callback just updates the UI.
        const subscription_unsub = subscribeToList(list.id, data => {
            //We are not using the data here, but we could use it to update the UI.
            console.log('Received websocket message:', data);

            // Handle special case where the list was deleted.
            if(data.event_type === 'LIST-DELETED'){
                navigate('lists');
                return;
            }
            else if(data.event_type === 'LIST-UPDATED'){
                //Just in case the title changed.
                list.title = data.title;
            }

            fetchItems();
        })

        // This function is the cleanup function, called on unmount.
        return () => {
            console.log('Websockets unsubscribing.');
            subscription_unsub();
        };
    }, []);

    const addItem = () => {
        const trimmed = newItemName.trim();
        if (!trimmed) return;

        fetch(`/list/${list.id}/item`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ listItemName: trimmed })
        }).then(() => {
            setNewItemName("");
            fetchItems();
        });
    };

    const markComplete = (item, completed) => {
        fetch(`/list/${list.id}/item/${item.id}/complete?completed=${completed}`, {
            method: "PATCH"
        }).then(fetchItems);
    };

    const openDeleteModal = (item) => setDeleteTarget(item);
    const cancelDelete = () => setDeleteTarget(null);

    const confirmDelete = () => {
        if (!deleteTarget) return;

        fetch(`/list/${list.id}/item/${deleteTarget.id}`, {
            method: "DELETE"
        }).then(() => {
            setDeleteTarget(null);
            fetchItems();
        });
    };

    const startEditItem = (item) => {
        setEditingItemId(item.id);
        setEditingItemName(item.listItemName || "");
    };

    const saveItem = () => {
        const trimmed = editingItemName.trim();
        if (!trimmed || !editingItemId) return;

        fetch(`/list/${list.id}/item/${editingItemId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ listItemName: trimmed })
        }).then(() => {
            setEditingItemId(null);
            setEditingItemName("");
            fetchItems();
        });
    };

    return html`
        <div class="space-y-8">

            <header class="flex items-center justify-between pb-2 border-b border-gray-200">
                <button class="w-8 h-8 rounded-md bg-blue-500 text-white shadow hover:bg-blue-600"
                        onClick=${e => {e.stopPropagation(); navigate("share", list); }}>
                    <i class="fa-solid fa-share-nodes"></i>
                </button>
                
                <h1 id="ListTitle" class="font-bold text-gray-900 text-2xl sm:text-3xl lg:text-4xl xl:text-5xl">
                    ${list.title || "Untitled List"}
                    
                </h1>
                
                <button class="text-blue-600 hover:text-blue-800
                               text-sm sm:text-base lg:text-lg xl:text-xl"
                        onClick=${() => navigate("lists")}>
                    ← Back to Lists
                </button>
            </header>

            <h2 class="font-semibold text-gray-800 border-b border-gray-200 pb-3
                       text-lg sm:text-xl lg:text-2xl xl:text-3xl">
                Items
            </h2>

            <div class="space-y-3">
                ${items.length === 0 && html`
                    <p class="text-gray-400 text-sm sm:text-base lg:text-lg xl:text-xl">
                        No items yet. Add one below.
                    </p>
                `}

                ${items.map(item => {
                    const isEditing = editingItemId === item.id;

                    return html`
                        <div key=${item.id}
                             class="flex items-center justify-between border border-gray-200 rounded-lg px-3 py-2">

                            <div class="flex items-center gap-3 flex-1 min-w-0">
                                <input type="checkbox"
                                       class="h-4 w-4 lg:h-5 lg:w-5"
                                       checked=${item.completed}
                                       onChange=${e => markComplete(item, e.target.checked)} />

                                ${isEditing
                                        ? html`
                                            <input value=${editingItemName}
                                                   onInput=${e => setEditingItemName(e.target.value)}
                                                   class="flex-1 min-w-0 border border-gray-300 rounded-lg px-3 py-2
                                                      text-sm sm:text-base lg:text-lg xl:text-xl
                                                      focus:ring-2 focus:ring-blue-400 outline-none" />
                                        `
                                        : html`
                                            <span class="flex-1 truncate text-gray-800
                                                     text-sm sm:text-base lg:text-lg xl:text-xl">
                                            ${item.listItemName}
                                        </span>
                                        `
                                }
                            </div>

                            <div class="flex items-center gap-2 ml-4">

                                ${isEditing
                                        ? html`
                                            <button class="w-8 h-8 lg:w-10 lg:h-10 rounded-md bg-green-500
                                                       text-white shadow hover:bg-green-600"
                                                    onClick=${saveItem}>
                                                <i class="fa-solid fa-check"></i>
                                            </button>
                                        `
                                        : html`
                                            <button class="w-8 h-8 lg:w-10 lg:h-10 rounded-md bg-yellow-400
                                                       text-white shadow hover:bg-yellow-500"
                                                    onClick=${() => startEditItem(item)}>
                                                <i class="fa-solid fa-pen"></i>
                                            </button>
                                        `
                                }

                                <button class="w-8 h-8 lg:w-10 lg:h-10 rounded-md bg-red-500
                                               text-white shadow hover:bg-red-600"
                                        onClick=${() => openDeleteModal(item)}>
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    `;
                })}
            </div>

            <div class="mt-6 flex flex-col sm:flex-row gap-3">
                <input type="text"
                       placeholder="New item name"
                       value=${newItemName}
                       onInput=${e => setNewItemName(e.target.value)}
                       class="flex-1 border border-gray-300 rounded-lg px-3 py-2
                              text-sm sm:text-base lg:text-lg xl:text-xl
                              focus:ring-2 focus:ring-blue-400 outline-none" />

                <button class="rounded-lg shadow px-5 py-2
                               bg-blue-500 text-white hover:bg-blue-600
                               text-sm sm:text-base lg:text-lg xl:text-xl"
                        onClick=${addItem}>
                    + Add item
                </button>
            </div>

            <!-- FIXED RESPONSIVE MODAL -->
            ${deleteTarget && html`
                <${Modal} onClose=${cancelDelete}>
                    <div class="text-center space-y-4">

                        <h2 class="
                            font-semibold text-gray-800
                            text-base sm:text-lg lg:text-xl xl:text-2xl
                        ">
                            Confirm Delete
                        </h2>

                        <p class="
                            text-gray-600
                            text-sm sm:text-base lg:text-lg xl:text-xl
                        ">
                            Delete "<span class="font-semibold">${deleteTarget.listItemName}</span>"?
                        </p>

                        <div class="flex justify-center gap-3 mt-2">
                            <button class="
                                px-4 py-2 rounded-lg bg-red-500 text-white
                                text-sm sm:text-base lg:text-lg xl:text-xl
                                hover:bg-red-600
                            "
                                    onClick=${confirmDelete}>
                                Yes, delete
                            </button>

                            <button class="
                                px-4 py-2 rounded-lg bg-gray-200 text-gray-700
                                text-sm sm:text-base lg:text-lg xl:text-xl
                                hover:bg-gray-300
                            "
                                    onClick=${cancelDelete}>
                                No
                            </button>
                        </div>

                    </div>
                <//>
            `}
        </div>
    `;
};

export default ListDetailPage;
