<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBookBySlug, getReadingUrl } from '../services/api'

const route = useRoute()
const router = useRouter()

const book = ref(null)
const isLoading = ref(true)
const errorMessage = ref('')
const readingUrl = ref('')

const pdfEdition = computed(() =>
  book.value?.editions?.find(
    edition => edition.format === 'EBOOK_PDF' && (edition.fileUrl || edition.fileObjectName),
  ),
)

const pdfUrl = computed(() => readingUrl.value)

const currentChapterIndex = ref(0)
const fontSize = ref(18) // in px
const theme = ref('cream') // 'cream', 'pearl', 'night'
const isBookmarked = ref(false)

// Mock book content for demonstration reading if text content is not in DB
const mockChapters = ref([
  {
    title: 'Chương 1: Khởi đầu mới',
    content: `Đó là một buổi chiều mùa thu dịu nhẹ. Ánh nắng hanh vàng trải dài trên những lối đi lát đá cổ kính của thị trấn. Gió mơn man qua kẽ lá, mang theo hương vị của đất ẩm và những quả chín muộn màng.

Tôi ngồi bên bệ cửa sổ, tay lật giở từng trang sách cũ. Những dòng chữ như đang nhảy múa dưới ánh sáng ban chiều. Cuộc sống ngoài kia vẫn trôi đi lặng lẽ, nhưng trong thế giới của trang giấy này, một hành trình phi thường chỉ vừa mới bắt đầu.

Người ta thường nói, mỗi cuốn sách là một cánh cửa dẫn đến một vùng đất khác, nơi những giấc mơ chưa trọn vẹn được chắp cánh và những nỗi buồn được xoa dịu bằng ngôn từ. Tôi tin vào điều đó. Khi những ngón tay chạm vào thớ giấy thô ráp, tôi cảm nhận được nhịp đập của một trái tim khác, một cuộc đời khác đang thở dài hay mỉm cười qua từng nét mực.`
  },
  {
    title: 'Chương 2: Những người bạn đồng hành',
    content: `Con đường phía trước dài dằng dặc và phủ đầy sương mù. Nhưng tôi biết mình không đơn độc. Trên chiếc xe ngựa gỗ cũ kỹ, có những người bạn đồng hành kỳ lạ mà số phận đã đưa đến.

Người ngồi đối diện tôi là một bác sĩ già lập dị, mái tóc bạc trắng dựng ngược và đôi mắt luôn lấp lánh sự tò mò trẻ thơ. Cạnh ông ta là một cô bé câm có đôi mắt to tròn biết nói, đôi tay nhỏ nhắn luôn ôm chặt cuốn sổ vẽ nguệch ngoạc những hình thù bí ẩn.

Chúng tôi không nói nhiều với nhau. Tiếng bánh xe lăn đều đều trên mặt đường đá gồ ghề tạo nên một bản nhạc nền êm ái, kéo tất cả vào những dòng suy nghĩ riêng tư. Nhưng khi đêm xuống và đốm lửa trại được nhóm lên, khoảng cách giữa chúng tôi dường như tan biến.`
  },
  {
    title: 'Chương 3: Thư viện trong sương',
    content: `Khi bình minh lên, qua màn sương mù dày đặc of thung lũng, một tòa lâu đài đá cổ kính hiện ra. Đó chính là thư viện lớn của vùng đất phía Bắc - nơi lưu trữ toàn bộ lịch sử và ký ức của loài người trước kỷ băng hà.

Những bức tường phủ đầy rêu phong và những cửa kính màu rực rỡ lấp lánh dưới ánh bình minh yếu ớt. Bên trong, hàng triệu cuốn sách xếp chồng lên nhau từ sàn nhà lên tới trần cao vút. Mùi giấy cũ, mùi sáp nến và mùi của thời gian hòa quyện vào nhau tạo nên một không khí trang nghiêm đến kỳ lạ.

Tôi bước đi nhẹ nhàng giữa những hàng kệ gỗ sồi, cảm giác như mình đang bước vào một ngôi đền linh thiêng. Mỗi cuốn sách ở đây đều chứa đựng một linh hồn, chờ đợi một người đọc tri kỷ đến đánh thức.`
  }
])

const chapters = computed(() => {
  // If the book has editions with chapters, we can use them, else fallback to mock chapters
  if (book.value?.editions) {
    const audioEdition = book.value.editions.find(e => e.format === 'AUDIOBOOK')
    if (audioEdition?.audioChapters && audioEdition.audioChapters.length > 0) {
      return audioEdition.audioChapters.map((ch, idx) => ({
        title: ch.title || `Chương ${ch.chapterNumber}`,
        content: `Nội dung của ${ch.title || `Chương ${ch.chapterNumber}`}. Phiên bản Audiobook có thời lượng ${ch.duration || 0} phút. Đây là bản xem trước văn bản dùng thử để trải nghiệm đọc tốt nhất.\n\nSách hay cần được thưởng thức chậm rãi. Hãy điều chỉnh kích thước chữ và màu nền để phù hợp với mắt của bạn trong những buổi đọc sách đêm muộn.`
      }))
    }
  }
  return mockChapters.value
})

const currentChapter = computed(() => {
  if (chapters.value.length === 0) return null
  return chapters.value[currentChapterIndex.value] || chapters.value[0]
})

const progressPercentage = computed(() => {
  if (chapters.value.length <= 1) return 100
  return Math.round(((currentChapterIndex.value + 1) / chapters.value.length) * 100)
})

async function fetchBookDetail() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const slug = route.params.slug
    const data = await getBookBySlug(slug)
    book.value = data
    if (pdfEdition.value) {
      const access = await getReadingUrl(pdfEdition.value.id)
      readingUrl.value = access.url
    }
    
    // Restore reading progress from localStorage
    const savedProgress = localStorage.getItem(`reader-progress-${slug}`)
    if (savedProgress) {
      const parsed = JSON.parse(savedProgress)
      currentChapterIndex.value = parsed.chapterIndex || 0
      fontSize.value = parsed.fontSize || 18
      theme.value = parsed.theme || 'cream'
    }
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : 'Không thể tải sách để đọc.'
  } finally {
    isLoading.value = false
  }
}

function saveProgress() {
  if (!book.value) return
  const slug = route.params.slug
  localStorage.setItem(`reader-progress-${slug}`, JSON.stringify({
    chapterIndex: currentChapterIndex.value,
    fontSize: fontSize.value,
    theme: theme.value
  }))
}

function selectChapter(index) {
  currentChapterIndex.value = index
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function nextChapter() {
  if (currentChapterIndex.value < chapters.value.length - 1) {
    currentChapterIndex.value++
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function prevChapter() {
  if (currentChapterIndex.value > 0) {
    currentChapterIndex.value--
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function increaseFontSize() {
  if (fontSize.value < 32) fontSize.value += 2
}

function decreaseFontSize() {
  if (fontSize.value > 14) fontSize.value -= 2
}

function toggleBookmark() {
  isBookmarked.value = !isBookmarked.value
}

function exitReader() {
  if (book.value) {
    router.push(`/book/${book.value.slug}`)
  } else {
    router.push('/library')
  }
}

watch([currentChapterIndex, fontSize, theme], () => {
  saveProgress()
})

onMounted(() => {
  fetchBookDetail()
})
</script>

<template>
  <div :class="['reader-container', `theme-${theme}`]">
    <div v-if="isLoading" class="reader-loading">
      <div class="spinner"></div>
      <p>Đang chuẩn bị không gian đọc...</p>
    </div>

    <div v-else-if="errorMessage" class="reader-error">
      <p class="error-msg">{{ errorMessage }}</p>
      <button class="btn btn-secondary" @click="router.push('/library')">Quay lại thư viện</button>
    </div>

    <div v-else class="reader-shell">
      <!-- Top quiet navigation bar -->
      <header class="reader-header">
        <button class="btn-back" @click="exitReader" aria-label="Quay lại">
          <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
          <span>Thoát</span>
        </button>

        <div class="book-info">
          <span class="book-title">{{ book.title }}</span>
          <span class="dot">·</span>
          <span class="chapter-title">{{ currentChapter?.title }}</span>
        </div>

        <button :class="['btn-bookmark', { active: isBookmarked }]" @click="toggleBookmark" aria-label="Đánh dấu trang">
          <svg viewBox="0 0 24 24" width="20" height="20" :fill="isBookmarked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
            <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
          </svg>
        </button>
      </header>

      <!-- Main Reader Layout -->
      <div v-if="pdfUrl" class="reader-main reader-main--pdf">
        <main class="reader-content-wrapper reader-content-wrapper--pdf">
          <iframe
            class="pdf-viewer"
            :src="pdfUrl"
            :title="`Đọc sách ${book.title}`"
          ></iframe>
        </main>
      </div>

      <div v-else class="reader-main">
        <!-- Sidebar controls (TOC & Typography settings) -->
        <aside class="reader-sidebar">
          <div class="sidebar-section">
            <h4>Mục lục</h4>
            <ul class="chapter-list">
              <li 
                v-for="(ch, idx) in chapters" 
                :key="idx"
                :class="{ active: currentChapterIndex === idx }"
                @click="selectChapter(idx)"
              >
                {{ ch.title }}
              </li>
            </ul>
          </div>

          <div class="sidebar-section typography-settings">
            <h4>Giao diện đọc</h4>
            
            <!-- Font sizing controls -->
            <div class="setting-item">
              <span class="label">Cỡ chữ</span>
              <div class="font-controls">
                <button @click="decreaseFontSize" :disabled="fontSize <= 14">A-</button>
                <span class="size-display">{{ fontSize }}px</span>
                <button @click="increaseFontSize" :disabled="fontSize >= 32">A+</button>
              </div>
            </div>

            <!-- Background Theme selector -->
            <div class="setting-item">
              <span class="label">Màu nền</span>
              <div class="theme-controls">
                <button 
                  class="theme-btn theme-cream" 
                  :class="{ active: theme === 'cream' }"
                  @click="theme = 'cream'"
                  title="Kem nhạt"
                ></button>
                <button 
                  class="theme-btn theme-pearl" 
                  :class="{ active: theme === 'pearl' }"
                  @click="theme = 'pearl'"
                  title="Trắng ngà"
                ></button>
                <button 
                  class="theme-btn theme-night" 
                  :class="{ active: theme === 'night' }"
                  @click="theme = 'night'"
                  title="Chế độ tối"
                ></button>
              </div>
            </div>
          </div>
        </aside>

        <!-- Reader Text Area -->
        <main class="reader-content-wrapper">
          <article class="reader-article" :style="{ fontSize: fontSize + 'px' }">
            <h1 class="chapter-headline">{{ currentChapter?.title }}</h1>
            
            <div class="chapter-paragraphs">
              <p v-for="(p, pIdx) in currentChapter?.content.split('\n\n')" :key="pIdx">
                {{ p }}
              </p>
            </div>
          </article>
          
          <!-- Navigation buttons between chapters -->
          <footer class="reader-navigation">
            <button 
              class="nav-btn" 
              :disabled="currentChapterIndex === 0" 
              @click="prevChapter"
            >
              ← Chương trước
            </button>
            <span class="progress-indicator">{{ progressPercentage }}% đã đọc</span>
            <button 
              class="nav-btn" 
              :disabled="currentChapterIndex === chapters.length - 1" 
              @click="nextChapter"
            >
              Chương sau →
            </button>
          </footer>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reader-container {
  min-height: 100vh;
  width: 100%;
  transition: background-color 300ms ease, color 300ms ease;
  display: flex;
  flex-direction: column;
}

/* Themes colors definition */
.theme-cream {
  background-color: #F7F6F3;
  color: #2F3437;
}
.theme-pearl {
  background-color: #FFFFFF;
  color: #111111;
}
.theme-night {
  background-color: #181818;
  color: #EAEAEA;
}

/* Spinner and Loading */
.reader-loading, .reader-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 3rem;
  font-family: var(--font-body);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(43, 33, 24, 0.1);
  border-radius: 50%;
  border-top-color: var(--accent, #2F4F3E);
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 1.5rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-msg {
  color: #a83a32;
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
}

/* Reader header styling */
.reader-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  border-bottom: 1px solid rgba(43, 33, 24, 0.08);
  font-family: var(--font-body);
}

.theme-night .reader-header {
  border-bottom-color: rgba(210, 197, 184, 0.08);
}

.btn-back, .btn-bookmark {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: inherit;
  opacity: 0.8;
  transition: opacity 200ms ease;
}

.btn-back:hover, .btn-bookmark:hover {
  opacity: 1;
}

.book-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
}

.book-title {
  font-weight: 600;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chapter-title {
  opacity: 0.8;
  max-width: 250px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.btn-bookmark.active {
  color: var(--accent-gold, #D8B26E);
  opacity: 1;
}

/* Layout Main split */
.reader-main {
  display: flex;
  flex: 1;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
}

.reader-main--pdf {
  max-width: none;
  min-height: calc(100vh - 60px);
}

.reader-content-wrapper--pdf {
  max-width: none;
  padding: 0;
}

.pdf-viewer {
  width: 100%;
  min-height: calc(100vh - 60px);
  border: 0;
  background: #fff;
}

/* Sidebar configuration */
.reader-sidebar {
  width: 280px;
  padding: 2.5rem 1.5rem;
  border-right: 1px solid rgba(43, 33, 24, 0.08);
  font-family: var(--font-body);
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.theme-night .reader-sidebar {
  border-right-color: rgba(210, 197, 184, 0.08);
}

.sidebar-section h4 {
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  opacity: 0.6;
  margin-bottom: 1rem;
  font-weight: 600;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.chapter-list li {
  padding: 0.6rem 0.8rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: background-color 200ms ease;
}

.chapter-list li:hover {
  background-color: rgba(43, 33, 24, 0.04);
}

.theme-night .chapter-list li:hover {
  background-color: rgba(210, 197, 184, 0.04);
}

.chapter-list li.active {
  background-color: rgba(47, 79, 62, 0.08);
  color: var(--accent, #2F4F3E);
  font-weight: 600;
}

.theme-night .chapter-list li.active {
  background-color: rgba(216, 178, 110, 0.12);
  color: var(--accent-gold, #D8B26E);
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-bottom: 1.25rem;
}

.setting-item .label {
  font-size: 0.85rem;
  opacity: 0.8;
}

.font-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.font-controls button {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(43, 33, 24, 0.15);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: inherit;
}

.theme-night .font-controls button {
  border-color: rgba(210, 197, 184, 0.15);
}

.font-controls button:hover:not(:disabled) {
  background-color: rgba(43, 33, 24, 0.04);
}

.theme-night .font-controls button:hover:not(:disabled) {
  background-color: rgba(210, 197, 184, 0.04);
}

.font-controls button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.size-display {
  font-size: 0.95rem;
  min-width: 50px;
  text-align: center;
}

.theme-controls {
  display: flex;
  gap: 0.6rem;
}

.theme-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
  transition: transform 200ms ease;
}

.theme-btn.active {
  border-color: var(--accent, #2F4F3E);
  transform: scale(1.1);
}

.theme-night .theme-btn.active {
  border-color: var(--accent-gold, #D8B26E);
}

.theme-btn.theme-cream {
  background-color: #F7F6F3;
  box-shadow: inset 0 0 0 1px rgba(0,0,0,0.08);
}
.theme-btn.theme-pearl {
  background-color: #FFFFFF;
  box-shadow: inset 0 0 0 1px rgba(0,0,0,0.08);
}
.theme-btn.theme-night {
  background-color: #181818;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.08);
}

/* Content Area layout */
.reader-content-wrapper {
  flex: 1;
  padding: 3rem clamp(2rem, 8vw, 8rem);
  display: flex;
  flex-direction: column;
  max-width: 850px;
  margin: 0 auto;
}

.reader-article {
  font-family: var(--font-display);
  line-height: 1.8;
  margin-bottom: 4rem;
}

.chapter-headline {
  font-size: 2.2em;
  margin-bottom: 2rem;
  text-align: center;
  font-weight: 400;
}

.chapter-paragraphs p {
  margin-bottom: 1.65em;
  text-indent: 2em;
  text-align: justify;
  font-family: var(--font-display);
}

/* Reader navigation footer */
.reader-navigation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 2rem;
  border-top: 1px solid rgba(43, 33, 24, 0.08);
  font-family: var(--font-body);
  margin-top: auto;
}

.theme-night .reader-navigation {
  border-top-color: rgba(210, 197, 184, 0.08);
}

.nav-btn {
  padding: 0.6rem 1.2rem;
  border: 1px solid rgba(43, 33, 24, 0.15);
  border-radius: 6px;
  background-color: transparent;
  color: inherit;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 200ms ease;
}

.theme-night .nav-btn {
  border-color: rgba(210, 197, 184, 0.15);
}

.nav-btn:hover:not(:disabled) {
  border-color: var(--accent, #2F4F3E);
  color: var(--accent, #2F4F3E);
}

.theme-night .nav-btn:hover:not(:disabled) {
  border-color: var(--accent-gold, #D8B26E);
  color: var(--accent-gold, #D8B26E);
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.progress-indicator {
  font-size: 0.85rem;
  opacity: 0.6;
}

/* Mobile responsive fixes */
@media (max-width: 868px) {
  .reader-sidebar {
    display: none;
  }
  .reader-content-wrapper {
    padding: 2rem 1.5rem;
  }
}
</style>
