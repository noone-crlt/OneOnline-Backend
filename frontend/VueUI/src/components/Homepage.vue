<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, BookOpenText, TrendingUp, Compass, MonitorSmartphone, Baby, BrainCircuit, Languages, ShoppingCart, Eye, Star, ChevronRight } from 'lucide-vue-next'
import AppFooter from './layout/AppFooter.vue'
import TopNavbar from './layout/TopNavbar.vue'
import FeaturedBooksHero from './FeaturedBooksHero.vue'
import { getBooks, getFileUrl } from '../services/api'
import { authUser } from '../stores/auth'
import gsap from 'gsap'

const router = useRouter()
const pageRoot = ref(null)
const books = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const searchInput = ref('')
const isSearchFocused = ref(false)
let motionContext
let homeMotionStarted = false

const featuredCategories = [
  { name: 'Văn học', icon: BookOpenText, count: 124 },
  { name: 'Kinh tế', icon: TrendingUp, count: 85 },
  { name: 'Kỹ năng sống', icon: Compass, count: 64 },
  { name: 'Công nghệ', icon: MonitorSmartphone, count: 42 },
  { name: 'Thiếu nhi', icon: Baby, count: 96 },
  { name: 'Trinh thám', icon: Search, count: 38 },
  { name: 'Tâm lý', icon: BrainCircuit, count: 55 },
  { name: 'Ngoại ngữ', icon: Languages, count: 72 },
]

// Resolve details and map from editions
const displayBooks = computed(() =>
  books.value.map((book, index) => {
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

    // Resolve prices
    let prices = book.editions ? book.editions.map(e => Number(e.salePrice)).filter(p => !isNaN(p)) : []
    let currentPrice = prices.length > 0 ? Math.min(...prices) : 0
    
    // Mock features for "Nổi bật" until API provides them
    const discount = 15 + (index % 3) * 5 // 15%, 20%, 25%
    const oldPrice = currentPrice > 0 ? Math.round(currentPrice / (1 - discount / 100) / 1000) * 1000 : 0
    const rating = 4.0 + (index % 10) / 10 // 4.0 -> 4.9
    const reviewCount = 85 + (index * 12)

    return {
      ...book,
      initials,
      coverUrl: cover,
      publisherName: book.publisherName || 'One Online',
      authorNamesText: book.authorNames ? book.authorNames.join(', ') : 'One Online',
      summary: summarizeDescription(book.description),
      currentPrice,
      oldPrice,
      discount,
      rating,
      reviewCount
    }
  }),
)

// In a real app, this should be sorted by a specific "Featured" score or fetched from a /api/books/featured endpoint.
// For now, we simulate this by picking the first 8 books from the fetched catalog.
const featuredBooks = computed(() => displayBooks.value.slice(0, 8))

function handleSearch(queryOverride) {
  const q = typeof queryOverride === 'string' ? queryOverride : searchInput.value.trim()
  if (q) {
    router.push({ name: 'library', query: { q } })
  }
}

// The featured book logic is now handled inside FeaturedBooksHero.vue
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
  if (!description) return '...'
  const words = description.trim().split(/\s+/)
  if (words.length <= 12) return description.trim()
  return `${words.slice(0, 12).join(' ')}...`
}

function openBookDetail(slug) {
  router.push(`/book/${slug}`)
}

function addToCart(event, book) {
  event.stopPropagation()
  // Add to cart logic will go here
  console.log('Add to cart', book.title)
}

function formatPrice(price) {
  if (!price) return 'Đang cập nhật'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(price)
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
    // Premium spring physics feel for the Bento grid reveal
    const timeline = gsap.timeline({
      defaults: {
        duration: 0.8,
        ease: 'back.out(1.2)', // Simulated spring
      },
    })

    // Staggered waterfall reveal
    timeline.from('.bento-item', {
      y: 40,
      opacity: 0,
      scale: 0.98,
      stagger: 0.1,
      duration: 1
    })
    
    // Add perpetual float to the featured book cover
    gsap.to('.featured-cover-wrapper', {
      y: -8,
      duration: 3,
      ease: 'sine.inOut',
      yoyo: true,
      repeat: -1,
      delay: 1 // Start after entrance
    })

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
  <div ref="pageRoot" class="page-shell bento-light-theme">
    <TopNavbar />

    <FeaturedBooksHero />

    <main class="homepage-main">
      <div class="bento-container">
        
        <!-- Search Palette Section -->
        <div class="search-section">
          <div class="search-palette" :class="{ 'is-focused': isSearchFocused }">
            <Search class="search-icon" :size="20" />
            <input 
              v-model="searchInput" 
              type="text" 
              class="search-input" 
              placeholder="Tìm kiếm tựa sách, tác giả, thể loại..."
              @focus="isSearchFocused = true"
              @blur="isSearchFocused = false"
              @keyup.enter="handleSearch"
            />
            <button class="search-submit" @click="handleSearch">Tìm kiếm</button>
          </div>
        </div>

        <!-- Categories Section -->
        <section class="section-container categories-section">
          <div class="section-header">
            <h2 class="section-title">Thể loại nổi bật</h2>
          </div>
          <div class="categories-grid">
            <div 
              v-for="cat in featuredCategories" 
              :key="cat.name" 
              class="category-card"
              @click="handleSearch(cat.name)"
            >
              <div class="category-icon-wrapper">
                <component :is="cat.icon" :size="28" stroke-width="1.5" class="category-icon" />
              </div>
              <div class="category-info">
                <h4 class="category-name">{{ cat.name }}</h4>
                <p class="category-count">{{ cat.count }} sách</p>
              </div>
            </div>
          </div>
        </section>

        <!-- Featured Books Section -->
        <section class="section-container featured-section">
          <div class="section-header">
            <h2 class="section-title">Sách nổi bật</h2>
            <button class="view-all-btn" @click="goToLibrary">
              Xem tất cả <ChevronRight :size="16" />
            </button>
          </div>
          
          <template v-if="isLoading">
            <div class="ecommerce-grid">
              <div v-for="i in 8" :key="`skeleton-${i}`" class="ecommerce-card skeleton-item">
                <div class="ecommerce-cover skeleton-cover"></div>
                <div class="ecommerce-info skeleton-labels">
                  <div class="skeleton-text skeleton-title"></div>
                  <div class="skeleton-text skeleton-author"></div>
                </div>
              </div>
            </div>
          </template>

          <div v-else-if="errorMessage" class="state-message">
            <p>Không thể tải kệ sách.</p>
          </div>

          <div v-else-if="featuredBooks.length === 0" class="state-message">
            <p>Chưa có sách nổi bật.</p>
          </div>

          <template v-else>
            <div class="ecommerce-grid">
              <div 
                v-for="book in featuredBooks" 
                :key="book.id ?? book.title" 
                class="ecommerce-card"
                @click="openBookDetail(book.slug)"
              >
                <!-- Image & Badges -->
                <div class="ecommerce-cover-wrapper">
                  <span v-if="book.discount > 0" class="discount-badge">-{{ book.discount }}%</span>
                  <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" loading="lazy" class="ecommerce-img" />
                  <div v-else class="ecommerce-fallback">{{ book.initials }}</div>
                  
                  <!-- Hover Overlay Actions -->
                  <div class="ecommerce-overlay">
                    <button class="action-btn quick-view-btn" @click.stop="openBookDetail(book.slug)">
                      <Eye :size="18" /> Xem chi tiết
                    </button>
                  </div>
                </div>

                <!-- Info -->
                <div class="ecommerce-info">
                  <div class="rating-row">
                    <Star class="star-icon" :size="14" fill="#fbbf24" color="#fbbf24" />
                    <span class="rating-score">{{ book.rating.toFixed(1) }}</span>
                    <span class="review-count">({{ book.reviewCount }})</span>
                  </div>
                  
                  <h4 class="book-title" :title="book.title">{{ book.title }}</h4>
                  <p class="book-author">{{ book.authorNamesText }}</p>
                  
                  <div class="price-row">
                    <div class="prices">
                      <span class="current-price">{{ formatPrice(book.currentPrice) }}</span>
                      <span v-if="book.oldPrice" class="old-price">{{ formatPrice(book.oldPrice) }}</span>
                    </div>
                  </div>
                  
                  <!-- Add to cart -->
                  <button class="add-to-cart-btn" @click.stop="addToCart($event, book)">
                    <ShoppingCart :size="16" /> Thêm vào giỏ
                  </button>
                </div>
              </div>
            </div>
          </template>
        </section>

      </div>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
/* 
  BENTO 2.0 LIGHT THEME (Vercel-core / Dribbble-clean)
*/
.bento-light-theme {
  --page-bg: #f9fafb;
  --card-bg: #ffffff;
  --card-border: rgba(226, 232, 240, 0.6);
  --text-main: #111827;
  --text-muted: #64748b;
  --accent: #000000;
  
  --radius-bento: 2.5rem;
  --radius-sm: 1.5rem;
  --shadow-bento: 0 20px 40px -15px rgba(0,0,0,0.05), 0 4px 10px rgba(0,0,0,0.02);
  --shadow-hover: 0 30px 60px -15px rgba(0,0,0,0.08), 0 4px 20px rgba(0,0,0,0.04);
  
  background-color: var(--page-bg);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  /* Enforce clean sans-serif */
  font-family: 'Geist', 'SF Pro Display', 'Inter', system-ui, sans-serif;
}

/* Ensure the navbar matches the clean aesthetic */
:deep(.topbar) {
  background-color: rgba(249, 250, 251, 0.8) !important;
  backdrop-filter: blur(12px) !important;
  border-bottom: 1px solid var(--card-border) !important;
}

.homepage-main {
  flex: 1;
  width: 100%;
  padding: 4rem 2rem 8rem;
  max-width: 1300px;
  margin: 0 auto;
}

.bento-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* --- Search Palette (design-taste-frontend-v1) --- */
.search-section {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-bottom: 2rem;
}

.search-palette {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 720px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 99px;
  padding: 0.5rem 0.5rem 0.5rem 1.5rem;
  box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.03), 0 0 0 0 rgba(0, 0, 0, 0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-palette.is-focused {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(0, 0, 0, 0.15);
  box-shadow: 0 8px 30px -4px rgba(0, 0, 0, 0.08), 0 0 0 3px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.search-icon {
  color: #64748b;
  flex-shrink: 0;
  transition: color 0.3s ease;
}

.search-palette.is-focused .search-icon {
  color: #0f172a;
}

.search-input {
  flex-grow: 1;
  background: transparent;
  border: none;
  outline: none;
  font-family: 'Inter', sans-serif;
  font-size: 1.05rem;
  color: #0f172a;
  padding: 0.75rem 1rem;
}

.search-input::placeholder {
  color: #94a3b8;
}

.search-submit {
  background: #0f172a;
  color: white;
  border: none;
  border-radius: 99px;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-submit:hover {
  background: #334155;
  transform: scale(1.02);
}

.search-submit:active {
  transform: scale(0.98);
}

.bento-grid {
  display: grid;
  gap: 2rem;
}

.bottom-row {
  grid-template-columns: repeat(4, 1fr);
  margin-top: 1rem;
}

/* --- Cards --- */
.bento-card {
  background-color: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-bento);
  box-shadow: var(--shadow-bento);
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.4s cubic-bezier(0.16, 1, 0.3, 1), transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.gallery-item {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.gallery-item:hover .bento-card,
.hero-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-4px);
}

.gallery-item:active .bento-card {
  transform: scale(0.98);
}

/* --- Hero Card --- */
.hero-card {
  height: 100%;
  min-height: 480px;
  padding: 3.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-content {
  max-width: 32rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1.5rem;
  z-index: 2;
}

.hero-badge {
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--text-main);
  background: #f1f5f9;
  padding: 0.4rem 0.8rem;
  border-radius: 99px;
  border: 1px solid #e2e8f0;
}

.hero-headline {
  font-size: clamp(3rem, 5vw, 4.5rem);
  font-weight: 600;
  line-height: 1.05;
  letter-spacing: -0.04em;
  color: var(--text-main);
}

.hero-subtitle {
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--text-muted);
}

.hero-btn {
  background: var(--text-main);
  color: #fff;
  padding: 0.85rem 1.8rem;
  border-radius: 99px;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.hero-btn:hover {
  background: #374151;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
}

.hero-btn:active {
  transform: translateY(0) scale(0.97);
}

/* Abstract visual inside hero for aesthetics */
.hero-visual {
  position: absolute;
  right: -5%;
  bottom: -10%;
  width: 400px;
  height: 400px;
  display: flex;
  gap: 1.5rem;
  transform: rotate(-10deg);
  opacity: 0.7;
  pointer-events: none;
}

.abstract-book {
  width: 120px;
  height: 100%;
  background: linear-gradient(135deg, #f8fafc, #e2e8f0);
  border-radius: 12px;
  box-shadow: -10px 10px 30px rgba(0,0,0,0.06);
  border: 1px solid #cbd5e1;
}
.b1 { transform: translateY(40px); }
.b2 { transform: translateY(0); background: linear-gradient(135deg, #ffffff, #f1f5f9); }
.b3 { transform: translateY(80px); }


/* --- Featured Card --- */
.featured-card {
  aspect-ratio: 1 / 1.1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  /* Inner glass effect */
  background: linear-gradient(145deg, #ffffff, #f8fafc);
}

.featured-cover-wrapper {
  width: 55%;
  aspect-ratio: 2/3;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25);
  border: 1px solid rgba(0,0,0,0.05);
  background: #e2e8f0;
}

.featured-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.featured-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  font-weight: 600;
  color: #94a3b8;
}

/* --- Shared Section Header --- */
.section-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1rem 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-main);
  letter-spacing: -0.02em;
}

.view-all-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: none;
  border: none;
  color: #3b82f6;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  padding: 0.5rem;
  transition: opacity 0.2s ease;
}

.view-all-btn:hover {
  opacity: 0.7;
}

/* --- Categories Grid --- */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
}

.category-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-sm);
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1.25rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}

.category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px -6px rgba(0,0,0,0.06);
  border-color: rgba(0,0,0,0.1);
}

.category-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  background: #f1f5f9;
  border-radius: 12px;
  color: #334155;
  transition: all 0.3s ease;
}

.category-card:hover .category-icon-wrapper {
  background: #0f172a;
  color: #ffffff;
}

.category-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.category-name {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text-main);
}

.category-count {
  font-size: 0.85rem;
  color: var(--text-muted);
}

/* --- E-commerce Books Grid (Sách nổi bật) --- */
.ecommerce-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem 1.5rem;
}

.ecommerce-card {
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
  border-radius: 1rem;
  transition: transform 0.3s ease;
  cursor: pointer;
}

.ecommerce-card:hover {
  transform: translateY(-4px);
}

.ecommerce-cover-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 2/3;
  border-radius: 0.75rem;
  overflow: hidden;
  background: #f1f5f9;
  border: 1px solid var(--card-border);
  margin-bottom: 1rem;
}

.discount-badge {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  background: #ef4444;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  z-index: 2;
}

.ecommerce-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.ecommerce-card:hover .ecommerce-img {
  transform: scale(1.05);
}

.ecommerce-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  color: #cbd5e1;
}

.ecommerce-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 2;
}

.ecommerce-card:hover .ecommerce-overlay {
  opacity: 1;
}

.quick-view-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255,255,255,0.95);
  color: #0f172a;
  border: none;
  padding: 0.6rem 1.25rem;
  border-radius: 99px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transform: translateY(10px);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.ecommerce-card:hover .quick-view-btn {
  transform: translateY(0);
}

.quick-view-btn:hover {
  background: #ffffff;
  transform: scale(1.05) !important;
}

.ecommerce-info {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.rating-score {
  font-weight: 600;
  color: #d97706;
}

.book-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text-main);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 2.6rem;
}

.book-author {
  font-size: 0.85rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price-row {
  margin-top: 0.5rem;
  margin-bottom: 0.75rem;
}

.prices {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
}

.current-price {
  font-size: 1.15rem;
  font-weight: 700;
  color: #0f172a;
}

.old-price {
  font-size: 0.85rem;
  color: #94a3b8;
  text-decoration: line-through;
}

.add-to-cart-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  background: #f1f5f9;
  color: #0f172a;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0.75rem;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.add-to-cart-btn:hover {
  background: #0f172a;
  color: #ffffff;
  border-color: #0f172a;
}

.state-message {
  grid-column: 1 / -1;
  padding: 4rem 0;
  text-align: center;
  color: var(--text-muted);
  font-weight: 500;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #e2e8f0;
  border-top-color: var(--text-main);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* --- Skeleton & Blur Effects --- */
.skeleton-item {
  pointer-events: none;
  filter: blur(2px);
  opacity: 0.7;
}

.skeleton-card {
  background: #f1f5f9;
  border-color: transparent;
  box-shadow: none;
}

.skeleton-cover {
  width: 100%;
  aspect-ratio: 2/3;
  border-radius: 0.75rem;
  background: linear-gradient(90deg, #e2e8f0 25%, #f8fafc 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 2s infinite linear;
}

.skeleton-labels {
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.skeleton-text {
  background: linear-gradient(90deg, #e2e8f0 25%, #f8fafc 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 2s infinite linear;
  border-radius: 4px;
}

.skeleton-tag { width: 60px; height: 12px; margin-bottom: 0.5rem; }
.skeleton-title { width: 70%; height: 24px; margin-bottom: 0.3rem; }
.skeleton-desc { width: 90%; height: 32px; }

.skeleton-author { width: 50%; height: 10px; margin-bottom: 0.25rem; }
.skeleton-title-sm { width: 80%; height: 16px; }

@keyframes skeleton-shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}

/* --- Responsive Behavior --- */
@media (max-width: 1024px) {
  .categories-grid, .ecommerce-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .categories-grid, .ecommerce-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .homepage-main {
    padding: 2rem 1rem;
  }
  
  .bento-container {
    gap: 1.5rem;
  }

  .search-palette {
    padding: 0.35rem 0.35rem 0.35rem 1rem;
  }
  
  .search-input {
    font-size: 0.95rem;
    padding: 0.5rem 0.5rem;
  }

  .search-submit {
    padding: 0.6rem 1.25rem;
    font-size: 0.85rem;
  }

  .categories-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .category-card {
    padding: 1rem;
    gap: 1rem;
    flex-direction: column;
    text-align: center;
    justify-content: center;
  }

  .category-icon-wrapper {
    width: 44px;
    height: 44px;
  }

  .category-name {
    font-size: 0.95rem;
  }

  .ecommerce-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem 1rem;
  }

  .current-price {
    font-size: 1.05rem;
  }

  .old-price {
    font-size: 0.8rem;
  }

  .add-to-cart-btn {
    padding: 0.6rem;
    font-size: 0.85rem;
  }
}
</style>
