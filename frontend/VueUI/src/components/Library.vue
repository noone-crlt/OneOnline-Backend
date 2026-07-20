<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import TopNavbar from './layout/TopNavbar.vue'
import { getBookCatalog, getCategories, getFileUrl } from '../services/api'

const router = useRouter()
const route = useRoute()
const searchQuery = ref(route.query.q || '')
const activeFormat = ref('ALL')
const activeCategory = ref('ALL')
const books = ref([])
const categories = ref([])
const totalBooks = ref(0)
const isLoading = ref(false)
const errorMessage = ref('')
let searchTimer = null
let activeRequest = null

const formatFilters = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Sách giấy', value: 'PHYSICAL' },
  { label: 'Sách điện tử', value: 'EBOOK' },
  { label: 'Sách nói', value: 'AUDIOBOOK' },
]

// Resolve nested data from editions
const displayBooks = computed(() =>
  books.value.map((book) => {
    const initials = getInitials(book.title)
    
    // Resolve cover image: first available edition cover or main imageUrl
    let cover = ''
    if (book.editions && book.editions.length > 0) {
      const coverEd = book.editions.find(e => e.coverObjectName)
      if (coverEd) cover = coverEd.coverUrl || getFileUrl(coverEd.coverObjectName)
    }
    if (!cover && book.imageUrls && book.imageUrls.length > 0) {
      cover = getFileUrl(book.imageUrls[0])
    }

    // Resolve format lists
    const formats = book.editions ? book.editions.map(e => e.format) : []
    
    // Resolve price range or minimum price
    let prices = book.editions ? book.editions.map(e => Number(e.salePrice)).filter(p => !isNaN(p)) : []
    let minPrice = prices.length > 0 ? Math.min(...prices) : null
    let maxPrice = prices.length > 0 ? Math.max(...prices) : null

    // Check stock
    const isOutOfStock = book.editions ? book.editions.every(e => e.format === 'PHYSICAL' && (!e.stock || e.stock <= 0)) : false

    return {
      ...book,
      initials,
      coverUrl: cover,
      formats,
      minPrice,
      maxPrice,
      isOutOfStock,
      description: book.description || 'Chưa có mô tả tóm tắt cho tác phẩm này.',
      publisherName: book.publisherName || 'One Online'
    }
  }),
)

async function loadBooks() {
  activeRequest?.abort()
  const controller = new AbortController()
  activeRequest = controller
  isLoading.value = true
  errorMessage.value = ''

  try {
    const payload = await getBookCatalog({
      q: searchQuery.value,
      category: activeCategory.value === 'ALL' ? '' : activeCategory.value,
      format: activeFormat.value === 'ALL' ? '' : activeFormat.value,
      page: 0,
      size: 100,
    }, { signal: controller.signal })

    books.value = Array.isArray(payload?.content) ? payload.content : []
    totalBooks.value = Number(payload?.totalElements ?? books.value.length)
  } catch (error) {
    if (error?.name !== 'AbortError') {
      errorMessage.value = error instanceof Error ? error.message : 'Không thể tải danh sách sách.'
    }
  } finally {
    if (activeRequest === controller) {
      isLoading.value = false
    }
  }
}

async function loadCategories() {
  try {
    const payload = await getCategories()
    categories.value = Array.isArray(payload)
      ? payload.map((category) => category.name).filter(Boolean)
      : []
  } catch {
    categories.value = []
  }
}

function getInitials(title) {
  if (!title) return 'BA'
  return title.trim().split(/\s+/).slice(0, 2).map((word) => word.charAt(0)).join('').toUpperCase()
}

function formatPriceRange(book) {
  if (book.minPrice == null) return 'Đang cập nhật'
  if (book.minPrice === book.maxPrice) {
    return formatPrice(book.minPrice)
  }
  return `${formatPrice(book.minPrice)} - ${formatPrice(book.maxPrice)}`
}

function formatPrice(price) {
  if (price == null) return ''
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(Number(price))
}

function formatFormatLabel(format) {
  switch (format) {
    case 'PHYSICAL': return '📖 Sách giấy'
    case 'EBOOK_PDF': return '📱 PDF E-book'
    case 'EBOOK_EPUB': return '📱 EPUB E-book'
    case 'AUDIOBOOK': return '🎧 Sách nói'
    default: return format
  }
}

function openBookDetail(slug) {
  router.push(`/book/${slug}`)
}

watch(searchQuery, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(loadBooks, 300)
})

watch([activeFormat, activeCategory], loadBooks)

watch(() => route.query.q, (newQ) => {
  if (newQ !== undefined) {
    searchQuery.value = newQ
  }
})

onMounted(async () => {
  await loadCategories()
  await loadBooks()
})

onBeforeUnmount(() => {
  clearTimeout(searchTimer)
  activeRequest?.abort()
})
</script>

<template>
  <div id="library-top" class="library-shell">
    <TopNavbar />

    <main class="library-main-area">
      <!-- Minimalist Hero Search Section -->
      <section class="library-hero" aria-labelledby="library-title">
        <div class="library-hero-copy">
          <p class="library-kicker">ONE ONLINE STORE</p>
          <h1 id="library-title">Khám phá kho sách trực tuyến</h1>
          <p class="library-subtext">
            Tìm kiếm nhanh chóng, chọn định dạng yêu thích và bắt đầu trải nghiệm đọc.
          </p>

          <form class="library-search" role="search" @submit.prevent>
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
                placeholder="Tìm tựa sách, tác giả, nhà xuất bản..."
                aria-label="Tìm kiếm trong thư viện"
              />
            </div>
          </form>
        </div>
      </section>

      <!-- Catalog grid with Filters -->
      <section id="catalog" class="catalog-section" aria-labelledby="catalog-title">
        <div class="catalog-controls">
          <div class="catalog-intro-title">
            <h2 id="catalog-title">Tất cả tác phẩm</h2>
            <p class="catalog-count">{{ totalBooks }} tác phẩm được tìm thấy</p>
          </div>

          <div class="catalog-filter-groups">
            <div class="filter-group">
              <span class="filter-group-label">Định dạng</span>
              <div class="mood-filter" aria-label="Lọc sách theo định dạng">
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

            <div v-if="categories.length" class="filter-group">
              <span class="filter-group-label">Thể loại</span>
              <div class="mood-filter mood-filter-categories" aria-label="Lọc sách theo thể loại">
                <button
                  type="button"
                  :class="{ active: activeCategory === 'ALL' }"
                  @click="activeCategory = 'ALL'"
                >
                  Tất cả
                </button>
                <button
                  v-for="category in categories"
                  :key="category"
                  type="button"
                  :class="{ active: activeCategory === category }"
                  @click="activeCategory = category"
                >
                  {{ category }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Books Grid -->
        <div class="book-list-container">
          <div v-if="isLoading" class="empty-state">
            <div class="spinner"></div>
            <p>Đang tải dữ liệu từ kệ sách...</p>
          </div>

          <div v-else-if="errorMessage" class="empty-state empty-state-action">
            <span>Không thể tải kệ sách: {{ errorMessage }}</span>
            <button type="button" class="btn btn-secondary" @click="loadBooks">Tải lại</button>
          </div>

          <div v-else-if="books.length === 0" class="empty-state">
            Hiện tại cửa hàng chưa có tác phẩm trực tuyến. Vui lòng quay lại sau.
          </div>

          <div v-else-if="displayBooks.length === 0" class="empty-state">
            Không tìm thấy cuốn sách nào khớp với tìm kiếm của bạn.
          </div>

          <div v-else class="editorial-books-grid">
            <article
              v-for="book in displayBooks"
              :key="book.id ?? book.title"
              class="book-card"
              @click="openBookDetail(book.slug)"
            >
              <!-- Book Cover Thumbnail -->
              <div class="book-card-cover">
                <img v-if="book.coverUrl" :src="book.coverUrl" :alt="`Bìa sách ${book.title}`" loading="lazy" />
                <strong v-else class="cover-placeholder">{{ book.initials }}</strong>
                
                <!-- Format badges overlay -->
                <div class="format-badges" v-if="book.formats && book.formats.length > 0">
                  <span v-for="f in book.formats" :key="f" :class="['mini-badge', f.toLowerCase()]" :title="formatFormatLabel(f)">
                    {{ f === 'PHYSICAL' ? 'PHYS' : f === 'AUDIOBOOK' ? 'AUDIO' : 'E-BOOK' }}
                  </span>
                </div>
              </div>

              <!-- Book Copy Metadata -->
              <div class="book-card-meta">
                <p class="book-authors-line" v-if="book.authorNames && book.authorNames.length > 0">
                  {{ book.authorNames.join(', ') }}
                </p>
                <h3 class="book-card-title">{{ book.title }}</h3>
                <p class="book-publisher">{{ book.publisherName }}</p>
                
                <div class="book-card-footer">
                  <span class="book-price">{{ formatPriceRange(book) }}</span>
                  <span class="book-stock-tag" :class="{ 'out-of-stock': book.isOutOfStock }">
                    {{ book.isOutOfStock ? 'Hết hàng' : 'Có sẵn' }}
                  </span>
                </div>
              </div>
            </article>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.library-shell {
  min-height: 100vh;
  background-color: var(--page-bg);
  display: flex;
  flex-direction: column;
}



.topbar-nav {
  display: flex;
  gap: 2rem;
}

.nav-link {
  font-family: var(--font-body);
  font-size: 0.95rem;
  color: var(--text-soft);
  font-weight: 500;
  padding: 0.25rem 0;
  transition: color 200ms ease;
}

.nav-link:hover, .nav-link.active {
  color: var(--text-strong);
}

.topbar-utility {
  display: inline-flex;
  align-items: center;
  height: 38px;
  padding: 0 1.2rem;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius-sm);
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--text-strong);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 600;
  transition: background-color 200ms ease, border-color 200ms ease;
}

.topbar-utility:hover {
  background-color: var(--surface-soft);
  border-color: var(--accent);
}

/* Main Area Container */
.library-main-area {
  flex: 1;
  width: 100%;
  max-width: var(--content-width);
  margin: 0 auto;
  padding: 2.5rem 2rem 5rem;
}

/* Search Hero Section */
.library-hero {
  text-align: center;
  max-width: 650px;
  margin: 0 auto 4rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.library-kicker {
  font-family: var(--font-body);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: var(--accent);
  font-weight: 700;
  margin-bottom: 0.75rem;
}

.library-hero h1 {
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3rem);
  line-height: 1.15;
  color: var(--text-strong);
  margin-bottom: 1rem;
  font-weight: 500;
}

.library-subtext {
  font-family: var(--font-body);
  font-size: 1.05rem;
  color: var(--text-soft);
  margin-bottom: 2rem;
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
  color: var(--text-soft);
  pointer-events: none;
}

.search-field input {
  width: 100%;
  height: 52px;
  padding: 0 1.5rem 0 3.2rem;
  background-color: var(--surface);
  border: 1px solid var(--line-strong);
  border-radius: var(--radius-md);
  font-family: var(--font-body);
  font-size: 0.98rem;
  color: var(--text-strong);
  box-shadow: var(--shadow-soft);
  transition: all 200ms ease;
}

.search-field input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 8px 24px rgba(47, 79, 62, 0.06);
}

/* Catalog header and filter styling */
.catalog-section {
  border-top: 1px solid var(--line-soft);
  padding-top: 2.5rem;
}

.catalog-controls {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 2.5rem;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.catalog-intro-title h2 {
  font-family: var(--font-display);
  font-size: 1.8rem;
  color: var(--text-strong);
  font-weight: 500;
  margin-bottom: 0.3rem;
}

.catalog-count {
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--text-soft);
}

.catalog-filter-groups {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.8rem;
  max-width: min(850px, 100%);
  overflow: hidden;
}

.filter-group {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
  max-width: 100%;
  overflow: hidden;
}

.filter-group-label {
  flex: 0 0 auto;
  font-family: var(--font-body);
  font-size: 0.72rem;
  font-weight: 700;
  color: var(--text-soft);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.mood-filter {
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  gap: 0.5rem;
  background-color: var(--surface-soft);
  padding: 0.3rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--line-soft);
  /* Hide scrollbar */
  scrollbar-width: none;
  -ms-overflow-style: none;
  max-width: 100%;
}

.mood-filter::-webkit-scrollbar {
  display: none;
}

.mood-filter button {
  flex-shrink: 0;
  padding: 0.45rem 1.2rem;
  font-family: var(--font-body);
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-soft);
  border-radius: var(--radius-sm);
  transition: all 200ms ease;
}

.mood-filter button.active {
  background-color: var(--accent);
  color: var(--surface);
  box-shadow: none;
}

.mood-filter-categories {
  justify-content: flex-end;
}

/* Spinner and loading */
.book-list-container {
  min-height: 300px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 250px;
  font-family: var(--font-body);
  color: var(--text-soft);
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(43, 33, 24, 0.1);
  border-radius: 50%;
  border-top-color: var(--accent);
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state-action button {
  margin-top: 1rem;
}

/* Editorial E-commerce Books Grid */
.editorial-books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 2.5rem;
}

.book-card {
  display: flex;
  flex-direction: column;
  background-color: var(--surface);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-md);
  padding: 0.95rem;
  cursor: pointer;
  box-shadow: var(--shadow-soft);
  transition: transform 250ms cubic-bezier(0.16, 1, 0.3, 1), box-shadow 250ms cubic-bezier(0.16, 1, 0.3, 1);
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-strong);
}

.book-card-cover {
  width: 100%;
  aspect-ratio: 2/3;
  border-radius: 4px;
  background-color: var(--surface-soft);
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  box-shadow: 0 4px 12px rgba(43, 33, 24, 0.06);
}

.book-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-family: var(--font-display);
  font-size: 2.2rem;
  font-weight: 600;
  color: var(--text-soft);
  opacity: 0.4;
}

.format-badges {
  position: absolute;
  bottom: 0.5rem;
  right: 0.5rem;
  display: flex;
  gap: 0.25rem;
}

.mini-badge {
  border-radius: 4px;
  padding: 0.15rem 0.35rem;
  font-family: var(--font-mono);
  font-size: 0.65rem;
  font-weight: 700;
  border: 1px solid var(--line-soft);
  text-transform: uppercase;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 22px;
}
.mini-badge.physical {
  background-color: var(--pastel-green-bg);
  color: var(--pastel-green-text);
  border-color: rgba(52, 101, 56, 0.15);
}
.mini-badge.audiobook {
  background-color: var(--pastel-yellow-bg);
  color: var(--pastel-yellow-text);
  border-color: rgba(149, 100, 0, 0.15);
}
.mini-badge.ebook_pdf, .mini-badge.ebook_epub {
  background-color: var(--pastel-blue-bg);
  color: var(--pastel-blue-text);
  border-color: rgba(31, 108, 159, 0.15);
}

.book-card-meta {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.book-authors-line {
  font-family: var(--font-body);
  font-size: 0.78rem;
  color: var(--text-soft);
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-card-title {
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 500;
  color: var(--text-strong);
  margin-bottom: 0.25rem;
  line-height: 1.25;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 2.5rem;
}

.book-publisher {
  font-family: var(--font-body);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--accent);
  font-weight: 600;
  margin-bottom: 1rem;
}

.book-card-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--line-soft);
  padding-top: 0.75rem;
}

.book-price {
  font-family: var(--font-body);
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--text-strong);
}

.book-stock-tag {
  font-family: var(--font-body);
  font-size: 0.72rem;
  font-weight: 600;
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  background-color: var(--pastel-green-bg);
  color: var(--pastel-green-text);
  border: 1px solid rgba(52, 101, 56, 0.1);
}

.book-stock-tag.out-of-stock {
  background-color: var(--pastel-red-bg);
  color: var(--pastel-red-text);
  border: 1px solid rgba(159, 47, 45, 0.1);
}

/* Mobile adjustments */
@media (max-width: 600px) {
  .catalog-controls {
    flex-direction: column;
    align-items: flex-start;
  }

  .catalog-filter-groups,
  .filter-group {
    width: 100%;
    align-items: flex-start;
  }

  .filter-group {
    flex-direction: column;
  }

  .mood-filter,
  .mood-filter-categories {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
