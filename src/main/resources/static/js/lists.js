// src/main/resources/static/js/lists.js

const { h } = preact;
const { useState, useEffect } = preactHooks;
import { Modal } from "./modal.js";

const html = htm.bind(h);

const ListsPage = ({ navigate }) => {
    const [lists, setLists] = useState([]);
    const [newListName, setNewListName] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    const [editingId, setEditingId] = useState(null);
    const [editingTitle, setEditingTitle] = useState("");

    const [deleteTarget, setDeleteTarget] = useState(null);

    const fetchLists = () => {
        fetch("/list")
            .then(res => res.json())
            .then(data => setLists(data || []));
    };

    useEffect(fetchLists, []);

    const addList = () => {
        const trimmed = newListName.trim();
        if (!trimmed) return;

        setIsSaving(true);
        fetch("/list", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ title: trimmed })
        })
            .then(() => {
                setNewListName("");
                fetchLists();
            })
            .finally(() => setIsSaving(false));
    };

    const openDeleteModal = (list) => setDeleteTarget(list);
    const cancelDelete = () => setDeleteTarget(null);

    const confirmDelete = () => {
        if (!deleteTarget) return;
        fetch(`/list/${deleteTarget.id}`, { method: "DELETE" })
            .then(() => {
                setDeleteTarget(null);
                fetchLists();
            });
    };

    const startEdit = (list) => {
        setEditingId(list.id);
        setEditingTitle(list.title || "");
    };

    const saveEdit = () => {
        const trimmed = editingTitle.trim();
        if (!editingId || !trimmed) return;

        fetch(`/list/${editingId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ title: trimmed })
        })
            .then(() => {
                setEditingId(null);
                setEditingTitle("");
                fetchLists();
            });
    };

    const goToList = (list) => navigate("list-detail", { list });

    const handleNewInput = (e) => setNewListName(e.target.value.slice(0, 50));
    const handleEditInput = (e) => setEditingTitle(e.target.value.slice(0, 50));

    const canAdd = newListName.trim().length > 0 && !isSaving;

    return html`
        <div class="flex justify-center px-4 pt-10 pb-16">
            <div class="w-full max-w-3xl lg:max-w-5xl">

                <div class="bg-white shadow-2xl rounded-2xl p-6 sm:p-8">

                    <header class="mb-8 text-center">
                        <h1 class="font-bold tracking-tight text-gray-900 text-3xl sm:text-4xl lg:text-5xl xl:text-6xl">
                            Mutual Inclusion
                        </h1>
                    </header>

                    <h2 class="text-center text-gray-800 font-semibold border-b border-gray-200 pb-3 mb-6 text-lg sm:text-xl lg:text-2xl xl:text-3xl">
                        Lists
                    </h2>

                    <div class="space-y-3">
                        ${lists.length === 0 && html`
                            <p class="text-center text-gray-400 text-sm sm:text-base lg:text-lg">No lists yet. Create your first one below.</p>
                        `}

                        ${lists.map(list => {
                            const isEditing = editingId === list.id;

                            const onRowClick = () => {
                                if (isEditing) return;
                                goToList(list);
                            };

                            return html`
                                <div key=${list.id}
                                     class="flex items-center justify-between border border-gray-200 rounded-lg px-3 py-2 cursor-pointer hover:bg-gray-50 transition"
                                     onClick=${onRowClick}>

                                    <div class="flex items-center gap-2 flex-1 min-w-0">

                                        ${isEditing ? html`
                                            <button class="w-8 h-8 rounded-md bg-green-500 text-white shadow hover:bg-green-600 flex-shrink-0"
                                                    onClick=${e => { e.stopPropagation(); saveEdit(); }}>
                                                <i class="fa-solid fa-check"></i>
                                            </button>
                                        ` : html`
                                            <button class="w-8 h-8 rounded-md bg-yellow-400 text-white shadow hover:bg-yellow-500 flex-shrink-0"
                                                    onClick=${e => { e.stopPropagation(); startEdit(list); }}>
                                                <i class="fa-solid fa-pen"></i>
                                            </button>
                                        `}

                                        ${isEditing ? html`
                                            <input value=${editingTitle}
                                                   onInput=${handleEditInput}
                                                   onClick=${e => e.stopPropagation()}
                                                   class="flex-1 min-w-0 border border-gray-300 rounded-lg px-3 py-2 text-sm sm:text-base lg:text-lg focus:ring-2 focus:ring-blue-400 outline-none" />
                                        ` : html`
                                            <span class="flex-1 truncate text-gray-800 text-sm sm:text-base lg:text-lg xl:text-xl">${list.title}</span>
                                        `}
                                    </div>

                                    <div class="flex items-center gap-2 ml-4 flex-shrink-0">
                                        <button class="w-8 h-8 rounded-md bg-blue-500 text-white shadow hover:bg-blue-600"
                                                onClick=${e => e.stopPropagation()}>
                                            <i class="fa-solid fa-share-nodes"></i>
                                        </button>

                                        <button class="w-8 h-8 rounded-md bg-red-500 text-white shadow hover:bg-red-600"
                                                onClick=${e => { e.stopPropagation(); openDeleteModal(list); }}>
                                            <i class="fa-solid fa-trash"></i>
                                        </button>
                                    </div>
                                </div>
                            `;
                        })}
                    </div>

                    <div class="mt-8 flex flex-col sm:flex-row gap-3">
                        <div class="flex-1">
                            <input maxlength="50"
                                   value=${newListName}
                                   onInput=${handleNewInput}
                                   placeholder="New list name"
                                   class="w-full border border-gray-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-blue-400 text-sm sm:text-base lg:text-lg" />
                            <p class="mt-1 text-gray-400 text-xs sm:text-sm">${newListName.length}/50</p>
                        </div>

                        <button class="rounded-lg shadow px-5 py-2 text-sm sm:text-base lg:text-lg ${canAdd ? "bg-blue-500 text-white hover:bg-blue-600" : "bg-gray-200 text-gray-400 cursor-not-allowed"}"
                                onClick=${addList}
                                disabled=${!canAdd}>
                            + Add new list
                        </button>
                    </div>
                </div>
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
                            Delete "<span class="font-semibold">${deleteTarget.title}</span>"?
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

export default ListsPage;
