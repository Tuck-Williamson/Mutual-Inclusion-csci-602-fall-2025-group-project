const { h } = preact;
const html = htm.bind(h);

export const Modal = ({ children, onClose }) => {
    return html`
        <div
                class="fixed inset-0 z-50 flex items-center justify-center
                   bg-black bg-opacity-40 backdrop-blur-sm"
                onClick=${onClose}
        >
            <div
                    class="
                    bg-white rounded-xl shadow-xl
                    w-11/12
                    sm:w-10/12
                    md:w-3/4
                    lg:w-1/2
                    xl:w-1/3
                    p-4 sm:p-6 lg:p-8 xl:p-10
                    transition-all
                "
                    onClick=${e => e.stopPropagation()}
            >
                <div class="text-sm sm:text-base lg:text-lg xl:text-xl space-y-4">
                    ${children}
                </div>
            </div>
        </div>
    `;
};
