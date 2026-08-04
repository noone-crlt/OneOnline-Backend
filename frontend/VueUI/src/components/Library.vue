<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import TopNavbar from './layout/TopNavbar.vue'
import AppFooter from './layout/AppFooter.vue'
import { getUserLibrary, getFileUrl } from '../services/api'
import { authUser } from '../stores/auth'

const router = useRouter()
const searchQuery = ref('')
const activeFormat = ref('ALL')
const purchasedBooks = ref([])
const isLoading = ref(true)
const errorMessage = ref('')

const formatFilters = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Sách điện tử', value: 'EBOOK' },
  { label: 'Sách nói', value: 'AUDIOBOOK' },
  { label: 'Sách giấy', value: 'PHYSICAL' },
]

const filteredBooks = computed(() => {
  return purchasedBooks.value.filter((item) => {
    // Lọc theo từ khóa tìm kiếm
    const matchesSearch =
      !searchQuery.value.trim() ||
      (item.bookTitle && item.bookTitle.toLowerCase().includes(searchQuery.value.toLowerCase().trim())) ||
      (item.authorName && item.authorName.toLowerCase().includes(searchQuery.value.toLowerCase().trim()))

    // Lọc theo định dạng
    let matchesFormat = true
    if (activeFormat.value === 'EBOOK') {
      matchesFormat = item.format === 'EBOOK_PDF' || item.format === 'EBOOK_EPUB' || item.format === 'EBOOK'
    } else if (activeFormat.value === 'AUDIOBOOK') {
      matchesFormat = item.format === 'AUDIOBOOK'
    } else if (activeFormat.value === 'PHYSICAL') {
      matchesFormat = item.format === 'PHYSICAL'
    }

    return matchesSearch && matchesFormat
  })
})

async function fetchLibrary() {
  if (!authUser.value) {
    isLoading.value = false
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  try {
    const list = await getUserLibrary(0, 100)
    purchasedBooks.value = Array.isArray(list) ? list : []
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể tải thư viện sách cá nhân.'
  } finally {
    isLoading.value = false
  }
}

function resolveCover(item) {
  if (item.coverUrl) return item.coverUrl
  if (item.coverImageUrl) return getFileUrl(item.coverImageUrl)
  return ''
}

function getInitials(title) {
  if (!title) return 'BA'
  return title.trim().split(/\s+/).slice(0, 2).map((word) => word.charAt(0)).join('').toUpperCase()
}

function getFormatBadgeLabel(format) {
  if (format === 'AUDIOBOOK') return 'AUDIOBOOK'
  if (format === 'PHYSICAL') return 'SÁCH GIẤY'
  return 'E-BOOK'
}

function getFormatBadgeClass(format) {
  if (format === 'AUDIOBOOK') return 'badge-audio'
  if (format === 'PHYSICAL') return 'badge-physical'
  return 'badge-ebook'
}

function handleAction(item) {
  if (item.format === 'PHYSICAL') {
    router.push(`/book/${item.slug}`)
  } else {
    router.push(`/read/${item.slug}`)
  }
}

onMounted(() => {
  fetchLibrary()
})
</script>

<template>
  <div id="library-top" class="library-shell">
    <TopNavbar />

    <main class="library-main-area">
      <!-- Minimalist Hero Section -->
      <section class="library-hero" aria-labelledby="library-title">
        <div class="library-hero-copy">
          <p class="library-kicker">THƯ VIỆN CỦA TÔI</p>
          <h1 id="library-title">Kho sách đã sở hữu</h1>
          <p class="library-subtext">
            Quản lý, đọc và nghe tất cả các tác phẩm sách điện tử, sách nói và sách giấy bạn đã sở hữu.
          </p>

          <form v-if="authUser && purchasedBooks.length > 0" class="library-search" role="search" @submit.prevent>
            <div class="search-field">
              <svg viewBox="0 0 24 24" aria-hidden="true" width="20" height="20">
                <path
                  d="m20 20-4.6-4.6m2.6-5.4a8 8 0 1 1-16 0 8 8 0 0 1 16 0Z"
                  fill="none"
                  stroke="currentColor"
                  stroke-linecap="round"
                  stroke-width="2"
                />
              </svg>
              <input
                id="library-search-input"
                v-model="searchQuery"
                type="search"
                placeholder="Tìm sách trong thư viện của bạn..."
                aria-label="Tìm kiếm trong thư viện cá nhân"
              />
            </div>
          </form>
        </div>
      </section>

      <!-- Main Content Area -->
      <section class="catalog-section">
        <!-- NOT LOGGED IN STATE -->
        <div v-if="!authUser" class="empty-state empty-state-action">
          <div class="empty-icon-circle">🔒</div>
          <h3>Vui lòng đăng nhập</h3>
          <p>Bạn cần đăng nhập tài khoản để truy cập kho sách cá nhân của mình.</p>
          <RouterLink to="/login" class="btn btn-primary-action">Đăng nhập ngay</RouterLink>
        </div>

        <!-- LOGGED IN CONTENT -->
        <template v-else>
          <!-- Controls Header -->
          <div v-if="purchasedBooks.length > 0" class="catalog-controls">
            <div class="catalog-intro-title">
              <h2>Sách đã mua ({{ filteredBooks.length }})</h2>
            </div>

            <div class="mood-filter">
              <button
                v-for="filter in formatFilters"
                :key="filter.value"
                type="button"
                :class="{ active: activeFormat === filter.value }"
                @click="activeFormat = filter.value"
              >
                {{ filter.label }}
              </button>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="isLoading" class="empty-state">
            <div class="spinner"></div>
            <p>Đang tải danh sách sách đã mua của bạn...</p>
          </div>

          <!-- Error State -->
          <div v-else-if="errorMessage" class="empty-state empty-state-action">
            <span>{{ errorMessage }}</span>
            <button type="button" class="btn btn-secondary" @click="fetchLibrary">Tải lại</button>
          </div>

          <!-- Empty Purchased Books State -->
          <div v-else-if="purchasedBooks.length === 0" class="empty-state empty-state-action">
            <div class="empty-icon-circle">📚</div>
            <h3>Thư viện của bạn đang trống</h3>
            <p>Bạn chưa sở hữu tác phẩm nào. Hãy khám phá kho sách và mua ngay để thưởng thức!</p>
            <RouterLink to="/" class="btn btn-primary-action">Khám phá sách ngay</RouterLink>
          </div>

          <!-- No Search Results -->
          <div v-else-if="filteredBooks.length === 0" class="empty-state">
            Không tìm thấy cuốn sách nào khớp với cụm từ tìm kiếm của bạn.
          </div>

          <!-- Grid of Purchased Books -->
          <div v-else class="editorial-books-grid">
            <article
              v-for="item in filteredBooks"
              :key="item.id || item.slug"
              class="purchased-book-card"
            >
              <div class="book-cover-wrapper" @click="handleAction(item)">
                <img v-if="resolveCover(item)" :src="resolveCover(item)" :alt="item.bookTitle" />
                <div v-else class="cover-placeholder-box">
                  {{ getInitials(item.bookTitle) }}
                </div>
                <span class="format-badge" :class="getFormatBadgeClass(item.format)">
                  {{ getFormatBadgeLabel(item.format) }}
                </span>
              </div>

              <div class="book-card-info">
                <h3 class="purchased-book-title" :title="item.bookTitle">{{ item.bookTitle }}</h3>
                <p class="purchased-book-author">{{ item.authorName || 'One Online' }}</p>

                <button class="action-read-btn" :class="getFormatBadgeClass(item.format)" @click="handleAction(item)">
                  <template v-if="item.format === 'AUDIOBOOK'">🎧 Nghe ngay</template>
                  <template v-else-if="item.format === 'PHYSICAL'">📦 Xem chi tiết</template>
                  <template v-else>📖 Đọc ngay</template>
                </button>
              </div>
            </article>
          </div>
        </template>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.library-shell {
  min-height: 100vh;
  background-color: var(--page-bg, #fafafa);
  display: flex;
  flex-direction: column;
}

.library-main-area {
  flex: 1;
  width: 100%;
  max-width: var(--content-width, 1200px);
  margin: 0 auto;
  padding: 2.5rem 2rem 5rem;
}

/* Search Hero Section */
.library-hero {
  text-align: center;
  max-width: 650px;
  margin: 0 auto 3rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.library-kicker {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: var(--accent, #059669);
  font-weight: 700;
  margin-bottom: 0.75rem;
}

.library-hero h1 {
  font-size: clamp(2rem, 4vw, 2.75rem);
  line-height: 1.15;
  color: #09090b;
  margin-bottom: 0.75rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.library-subtext {
  font-size: 1.05rem;
  color: #71717a;
  margin-bottom: 1.75rem;
}

.library-search {
  width: 100%;
}

.search-field {
  position: relative;
  width: 100%;
}

.search-field svg {
  position: absolute;
  left: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  color: #a1a1aa;
  pointer-events: none;
}

.search-field input {
  width: 100%;
  height: 52px;
  padding: 0 1.5rem 0 3.2rem;
  background-color: #ffffff;
  border: 1px solid #e4e4e7;
  border-radius: 99px;
  font-size: 0.98rem;
  color: #09090b;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 200ms ease;
}

.search-field input:focus {
  outline: none;
  border-color: #09090b;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

/* Catalog section */
.catalog-section {
  border-top: 1px solid #e4e4e7;
  padding-top: 2.5rem;
}

.catalog-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.catalog-intro-title h2 {
  font-size: 1.5rem;
  color: #09090b;
  font-weight: 700;
  margin: 0;
}

.mood-filter {
  display: flex;
  gap: 0.5rem;
  background-color: #f4f4f5;
  padding: 0.3rem;
  border-radius: 99px;
  border: 1px solid #e4e4e7;
}

.mood-filter button {
  padding: 0.45rem 1.2rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: #71717a;
  border-radius: 99px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 200ms ease;
}

.mood-filter button.active {
  background-color: #09090b;
  color: #ffffff;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 250px;
  color: #71717a;
  text-align: center;
}

.empty-state-action {
  background: #ffffff;
  padding: 3rem 2rem;
  border-radius: 16px;
  border: 1px dashed #d4d4d8;
}

.empty-icon-circle {
  font-size: 2.75rem;
  margin-bottom: 1rem;
}

.empty-state-action h3 {
  font-size: 1.35rem;
  font-weight: 700;
  color: #09090b;
  margin-bottom: 0.5rem;
}

.empty-state-action p {
  color: #71717a;
  max-width: 440px;
  margin-bottom: 1.25rem;
}

.btn-primary-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.65rem 1.75rem;
  border-radius: 99px;
  background: #09090b;
  color: #ffffff;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s ease;
}

.btn-primary-action:hover {
  background: #27272a;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #09090b;
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Purchased Books Grid */
.editorial-books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 2rem;
}

.purchased-book-card {
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border: 1px solid #e4e4e7;
  border-radius: 14px;
  padding: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.purchased-book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

.book-cover-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 2/3;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f4f4f5;
  cursor: pointer;
  margin-bottom: 0.85rem;
}

.book-cover-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder-box {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display, serif);
  font-size: 2.2rem;
  font-weight: 700;
  color: #71717a;
}

.format-badge {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  padding: 0.2rem 0.55rem;
  border-radius: 6px;
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.format-badge.badge-ebook {
  background: #eff6ff;
  color: #2563eb;
}

.format-badge.badge-audio {
  background: #fef3c7;
  color: #d97706;
}

.format-badge.badge-physical {
  background: #ecfdf5;
  color: #059669;
}

.book-card-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.purchased-book-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #09090b;
  margin: 0 0 0.25rem 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.purchased-book-author {
  font-size: 0.82rem;
  color: #71717a;
  margin: 0 0 1rem 0;
}

.action-read-btn {
  margin-top: auto;
  width: 100%;
  padding: 0.55rem;
  border-radius: 8px;
  border: none;
  font-size: 0.85rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-read-btn.badge-ebook {
  background: #2563eb;
  color: #ffffff;
}

.action-read-btn.badge-ebook:hover {
  background: #1d4ed8;
}

.action-read-btn.badge-audio {
  background: #d97706;
  color: #ffffff;
}

.action-read-btn.badge-audio:hover {
  background: #b45309;
}

.action-read-btn.badge-physical {
  background: #059669;
  color: #ffffff;
}

.action-read-btn.badge-physical:hover {
  background: #047857;
}
</style>
