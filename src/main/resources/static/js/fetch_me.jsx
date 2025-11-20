// fetchMe.js
export async function fetchMe() {
  const res = await fetch('/api/me', {
    method: 'GET',
    credentials: 'same-origin' // ensures session cookie is sent
  });
  return await res.json();
}
