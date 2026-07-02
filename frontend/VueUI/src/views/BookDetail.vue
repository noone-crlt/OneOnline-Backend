<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addCartItem, getBookBySlug, getFileUrl } from '../services/api'
import { toast } from 'vue-sonner'
import TopNavbar from '../components/layout/TopNavbar.vue'
import AppFooter from '../components/layout/AppFooter.vue'
import { authUser } from '../stores/auth'
import { fetchCartItemCount } from '../stores/cart'

const route = useRoute()
const router = useRouter()

const book = ref(null)
const isLoading = ref(true)
const errorMessage = ref('')
const selectedEditionIndex = ref(0)
const activeTab = ref('description') // 'description', 'chapters', 'reviews'

const isSignedIn = computed(() => Boolean(authUser.value))

const selectedEdition = computed(() => {
  if (!book.value?.editions || book.value.editions.length === 0) return null
  return book.value.editions[selectedEditionIndex.value] || book.value.editions[0]
})

const coverUrl = computed(() => {
  // Try selected edition cover first
  if (selectedEdition.value?.coverObjectName) {
    return selectedEdition.value.coverUrl || getFileUrl(selectedEdition.value.coverObjectName)
  }
  // Try main book images next
  if (book.value?.imageUrls && book.value.imageUrls.length > 0) {
    return getFileUrl(book.value.imageUrls[0])
  }
  return ''
})

const getInitials = (title) => {
  if (!title) return 'BA'
  return title.trim().split(/\s+/).slice(0, 2).map(w => w.charAt(0)).join('').toUpperCase()
}

function formatPrice(price) {
  if (price == null) return 'Nhiều định dạng'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(Number(price))
}

function selectEdition(index) {
  selectedEditionIndex.value = index
}

function formatFormatName(format) {
  switch (format) {
    case 'PHYSICAL': return 'Sách giấy'
    case 'EBOOK_PDF': return 'PDF E-book'
    case 'EBOOK_EPUB': return 'EPUB E-book'
    case 'AUDIOBOOK': return 'Sách nói'
    default: return format
  }
}

function getFormatIcon(format) {
  switch (format) {
    case 'PHYSICAL': return 'PHYS'
    case 'EBOOK_PDF': return 'PDF'
    case 'EBOOK_EPUB': return 'EPUB'
    case 'AUDIOBOOK': return 'AUDIO'
    default: return 'BOOK'
  }
}

function getStockLabel(edition) {
  if (!edition) return 'Đang cập nhật'
  if (edition.format !== 'PHYSICAL') return 'Đọc trực tuyến ngay'
  if (edition.stock == null || edition.stock <= 0) return 'Tạm hết hàng'
  return `Còn lại ${edition.stock} cuốn`
}

function readSample() {
  if (book.value) {
    router.push(`/read/${book.value.slug}`)
  }
}

async function addToCart() {
  if (!isSignedIn.value) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await addCartItem(selectedEdition.value.id, 1)
    await fetchCartItemCount()
    toast.success('Đã thêm sách vào giỏ hàng thành công!')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'Không thể thêm sách vào giỏ hàng.')
  }
}

async function buyNow() {
  if (!isSignedIn.value) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await addCartItem(selectedEdition.value.id, 1)
    router.push({ name: 'cart' })
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'Không thể bắt đầu thanh toán.')
  }
}

const reviews = ref([
  {
    id: 1,
    userName: 'Nguyễn Văn Hải',
    rating: 5,
    createdAt: '2026-06-20',
    comment: 'Tác phẩm dịch rất mượt mà, bìa sách tối giản sang trọng. Trải nghiệm đọc trực tuyến cực kỳ dễ chịu cho mắt.'
  },
  {
    id: 2,
    userName: 'Trần Thị Mai',
    rating: 4,
    createdAt: '2026-06-25',
    comment: 'Nội dung rất hay, ý nghĩa sâu sắc. Sách nói giọng đọc truyền cảm, âm thanh chất lượng tốt. Sẽ tiếp tục mua thêm.'
  }
])

const newRating = ref(5)
const newComment = ref('')

function submitReview() {
  if (!newComment.value.trim()) {
    toast.error('Vui lòng nhập nội dung đánh giá.')
    return
  }
  
  reviews.value.unshift({
    id: Date.now(),
    userName: authUser.value?.fullName || 'Khách viếng thăm',
    rating: newRating.value,
    createdAt: new Date().toISOString().split('T')[0],
    comment: newComment.value.trim()
  })
  
  newComment.value = ''
  newRating.value = 5
  toast.success('Cảm ơn bạn đã gửi đánh giá thành công!')
}

async function fetchBookDetail() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const slug = route.params.slug
    const data = await getBookBySlug(slug)
    book.value = data
    // Default to the first ebook/audiobook format if available, or first format overall
    if (data.editions && data.editions.length > 0) {
      const bestIndex = data.editions.findIndex(e => e.format !== 'PHYSICAL')
      selectedEditionIndex.value = bestIndex !== -1 ? bestIndex : 0
    }
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : 'Không thể tải chi tiết sách.'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchBookDetail()
})
</script>

<template>
  <div class="book-detail-shell">
    <TopNavbar />

    <main class="detail-container">
      <div v-if="isLoading" class="detail-loading">
        <div class="spinner"></div>
        <p>Đang chuẩn bị trang tác phẩm...</p>
      </div>

      <div v-else-if="errorMessage" class="detail-error">
        <p class="error-msg">{{ errorMessage }}</p>
        <button class="btn btn-secondary" @click="router.push('/library')">Quay lại thư viện</button>
      </div>

      <div v-else class="detail-wrapper">
        <!-- Breadcrumb navigation -->
        <nav class="breadcrumb" aria-label="Breadcrumb">
          <RouterLink to="/">Trang chủ</RouterLink>
          <span class="sep">/</span>
          <RouterLink to="/library">Thư viện</RouterLink>
          <span class="sep">/</span>
          <span class="current">{{ book.title }}</span>
        </nav>

        <!-- Product Grid -->
        <div class="product-grid">
          <!-- Left: Elegant Book Cover -->
          <div class="cover-column">
            <div class="book-cover-container">
              <div class="book-cover shadow-elegant">
                <img v-if="coverUrl" :src="coverUrl" :alt="`Bìa sách ${book.title}`" />
                <span v-else class="cover-initials">{{ getInitials(book.title) }}</span>
              </div>
            </div>
            
            <!-- Category Tag Pills -->
            <div class="categories-tags" v-if="book.categoryNames && book.categoryNames.length > 0">
              <span v-for="cat in book.categoryNames" :key="cat" class="cat-pill">
                {{ cat }}
              </span>
            </div>
          </div>

          <!-- Right: Book Info & E-commerce actions -->
          <div class="info-column">
            <div class="book-header-section">
              <p class="publisher-meta">{{ book.publisherName || 'Thư viện số' }}</p>
              <h1 class="book-title-heading">{{ book.title }}</h1>
              
              <div class="authors-list" v-if="book.authorNames && book.authorNames.length > 0">
                <span>Tác giả:</span>
                <strong>{{ book.authorNames.join(', ') }}</strong>
              </div>
            </div>

            <!-- Format Selector (E-commerce UX) -->
            <div class="formats-section" v-if="book.editions && book.editions.length > 0">
              <h3>Chọn định dạng sách</h3>
              <div class="format-options">
                <button
                  v-for="(ed, idx) in book.editions"
                  :key="ed.id"
                  :class="['format-btn', { active: selectedEditionIndex === idx }]"
                  @click="selectEdition(idx)"
                >
                  <span class="format-icon">{{ getFormatIcon(ed.format) }}</span>
                  <span class="format-name">{{ formatFormatName(ed.format) }}</span>
                </button>
              </div>
            </div>

            <!-- Price & Availability Section -->
            <div class="pricing-section" v-if="selectedEdition">
              <div class="price-row">
                <span class="sale-price">{{ formatPrice(selectedEdition.salePrice) }}</span>
                <span class="original-price" v-if="selectedEdition.originalPrice && selectedEdition.originalPrice > selectedEdition.salePrice">
                  {{ formatPrice(selectedEdition.originalPrice) }}
                </span>
              </div>
              <div class="availability-row">
                <span class="status-indicator"></span>
                <span class="availability-text">{{ getStockLabel(selectedEdition) }}</span>
              </div>
            </div>

            <!-- Tactile Action Grid -->
            <div class="actions-section">
              <button 
                class="btn btn-primary btn-cta-buy" 
                @click="buyNow" 
                :disabled="selectedEdition?.format === 'PHYSICAL' && selectedEdition?.stock <= 0"
              >
                Mua ngay
              </button>
              <button 
                class="btn btn-secondary btn-cta-cart" 
                @click="addToCart" 
                :disabled="selectedEdition?.format === 'PHYSICAL' && selectedEdition?.stock <= 0"
              >
                Thêm vào giỏ hàng
              </button>
              <button 
                v-if="selectedEdition?.format !== 'PHYSICAL'"
                class="btn btn-outline btn-cta-read" 
                @click="readSample"
              >
                Đọc thử
              </button>
            </div>

            <!-- Info Tab Sections (Editorial Style) -->
            <div class="tabs-section">
              <div class="tabs-header">
                <button 
                  :class="['tab-title', { active: activeTab === 'description' }]" 
                  @click="activeTab = 'description'"
                >
                  Tóm tắt & Giới thiệu
                </button>
                <button 
                  v-if="selectedEdition?.format === 'AUDIOBOOK' && selectedEdition?.audioChapters?.length > 0"
                  :class="['tab-title', { active: activeTab === 'chapters' }]" 
                  @click="activeTab = 'chapters'"
                >
                  Mục lục chương
                </button>
                <button 
                  :class="['tab-title', { active: activeTab === 'reviews' }]" 
                  @click="activeTab = 'reviews'"
                >
                  Đánh giá & Bình luận ({{ reviews.length }})
                </button>
              </div>

              <div class="tab-content">
                <div v-if="activeTab === 'description'" class="tab-description">
                  <p class="description-text">{{ book.description || 'Chưa có tóm tắt chi tiết cho tác phẩm này.' }}</p>
                </div>

                <div v-else-if="activeTab === 'chapters'" class="tab-chapters">
                  <ul class="chapters-grid">
                    <li v-for="ch in selectedEdition.audioChapters" :key="ch.id" class="chapter-item">
                      <span class="ch-num">#{ ch.chapterNumber }</span>
                      <span class="ch-name">{{ ch.title }}</span>
                      <span class="ch-dur">{{ ch.duration }}p</span>
                    </li>
                  </ul>
                </div>

                <div v-else-if="activeTab === 'reviews'" class="tab-reviews">
                  <!-- Review Form -->
                  <form class="review-form" @submit.prevent="submitReview">
                    <h4>Viết đánh giá của bạn</h4>
                    <div class="rating-select-group">
                      <span class="label">Đánh giá điểm:</span>
                      <div class="star-rating-options">
                        <button 
                          v-for="star in 5" 
                          :key="star" 
                          type="button"
                          :class="['star-option-btn', { active: newRating >= star }]"
                          @click="newRating = star"
                          aria-label="Chọn sao"
                        >
                          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                            <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                          </svg>
                        </button>
                      </div>
                    </div>

                    <div class="comment-input-group">
                      <textarea 
                        v-model="newComment" 
                        placeholder="Chia sẻ cảm nghĩ của bạn về tác phẩm này..."
                        rows="3"
                        required
                      ></textarea>
                    </div>

                    <button class="btn btn-secondary btn-submit-review" type="submit">
                      Gửi bình luận
                    </button>
                  </form>

                  <!-- Reviews List -->
                  <div class="reviews-list-container">
                    <div v-if="reviews.length === 0" class="no-reviews">
                      Chưa có đánh giá nào cho tác phẩm này. Hãy là người đầu tiên chia sẻ cảm nhận!
                    </div>
                    <div v-else class="reviews-list">
                      <div v-for="rev in reviews" :key="rev.id" class="review-card">
                        <div class="review-card-header">
                          <span class="user-name">{{ rev.userName }}</span>
                          <span class="review-date">{{ rev.createdAt }}</span>
                        </div>
                        <div class="review-stars">
                          <svg v-for="star in 5" :key="star" viewBox="0 0 24 24" width="12" height="12" :fill="rev.rating >= star ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.5">
                            <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                          </svg>
                        </div>
                        <p class="review-comment-text">{{ rev.comment }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<style scoped>
.book-detail-shell {
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

/* Main content layout */
.detail-container {
  flex: 1;
  width: 100%;
  max-width: var(--content-width);
  margin: 0 auto;
  padding: 2rem 2rem 5rem;
}

.detail-loading, .detail-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  font-family: var(--font-body);
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

.error-msg {
  color: #a83a32;
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
}

.btn {
  padding: 0.75rem 1.8rem;
  border-radius: var(--radius-sm);
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 200ms ease;
}

.btn-secondary {
  border: 1px solid var(--line-strong);
  color: var(--text-strong);
}

.btn-secondary:hover {
  background-color: rgba(43, 33, 24, 0.03);
}

/* Breadcrumb */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--text-soft);
  margin-bottom: 2.5rem;
}

.breadcrumb a {
  color: var(--text-soft);
  transition: color 200ms ease;
}

.breadcrumb a:hover {
  color: var(--accent);
}

.breadcrumb .sep {
  opacity: 0.5;
}

.breadcrumb .current {
  color: var(--text-strong);
  font-weight: 500;
}

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 4rem;
  align-items: start;
}

/* Left Cover Image styling */
.cover-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.book-cover-container {
  width: 100%;
  max-width: 300px;
}

.book-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 2/3;
  background-color: var(--surface-soft);
  border-radius: var(--radius-sm);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--line-soft);
}

/* Spine crease shadow simulation for premium editorial feel */
.book-cover::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 10px;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.14) 0%, rgba(0, 0, 0, 0.04) 30%, transparent 100%);
  pointer-events: none;
}

.shadow-elegant {
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.06), 0 8px 16px rgba(0, 0, 0, 0.04);
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-initials {
  font-family: var(--font-body);
  font-size: 2.2rem;
  font-weight: 700;
  color: var(--text-soft);
  letter-spacing: 0.05em;
}

.categories-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.cat-pill {
  font-family: var(--font-body);
  font-size: 0.75rem;
  background-color: var(--surface-soft);
  color: var(--text-soft);
  padding: 0.3rem 0.8rem;
  border-radius: 99px;
  border: 1px solid var(--line-soft);
}

/* Right Info column styling */
.info-column {
  display: flex;
  flex-direction: column;
  gap: 2.2rem;
}

.publisher-meta {
  font-family: var(--font-body);
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--accent);
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.book-title-heading {
  font-family: var(--font-display);
  font-size: clamp(2rem, 3.5vw, 3.2rem);
  line-height: 1.1;
  color: var(--text-strong);
  font-weight: 500;
  margin-bottom: 0.8rem;
}

.authors-list {
  font-family: var(--font-body);
  font-size: 0.95rem;
  color: var(--text);
}

.authors-list strong {
  color: var(--text-strong);
}

/* Format selector styles */
.formats-section h3 {
  font-family: var(--font-body);
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-soft);
  margin-bottom: 0.8rem;
  font-weight: 600;
}

.format-options {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.format-btn {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.6rem 1rem;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background-color: var(--surface);
  color: var(--text);
  font-family: var(--font-body);
  font-size: 0.88rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 180ms ease;
}

.format-btn:hover {
  background-color: var(--surface-soft);
  border-color: var(--line-strong);
}

.format-btn.active {
  border-color: var(--text-strong);
  background-color: var(--surface-soft);
  color: var(--text-strong);
  font-weight: 600;
}

.format-icon {
  font-family: var(--font-mono);
  font-size: 0.65rem;
  font-weight: 700;
  padding: 0.1rem 0.3rem;
  border-radius: var(--radius-sm);
  background-color: var(--surface);
  color: var(--text-soft);
  border: 1px solid var(--line-soft);
}

/* Pricing and Availability styling */
.pricing-section {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  background-color: var(--surface);
  padding: 1.2rem 1.6rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--line-soft);
  max-width: 450px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 0.8rem;
}

.sale-price {
  font-family: var(--font-display);
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text-strong);
}

.original-price {
  font-family: var(--font-body);
  font-size: 1rem;
  text-decoration: line-through;
  color: var(--text-soft);
}

.availability-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: var(--font-body);
  font-size: 0.85rem;
}

.status-indicator {
  width: 8px;
  height: 8px;
  background-color: var(--pastel-green-text);
  border-radius: 50%;
}

.availability-text {
  color: var(--pastel-green-text);
  font-weight: 500;
}

/* Actions buttons styling */
.actions-section {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.btn-cta-buy {
  background-color: var(--text-strong);
  color: var(--surface);
  border: 1px solid var(--text-strong);
  flex: 1.5;
  min-width: 140px;
}

.btn-cta-buy:hover {
  background-color: #2F3437;
  border-color: #2F3437;
}

.btn-cta-buy:active,
.btn-cta-cart:active,
.btn-cta-read:active {
  transform: scale(0.98);
}

.btn-cta-cart {
  background-color: var(--surface);
  color: var(--text-strong);
  border: 1px solid var(--line-strong);
  flex: 1.5;
  min-width: 140px;
}

.btn-cta-cart:hover {
  background-color: var(--surface-soft);
}

.btn-cta-read {
  background-color: var(--surface);
  color: var(--text);
  border: 1px solid var(--line-soft);
  flex: 1;
  min-width: 100px;
}

.btn-cta-read:hover {
  background-color: var(--surface-soft);
  color: var(--text-strong);
}

.btn-cta-buy:disabled,
.btn-cta-cart:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

/* Editorial Tabs styling */
.tabs-section {
  border-top: 1px solid var(--line-soft);
  padding-top: 1.5rem;
  margin-top: 1rem;
}

.tabs-header {
  display: flex;
  gap: 2rem;
  border-bottom: 1px solid var(--line-soft);
  margin-bottom: 1.25rem;
}

.tab-title {
  font-family: var(--font-body);
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-soft);
  padding-bottom: 0.75rem;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 200ms ease;
}

.tab-title:hover, .tab-title.active {
  color: var(--text-strong);
}

.tab-title.active {
  border-bottom-color: var(--accent);
}

.tab-description {
  max-width: 65ch;
}

.description-text {
  font-family: var(--font-body);
  font-size: 1rem;
  line-height: 1.7;
  color: var(--text);
  text-align: justify;
}

.chapters-grid {
  display: flex;
  flex-direction: column;
  max-height: 300px;
  overflow-y: auto;
  padding-right: 0.5rem;
}

.chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.85rem 0;
  border-bottom: 1px solid var(--line-soft);
  font-family: var(--font-body);
  font-size: 0.92rem;
  background: none;
  border-radius: 0;
}

.ch-num {
  font-family: var(--font-mono);
  font-size: 0.8rem;
  opacity: 0.5;
  width: 40px;
}

.ch-name {
  flex: 1;
  font-weight: 500;
  color: var(--text-strong);
}

.ch-dur {
  font-family: var(--font-mono);
  font-size: 0.8rem;
  opacity: 0.6;
}

/* Mobile responsive layout */
@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: 1fr;
    gap: 2.5rem;
  }
  .cover-column {
    align-items: center;
  }
}

/* Reviews Tab Styling */
.review-form {
  margin-bottom: 2.5rem;
  padding: 1.5rem;
  background-color: var(--surface-soft);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
}

.review-form h4 {
  font-family: var(--font-body);
  margin: 0 0 1rem;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-strong);
}

.rating-select-group {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.rating-select-group .label {
  font-size: 0.88rem;
  color: var(--text-soft);
}

.star-rating-options {
  display: flex;
  gap: 0.25rem;
}

.star-option-btn {
  background: none;
  border: none;
  color: var(--line-strong);
  cursor: pointer;
  padding: 2px;
  transition: color 150ms ease;
}

.star-option-btn:hover,
.star-option-btn.active {
  color: var(--text-strong);
}

.comment-input-group textarea {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background-color: var(--surface);
  color: var(--text);
  font-family: var(--font-body);
  font-size: 0.92rem;
  line-height: 1.5;
  resize: vertical;
  outline: none;
  transition: border-color 150ms ease;
  margin-bottom: 1rem;
}

.comment-input-group textarea:focus {
  border-color: var(--text-strong);
}

.btn-submit-review {
  padding: 0.6rem 1.2rem;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.no-reviews {
  padding: 2rem 0;
  text-align: center;
  color: var(--text-soft);
  font-size: 0.92rem;
}

.review-card {
  padding: 1.2rem 0;
  border-bottom: 1px solid var(--line-soft);
}

.review-card:last-child {
  border-bottom: none;
}

.review-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.4rem;
}

.review-card-header .user-name {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--text-strong);
}

.review-card-header .review-date {
  font-family: var(--font-mono);
  font-size: 0.78rem;
  color: var(--text-soft);
}

.review-stars {
  display: flex;
  gap: 2px;
  color: var(--text-strong);
  margin-bottom: 0.6rem;
}

.review-comment-text {
  font-size: 0.92rem;
  line-height: 1.6;
  color: var(--text);
  margin: 0;
  text-align: justify;
}
</style>
