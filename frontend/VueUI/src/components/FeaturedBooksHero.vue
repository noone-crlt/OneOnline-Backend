<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, ArrowUpRight, BookOpen } from 'lucide-vue-next'
import { getBooks, getFileUrl } from '../services/api'

const router = useRouter()
const books = ref([])
const activeIndex = ref(0)
const isAnimating = ref(false)
const isLoading = ref(true)
const isMobile = ref(false)
const errorMessage = ref('')

const BACKGROUND_COLORS = [
  '#D76545', '#4B6B8A', '#8B6F47', '#586B4D', '#715B83', '#B37958'
]

const GHOST_COLORS = [
  'rgba(255, 239, 230, 0.75)',
  'rgba(215, 232, 255, 0.75)',
  'rgba(255, 232, 190, 0.75)',
  'rgba(221, 242, 210, 0.75)',
  'rgba(237, 218, 255, 0.75)',
  'rgba(255, 220, 205, 0.75)',
]

// Fetch and normalize books
async function fetchFeaturedBooks() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const data = await getBooks()
    
    // Select up to 6 books that have covers
    const validBooks = (Array.isArray(data) ? data : [])
      .map((book, idx) => {
        let coverUrl = ''
        if (book.editions && book.editions.length > 0) {
          const coverEd = book.editions.find(e => e.coverObjectName)
          if (coverEd) coverUrl = coverEd.coverUrl || getFileUrl(coverEd.coverObjectName)
        }
        if (!coverUrl && book.imageUrls && book.imageUrls.length > 0) {
          coverUrl = getFileUrl(book.imageUrls[0])
        }
        
        return {
          id: book.id || book.slug || idx,
          title: book.title,
          author: book.publisherName || 'One Online', // Fallback for author
          description: book.description || '',
          coverUrl: coverUrl,
          slug: book.slug,
          backgroundColor: BACKGROUND_COLORS[idx % BACKGROUND_COLORS.length],
          ghostColor: GHOST_COLORS[idx % GHOST_COLORS.length]
        }
      })
      .filter(b => b.coverUrl)
      .slice(0, 6)

    if (validBooks.length === 0) {
      throw new Error('No valid featured books found')
    }
    
    books.value = validBooks
    
    // Preload images
    validBooks.forEach(b => {
      const img = new Image()
      img.src = b.coverUrl
    })

  } catch (err) {
    // Fallback data if API fails or is empty
    books.value = [
      { id: 'f1', title: 'Mắt Biếc', author: 'Nguyễn Nhật Ánh', description: 'Câu chuyện tình yêu lãng mạn và đầy tiếc nuối...', coverUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop', slug: 'mat-biec', backgroundColor: BACKGROUND_COLORS[0], ghostColor: GHOST_COLORS[0] },
      { id: 'f2', title: 'Nhà Giả Kim', author: 'Paulo Coelho', description: 'Hành trình theo đuổi giấc mơ và lắng nghe trái tim...', coverUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop', slug: 'nha-gia-kim', backgroundColor: BACKGROUND_COLORS[1], ghostColor: GHOST_COLORS[1] },
      { id: 'f3', title: 'Đắc Nhân Tâm', author: 'Dale Carnegie', description: 'Nghệ thuật thu phục lòng người...', coverUrl: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?q=80&w=600&auto=format&fit=crop', slug: 'dac-nhan-tam', backgroundColor: BACKGROUND_COLORS[2], ghostColor: GHOST_COLORS[2] },
      { id: 'f4', title: 'Harry Potter', author: 'J.K. Rowling', description: 'Thế giới phép thuật đầy màu sắc...', coverUrl: 'https://images.unsplash.com/photo-1626618011154-1b5e0ce245a8?q=80&w=600&auto=format&fit=crop', slug: 'harry-potter', backgroundColor: BACKGROUND_COLORS[3], ghostColor: GHOST_COLORS[3] }
    ]
  } finally {
    isLoading.value = false
    startAutoplay()
  }
}

// Circular Index Math
function getRelativeIndex(index) {
  const len = books.value.length
  if (len === 0) return 0
  let diff = index - activeIndex.value
  const half = len / 2
  if (diff > half) diff -= len
  else if (diff <= -half) diff += len
  return diff
}

function getItemStyle(index) {
  const diff = getRelativeIndex(index)
  const absDiff = Math.abs(diff)
  const sign = Math.sign(diff)

  // Mobile limits the spacing and scale differently
  const translateBase = isMobile.value ? 90 : 105
  const rotateBase = 8
  
  const xOffset = isMobile.value ? 0 : 25 // shift 25% of its own width to right on desktop
  const yOffset = -46 // -46%

  if (absDiff === 0) {
    return {
      left: '50%',
      top: '50%',
      transform: `translate(calc(-50% + ${xOffset}%), ${yOffset}%) scale(1) rotate(0deg)`,
      opacity: 1,
      zIndex: 10,
      filter: 'blur(0px)'
    }
  } else if (absDiff === 1 || (books.value.length === 2 && absDiff > 0)) {
    return {
      left: '50%',
      top: '50%',
      transform: `translate(calc(-50% + ${xOffset}% + ${sign * translateBase}%), ${yOffset}%) scale(0.75) rotate(${sign * rotateBase}deg)`,
      opacity: isMobile.value ? 0.35 : 0.65,
      zIndex: 5,
      filter: 'blur(2px)',
      cursor: 'pointer'
    }
  } else {
    return {
      left: '50%',
      top: '50%',
      transform: `translate(calc(-50% + ${xOffset}% + ${sign * (translateBase * 1.5)}%), ${yOffset}%) scale(0.5) rotate(${sign * 12}deg)`,
      opacity: 0,
      zIndex: 1,
      filter: 'blur(4px)',
      pointerEvents: 'none'
    }
  }
}

const activeBook = computed(() => books.value[activeIndex.value] || null)
const activeBgColor = computed(() => activeBook.value ? activeBook.value.backgroundColor : '#1e293b')
const activeGhostColor = computed(() => activeBook.value ? activeBook.value.ghostColor : 'rgba(255,255,255,0.75)')

const ghostStrokeStyle = computed(() => {
  const width = isMobile.value ? 'clamp(1px, 0.5vw, 1.5px)' : 'clamp(1px, 0.15vw, 2.5px)'
  return `${width} ${activeGhostColor.value}`
})

let autoplayInterval = null

function startAutoplay() {
  stopAutoplay()
  if (books.value.length <= 1) return
  autoplayInterval = setInterval(() => {
    next()
  }, 5000)
}

function stopAutoplay() {
  if (autoplayInterval) {
    clearInterval(autoplayInterval)
    autoplayInterval = null
  }
}

function next() {
  if (isAnimating.value || books.value.length <= 1) return
  isAnimating.value = true
  activeIndex.value = (activeIndex.value + 1) % books.value.length
  setTimeout(() => { isAnimating.value = false }, 650)
  startAutoplay()
}

function prev() {
  if (isAnimating.value || books.value.length <= 1) return
  isAnimating.value = true
  activeIndex.value = (activeIndex.value - 1 + books.value.length) % books.value.length
  setTimeout(() => { isAnimating.value = false }, 650)
  startAutoplay()
}

function selectBook(index) {
  const diff = getRelativeIndex(index)
  if (diff === 1) next()
  else if (diff === -1) prev()
}

function handleKeyDown(e) {
  if (e.key === 'ArrowRight') next()
  else if (e.key === 'ArrowLeft') prev()
}

// Swipe detection
let touchStartX = 0
function onTouchStart(e) {
  touchStartX = e.changedTouches[0].screenX
}
function onTouchEnd(e) {
  const touchEndX = e.changedTouches[0].screenX
  if (touchStartX - touchEndX > 50) next()
  else if (touchEndX - touchStartX > 50) prev()
}

function handleResize() {
  isMobile.value = window.innerWidth < 768
}

function viewDetail(slug) {
  router.push(`/book/${slug}`)
}
function goLibrary() {
  router.push('/library')
}

function truncateDesc(text) {
  if (!text) return ''
  const lines = isMobile.value ? 60 : 120
  return text.length > lines ? text.substring(0, lines) + '...' : text
}

onMounted(() => {
  fetchFeaturedBooks()
  handleResize()
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeyDown)
  stopAutoplay()
})
</script>

<template>
  <div 
    class="hero-container" 
    :style="{ backgroundColor: activeBgColor }"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
    @mouseenter="stopAutoplay"
    @mouseleave="startAutoplay"
  >
    <!-- SVG Grain Overlay -->
    <div class="grain-overlay"></div>

    <!-- Giant Ghost Text -->
    <div 
      class="ghost-text" 
      :style="{ WebkitTextStroke: ghostStrokeStyle }"
      aria-hidden="true"
    >
      {{ isMobile ? 'ONE' : 'ONE ONLINE' }}
    </div>

    <!-- Brand Label -->
    <div class="brand-label">ONEONLINE</div>

    <!-- Main Carousel Area -->
    <div v-if="!isLoading && books.length > 0" class="carousel-wrapper">
      
      <!-- Books Layer -->
      <div class="carousel-track">
        <div 
          v-for="(book, index) in books" 
          :key="book.id"
          class="carousel-item"
          :style="getItemStyle(index)"
          @click="getRelativeIndex(index) !== 0 ? selectBook(index) : null"
        >
          <img 
            :src="book.coverUrl" 
            :alt="book.title" 
            class="book-cover"
            draggable="false"
            :loading="index === activeIndex ? 'eager' : 'lazy'"
          />
        </div>
      </div>

      <!-- Info Layer (Active Book) -->
      <transition name="fade-slide" mode="out-in">
        <div class="book-info" :key="activeBook.id" v-if="activeBook">
          <div class="book-meta">
            <span class="book-author">{{ activeBook.author }}</span>
            <span class="book-index">{{ String(activeIndex + 1).padStart(2, '0') }} / {{ String(books.length).padStart(2, '0') }}</span>
          </div>
          <h2 class="book-title">{{ activeBook.title }}</h2>
          <p class="book-desc">{{ truncateDesc(activeBook.description) }}</p>
          
          <div class="book-actions">
            <button class="action-btn primary" @click="viewDetail(activeBook.slug)">
              <BookOpen class="icon" :size="18" />
              Xem chi tiết
            </button>
          </div>
        </div>
      </transition>

      <!-- Navigation Arrows -->
      <div class="nav-arrows" v-if="books.length > 1">
        <button class="nav-btn prev" @click="prev" :disabled="isAnimating" aria-label="Previous Book">
          <ArrowLeft :size="24" />
        </button>
        <button class="nav-btn next" @click="next" :disabled="isAnimating" aria-label="Next Book">
          <ArrowRight :size="24" />
        </button>
      </div>

      <!-- Bottom Right Link -->
      <button class="explore-link" @click="goLibrary">
        KHÁM PHÁ THƯ VIỆN <ArrowUpRight :size="20" class="explore-icon" />
      </button>

    </div>
    
    <!-- Loading State -->
    <div v-else-if="isLoading" class="loading-state">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<style scoped>
/* Base Container */
.hero-container {
  position: relative;
  width: 100%;
  min-height: 100svh;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
  transition: background-color 650ms cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 0;
}

/* SVG Grain Overlay */
.grain-overlay {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  opacity: 0.4;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.08'/%3E%3C/svg%3E");
  background-size: 200px 200px;
  background-repeat: repeat;
}

/* Giant Ghost Text */
.ghost-text {
  position: absolute;
  inset-x: 0;
  top: 50%;
  transform: translateY(-58%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  pointer-events: none;
  user-select: none;
  z-index: 1;
  font-family: 'Anton', sans-serif;
  font-size: clamp(72px, 18vw, 250px);
  font-weight: 400;
  color: transparent;
  opacity: 0.3;
  line-height: 0.9;
  text-transform: uppercase;
  letter-spacing: -0.02em;
  white-space: nowrap;
  transition: opacity 650ms cubic-bezier(0.4,0,0.2,1), -webkit-text-stroke-color 650ms cubic-bezier(0.4,0,0.2,1);
}

/* Brand Label */
.brand-label {
  position: absolute;
  top: 1.5rem;
  left: 2rem;
  z-index: 60;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  color: white;
  opacity: 0.9;
  letter-spacing: 0.18em;
}

/* Carousel Layout */
.carousel-wrapper {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-track {
  position: absolute;
  inset: 0;
  perspective: 1000px;
}

.carousel-item {
  position: absolute;
  will-change: transform, opacity;
  transition: all 650ms cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  justify-content: center;
  align-items: center;
  transform-origin: center bottom;
}

.book-cover {
  height: 46vh;
  max-height: 560px;
  width: auto;
  max-width: 34vw;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 30px 60px -12px rgba(0,0,0,0.4), 0 18px 36px -18px rgba(0,0,0,0.3);
  /* Subtly separate cover from background */
  border: 1px solid rgba(255,255,255,0.1);
}

@media (min-width: 768px) {
  .book-cover {
    height: 58vh;
  }
}

/* Active Book Info */
.book-info {
  position: absolute;
  bottom: 8%;
  left: 8%;
  max-width: 400px;
  color: white;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.book-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  opacity: 0.9;
  font-weight: 600;
}

.book-index {
  font-family: 'Anton', sans-serif;
  letter-spacing: 0.1em;
  opacity: 0.7;
}

.book-title {
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.02em;
  text-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.book-desc {
  font-size: 1rem;
  line-height: 1.6;
  opacity: 0.85;
  margin-bottom: 0.5rem;
}

.book-actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.85rem 1.5rem;
  border-radius: 99px;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.action-btn.primary {
  background: white;
  color: #000;
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
}

.action-btn.primary:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 12px 24px rgba(0,0,0,0.2);
}

.action-btn.primary:active {
  transform: translateY(0) scale(0.98);
}

/* Nav Arrows */
.nav-arrows {
  position: absolute;
  bottom: 8%;
  right: 8%;
  display: flex;
  gap: 1rem;
  z-index: 20;
}

.nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.2);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn:hover:not(:disabled) {
  background: rgba(255,255,255,0.2);
  transform: scale(1.05);
}

.nav-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Bottom Right Explore Link */
.explore-link {
  position: absolute;
  top: 1.5rem;
  right: 2rem;
  z-index: 60;
  font-family: 'Anton', sans-serif;
  font-size: 1rem;
  color: white;
  letter-spacing: 0.15em;
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  opacity: 0.9;
  transition: all 0.3s ease;
}

.explore-link:hover {
  opacity: 1;
}

.explore-link:hover .explore-icon {
  transform: translate(2px, -2px);
}

.explore-icon {
  transition: transform 0.3s ease;
}

/* Info Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 400ms cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Loading State */
.loading-state {
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 1024px) {
  .book-cover {
    height: 45vh;
  }
}

@media (max-width: 768px) {
  .brand-label, .explore-link {
    top: 1rem;
  }
  .brand-label { left: 1rem; }
  .explore-link { right: 1rem; font-size: 0.875rem; }
  
  .ghost-text {
    font-size: clamp(90px, 32vw, 150px);
    opacity: 0.2;
  }

  .carousel-track {
    height: 100%;
    margin-top: 0;
  }
  .book-cover {
    height: 42svh;
    max-width: 72vw;
  }

  .book-info {
    bottom: 5%;
    left: 1rem;
    right: 1rem;
    text-align: center;
    align-items: center;
    max-width: none;
  }
  
  .book-actions {
    justify-content: center;
  }
  
  .nav-arrows {
    bottom: calc(5% + 180px); /* Position above text */
    right: 50%;
    transform: translateX(50%);
    width: 100%;
    justify-content: space-between;
    padding: 0 1rem;
  }
  
  .nav-btn {
    width: 44px;
    height: 44px;
  }
}
</style>
