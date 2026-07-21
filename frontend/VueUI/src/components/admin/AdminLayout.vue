<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  PhBooks,
  PhBookOpenText,
  PhBookmarkSimple,
  PhUsers,
  PhPenNib,
  PhSignOut,
  PhCaretDown,
  PhHouse
} from '@phosphor-icons/vue'

import { authUser, logout } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const menuOpen = ref(false)

function handleLogout() {
  logout()
  router.replace('/login')
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}
</script>

<template>
  <main class="admin-layout">
    
    <!-- Sidebar / Nav -->
    <aside class="bento-sidebar">
      <div class="sidebar-header bento-item">
        <div class="brand-logo">O</div>
        <div class="brand-text">
          <h1>One Online</h1>
          <span>Admin Center</span>
        </div>
      </div>
      
      <nav class="sidebar-nav bento-item">
        <RouterLink class="nav-link" active-class="active" to="/admin/dashboard">
          <PhBookOpenText :size="20" weight="duotone" />
          Tổng quan
        </RouterLink>
        <RouterLink class="nav-link" active-class="active" to="/admin/books">
          <PhBooks :size="20" weight="duotone" />
          Sách & Phiên bản
        </RouterLink>
        <RouterLink class="nav-link" active-class="active" to="/admin/categories">
          <PhBookmarkSimple :size="20" weight="duotone" />
          Danh mục
        </RouterLink>
        <RouterLink class="nav-link" active-class="active" to="/admin/users">
          <PhUsers :size="20" weight="duotone" />
          Người dùng
        </RouterLink>
        <RouterLink class="nav-link" active-class="active" to="/admin/authors">
          <PhPenNib :size="20" weight="duotone" />
          Tác giả
        </RouterLink>
        
        <div class="nav-divider"></div>
        
        <RouterLink class="nav-link return-link" to="/library">
          <PhHouse :size="20" weight="duotone" />
          Trang chủ đọc sách
        </RouterLink>
      </nav>
      
      <div class="sidebar-footer bento-item" @click="toggleMenu">
        <div class="user-profile">
          <div class="avatar">{{ (authUser?.fullName || 'A').charAt(0).toUpperCase() }}</div>
          <div class="user-info">
            <strong>{{ authUser?.fullName || 'Quản trị viên' }}</strong>
            <span>{{ authUser?.email }}</span>
          </div>
          <PhCaretDown :size="16" class="dropdown-icon" />
        </div>
        
        <transition name="fade">
          <div v-if="menuOpen" class="user-dropdown">
            <button class="dropdown-item danger" @click.stop="handleLogout">
              <PhSignOut :size="18" /> Đăng xuất
            </button>
          </div>
        </transition>
      </div>
    </aside>

    <!-- Main Content Area -->
    <section class="admin-main">
      <RouterView v-slot="{ Component }">
        <transition name="route-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </section>
  </main>
</template>

<style scoped>
/* Bento 2.0 Aesthetic Variables */
.admin-layout {
  --bento-bg: #f9fafb; /* Zinc 50 */
  --bento-surface: #ffffff;
  --bento-border: rgba(226, 232, 240, 0.8);
  --bento-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.05);
  --bento-radius: 2.5rem;
  --text-main: #09090b; /* Zinc 950 */
  --text-muted: #71717a; /* Zinc 500 */
  --accent: #18181b; /* Zinc 900 */
  
  font-family: 'Satoshi', 'Inter', sans-serif;
  display: flex;
  background-color: var(--bento-bg);
  min-height: 100vh;
  color: var(--text-main);
  padding: 1.5rem;
  gap: 2rem;
}

/* Sidebar */
.bento-sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  flex-shrink: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0 1rem;
}

.brand-logo {
  width: 48px;
  height: 48px;
  background: var(--accent);
  color: white;
  border-radius: 1.5rem;
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 1.5rem;
}

.brand-text h1 {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.04em;
}

.brand-text span {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-weight: 500;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1.1rem 1.25rem;
  border-radius: 1.5rem;
  color: var(--text-muted);
  text-decoration: none;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid transparent;
}

/* Liquid Glass Refraction on Active */
.nav-link:hover, .nav-link.active:not(.return-link) {
  background: var(--bento-surface);
  color: var(--text-main);
  border: 1px solid var(--bento-border);
  box-shadow: 
    var(--bento-shadow),
    inset 0 1px 0 rgba(255, 255, 255, 1),
    inset 0 -1px 0 rgba(0, 0, 0, 0.02);
  transform: translateY(-1px);
}

.nav-link:active {
  transform: scale(0.98) translateY(0);
}

.nav-divider {
  height: 1px;
  background: var(--bento-border);
  margin: 1rem 1.25rem;
}

.return-link {
  color: var(--text-main);
  margin-top: auto; 
}

.return-link:hover {
  background: #f4f4f5;
  box-shadow: none;
  border-color: transparent;
}

.sidebar-footer {
  position: relative;
  background: var(--bento-surface);
  border-radius: var(--bento-radius);
  padding: 1.25rem;
  box-shadow: var(--bento-shadow);
  border: 1px solid var(--bento-border);
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.sidebar-footer:active {
  transform: scale(0.98);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f4f4f5;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: var(--accent);
}

.user-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.user-info strong {
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info span {
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-dropdown {
  position: absolute;
  bottom: calc(100% + 10px);
  left: 0;
  width: 100%;
  background: var(--bento-surface);
  border-radius: 1.25rem;
  box-shadow: 0 24px 60px rgba(0,0,0,0.1);
  border: 1px solid var(--bento-border);
  padding: 0.5rem;
  z-index: 10;
}

.dropdown-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  border-radius: 0.75rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s;
}

.dropdown-item.danger {
  color: #ef4444;
}

.dropdown-item.danger:hover {
  background: #fef2f2;
}

/* Main Content Area */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.route-fade-enter-active,
.route-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.route-fade-enter-from,
.route-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
