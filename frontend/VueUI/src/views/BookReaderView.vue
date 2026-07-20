<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VuePdfEmbed from 'vue-pdf-embed'
import { ApiError, getBookBySlug, getReadingUrl } from '../services/api'

const route = useRoute()
const router = useRouter()

const book = ref(null)
const currentPage = ref(1)
const totalPages = ref(0)
const loading = ref(true)
const errorMessage = ref('')
const bookmarkedPages = ref([])
const pdfSource = ref('')
const readerViewport = ref(null)
let abortController = null

const pdfEdition = computed(() =>
  book.value?.editions?.find(
    (edition) => edition.format === 'EBOOK_PDF' && (edition.fileUrl || edition.fileObjectName),
  ),
)

const chapterTitle = computed(
  () => pdfEdition.value?.chapterTitle || pdfEdition.value?.currentChapterTitle || '',
)

const isBookmarked = computed(() => bookmarkedPages.value.includes(currentPage.value))
const progressStorageKey = computed(() => `pdf-reader-progress-${route.params.slug}`)

function releasePdfSource() {
  if (pdfSource.value) {
    URL.revokeObjectURL(pdfSource.value)
    pdfSource.value = ''
  }
}

function restoreReaderState() {
  try {
    const saved = JSON.parse(localStorage.getItem(progressStorageKey.value) || '{}')
    currentPage.value = Number.isInteger(saved.currentPage) && saved.currentPage > 0 ? saved.currentPage : 1
    bookmarkedPages.value = Array.isArray(saved.bookmarkedPages)
      ? [...new Set(saved.bookmarkedPages.filter((page) => Number.isInteger(page) && page > 0))]
      : []
  } catch {
    currentPage.value = 1
    bookmarkedPages.value = []
  }
}

function saveReaderState() {
  localStorage.setItem(progressStorageKey.value, JSON.stringify({
    currentPage: currentPage.value,
    bookmarkedPages: bookmarkedPages.value,
  }))
}

function getFriendlyError(error) {
  if (error instanceof ApiError) {
    if (error.status === 401) return 'Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.'
    if (error.status === 403) return 'Bạn chưa mua sách này hoặc không có quyền đọc.'
    if (error.status === 404) return 'Không tìm thấy sách hoặc tệp PDF.'
    return error.message || 'Không thể tải sách để đọc.'
  }

  if (error?.name === 'AbortError') return ''
  if (error?.status === 401) return 'Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.'
  if (error?.status === 403) return 'Bạn chưa mua sách này hoặc không có quyền đọc.'
  if (error?.status === 404) return 'Không tìm thấy sách hoặc tệp PDF.'
  if (error instanceof TypeError) return 'Không thể kết nối mạng để tải tệp PDF. Vui lòng thử lại.'
  return 'Tệp PDF không hợp lệ hoặc không thể tải để đọc.'
}

async function loadPdf() {
  abortController?.abort()
  releasePdfSource()
  const controller = new AbortController()
  abortController = controller
  loading.value = true
  errorMessage.value = ''
  totalPages.value = 0

  try {
    const data = await getBookBySlug(route.params.slug, { signal: controller.signal })
    book.value = data

    if (!pdfEdition.value) {
      throw new ApiError('Sách này chưa có phiên bản PDF để đọc trực tuyến.', 404, null)
    }

    restoreReaderState()

    // Endpoint này yêu cầu JWT và backend kiểm tra quyền sở hữu sách trước khi trả URL có thời hạn.
    const access = await getReadingUrl(pdfEdition.value.id, { signal: controller.signal })
    if (!access?.url) {
      throw new ApiError('Không nhận được địa chỉ tệp PDF.', 404, null)
    }

    // URL MinIO đã ký là quyền truy cập tạm thời; không thêm Authorization để tránh CORS preflight không cần thiết.
    const response = await fetch(access.url, { signal: controller.signal })
    if (!response.ok) {
      throw new ApiError('Không thể tải tệp PDF.', response.status, null)
    }

    const contentType = response.headers.get('content-type') || ''
    if (!contentType.toLowerCase().includes('application/pdf')) {
      throw new Error('Máy chủ không trả về tệp PDF hợp lệ.')
    }

    const blob = await response.blob()
    if (!blob.size) {
      throw new Error('Tệp PDF trống hoặc không hợp lệ.')
    }

    pdfSource.value = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
  } catch (error) {
    if (error?.name !== 'AbortError') {
      errorMessage.value = getFriendlyError(error)
    }
  } finally {
    if (abortController === controller && !controller.signal.aborted) {
      loading.value = false
    }
  }
}

function onPdfLoaded(pdfDocument) {
  totalPages.value = pdfDocument?.numPages || 0
  currentPage.value = Math.min(Math.max(currentPage.value, 1), totalPages.value || 1)
}

function onPdfLoadingFailed(error) {
  errorMessage.value = getFriendlyError(error)
  loading.value = false
}

function scrollReaderToTop() {
  readerViewport.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

function goToPage(page) {
  if (!totalPages.value) return
  const nextPage = Math.min(Math.max(page, 1), totalPages.value)
  if (nextPage === currentPage.value) return
  currentPage.value = nextPage
  scrollReaderToTop()
}

function previousPage() {
  goToPage(currentPage.value - 1)
}

function nextPage() {
  goToPage(currentPage.value + 1)
}

function toggleBookmark() {
  bookmarkedPages.value = isBookmarked.value
    ? bookmarkedPages.value.filter((page) => page !== currentPage.value)
    : [...bookmarkedPages.value, currentPage.value].sort((a, b) => a - b)
}

function exitReader() {
  router.push(book.value ? `/book/${book.value.slug}` : '/library')
}

watch([currentPage, bookmarkedPages], saveReaderState, { deep: true })

onMounted(loadPdf)

onBeforeUnmount(() => {
  abortController?.abort()
  releasePdfSource()
})
</script>

<template>
  <section class="reader-container">
    <header class="reader-header">
      <button class="reader-icon-button reader-exit" type="button" aria-label="Thoát khỏi trang đọc" @click="exitReader">
        <span aria-hidden="true">←</span>
        <span>Thoát</span>
      </button>

      <div class="reader-heading">
        <strong>{{ book?.title || 'Đang mở sách' }}</strong>
        <span v-if="chapterTitle">{{ chapterTitle }}</span>
      </div>

      <button
        class="reader-icon-button bookmark-button"
        :class="{ active: isBookmarked }"
        type="button"
        :aria-label="isBookmarked ? 'Bỏ đánh dấu trang' : 'Đánh dấu trang'"
        :disabled="!totalPages"
        @click="toggleBookmark"
      >
        <span aria-hidden="true">{{ isBookmarked ? '★' : '☆' }}</span>
        <span class="bookmark-label">Đánh dấu</span>
      </button>
    </header>

    <main ref="readerViewport" class="reader-viewport">
      <div v-if="loading" class="reader-state" role="status">
        <span class="reader-spinner" aria-hidden="true"></span>
        <p>Đang tải sách…</p>
      </div>

      <div v-else-if="errorMessage" class="reader-state reader-state-error" role="alert">
        <h1>Chưa thể mở sách</h1>
        <p>{{ errorMessage }}</p>
        <div class="error-actions">
          <button class="reader-button reader-button-secondary" type="button" @click="exitReader">Quay lại</button>
          <button class="reader-button" type="button" @click="loadPdf">Thử lại</button>
        </div>
      </div>

      <div v-else-if="pdfSource" class="pdf-stage">
        <div class="pdf-page-frame">
          <VuePdfEmbed
            :source="pdfSource"
            :page="currentPage"
            :title="`Trang ${currentPage} của ${book?.title || 'sách'}`"
            @loaded="onPdfLoaded"
            @loading-failed="onPdfLoadingFailed"
          />
        </div>
      </div>
    </main>

    <footer class="reader-footer">
      <button class="reader-button reader-button-secondary" type="button" :disabled="currentPage <= 1 || loading" @click="previousPage">
        ← Trang trước
      </button>
      <span class="page-status" aria-live="polite">Trang {{ currentPage }} / {{ totalPages || '—' }}</span>
      <button class="reader-button" type="button" :disabled="!totalPages || currentPage >= totalPages || loading" @click="nextPage">
        Trang sau →
      </button>
    </footer>
  </section>
</template>

<style scoped>
.reader-container {
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  background: #f4f1ea;
  color: #292820;
}

.reader-header,
.reader-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.9rem clamp(1rem, 4vw, 2.5rem);
  background: rgb(255 253 248 / 94%);
  border-color: #ded9ce;
  font-family: var(--font-body, sans-serif);
}

.reader-header { border-bottom: 1px solid #ded9ce; }
.reader-footer { border-top: 1px solid #ded9ce; }

.reader-heading {
  min-width: 0;
  display: grid;
  gap: 0.15rem;
  text-align: center;
}

.reader-heading strong,
.reader-heading span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reader-heading strong { font-family: var(--font-display, Georgia, serif); font-size: 1.1rem; }
.reader-heading span { color: #777163; font-size: 0.8rem; }

.reader-icon-button,
.reader-button {
  border: 1px solid #cfc8ba;
  border-radius: 0.5rem;
  background: #fffdf8;
  color: inherit;
  cursor: pointer;
  font: 600 0.86rem/1 var(--font-body, sans-serif);
  transition: background-color 150ms ease, border-color 150ms ease, color 150ms ease;
}

.reader-icon-button { display: inline-flex; align-items: center; gap: 0.45rem; padding: 0.65rem 0.75rem; }
.reader-button { padding: 0.75rem 1rem; background: #314b3c; border-color: #314b3c; color: #fff; }
.reader-button-secondary { background: transparent; color: #314b3c; border-color: #779080; }
.reader-icon-button:hover:not(:disabled), .reader-button:hover:not(:disabled) { background: #e9e4d9; }
.reader-button:hover:not(:disabled) { border-color: #263d30; background: #263d30; }
.reader-button-secondary:hover:not(:disabled) { color: #263d30; background: #e9e4d9; }
.bookmark-button.active { border-color: #ac7a18; color: #8c6113; background: #fff4d9; }
button:disabled { cursor: not-allowed; opacity: 0.45; }

.reader-viewport { overflow: auto; padding: clamp(1rem, 3vw, 2.5rem); }
.pdf-stage { display: grid; justify-items: center; }
.pdf-page-frame {
  width: min(880px, 100%);
  overflow: hidden;
  background: #fff;
  box-shadow: 0 0.8rem 2.3rem rgb(48 42 29 / 14%);
}

.pdf-page-frame :deep(canvas) {
  width: 100% !important;
  height: auto !important;
  display: block;
}

.reader-state {
  min-height: 50vh;
  display: grid;
  place-content: center;
  justify-items: center;
  gap: 1rem;
  text-align: center;
  font-family: var(--font-body, sans-serif);
}

.reader-state h1 { margin: 0; font: 500 clamp(1.8rem, 5vw, 2.5rem)/1.1 var(--font-display, Georgia, serif); }
.reader-state p { max-width: 34rem; margin: 0; color: #6d675b; line-height: 1.55; }
.reader-spinner { width: 2.25rem; height: 2.25rem; border: 3px solid #d9d1c1; border-top-color: #314b3c; border-radius: 50%; animation: rotate 800ms linear infinite; }
.error-actions { display: flex; flex-wrap: wrap; justify-content: center; gap: 0.75rem; margin-top: 0.5rem; }
.page-status { min-width: 8rem; text-align: center; color: #5d594e; font: 600 0.85rem/1 var(--font-body, sans-serif); }

@keyframes rotate { to { transform: rotate(360deg); } }

@media (max-width: 640px) {
  .reader-header { padding: 0.75rem 1rem; }
  .reader-exit span:last-child, .bookmark-label { display: none; }
  .reader-heading strong { font-size: 1rem; }
  .reader-viewport { padding: 0.75rem 0; }
  .pdf-page-frame { width: 100%; box-shadow: none; }
  .reader-footer { gap: 0.5rem; padding: 0.75rem; }
  .reader-button { padding: 0.7rem; font-size: 0.78rem; }
  .page-status { min-width: 5.7rem; font-size: 0.76rem; }
}
</style>
