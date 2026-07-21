<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import {
  PhMagnifyingGlass,
  PhPlus,
  PhPencilSimple,
  PhTrash,
  PhX,
} from '@phosphor-icons/vue'
import {
  createAdminBook,
  getAdminBook,
  getAdminBookFormOptions,
  getAdminBooks,
  getFileUrl,
  updateAdminBook,
  updateAdminBookStatus,
} from '../../services/api'

const books = ref([])
const totalBooks = ref(0)
const currentPage = ref(0)
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const isLoading = ref(false)
const isSaving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const isModalOpen = ref(false)
const editingBookId = ref(null)
const coverFile = ref(null)
const coverPreview = ref('')
const failedCoverIds = ref(new Set())
const formOptions = ref({ publishers: [], authors: [], categories: [] })
let searchTimer = null

const pageSize = 10
const totalPages = computed(() => Math.max(1, Math.ceil(totalBooks.value / pageSize)))
const modalTitle = computed(() => editingBookId.value ? 'Chỉnh sửa sách' : 'Thêm sách mới')

const form = reactive({
  title: '',
  slug: '',
  description: '',
  publisherId: '',
  authorIds: [],
  categoryIds: [],
  isActive: true,
})

function clearMessages() {
  errorMessage.value = ''
  successMessage.value = ''
}

function toStatusFilter() {
  if (selectedStatus.value === 'active') return true
  if (selectedStatus.value === 'inactive') return false
  return undefined
}

function getCoverUrl(book) {
  const objectName = book.imageUrls?.[0]
  return objectName && !failedCoverIds.value.has(book.id) ? getFileUrl(objectName) : ''
}

function getCoverInitials(title) {
  return String(title ?? 'S').trim().slice(0, 1).toUpperCase()
}

function markCoverAsUnavailable(bookId) {
  failedCoverIds.value = new Set([...failedCoverIds.value, bookId])
}

function resetForm() {
  Object.assign(form, {
    title: '',
    slug: '',
    description: '',
    publisherId: '',
    authorIds: [],
    categoryIds: [],
    isActive: true,
  })
  editingBookId.value = null
  coverFile.value = null
  coverPreview.value = ''
}

async function loadBooks() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const payload = await getAdminBooks({
      page: currentPage.value,
      size: pageSize,
      q: searchQuery.value,
      category: selectedCategory.value,
      isActive: toStatusFilter(),
    })
    books.value = Array.isArray(payload?.content) ? payload.content : []
    failedCoverIds.value = new Set()
    totalBooks.value = Number(payload?.totalElements ?? books.value.length)
  } catch (error) {
    books.value = []
    totalBooks.value = 0
    errorMessage.value = error instanceof Error ? error.message : 'Không thể tải danh sách sách.'
  } finally {
    isLoading.value = false
  }
}

async function loadFormOptions() {
  try {
    formOptions.value = await getAdminBookFormOptions()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể tải dữ liệu cho biểu mẫu.'
  }
}

async function openCreateModal() {
  clearMessages()
  resetForm()
  if (formOptions.value.publishers.length === 0) await loadFormOptions()
  if (formOptions.value.publishers.length > 0) isModalOpen.value = true
}

async function openEditModal(bookId) {
  clearMessages()
  if (formOptions.value.publishers.length === 0) await loadFormOptions()
  if (formOptions.value.publishers.length === 0) return

  try {
    const book = await getAdminBook(bookId)
    editingBookId.value = book.id
    Object.assign(form, {
      title: book.title ?? '',
      slug: book.slug ?? '',
      description: book.description ?? '',
      publisherId: String(book.publisherId ?? ''),
      authorIds: (book.authorIds ?? []).map(String),
      categoryIds: (book.categoryIds ?? []).map(String),
      isActive: Boolean(book.isActive),
    })
    coverFile.value = null
    coverPreview.value = book.imageUrls?.[0] ? getFileUrl(book.imageUrls[0]) : ''
    isModalOpen.value = true
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể tải thông tin sách.'
  }
}

function closeModal() {
  if (isSaving.value) return
  isModalOpen.value = false
  resetForm()
}

function handleCoverChange(event) {
  const [file] = event.target.files ?? []
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    errorMessage.value = 'Ảnh bìa phải có định dạng JPG, PNG hoặc WebP.'
    event.target.value = ''
    return
  }

  coverFile.value = file
  coverPreview.value = URL.createObjectURL(file)
}

function buildPayload() {
  return {
    title: form.title.trim(),
    slug: form.slug.trim(),
    description: form.description.trim(),
    publisherId: Number(form.publisherId),
    authorIds: form.authorIds.map(Number),
    categoryIds: form.categoryIds.map(Number),
    isActive: form.isActive,
  }
}

async function saveBook() {
  clearMessages()
  isSaving.value = true

  try {
    const payload = buildPayload()
    if (editingBookId.value) {
      await updateAdminBook(editingBookId.value, payload, coverFile.value)
      successMessage.value = 'Cập nhật sách thành công.'
    } else {
      await createAdminBook(payload, coverFile.value)
      successMessage.value = 'Tạo sách thành công.'
    }
    isModalOpen.value = false
    resetForm()
    await loadBooks()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể lưu sách.'
  } finally {
    isSaving.value = false
  }
}

async function toggleBookStatus(book) {
  const nextStatus = !book.isActive
  const action = nextStatus ? 'hiển thị lại' : 'ẩn'
  if (!window.confirm(`Bạn có chắc chắn muốn ${action} sách “${book.title}” không?`)) return

  clearMessages()
  try {
    await updateAdminBookStatus(book.id, nextStatus)
    successMessage.value = nextStatus ? 'Đã hiển thị lại sách.' : 'Đã ẩn sách khỏi thư viện.'
    await loadBooks()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể thay đổi trạng thái sách.'
  }
}

function changePage(nextPage) {
  if (nextPage < 0 || nextPage >= totalPages.value || nextPage === currentPage.value) return
  currentPage.value = nextPage
  loadBooks()
}

watch(searchQuery, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 0
    loadBooks()
  }, 350)
})

watch([selectedCategory, selectedStatus], () => {
  currentPage.value = 0
  loadBooks()
})

onMounted(async () => {
  await Promise.all([loadBooks(), loadFormOptions()])
})

onBeforeUnmount(() => clearTimeout(searchTimer))
</script>

<template>
  <div class="admin-page">
    <!-- Header -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Sách</h2>
        <p>Thêm, sửa, xóa và quản lý các tác phẩm trên hệ thống.</p>
      </div>
      <button class="primary-btn" @click="openCreateModal">
        <PhPlus :size="20" weight="bold" />
        Thêm sách mới
      </button>
    </header>

    <!-- Toolbar -->
    <div class="toolbar bento-item">
      <div class="search-box">
        <PhMagnifyingGlass :size="20" class="search-icon" />
        <input type="text" v-model="searchQuery" placeholder="Tìm kiếm sách theo tên, tác giả..." />
      </div>
      <div class="filter-actions">
        <select v-model="selectedCategory" class="bento-select">
          <option value="">Tất cả thể loại</option>
          <option v-for="category in formOptions.categories" :key="category.id" :value="category.name">
            {{ category.name }}
          </option>
        </select>
        <select v-model="selectedStatus" class="bento-select">
          <option value="">Trạng thái</option>
          <option value="active">Đang hiển thị</option>
          <option value="inactive">Đã ẩn</option>
        </select>
      </div>
    </div>

    <!-- Intelligent List -->
    <div class="intelligent-list-container bento-item">
      <div class="list-header">
        <div style="width: 64px; flex-shrink: 0;">Bìa sách</div>
        <div class="flex-2">Tên sách</div>
        <div class="flex-2">Tác giả</div>
        <div class="flex-1">Thể loại</div>
        <div style="width: 120px; flex-shrink: 0;">Trạng thái</div>
        <div class="actions-col" style="width: 100px; flex-shrink: 0; justify-content: flex-end;">Thao tác</div>
      </div>
      
      <transition-group name="list-stagger" tag="div" class="list-body">
        <div v-if="isLoading" class="empty-state" key="loading">
          <p>Đang tải danh sách sách…</p>
        </div>
        
        <div v-else v-for="book in books" :key="book.id" class="list-row">
          <div style="width: 64px; flex-shrink: 0;">
              <div class="book-cover" :class="{ 'book-cover--missing': !getCoverUrl(book) }">
                <img
                  v-if="getCoverUrl(book)"
                  :src="getCoverUrl(book)"
                  :alt="`Bìa sách ${book.title}`"
                  @error="markCoverAsUnavailable(book.id)"
                />
                <span v-else class="book-cover-fallback" aria-hidden="true">{{ getCoverInitials(book.title) }}</span>
            </div>
          </div>
          <div class="flex-2 text-truncate">
            <strong class="book-title" :title="book.title">{{ book.title }}</strong>
            <span class="book-id">#{{ book.id }}</span>
          </div>
          <div class="flex-2 text-truncate">
            <span class="author-names" :title="book.authorNames?.join(', ')">{{ book.authorNames?.join(', ') || 'Chưa cập nhật' }}</span>
          </div>
          <div class="flex-1 text-truncate">
            <span class="badge" :title="book.categoryNames?.join(', ')">{{ book.categoryNames?.join(', ') || 'Chưa phân loại' }}</span>
          </div>
          <div style="width: 120px; flex-shrink: 0;">
            <span class="status-badge" :class="book.isActive ? 'success' : 'warning'">
              {{ book.isActive ? 'Đang hiển thị' : 'Đã ẩn' }}
            </span>
          </div>
          <div class="actions-col" style="width: 100px; justify-content: flex-end;">
            <div class="action-buttons">
              <button class="icon-btn edit magnetic-btn" @click="openEditModal(book.id)" title="Chỉnh sửa">
                <PhPencilSimple :size="18" />
              </button>
              <button
                class="icon-btn magnetic-btn"
                :class="book.isActive ? 'danger' : 'info'"
                @click="toggleBookStatus(book)"
                :title="book.isActive ? 'Ẩn sách' : 'Hiển thị lại sách'"
              >
                <PhTrash :size="18" />
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="!isLoading && books.length === 0" class="empty-state" key="empty">
          <p>{{ errorMessage || 'Không tìm thấy sách phù hợp.' }}</p>
        </div>
      </transition-group>
    </div>

    <div v-if="books.length > 0" class="pagination">
      <button class="page-btn" :disabled="currentPage === 0 || isLoading" @click="changePage(currentPage - 1)">Trước</button>
      <span>Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
      <button class="page-btn" :disabled="currentPage + 1 >= totalPages || isLoading" @click="changePage(currentPage + 1)">Sau</button>
    </div>

    <p v-if="successMessage" class="feedback-message success-message">{{ successMessage }}</p>
    <p v-if="errorMessage && books.length > 0" class="feedback-message error-message">{{ errorMessage }}</p>

    <div v-if="isModalOpen" class="modal-backdrop" @click.self="closeModal">
      <section class="book-modal" role="dialog" aria-modal="true" :aria-label="modalTitle">
        <header class="modal-header">
          <div>
            <h3>{{ modalTitle }}</h3>
            <p>Thông tin có dấu * là bắt buộc.</p>
          </div>
          <button class="icon-btn" type="button" title="Đóng" @click="closeModal"><PhX :size="22" /></button>
        </header>

        <form class="book-form" @submit.prevent="saveBook">
          <label>
            Tên sách *
            <input v-model="form.title" required maxlength="255" />
          </label>
          <label>
            Đường dẫn SEO *
            <input v-model="form.slug" required maxlength="255" placeholder="ten-sach-khong-dau" />
          </label>
          <label class="full-width">
            Mô tả
            <textarea v-model="form.description" rows="4" placeholder="Mô tả ngắn về sách"></textarea>
          </label>
          <label>
            Nhà xuất bản *
            <select v-model="form.publisherId" required>
              <option disabled value="">Chọn nhà xuất bản</option>
              <option v-for="publisher in formOptions.publishers" :key="publisher.id" :value="String(publisher.id)">{{ publisher.name }}</option>
            </select>
          </label>
          <label>
            Trạng thái
            <select v-model="form.isActive">
              <option :value="true">Đang hiển thị</option>
              <option :value="false">Đã ẩn</option>
            </select>
          </label>
          <label>
            Tác giả *
            <select v-model="form.authorIds" multiple required>
              <option v-for="author in formOptions.authors" :key="author.id" :value="String(author.id)">{{ author.name }}</option>
            </select>
            <small>Giữ Ctrl hoặc Cmd để chọn nhiều tác giả.</small>
          </label>
          <label>
            Thể loại *
            <select v-model="form.categoryIds" multiple required>
              <option v-for="category in formOptions.categories" :key="category.id" :value="String(category.id)">{{ category.name }}</option>
            </select>
            <small>Giữ Ctrl hoặc Cmd để chọn nhiều thể loại.</small>
          </label>
          <label class="full-width">
            Ảnh bìa {{ editingBookId ? '(tùy chọn)' : '' }}
            <input type="file" accept="image/jpeg,image/png,image/webp" @change="handleCoverChange" />
            <small>Chỉ nhận JPG, PNG hoặc WebP.</small>
          </label>
          <img v-if="coverPreview" class="cover-preview" :src="coverPreview" alt="Xem trước ảnh bìa" />

          <div class="modal-actions full-width">
            <button class="secondary-btn" type="button" :disabled="isSaving" @click="closeModal">Hủy</button>
            <button class="primary-btn" type="submit" :disabled="isSaving">
              {{ isSaving ? 'Đang lưu…' : editingBookId ? 'Lưu thay đổi' : 'Tạo sách' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 0.5rem;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.04em;
  margin: 0 0 0.25rem 0;
}

.page-header p {
  color: var(--text-muted);
  margin: 0;
  font-size: 1.05rem;
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--text-main);
  color: var(--bento-surface);
  border: none;
  padding: 0.85rem 1.5rem;
  border-radius: 99px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px -5px rgba(0,0,0,0.2);
}

.primary-btn:active {
  transform: scale(0.96);
}

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bento-surface);
  padding: 1rem;
  border-radius: 1.5rem;
  border: 1px solid var(--bento-border);
  box-shadow: 0 10px 30px -15px rgba(0,0,0,0.03);
}

.search-box {
  position: relative;
  width: 350px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
}

.search-box input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.75rem;
  border: 1px solid var(--bento-border);
  border-radius: 99px;
  background: var(--bento-bg);
  font-family: inherit;
  font-size: 0.95rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
}

.search-box input:focus {
  border-color: #a1a1aa;
  box-shadow: 0 0 0 3px rgba(0,0,0,0.05);
}

.filter-actions {
  display: flex;
  gap: 0.75rem;
}

.bento-select {
  padding: 0.75rem 1.25rem;
  border: 1px solid var(--bento-border);
  border-radius: 99px;
  background: var(--bento-bg);
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  outline: none;
  transition: all 0.2s;
}

.bento-select:hover {
  border-color: #d4d4d8;
}

/* Intelligent List */
.intelligent-list-container {
  background: var(--bento-surface);
  border-radius: 2.5rem;
  border: 1px solid var(--bento-border);
  box-shadow: var(--bento-shadow);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem 2rem;
  background: #fafafa;
  border-bottom: 1px solid var(--bento-border);
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.list-body {
  display: flex;
  flex-direction: column;
}

.list-row {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.25rem 2rem;
  border-bottom: 1px solid var(--bento-border);
  transition: background 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.list-row:last-child {
  border-bottom: none;
}

.list-row:hover {
  background: #fdfdfd;
}

.flex-1 { flex: 1 1 0%; min-width: 0; }
.flex-2 { flex: 2 1 0%; min-width: 0; }

.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-title {
  color: var(--text-main);
  font-size: 1.05rem;
  display: block;
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-id {
  font-family: monospace;
  color: var(--text-muted);
  font-size: 0.85rem;
}

.author-names {
  color: var(--text-muted);
  font-size: 0.95rem;
}

.actions-col { display: flex; align-items: center; gap: 0.75rem; }

/* List Transition */
.list-stagger-enter-active,
.list-stagger-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.list-stagger-enter-from,
.list-stagger-leave-to {
  opacity: 0;
  transform: translateY(15px);
}

.book-cover {
  width: 52px;
  height: 72px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 0.65rem;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 18px -14px rgba(15, 23, 42, 0.45);
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.list-row:hover .book-cover img {
  transform: scale(1.05);
}

.book-cover--missing {
  background:
    linear-gradient(145deg, rgba(226, 232, 240, 0.88), rgba(248, 250, 252, 0.98));
}

.book-cover-fallback {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 1px solid rgba(148, 163, 184, 0.32);
  border-radius: 50%;
  color: #475569;
  background: rgba(255, 255, 255, 0.72);
  font-size: 0.85rem;
  font-weight: 800;
}

.badge {
  background: #f4f4f5;
  color: var(--text-muted);
  padding: 0.25rem 0.75rem;
  border-radius: 99px;
  font-size: 0.85rem;
  font-weight: 600;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.status-badge {
  padding: 0.35rem 0.85rem;
  border-radius: 99px;
  font-size: 0.8rem;
  font-weight: 700;
}

.status-badge.success {
  background: #ecfdf5;
  color: #059669;
}

.status-badge.warning {
  background: #fffbeb;
  color: #d97706;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.icon-btn {
  background: transparent;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 1rem;
  display: grid;
  place-items: center;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.icon-btn:hover {
  background: #f4f4f5;
  color: var(--text-main);
}

.icon-btn.danger:hover {
  background: #fef2f2;
  color: #ef4444;
}

.icon-btn.info:hover {
  color: #8b5cf6;
  background: #f5f3ff;
}

.icon-btn.edit:hover {
  color: #3b82f6;
  background: #eff6ff;
}

/* Magnetic physics simulation */
.magnetic-btn:active {
  transform: scale(0.92);
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.9rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.page-btn,
.secondary-btn {
  border: 1px solid var(--bento-border);
  border-radius: 99px;
  background: var(--bento-surface);
  color: var(--text-main);
  padding: 0.65rem 1rem;
  cursor: pointer;
  font: inherit;
}

.page-btn:disabled,
.secondary-btn:disabled,
.primary-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.feedback-message {
  margin: 0;
  padding: 0.9rem 1rem;
  border-radius: 1rem;
  font-weight: 600;
}

.success-message {
  background: #ecfdf5;
  color: #047857;
}

.error-message {
  background: #fef2f2;
  color: #b91c1c;
}

.modal-backdrop {
  position: fixed;
  z-index: 50;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background: rgba(9, 9, 11, 0.45);
}

.book-modal {
  width: min(760px, 100%);
  max-height: calc(100vh - 3rem);
  overflow: auto;
  border: 1px solid var(--bento-border);
  border-radius: 1.5rem;
  background: var(--bento-surface);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.25);
  padding: 1.5rem;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.modal-header h3,
.modal-header p {
  margin: 0;
}

.modal-header h3 {
  font-size: 1.4rem;
}

.modal-header p,
.book-form small {
  color: var(--text-muted);
  font-size: 0.85rem;
}

.book-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.book-form label {
  display: grid;
  gap: 0.45rem;
  color: var(--text-main);
  font-weight: 600;
  font-size: 0.9rem;
}

.book-form input,
.book-form select,
.book-form textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--bento-border);
  border-radius: 0.75rem;
  background: #fff;
  padding: 0.7rem 0.8rem;
  color: var(--text-main);
  font: inherit;
}

.book-form select[multiple] {
  min-height: 8rem;
}

.full-width {
  grid-column: 1 / -1;
}

.cover-preview {
  width: 96px;
  height: 128px;
  border-radius: 0.75rem;
  object-fit: cover;
  border: 1px solid var(--bento-border);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

@media (max-width: 720px) {
  .toolbar,
  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }

  .filter-actions,
  .filter-actions select {
    width: 100%;
  }

  .book-form {
    grid-template-columns: 1fr;
  }
}
</style>
