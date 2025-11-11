const { h, render, Component } = preact;
const { useState, useEffect } = preactHooks;
const html = htm.bind(h);

export const Modal = ({ children, onClose }) => {
    return html`
                <div class="modal" onClick=${onClose}>
                    <div class="modal-content" onClick=${e => e.stopPropagation()}>
                        ${children}
                    </div>
                </div>
            `;
};
