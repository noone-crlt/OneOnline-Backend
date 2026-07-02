<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authUser, authIsAuthenticated } from '@/stores/auth'
import { cartItemCount, fetchCartItemCount } from '@/stores/cart'
import UserDropdown from '../UserDropdown.vue'

const router = useRouter()
const route = useRoute()

const navItems = [
  { label: 'Trang chủ', to: '/' },
  { label: 'Thư viện', to: '/library' },
  { label: 'Mới nhất', to: '/#latest-books' }
]

const isSignedIn = computed(() => Boolean(authUser.value))

function handleNavClick(item) {
  if (item.to.startsWith('/#')) {
    if (route.path === '/') {
      // scroll manually if already on home
      const id = item.to.replace('/#', '')
      const el = document.getElementById(id)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth' })
      }
    } else {
      router.push(item.to)
    }
  } else {
    router.push(item.to)
  }
}

function isActive(item) {
  if (item.to === '/') {
    return route.path === '/' && !route.hash
  }
  if (item.to.startsWith('/#')) {
    return route.path === '/' && route.hash === item.to.replace('/', '')
  }
  return route.path.startsWith(item.to)
}

onMounted(() => {
  fetchCartItemCount()
})

watch(authIsAuthenticated, () => {
  fetchCartItemCount()
})
</script>

<template>
  <header class="topbar">
    <div class="topbar__inner">
      <div class="topbar-side topbar-side-left">
        <RouterLink class="brand" to="/" aria-label="Về trang chủ One Online">
          <img src="@/assets/logo/vectorized.svg" alt="One Online Logo" class="brand-logo-img" />
        </RouterLink>
      </div>

      <nav class="topbar-nav" aria-label="Điều hướng chính">
        <button
          v-for="item in navItems"
          :key="item.label"
          type="button"
          :class="{ active: isActive(item) }"
          @click="handleNavClick(item)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="topbar-side topbar-side-right">
        <!-- E-commerce Shopping Cart -->
        <button class="nav-cart-btn" aria-label="Xem giỏ hàng" @click="router.push('/cart')">
          <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
            <circle cx="9" cy="21" r="1"></circle>
            <circle cx="20" cy="21" r="1"></circle>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
          </svg>
          <span class="cart-badge-count" v-if="cartItemCount > 0">{{ cartItemCount }}</span>

        </button>

        <template v-if="!isSignedIn">
          <RouterLink class="topbar-utility" to="/login">Đăng nhập</RouterLink>
        </template>
        <template v-else>
          <UserDropdown />
        </template>
      </div>
    </div>
  </header>
</template>
