<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { authIsAdmin, authUser, logout } from '../stores/auth'

const router = useRouter()
const dropdownOpen = ref(false)
const dropdownRef = ref(null)

const displayName = computed(() => authUser.value?.fullName || authUser.value?.email || 'Tài khoản')
const displayEmail = computed(() => authUser.value?.email || '')
const avatarLabel = computed(() => {
  const source = authUser.value?.fullName || authUser.value?.email || 'BA'

  return source
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0))
    .join('')
    .toUpperCase()
})

function navigateTo(path) {
  dropdownOpen.value = false
  router.push(path)
}

function handleLogout() {
  dropdownOpen.value = false
  logout()
  router.push('/login')
}

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function handleClickOutside(event) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    dropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="premium-dropdown" ref="dropdownRef">
    <button
      class="premium-dropdown__trigger"
      :class="{ 'is-active': dropdownOpen }"
      type="button"
      @click="toggleDropdown"
      aria-expanded="dropdownOpen"
    >
      <span class="premium-avatar">{{ avatarLabel }}</span>

      <span class="premium-meta">
        <span class="premium-name">{{ displayName }}</span>
        <span v-if="displayEmail" class="premium-email">{{ displayEmail }}</span>
      </span>
      
      <svg class="premium-chevron" :class="{ 'is-open': dropdownOpen }" viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </button>

    <transition name="dropdown-fade">
      <ul v-if="dropdownOpen" class="premium-dropdown__menu">
        <li v-if="authIsAdmin">
          <button type="button" class="premium-item" @click="navigateTo('/admin')">
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" class="premium-icon">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="3" y1="9" x2="21" y2="9"></line>
              <line x1="9" y1="21" x2="9" y2="9"></line>
            </svg>
            Dashboard / Admin
          </button>
        </li>
        <li>
          <button type="button" class="premium-item" @click="navigateTo('/library')">
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" class="premium-icon">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            </svg>
            Thư viện
          </button>
        </li>
        <li>
          <button type="button" class="premium-item" @click="navigateTo('/profile')">
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" class="premium-icon">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            Thông tin cá nhân
          </button>
        </li>
        <li><hr class="premium-divider"></li>
        <li>
          <button type="button" class="premium-item premium-item--danger" @click="handleLogout">
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" class="premium-icon">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            Đăng xuất
          </button>
        </li>
      </ul>
    </transition>
  </div>
</template>

<style scoped>
/* 
  DESIGN-TASTE-FRONTEND-V1: HIGH-END BENTO/VERCEL AESTHETIC 
*/
.premium-dropdown {
  position: relative;
  display: inline-block;
  font-family: 'Geist', 'SF Pro Display', 'Inter', system-ui, sans-serif;
}

/* --- Trigger Button --- */
.premium-dropdown__trigger {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.35rem 0.85rem 0.35rem 0.35rem;
  background-color: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 99px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 2px 5px rgba(0,0,0,0.02);
  color: #111827;
  text-align: left;
}

.premium-dropdown__trigger:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  box-shadow: 0 8px 20px -8px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}

.premium-dropdown__trigger:active {
  transform: scale(0.98);
}

.premium-dropdown__trigger.is-active {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* --- Avatar --- */
.premium-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e293b, #0f172a);
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  flex-shrink: 0;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.1);
}

/* --- Meta Info --- */
.premium-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
  line-height: 1.2;
}

.premium-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #0f172a;
  letter-spacing: -0.01em;
}

.premium-email {
  font-size: 0.7rem;
  color: #64748b;
  margin-top: 2px;
}

/* --- Chevron --- */
.premium-chevron {
  color: #94a3b8;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  margin-left: 0.5rem;
}

.premium-chevron.is-open {
  transform: rotate(180deg);
}

/* --- Dropdown Menu (Glassmorphism / Bento) --- */
.premium-dropdown__menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  min-width: 240px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 1.25rem;
  padding: 0.5rem;
  margin: 0;
  list-style: none;
  box-shadow: 0 20px 40px -15px rgba(0,0,0,0.08), 0 4px 10px rgba(0,0,0,0.03), inset 0 1px 0 rgba(255,255,255,0.5);
  z-index: 1000;
  transform-origin: top right;
}

/* Vue Transition */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-10px);
}

/* --- Menu Items --- */
.premium-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.65rem 0.875rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #334155;
  background: transparent;
  border: none;
  border-radius: 0.75rem;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
}

.premium-icon {
  color: #94a3b8;
  transition: color 0.2s ease;
}

.premium-item:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.premium-item:hover .premium-icon {
  color: #0f172a;
}

.premium-item:active {
  background: #e2e8f0;
  transform: scale(0.98);
}

/* --- Danger Item --- */
.premium-item--danger {
  color: #ef4444;
}
.premium-item--danger .premium-icon {
  color: #f87171;
}

.premium-item--danger:hover {
  background: #fef2f2;
  color: #dc2626;
}
.premium-item--danger:hover .premium-icon {
  color: #dc2626;
}

.premium-item--danger:active {
  background: #fee2e2;
}

/* --- Divider --- */
.premium-divider {
  height: 1px;
  background: #e2e8f0;
  border: none;
  margin: 0.5rem 0;
}
</style>
