<script setup lang="ts">
import { ref, onMounted } from 'vue'

const navItems = [
  { label: 'Home', href: '#' },
  { label: 'Library', href: '#library' },
  { label: 'Authors', href: '#authors' },
  { label: 'Genres', href: '#genres' },
  { label: 'Membership', href: '#membership' },
]

const utilityLinks = [{ label: 'Admin Desk', to: '/admin' }]

</script>
<template>
  <header class="topbar">
    <div class="topbar-side topbar-side-left">
      <RouterLink class="brand" to="/" aria-label="One Online Home">
        <img src="@/assets/logo/vectorized.svg" alt="One Online Logo" class="brand-logo-img" />
      </RouterLink>
    </div>

    <nav class="topbar-nav" aria-label="Primary">
      <a v-for="item in navItems" :key="item.label" :href="item.href">{{ item.label }}</a>
    </nav>

    <div class="topbar-side topbar-side-right">
      <RouterLink v-for="item in utilityLinks" :key="item.label" class="topbar-utility" :to="item.to">
        {{ item.label }}
      </RouterLink>
    </div>
  </header>
</template>