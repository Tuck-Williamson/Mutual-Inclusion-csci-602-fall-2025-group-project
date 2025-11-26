const { h, render } = preact;
const { useState, useEffect } = preactHooks;

const html = htm.bind(h);

export const Notification = ({message, type, id}) => {
    const [visible, setVisible] = useState(true);
    const [shouldRender, setShouldRender] = useState(true);

    const icon = type === 'error' ? 'fa-circle-exclamation' : 'fa-circle-info';
    const bgColor = type === 'error' ? 'bg-red-800' : 'bg-purple-800';
    const textColor = type === 'error' ? 'text-white' : 'text-gray-50';
    const borderColor = type === 'error' ? 'border-red-500' : 'border-purple-500';

    useEffect(() => {
        const timeout = setTimeout(() => {
            setVisible(false);
            setTimeout(() => setShouldRender(false), 1000);
        }, 10000);

        return () => {
            clearTimeout(timeout);
            document.getElementById(id).remove();
        };
    }, []);

    if (!shouldRender) return null;

    return html`
            <div class="fixed top-8 left-4 max-md:top-10 w-5/6 transition-opacity duration-1000 ${visible ? 'opacity-100' : 'opacity-0'}">
                <div class="${bgColor} border-2 font-bold ${borderColor} rounded-lg p-4 shadow-lg grow items-center space-x-2 ${textColor}">
                    <i class="fas ${icon}"></i>
                    <span>${message}</span>
                </div>
            </div>
        `;
};

export const showNotification = (message, type = 'error') => {
    const notificationId = `notification-${Date.now()}`;
    const container = document.getElementById("banner-root");
    const notificationElement = document.createElement('div');
    notificationElement.id = notificationId;
    container.appendChild(notificationElement);
    render(
        html`
            <${Notification} message=${message} type=${type} id=${notificationId}/>`,
        notificationElement
    );
};
export default showNotification;