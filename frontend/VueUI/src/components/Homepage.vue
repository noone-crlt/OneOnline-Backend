<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { gsap } from 'gsap'
import AppFooter from './layout/AppFooter.vue'
import TopNavbar from './layout/TopNavbar.vue'
import { getBooks, getFileUrl } from '../services/api'
import { authUser } from '../stores/auth'

const router = useRouter()
const pageRoot = ref(null)
const books = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
let motionContext
let homeMotionStarted = false



// Resolve details and map from editions
const displayBooks = computed(() =>
  books.value.map((book) => {
    const initials = getInitials(book.title)
    
    // Resolve cover image
    let cover = ''
    if (book.editions && book.editions.length > 0) {
      const coverEd = book.editions.find(e => e.coverObjectName)
      if (coverEd) cover = coverEd.coverUrl || getFileUrl(coverEd.coverObjectName)
    }
    if (!cover && book.imageUrls && book.imageUrls.length > 0) {
      cover = getFileUrl(book.imageUrls[0])
    }

    // Resolve price
    let prices = book.editions ? book.editions.map(e => Number(e.salePrice)).filter(p => !isNaN(p)) : []
    let minPrice = prices.length > 0 ? Math.min(...prices) : null

    return {
      ...book,
      initials,
      coverUrl: cover,
      minPrice,
      publisherName: book.publisherName || 'One Online',
      summary: summarizeDescription(book.description)
    }
  }),
)

const latestBooks = computed(() =>
  [...displayBooks.value]
    .sort((left, right) => getBookTimestamp(right) - getBookTimestamp(left))
    .slice(0, 4),
)

// Select one book to feature in the Hero section side card
const featuredBook = computed(() => {
  if (displayBooks.value.length === 0) return null
  // Try to find "Harry Potter" or "Mắt Biếc", or default to the first book
  const found = displayBooks.value.find(b => b.slug === 'mat-biec' || b.slug === 'harry-potter-1')
  return found || displayBooks.value[0]
})

async function loadBooks() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const payload = await getBooks()
    books.value = Array.isArray(payload) ? payload : []
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể tải danh sách sách.'
  } finally {
    isLoading.value = false
  }
}

function getBookTimestamp(book) {
  const timestamp = Date.parse(book?.createdAt ?? '')
  return Number.isNaN(timestamp) ? 0 : timestamp
}

function getInitials(title) {
  if (!title) return 'BA'
  return title.trim().split(/\s+/).slice(0, 2).map((word) => word.charAt(0)).join('').toUpperCase()
}

function summarizeDescription(description) {
  if (!description) return 'Những cuốn sách mới thêm sẽ xuất hiện tại đây.'
  const words = description.trim().split(/\s+/)
  if (words.length <= 15) return description.trim()
  return `${words.slice(0, 15).join(' ')}...`
}

function openBookDetail(slug) {
  router.push(`/book/${slug}`)
}

function scrollToSection(sectionId) {
  document.getElementById(sectionId)?.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  })
}



function goToLibrary() {
  router.push('/library')
}

function initHomeMotion() {
  if (
    homeMotionStarted ||
    !pageRoot.value ||
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
  ) {
    return
  }

  homeMotionStarted = true

  motionContext = gsap.context(() => {
    const timeline = gsap.timeline({
      defaults: {
        duration: 0.6,
        ease: 'power2.out',
      },
    })

    timeline
      .from('.topbar', { y: -15, opacity: 0, duration: 0.4 })
      .from('.hero-left', { x: -20, opacity: 0, duration: 0.6 }, '-=0.2')
      .from('.hero-right', { scale: 0.98, opacity: 0, duration: 0.7 }, '-=0.4')
      .from('.latest-section', { y: 20, opacity: 0, duration: 0.5 }, '-=0.3')
  }, pageRoot.value)
}

onMounted(async () => {
  await nextTick()
  initHomeMotion()
  loadBooks()
})

onUnmounted(() => {
  motionContext?.revert()
})
</script>

<template>
  <div ref="pageRoot" class="page-shell">
    <TopNavbar />

    <main class="homepage-main">
      <!-- Split-screen Hero Section -->
      <section id="home" class="hero-split">
        <div class="hero-left">
          <p class="eyebrow">ONE ONLINE LIBRARY</p>
          <h1 class="hero-headline">
            Đọc những trang còn mãi.
          </h1>
          <p class="hero-text">
            Một thư viện số dịu lại vào cuối ngày, nơi bạn tìm sách, mở tiếp chương đang đọc và nghe những câu chuyện được tuyển chọn kỹ lưỡng.
          </p>

          <div class="hero-actions">
            <button class="button button-primary" type="button" @click="goToLibrary">
              Khám phá thư viện
            </button>
            <button class="button button-secondary" type="button" @click="scrollToSection('latest-books')">
              Sách mới kệ tối nay
            </button>
          </div>
        </div>

        <div class="hero-right">
          <!-- Featured Book Card (Editorial Style) -->
          <div v-if="featuredBook" class="featured-book-promo shadow-elegant" @click="openBookDetail(featuredBook.slug)">
            <div class="promo-cover">
              <img v-if="featuredBook.coverUrl" :src="featuredBook.coverUrl" :alt="`Bìa sách ${featuredBook.title}`" />
              <strong v-else>{{ featuredBook.initials }}</strong>
            </div>
            <div class="promo-meta">
              <span class="promo-tag">Tác phẩm nổi bật</span>
              <h3>{{ featuredBook.title }}</h3>
              <p class="promo-desc">{{ featuredBook.summary }}</p>
              <span class="promo-link">Khám phá tác phẩm →</span>
            </div>
          </div>
        </div>
      </section>

      <!-- New Arrivals Shelf -->
      <section id="latest-books" class="latest-section">
        <div class="section-heading">
          <p class="section-kicker">MỚI TRÊN KỆ</p>
          <h2>Những cuốn vừa cập nhật tối nay</h2>
        </div>

        <div v-if="isLoading" class="section-loading">
          <div class="spinner"></div>
          <p>Đang tải kệ sách mới nhất...</p>
        </div>

        <div v-else-if="errorMessage" class="section-empty section-empty-action">
          <span>Không thể tải sách mới nhất: {{ errorMessage }}</span>
          <button type="button" class="button button-secondary" @click="loadBooks">Tải lại</button>
        </div>

        <div v-else-if="latestBooks.length === 0" class="section-empty">
          Chưa có sách nào được thêm vào thư viện.
        </div>

        <div v-else class="latest-editorial-grid">
          <article 
            v-for="book in latestBooks" 
            :key="book.id ?? book.title" 
            class="latest-card"
            @click="openBookDetail(book.slug)"
          >
            <div class="latest-card-cover shadow-elegant">
              <img v-if="book.coverUrl" :src="book.coverUrl" :alt="`Bìa sách ${book.title}`" loading="lazy" />
              <strong v-else class="placeholder">{{ book.initials }}</strong>
            </div>

            <div class="latest-card-info">
              <p class="latest-card-author">{{ book.publisherName }}</p>
              <h3>{{ book.title }}</h3>
              <p class="latest-card-summary">{{ book.summary }}</p>
            </div>
          </article>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.page-shell {
  min-height: 100vh;
  background-color: var(--page-bg);
  display: flex;
  flex-direction: column;
}



.topbar-nav {
  display: flex;
  gap: 2rem;
}

.topbar-nav button {
  font-family: var(--font-body);
  font-size: 0.95rem;
  color: var(--text-soft);
  font-weight: 500;
  padding: 0.25rem 0;
  transition: color 200ms ease;
}

.topbar-nav button:hover, .topbar-nav button.active {
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

/* Main layouts */
.homepage-main {
  flex: 1;
  width: 100%;
  max-width: var(--content-width);
  margin: 0 auto;
  padding: 3rem 2rem 5rem;
}

/* Split Hero styles */
.hero-split {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 4rem;
  align-items: center;
  min-height: 480px;
  margin-bottom: 6rem;
}

.hero-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1.25rem;
}

.eyebrow {
  font-family: var(--font-body);
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--accent);
  font-weight: 700;
}

.hero-headline {
  font-family: var(--font-display);
  font-size: clamp(2.5rem, 5vw, 4.2rem);
  line-height: 1.1;
  color: var(--text-strong);
  font-weight: 500;
}

.hero-text {
  font-family: var(--font-body);
  font-size: 1.1rem;
  line-height: 1.7;
  color: var(--text);
  max-width: 48ch;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.button {
  padding: 0.75rem 1.8rem;
  border-radius: 0;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 200ms ease;
}

.button-primary {
  background-color: var(--accent);
  color: var(--surface);
}

.button-primary:hover {
  background-color: var(--accent-deep);
  transform: scale(0.98);
}

.button-primary:active {
  transform: scale(0.96);
}

.button-secondary {
  border: 1px solid var(--line-strong);
  color: var(--text-strong);
}

.button-secondary:hover {
  background-color: var(--surface-soft);
}

.button-secondary:active {
  transform: scale(0.98);
}

/* Featured side card */
.hero-right {
  display: flex;
  justify-content: center;
}

.featured-book-promo {
  display: flex;
  background-color: var(--surface);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  max-width: 480px;
  cursor: pointer;
  transition: transform 250ms ease, box-shadow 250ms ease;
}

.featured-book-promo:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-strong);
}

.promo-cover {
  width: 140px;
  aspect-ratio: 2/3;
  border-radius: 4px;
  background-color: var(--surface-soft);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(43, 33, 24, 0.08);
  flex-shrink: 0;
}

.promo-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.promo-cover strong {
  font-family: var(--font-display);
  font-size: 1.8rem;
  color: var(--text-soft);
  opacity: 0.5;
}

.promo-meta {
  padding-left: 1.5rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.5rem;
}

.promo-tag {
  font-family: var(--font-body);
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--accent);
  font-weight: 700;
}

.promo-meta h3 {
  font-family: var(--font-display);
  font-size: 1.4rem;
  color: var(--text-strong);
  font-weight: 500;
  line-height: 1.25;
}

.promo-desc {
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--text-soft);
  line-height: 1.5;
}

.promo-link {
  font-family: var(--font-body);
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--accent);
}

.shadow-elegant {
  box-shadow: 0 15px 35px rgba(43, 33, 24, 0.08), 0 5px 15px rgba(43, 33, 24, 0.04);
}

/* Latest Arrivals Section */
.latest-section {
  border-top: 1px solid var(--line-soft);
  padding-top: 4rem;
}

.section-heading {
  margin-bottom: 3rem;
}

.section-kicker {
  font-family: var(--font-body);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--accent);
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.section-heading h2 {
  font-family: var(--font-display);
  font-size: 2rem;
  color: var(--text-strong);
  font-weight: 500;
}

.section-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  font-family: var(--font-body);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(43, 33, 24, 0.1);
  border-radius: 50%;
  border-top-color: var(--accent);
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.section-empty {
  padding: 3rem;
  text-align: center;
  font-family: var(--font-body);
  color: var(--text-soft);
}

/* Editorial Grid for arrivals */
.latest-editorial-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 3rem;
}

.latest-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  cursor: pointer;
}

.latest-card-cover {
  width: 100%;
  aspect-ratio: 2/3;
  border-radius: 6px;
  background-color: var(--surface-soft);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 250ms cubic-bezier(0.16, 1, 0.3, 1);
}

.latest-card:hover .latest-card-cover {
  transform: translateY(-6px);
}

.latest-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.latest-card-cover .placeholder {
  font-family: var(--font-display);
  font-size: 2.5rem;
  color: var(--text-soft);
  opacity: 0.4;
}

.latest-card-info {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.latest-card-author {
  font-family: var(--font-body);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--accent);
  font-weight: 700;
}

.latest-card-info h3 {
  font-family: var(--font-display);
  font-size: 1.25rem;
  color: var(--text-strong);
  font-weight: 500;
  line-height: 1.25;
}

.latest-card-summary {
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--text-soft);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Mobile layout media query */
@media (max-width: 868px) {
  .hero-split {
    grid-template-columns: 1fr;
    gap: 3rem;
    text-align: center;
    min-height: auto;
  }
  .hero-left {
    align-items: center;
  }
  .hero-text {
    max-width: 100%;
  }
  .latest-editorial-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 2rem;
  }
}
</style>
