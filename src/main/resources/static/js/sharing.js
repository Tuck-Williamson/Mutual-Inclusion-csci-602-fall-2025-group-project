
const { h, render } = preact;
const { useState, useEffect } = preactHooks;
import { Modal } from "./modal.js";
import { showNotification } from "./notification_banner.js";

const html = htm.bind(h);

export const ShareList = ({ navigate, list, from /*, onShareAdded*/ }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [showShareModal, setShowShareModal] = useState(false);
    const [shareLink, setShareLink] = useState(null);
    const [shares, setShares] = useState(list?.shares || []);

    // Update shares when list prop changes
    useEffect(() => {
        setShares(list?.shares || []);
    }, [list?.shares]);

    const handleShareList = async () => {
        setIsLoading(true);
        try {
            const response = await fetch(`/list/${list.id}/share`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                if (response.status === 401) {
                    showNotification( "Unauthorized: Please log in to create a share.");
                }
                else{
                    showNotification( "Error creating share: " + response.statusText);
                }
                console.error(`Failed to create share: ${response.statusText}`);
                return
            }

            const data = await response.json();

            // Add the new share to the shares array
            const newShare = {
                token: data.token,
                link: window.location + data.link,
                expiryTime: data.expiryTime,
                user: null, // Not yet accepted
            };

            const updatedShares = [...shares, newShare];
            setShares(updatedShares);
            setShareLink(newShare.link);
            setShowShareModal(true);

            // if (onShareAdded) {
            //     onShareAdded(newShare);
            // }
        } catch (error) {
            console.error('Error creating share:', error);
            showNotification( "UI Error creating share.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteShare = async (index) => {
        try {
            const share = shares[index];
            const response = await fetch(`/list/${list.id}/share/${share.token}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                if (response.status === 401) {
                    showNotification( "Unauthorized: Please log in to delete a share.");
                }
                else{
                    showNotification( "Error deleting share. " + response.statusText);
                }
                console.error(`Failed to delete share: ${response.statusText}`);
            }

            // Remove the share from the array
            const updatedShares = shares.filter((_, i) => i !== index);
            setShares(updatedShares);
        } catch (error) {
            showNotification( "UI Error deleting share.");
            console.error('Error deleting share:', error);
        }
    };

    const handleDeleteAllShares = async () => {
        if (shares.length === 0) {
            return;
        }

        const confirmDelete = window.confirm(`Are you sure you want to delete all ${shares.length} share(s)?`);
        if (!confirmDelete) {
            return;
        }

        setIsLoading(true);
        try {
            // Create a copy of shares array since we'll be modifying it
            const sharesToDelete = [...shares];

            for (const share of sharesToDelete) {
                await fetch(`/list/${list.id}/share/${share.token}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                }).catch(error => {
                    showNotification( "UI Error deleting share.");
                    console.error('Error deleting share:', error)
                });
            }

            setShares([]);
        } catch (error) {
            showNotification( "UI Error deleting shares.");
            console.error('Error deleting all shares:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCopyLink = () => {
        if (shareLink) {
            navigator.clipboard.writeText(shareLink).then(() => {
                // alert('Share link copied to clipboard!');
                showNotification( "Share link copied to clipboard!","notification");
            }).catch(err => {
                showNotification( "UI Error copying share link.");
                console.error('Failed to copy link:', err);
            });
        }
    };

    const generateQRCode = (url) => {
        // Using QR Server API for simplicity - no additional library needed
        return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(url)}`;
    };

    return html`
        <div class="sharing-container mt-6 p-4 border rounded-lg bg-gray-50">
            <h3 class="text-lg font-semibold mb-4">List Shares</h3>

            <button class="text-blue-600 hover:text-blue-800
                               text-sm sm:text-base lg:text-lg xl:text-xl"
                    onClick=${() => navigate("list-detail", { list })}>
                ← Back
            </button>
            <!-- Shares List -->
            <div class="shares-list mb-6 max-h-64 overflow-y-auto">
                ${shares.length === 0 ? html`
                    <p class="text-gray-500 text-sm">No active shares yet</p>
                ` : shares.map((share, index) => html`
                    <div class="flex items-center justify-between p-3 bg-white rounded mb-2 border border-gray-200">
                        <div class="flex-1">
                            ${share.user === null || share.user === undefined ? html`
                                <span class="text-sm text-gray-600">
                                    <i class="text-yellow-500">Pending acceptance</i>
                                </span>
                            ` : html`
                                <span class="text-sm font-medium text-gray-800">
                                    Accepted by: <strong>${share.user}</strong>
                                </span>
                            `}
                        </div>
                        <button
                            onClick=${() => handleDeleteShare(index)}
                            class="ml-2 px-3 py-1 bg-red-500 text-white text-sm rounded hover:bg-red-600 transition-colors"
                            disabled=${isLoading}
                        >
                            Delete
                        </button>
                    </div>
                `)}
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-2">
                <button
                    onClick=${handleShareList}
                    disabled=${isLoading}
                    class="flex-1 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:bg-gray-400 transition-colors font-medium"
                >
                    ${isLoading ? 'Creating share...' : 'Share List'}
                </button>
                <button
                    onClick=${handleDeleteAllShares}
                    disabled=${isLoading || shares.length === 0}
                    class="flex-1 px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 disabled:bg-gray-400 transition-colors font-medium"
                >
                    Delete All
                </button>
            </div>

            <!-- Share Link Modal -->
            ${showShareModal && html`
                <${Modal} onClose=${() => setShowShareModal(false)}>
                    <div class="text-center">
                        <h2 class="text-xl font-bold mb-4">Share List Successfully Created!</h2>
                            <div class="mb-6">
                                <p class="text-gray-600 mb-3">Share this link or QR code:</p>
                                <div class="bg-gray-100 p-4 rounded break-all text-sm font-mono mb-4">
                                    ${shareLink}
                                </div>
                                <button
                                    onClick=${handleCopyLink}
                                    class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 transition-colors font-medium"
                                >
                                    📋 Copy Link
                                </button>
                            </div>

                            <div class="flex justify-center mb-4">
                                <img
                                    src=${generateQRCode(shareLink)}
                                    alt="QR Code for sharing"
                                    class="border-2 border-gray-300 rounded"
                                />
                            </div>

                            <p class="text-xs text-gray-500 mb-3">
                                Share link expires in 5 minutes
                            </p>
                        
                        <button
                            onClick=${() => setShowShareModal(false)}
                            class="w-full px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600 transition-colors font-medium">
                            Close
                        </button>
                    </div>
                <//>
            `}
        </div>
    `;
};

export default ShareList;
