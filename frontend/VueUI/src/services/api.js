export class ApiError extends Error {
  constructor(message, status, payload) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.payload = payload
  }
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const AUTH_STORAGE_KEY = 'book-area-auth-session'

function canUseStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function normalizeToken(token) {
  const value = typeof token === 'string' ? token.trim() : ''
  return value && value !== 'null' && value !== 'undefined' ? value : ''
}

function buildUrl(path) {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}

function normalizeBody(body) {
  if (
    body == null ||
    typeof body === 'string' ||
    body instanceof FormData ||
    body instanceof URLSearchParams ||
    body instanceof Blob
  ) {
    return body
  }

  return JSON.stringify(body)
}

function readStoredSession() {
  if (!canUseStorage()) {
    return null
  }

  const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw)
    const accessToken = normalizeToken(parsed.accessToken ?? parsed.token)

    if (!accessToken || !parsed.user) {
      return null
    }

    return {
      accessToken,
      token: accessToken,
      refreshToken: parsed.refreshToken ?? '',
      tokenType: parsed.tokenType ?? 'Bearer',
      user: parsed.user,
    }
  } catch {
    return null
  }
}

export function setStoredSession(session) {
  if (!canUseStorage()) {
    return session
  }

  const accessToken = normalizeToken(session?.accessToken ?? session?.token)

  if (!accessToken || !session?.user) {
    window.localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }

  const normalized = {
    accessToken,
    token: accessToken,
    refreshToken: session.refreshToken ?? '',
    tokenType: session.tokenType ?? 'Bearer',
    user: session.user,
  }

  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(normalized))
  return normalized
}

export function getStoredSession() {
  return readStoredSession()
}

export function getStoredUser() {
  return readStoredSession()?.user ?? null
}

export function getToken() {
  return readStoredSession()?.accessToken ?? ''
}

export function clearStoredSession() {
  if (canUseStorage()) {
    window.localStorage.removeItem(AUTH_STORAGE_KEY)
  }
}

export function authHeaders(headers = {}, token = getToken()) {
  const normalized = new Headers(headers)
  const accessToken = normalizeToken(token)

  if (accessToken) {
    normalized.set('Authorization', `Bearer ${accessToken}`)
  } else {
    normalized.delete('Authorization')
  }

  return normalized
}

export async function apiFetch(path, init = {}) {
  const headers = new Headers(init.headers)
  const body = normalizeBody(init.body)

  if (body != null && !(body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(buildUrl(path), {
    ...init,
    headers,
    body,
  })

  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

  if (!response.ok) {
    const message =
      typeof payload === 'object' && payload !== null && 'message' in payload
        ? String(payload.message)
        : response.statusText || 'Request failed'

    throw new ApiError(message, response.status, payload)
  }
  if (payload && typeof payload === 'object' && payload.status === 'success' && 'data' in payload) {
    return payload.data
  }

  return payload
}

function normalizeAuthResponse(payload) {
  const accessToken = payload?.accessToken ?? payload?.token ?? ''
  const user = payload?.user ?? (
    payload?.userId && payload?.email
      ? {
          id: payload.userId,
          email: payload.email,
          fullName: payload.fullName ?? '',
          phone: payload.phone ?? '',
          roles: Array.isArray(payload.roles) ? payload.roles : [],
        }
      : null
  )

  return {
    accessToken,
    token: accessToken,
    refreshToken: payload?.refreshToken ?? '',
    tokenType: payload?.tokenType ?? 'Bearer',
    user,
  }
}

export async function login(credentials) {
  const payload = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: credentials,
  })

  const session = normalizeAuthResponse(payload)
  setStoredSession(session)
  return session
}

export async function register(payload) {
  return apiFetch('/api/auth/register', {
    method: 'POST',
    body: payload,
  })
}

export async function getCurrentUser() {
  const token = getToken()

  if (!token) {
    throw new ApiError('Unauthorized', 401, null)
  }

  return apiFetch('/api/auth/me', {
    headers: authHeaders(),
  })
}

export function logout() {
  clearStoredSession()
}

export async function getBooks() {
  const payload = await apiFetch('/api/books')
  if (payload && payload.content && Array.isArray(payload.content)) {
    return payload.content
  }
  return Array.isArray(payload) ? payload : []
}

export function getBookBySlug(slug) {
  return apiFetch(`/api/books/${slug}`)
}

export function updateCurrentUserProfile(payload) {
  return apiFetch('/api/auth/me', {
    method: 'PATCH',
    headers: authHeaders(),
    body: payload,
  })
}

export function getCart() {
  return apiFetch('/api/cart', { headers: authHeaders() })
}

export function addCartItem(editionId, quantity = 1) {
  return apiFetch('/api/cart/items', {
    method: 'POST',
    headers: authHeaders(),
    body: { editionId, quantity },
  })
}

export function updateCartItem(itemId, quantity) {
  return apiFetch(`/api/cart/items/${itemId}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: { quantity },
  })
}

export function removeCartItem(itemId) {
  return apiFetch(`/api/cart/items/${itemId}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
}

export function getCheckoutOptions() {
  return apiFetch('/api/checkout/options', { headers: authHeaders() })
}

export function createOrder(payload) {
  return apiFetch('/api/orders', {
    method: 'POST',
    headers: authHeaders(),
    body: payload,
  })
}

export function getPaymentStatus(orderCode) {
  return apiFetch(`/api/payments/${encodeURIComponent(orderCode)}/status`, { headers: authHeaders() })
}

export function getOrderHistory(page = 0, size = 10) {
  return apiFetch(`/api/orders?page=${page}&size=${size}`, { headers: authHeaders() })
}

export function getReadingUrl(editionId) {
  return apiFetch(`/api/reading/${editionId}/url`, { headers: authHeaders() })
}

export async function getUserLibrary(page = 0, size = 20) {
  const payload = await apiFetch(`/api/library?page=${page}&size=${size}`, { headers: authHeaders() })
  return Array.isArray(payload?.content) ? payload.content : []
}

export function getFileUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  let cleanPath = path.startsWith('/') ? path.substring(1) : path

  // Xử lý các định dạng ảnh/PDF cũ và mới
  if (cleanPath.startsWith('covers/')) {
    // Định dạng mới, không cần prefix 'sach/'
    // cleanPath = cleanPath
  } else if (cleanPath.startsWith('anhbia/')) {
    cleanPath = `sach/${cleanPath}`
  } else if (cleanPath.startsWith('book/')) {
    cleanPath = `sach/${cleanPath.substring('book/'.length)}`
  }

  return `http://localhost:9000/book-area-files/${cleanPath}`
}
