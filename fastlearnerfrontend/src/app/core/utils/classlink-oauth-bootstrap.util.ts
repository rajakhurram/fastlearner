import { environment } from '../../../environments/environment.development';

function clearInvalidRedirectUrl(): void {
  const redirectUrl = localStorage.getItem('redirectUrl');
  if (!redirectUrl) {
    return;
  }

  const basePath = redirectUrl.split('?')[0];
  if (
    basePath.includes('/auth/sign-in') ||
    basePath.includes('/auth/sign-up') ||
    redirectUrl.includes('token=') ||
    redirectUrl.includes('error=')
  ) {
    localStorage.removeItem('redirectUrl');
  }
}

function getClassLinkHashParams(): URLSearchParams | null {
  const hash = window.location.hash || '';
  if (!hash.includes('auth/sign-in')) {
    return null;
  }

  const queryIndex = hash.indexOf('?');
  if (queryIndex === -1) {
    return null;
  }

  return new URLSearchParams(hash.substring(queryIndex + 1));
}

function saveClassLinkAuthToStorage(params: URLSearchParams): void {
  const token = params.get('token') || '';
  const refreshToken = params.get('refreshToken') || '';
  const expiredInSec = Number(params.get('expiredInSec') || '0');
  const response = {
    token,
    refreshToken,
    expiredInSec,
    name: params.get('name'),
    email: params.get('email'),
    role: params.get('role'),
    subscribed: params.get('subscribed') === 'true',
  };

  localStorage.setItem('token', token);
  localStorage.setItem('refresh_token', refreshToken);
  localStorage.setItem('isLoggedIn', 'true');
  localStorage.setItem('loggedInUserDetails', JSON.stringify(response));

  if (expiredInSec > 0) {
    localStorage.setItem(
      'expiresIn',
      String(Math.floor(Date.now() / 1000) + expiredInSec),
    );
  }

  if (response.role) {
    localStorage.setItem('role', response.role);
  }
}

function assignStudentRoleInBackground(token: string, hasRole: boolean): void {
  if (hasRole || !token) {
    return;
  }

  fetch(`${environment.baseUrl}user/add-role?role=STUDENT`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
    .then((response) => {
      if (response.ok) {
        localStorage.setItem('role', 'STUDENT');
      }
    })
    .catch((error) => console.error('ClassLink role assignment failed', error));
}

/**
 * Persist ClassLink auth and point the hash at the post-login route before Angular boots.
 * Avoid window.location.replace here: same-document hash changes skip bootstrap and freeze the page.
 */
function prepareClassLinkOAuthReturn(): void {
  const params = getClassLinkHashParams();
  if (!params || !params.has('token')) {
    return;
  }

  clearInvalidRedirectUrl();
  saveClassLinkAuthToStorage(params);

  const subscribed = params.get('subscribed') === 'true';
  const targetHash = subscribed ? '#/student' : '#/subscription-plan';
  const token = params.get('token') || '';

  assignStudentRoleInBackground(token, Boolean(params.get('role')));

  window.history.replaceState(
    null,
    '',
    `${window.location.origin}/${targetHash}`,
  );
}

function normalizeClassLinkOAuthCallbackUrl(): void {
  const { pathname, search, origin, hash } = window.location;
  if (hash || !search) {
    return;
  }

  const isSignInPath =
    pathname === '/auth/sign-in' || pathname.endsWith('/auth/sign-in');
  if (!isSignInPath) {
    return;
  }

  const params = new URLSearchParams(search);
  if (!params.has('token') && !params.has('error')) {
    return;
  }

  window.location.replace(`${origin}/#/auth/sign-in${search}`);
}

/** Runs before Angular bootstrap to handle ClassLink OAuth return URLs. */
export function handleClassLinkOAuthBootstrap(): void {
  clearInvalidRedirectUrl();
  normalizeClassLinkOAuthCallbackUrl();
  prepareClassLinkOAuthReturn();
}
