const BASE_URL = 'http://localhost:8080';

export function getToken() {
    return localStorage.getItem('token');
}

export function getUser() {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
}

/**
 * @param {{ token: string, email: string, role: string }} data
 */
export function saveSession(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role }));
}

export function clearSession() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
}

/**
 * @param {string} path
 * @param {{ method?: string, body?: any }} [options]
 */
export async function api(path, { method = 'GET', body } = {}) {
    /** @type {Record<string, string>} */
    const headers = {};
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (body !== undefined) headers['Content-Type'] = 'application/json';

    const response = await fetch(BASE_URL + path, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined
    });

    /** @type {any} */
    let data = null;
    const text = await response.text();
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = null;
        }
    }

    if (!response.ok) {
        const message =
            data?.message || (response.status === 401 ? 'Please log in again' : 'Something went wrong');
        throw new Error(message);
    }
    return data;
}