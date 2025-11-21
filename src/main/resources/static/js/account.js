const { h, render, Component } = preact;
const { useState, useEffect } = preactHooks;
const html = htm.bind(h);


const AccountPage = ({ navigate }) => {
  // TODO: make this navigate to grab an actual account
    const [account, setAccount] = useState(null);
    const [isEditing, setIsEditing] = useState(false);

    const fetchAccount = () => {
        fetch('/accounts/1')
            .then(res => res.json())
            .then(setAccount);
    };

    useEffect(fetchAccount, []);

    const saveAccount = () => {
        fetch('/accounts/1', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(account)
        }).then(() => {
            setIsEditing(false);
            fetchAccount();
        });
    };

    if (!account) {
        return html`<div>Loading...</div>`;
    }

    return html`
        <div>
            <header>
                <h1>Account Details</h1>
                <a onClick=${() => navigate('lists')}>Back to Lists</a>
            </header>

            ${isEditing ? html`
                <div class="form-group">
                    <label for="username">Username</label>
                    <input id="username" type="text"
                           value=${account.username}
                           onInput=${e => setAccount({ ...account, username: e.target.value })}
                    />
                </div>

                <div class="form-group">
                    <label for="email">Email</label>
                    <input id="email" type="text"
                           value=${account.email}
                           onInput=${e => setAccount({ ...account, email: e.target.value })}
                    />
                </div>

                <button onClick=${saveAccount}>Save</button>
                <button onClick=${() => setIsEditing(false)}
                        style=${{ background: '#aaa', marginLeft: '10px' }}>
                    Cancel
                </button>
            ` : html`
                <p><strong>Username:</strong> ${account.username}</p>
                <p><strong>Email:</strong> ${account.email}</p>
                <button onClick=${() => setIsEditing(true)}>Edit</button>
            `}
        </div>
    `;
};

export default AccountPage;
