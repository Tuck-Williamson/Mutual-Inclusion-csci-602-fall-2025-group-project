export async function logout() {
  // If you used CookieCsrfTokenRepository.withHttpOnlyFalse() the token is readable from cookie
  // Or fetch /csrf endpoint to get the token object (shown above).
  const tokenResp = await fetch('/csrf', { credentials: 'same-origin' });
  const tokenJson = await tokenResp.json();
  const csrf = tokenJson._csrf;

  await fetch('/logout', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrf,
      'Content-Type': 'application/json'
    }
  });

  // redirect / update UI
  window.location.href = '/';
}
