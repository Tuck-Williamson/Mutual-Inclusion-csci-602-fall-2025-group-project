const { h, render, Component } = preact;
const { useState, useEffect } = preactHooks;
import { Modal } from './modal.js';

const html = htm.bind(h);

export const ListDetailPage = ({ list, navigate }) => {
    const [items, setItems] = useState([]);
    const [newItemName, setNewItemName] = useState('');
    const [editingItem, setEditingItem] = useState(null);

    const fetchItems = () => {
        fetch(`/list/${list.id}`)
            .then(res => res.json())
            .then(data => setItems(data.listItems || []));
    }

    useEffect(fetchItems, [list.id]);

    const addItem = () => {
        if (!newItemName) return;
        fetch(`/list/${list.id}/item`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ listItemName: newItemName })
        }).then(() => {
            setNewItemName('');
            fetchItems();
        });
    };

    const deleteListItem = (itemId) => {
        fetch(`/list/${list.id}/item/${itemId}`, { method: 'DELETE' })
            .then(fetchItems);
    };

    const updateItem = (item, completed) => {
         fetch(`/list/${list.id}/item/${item.id}/complete?completed=${completed}`, {
            method: 'PATCH'
        }).then(fetchItems);
    }

    const saveItem = (item) => {
        fetch(`/list/${list.id}/item/${item.id}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ listItemName: item.listItemName })
        }).then(() => {
            setEditingItem(null);
            fetchItems();
        });
    };

    return html`
        <div>
            <header>
                <h1>${list.title || 'Untitled List'}</h1>
                <a onClick=${() => navigate('lists')}>Back</a>
            </header>
            <ul>
                ${items.map(item => html`
                    <li key=${item.id}>
                        <input type="checkbox" checked=${item.completed} onChange=${e => updateItem(item, e.target.checked)} />
                        <span style=${{ marginLeft: '10px', flexGrow: 1 }}>${item.listItemName}</span>
                        <button onClick=${() => setEditingItem(item)} style=${{ marginLeft: '10px' }}>Edit</button>
                        <button onClick=${() => deleteListItem(item.id)} style=${{ background: '#e53e3e' }}>Delete</button>
                    </li>
                `)}
            </ul>
            <input type="text" value=${newItemName} onInput=${e => setNewItemName(e.target.value)} placeholder="New item name" />
            <button onClick=${addItem}>Add Item</button>
            ${editingItem && html`
                <${Modal} onClose=${() => setEditingItem(null)}>
                    <h2>Edit Item</h2>
                    <input type="text" value=${editingItem.listItemName} onInput=${e => setEditingItem({ ...editingItem, listItemName: e.target.value })} />
                    <button onClick=${() => saveItem(editingItem)}>Save</button>
                <//>
            `}
        </div>
    `;
};
