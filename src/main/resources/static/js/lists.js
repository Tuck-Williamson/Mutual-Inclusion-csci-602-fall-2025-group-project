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
        <div>
            <header>
                <h1>Mutual Inclusion</h1>
                <button onClick=${() => navigate('account')}><i class="fas fa-edit"></i> Username</button>
            </header>
            <h2>Lists</h2>
            <ul>
                ${lists.map(list => html`
                    <li key=${list.id}>
                        <span onClick=${() => navigate('list-detail', list)} style=${{ flexGrow: 1, cursor: 'pointer' }}>
                            <i class="fas fa-square-plus"></i> - ${list.title || 'Untitled List'}
                        </span>
                        <button onClick=${() => setEditingList(list)} style=${{ marginLeft: '10px' }}><i class="fas fa-edit"></i> Edit</button>
                        <button onClick=${() => deleteList(list.id)} style=${{ marginLeft: '10px', background: '#e53e3e' }}><i class="fas fa-trash"></i></button>
                    </li>
                `)}
            </ul>
            <input type="text" value=${newListName} onInput=${e => setNewListName(e.target.value)} placeholder="New list name" />
            <button onClick=${addList}><i class="fas fa-square-plus"></i> Add new list</button>
            ${editingList && html`
                <${Modal} onClose=${() => setEditingList(null)}>
                    <h2>Edit List</h2>
                    <input type="text" value=${editingList.title} onInput=${e => setEditingList({ ...editingList, title: e.target.value })} />
                    <button onClick=${() => updateList(editingList)}>Save</button>
                <//>
            `}
        </div>
    `;
};
