const { h } = preact;
const { useState, useEffect } = preactHooks;
const html = htm.bind(h);
import {getCookie} from  './utils/cookie.js';


const UserPill = ({ navigate }) => {

  const [username, setUsername] = useState("Loading...");
  const [isLoggedIn, setIsLoggedIn] = useState(false);


 useEffect(async () => {
    await fetch('/api/user')
      .then(response => {
        if (response.ok) {
          return response.json();
        }
        // Not OK => 401 Unauthorized
        setIsLoggedIn(false);
        setUsername("Guest");
        return null;
      })
      .then(data => {
        if (data && data.name /*TODO: can we use data?.name for brevity? */) {
            console.log("User is logged in as:", data);
          setUsername(data.name);
          setIsLoggedIn(true);
        }
      })
      .catch(error => {
        console.error("Error fetching user status:", error);
        setUsername("Error");
      })
  }, []);

  const checkLogin = () => {
      return isLoggedIn && username !== "Guest";
    }

  const handleLogout = async () => {
      const csrfToken = getCookie('XSRF-TOKEN');
      console.log("CSRF token: " + csrfToken);
    console.log("Logging out...");
    await fetch('/logout', {
        method: 'POST',
        headers: {
            // May want to add CSRF token here if enabled
            'X-XSRF-TOKEN': csrfToken,
            'Content-Type': 'application/json'
        },
        // No body needed for logout
        credentials: 'include' // Include cookies,
    }).then(response => {
        console.log("Logout response: ", response);
        if (response.ok) {
            console.log("Logout successful.");
            setIsLoggedIn(false);
            setUsername("Guest");
            window.location.reload();// TODO: Better way to update UI after logout?
        }
    }).catch(error => {
        console.error("Error during logout:", error);
    });
  }

  const authorizeWithGithub = () => {
     console.log("Authorizing with GitHub...");
    // This is all we really need to do for an SPA
    window.location.href = "/oauth2/authorization/github";
  }

    return html`
        <button
            onClick="${ checkLogin() ? handleLogout : authorizeWithGithub }"
            id="username-pill"
            class="px-4 py-1.5 rounded-full shadow-lg text-white
               text-sm font-semibold
               lg:px-6 lg:py-2 lg:text-base
               hover:opacity-90 transition
               ${isLoggedIn ? 'bg-blue-600' : 'bg-green-600'}">
            <i class="fa-solid fa-user mr-2"></i> ${username}
        </button>
    `;
};

export default UserPill;
