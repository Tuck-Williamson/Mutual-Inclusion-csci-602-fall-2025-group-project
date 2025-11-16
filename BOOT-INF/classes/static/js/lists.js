const { h } = preact;
const { useState, useEffect } = preactHooks;
import { Modal } from './modal.js';

const html = htm.bind(h);

export const ListsPage = ({ navigate }) => {
    const [lists, setLists] = useState([]);
    const [newListName, setNewListName] = useState('');
    const [editingList, setEditingList] = useState(null);

    const fetchLists = () => {
        fetch('/list')
            .then(res => res.json())
            .then(setLists);
    };

    useEffect(fetchLists, []);

    const addList = () => {
        if (!newListName) return;
        fetch('/list', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title: newListName })
        }).then(() => {
            setNewListName('');
            fetchLists();
        });
    };

    const deleteList = (listId) => {
        fetch(`/list/${listId}`, { method: 'DELETE' })
            .then(fetchLists);
    };

    const updateList = (list) => {
        fetch(`/list/${list.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title: list.title })
        }).then(() => {
            setEditingList(null);
            fetchLists();
        });
    };

    return html`
        <div class="min-h-screen flex items-start sm:items-center justify-center">
            <div class="w-full max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                <div class="bg-white shadow-lg rounded-lg p-6 w-full">

                    <header class="flex items-center justify-between mb-4">
                        <h1 class="text-2xl font-bold">Mutual Inclusion</h1>
                        <button class="px-3 py-1 bg-blue-500 text-white rounded" onClick=${() => navigate('account')}>
                            <i class="fas fa-user"></i> Username
                        </button>
                    </header>

                    <h2 class="text-xl font-semibold mb-2">Lists</h2>

                    <ul class="space-y-2 mb-4">
                        ${lists.map(list => html`
                            <li key=${list.id} class="flex items-center justify-between py-2">

                                <!-- TITLE (auto truncation for responsiveness) -->
                                <span
                                        onClick=${() => navigate('list-detail', list)}
                                        class="flex-grow cursor-pointer truncate"
                                        style="max-width: 60%;"
                                >
                                    <i class="fas fa-list"></i> - ${list.title || 'Untitled List'}
                                </span>

                                <!-- BUTTON GROUP -->
                                <div class="flex items-center gap-3 ml-4 shrink-0">

                                    <!-- SHARE (blue) -->
                                    <button
                                            class="px-3 py-2 bg-blue-500 text-white rounded"
                                            onClick=${() => console.log("Share feature not implemented yet")}
                                    >
                                        <i class="fas fa-share"></i>
                                    </button>

                                    <!-- DELETE (red) -->
                                    <button
                                            class="px-3 py-2 bg-red-500 text-white rounded"
                                            onClick=${() => deleteList(list.id)}
                                    >
                                        <i class="fas fa-trash"></i>
                                    </button>

                                    <!-- EDIT (yellow) -->
                                    <button
                                            class="px-3 py-2 bg-yellow-400 text-white rounded"
                                            onClick=${() => setEditingList(list)}
                                    >
                                        <i class="fas fa-edit"></i>
                                    </button>

                                </div>
                            </li>
                        `)}
                    </ul>

                    <div class="flex items-center gap-2">
                        <input
                                type="text"
                                value=${newListName}
                                onInput=${e => setNewListName(e.target.value)}
                                placeholder="New list name"
                                class="flex-grow border rounded px-2 py-1"
                        />
                        <button class="px-3 py-1 bg-blue-500 text-white rounded" onClick=${addList}>
                            <i class="fas fa-plus"></i> Add new list
                        </button>
                    </div>

                    ${editingList && html`
                        <${Modal} onClose=${() => setEditingList(null)}>
                            <h2 class="text-xl font-semibold mb-2">Edit List</h2>
                            <input
                                    type="text"
                                    value=${editingList.title}
                                    onInput=${e => setEditingList({ ...editingList, title: e.target.value })}
                                    class="border rounded px-2 py-1 w-full mb-2"
                            />
                            <button class="px-3 py-1 bg-blue-500 text-white rounded" onClick=${() => updateList(editingList)}>
                                Save
                            </button>
                        <//>
                    `}
                </div>
            </div>
        </div>
    `;
};
