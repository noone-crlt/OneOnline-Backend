<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  PhMagnifyingGlass,
  PhPlus,
  PhPencilSimple,
  PhTrash,
  PhUserCircle,
  PhPenNib,
  PhBooks,
  PhTrophy,
  PhX,
} from '@phosphor-icons/vue'
import {
  getAuthors,
  createAdminAuthor,
  updateAdminAuthor,
  deleteAdminAuthor
} from '../../services/api'
import notify, { confirmDialog } from '../../services/toast'

const authors = ref([])
const isLoading = ref(false)
const isSaving = ref(false)
const searchQuery = ref('')
const isModalOpen = ref(false)
const editingAuthorId = ref(null)

const authorForm = ref({
  name: '',
  bio: '',
  avatar: ''
})

const filteredAuthors = computed(() => {
  if (!searchQuery.value.trim()) return authors.value
  const query = searchQuery.value.toLowerCase().trim()
  return authors.value.filter(a => 
    a.name?.toLowerCase().includes(query) || 
    a.bio?.toLowerCase().includes(query)
  )
})

const totalAuthorsCount = computed(() => authors.value.length)

const totalAuthoredBooks = computed(() => {
  return authors.value.reduce((acc, author) => acc + (Number(author.bookCount) || 0), 0)
})

const topAuthor = computed(() => {
  if (authors.value.length === 0) return null
  return [...authors.value].sort((a, b) => (Number(b.bookCount) || 0) - (Number(a.bookCount) || 0))[0]
})

const modalTitle = computed(() => editingAuthorId.value ? 'Chỉnh sửa tác giả' : 'Thêm tác giả mới')

async function loadAuthors() {
  isLoading.value = true
  try {
    authors.value = await getAuthors()
  } catch (error) {
    console.error(error)
    notify.error('Không thể tải danh sách tác giả.')
  } finally {
    isLoading.value = false
  }
}

function openCreateModal() {
  editingAuthorId.value = null
  authorForm.value = { name: '', bio: '', avatar: '' }
  isModalOpen.value = true
}

function openEditModal(author) {
  editingAuthorId.value = author.id
  authorForm.value = {
    name: author.name || '',
    bio: author.bio || '',
    avatar: author.avatar || ''
  }
  isModalOpen.value = true
}

function closeModal() {
  isModalOpen.value = false
  editingAuthorId.value = null
  authorForm.value = { name: '', bio: '', avatar: '' }
}

async function saveAuthor() {
  if (!authorForm.value.name.trim()) return

  isSaving.value = true
  try {
    const payload = {
      name: authorForm.value.name.trim(),
      bio: authorForm.value.bio?.trim() || '',
      avatar: authorForm.value.avatar?.trim() || ''
    }
    if (editingAuthorId.value) {
      await updateAdminAuthor(editingAuthorId.value, payload)
      notify.success('Cập nhật tác giả thành công.')
    } else {
      await createAdminAuthor(payload)
      notify.success('Thêm tác giả thành công.')
    }
    closeModal()
    await loadAuthors()
  } catch (error) {
    console.error(error)
    notify.error(error instanceof Error ? error.message : 'Không thể lưu thông tin tác giả.')
  } finally {
    isSaving.value = false
  }
}

async function handleDeleteAuthor(id) {
  const confirmed = await confirmDialog('Xác nhận xóa tác giả', 'Bạn có chắc chắn muốn xóa tác giả này không?', 'Xóa tác giả', 'Hủy')
  if (!confirmed) return

  try {
    await deleteAdminAuthor(id)
    notify.success('Xóa tác giả thành công.')
    await loadAuthors()
  } catch (error) {
    console.error(error)
    notify.error(error instanceof Error ? error.message : 'Không thể xóa tác giả.')
  }
}

onMounted(() => {
  loadAuthors()
})
</script>

<template>
  <div class="admin-page">
    <!-- Header -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Tác giả</h2>
        <p>Phân loại và tổ chức thông tin về các tác giả & dịch giả trên hệ thống.</p>
      </div>
      <button class="primary-btn" @click="openCreateModal">
        <PhPlus :size="20" weight="bold" />
        Thêm tác giả
      </button>
    </header>

    <!-- Author KPI Summary Bar -->
    <div class="author-kpi-bar bento-item">
      <div class="author-kpi-card">
        <div class="kpi-icon-pill" style="color: #0ea5e9; background-color: rgba(14, 165, 233, 0.12);">
          <PhPenNib :size="20" weight="duotone" />
        </div>
        <div class="kpi-info">
          <span class="kpi-title">TỔNG TÁC GIẢ</span>
          <span class="kpi-value">{{ totalAuthorsCount }}</span>
        </div>
      </div>

      <div class="author-kpi-card">
        <div class="kpi-icon-pill" style="color: #6366f1; background-color: rgba(99, 102, 241, 0.12);">
          <PhBooks :size="20" weight="duotone" />
        </div>
        <div class="kpi-info">
          <span class="kpi-title">TÁC PHẨM CÓ TÁC GIẢ</span>
          <span class="kpi-value">{{ totalAuthoredBooks }} tác phẩm</span>
        </div>
      </div>

      <div class="author-kpi-card">
        <div class="kpi-icon-pill" style="color: #f59e0b; background-color: rgba(245, 158, 11, 0.12);">
          <PhTrophy :size="20" weight="duotone" />
        </div>
        <div class="kpi-info">
          <span class="kpi-title">TÁC GIẢ NỔI BẬT NHẤT</span>
          <span class="kpi-value">{{ topAuthor ? `${topAuthor.name} (${topAuthor.bookCount || 0})` : 'Chưa có' }}</span>
        </div>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="toolbar bento-item">
      <div class="search-box">
        <PhMagnifyingGlass :size="20" class="search-icon" />
        <input type="text" v-model="searchQuery" placeholder="Tìm kiếm theo tên tác giả hoặc tiểu sử..." />
      </div>
    </div>

    <!-- Intelligent List / Table -->
    <div class="intelligent-list-container bento-item">
      <div class="list-header">
        <div style="width: 70px;">ID</div>
        <div class="flex-2">Tác giả</div>
        <div class="flex-3">Tiểu sử (Bio)</div>
        <div class="flex-1">Số lượng sách</div>
        <div class="actions-col" style="width: 100px; text-align: right;">Thao tác</div>
      </div>
      
      <transition-group name="list-stagger" tag="div" class="list-body">
        <div v-if="isLoading" class="empty-state" key="loading">
          <p>Đang tải danh sách tác giả…</p>
        </div>
        
        <div v-else v-for="author in filteredAuthors" :key="author.id" class="list-row">
          <div style="width: 70px;">
            <span class="id-badge">#{{ author.id }}</span>
          </div>
          <div class="flex-2">
            <div class="author-profile">
              <img v-if="author.avatar" :src="author.avatar" alt="Avatar" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <PhUserCircle :size="32" weight="duotone" color="#0ea5e9" />
              </div>
              <strong>{{ author.name }}</strong>
            </div>
          </div>
          <div class="flex-3">
            <p class="bio-text">{{ author.bio || 'Chưa cập nhật tiểu sử' }}</p>
          </div>
          <div class="flex-1">
            <span class="count-badge">{{ author.bookCount || 0 }} tác phẩm</span>
          </div>
          <div class="actions-col" style="width: 100px; justify-content: flex-end;">
            <div class="action-buttons">
              <button class="icon-btn edit magnetic-btn" @click="openEditModal(author)" title="Chỉnh sửa">
                <PhPencilSimple :size="18" />
              </button>
              <button class="icon-btn danger magnetic-btn" @click="handleDeleteAuthor(author.id)" title="Xóa">
                <PhTrash :size="18" />
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="!isLoading && filteredAuthors.length === 0" class="empty-state" key="empty">
          <p>Chưa có tác giả nào.</p>
        </div>
      </transition-group>
    </div>

    <!-- Author Modal -->
    <div v-if="isModalOpen" class="modal-backdrop" @click.self="closeModal">
      <section class="author-modal" role="dialog" aria-modal="true" :aria-label="modalTitle">
        <header class="modal-header">
          <div>
            <h3>{{ modalTitle }}</h3>
            <p>Thông tin có dấu * là bắt buộc.</p>
          </div>
          <button class="icon-btn" type="button" title="Đóng" @click="closeModal"><PhX :size="22" /></button>
        </header>

        <form class="author-form" @submit.prevent="saveAuthor">
          <label>
            Tên tác giả *
            <input v-model="authorForm.name" required placeholder="Ví dụ: Dale Carnegie, Rosie Nguyễn..." maxlength="255" />
          </label>

          <label>
            URL Ảnh đại diện
            <input v-model="authorForm.avatar" placeholder="https://example.com/avatar.jpg" maxlength="500" />
          </label>

          <label>
            Tiểu sử / Tóm tắt về tác giả
            <textarea v-model="authorForm.bio" rows="4" placeholder="Nhập tóm tắt tiểu sử và phong cách sáng tác của tác giả..."></textarea>
          </label>

          <div class="modal-actions">
            <button class="secondary-btn" type="button" :disabled="isSaving" @click="closeModal">Hủy</button>
            <button class="primary-btn" type="submit" :disabled="isSaving">
              {{ isSaving ? 'Đang lưu…' : editingAuthorId ? 'Lưu thay đổi' : 'Tạo tác giả' }}
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

/* Author KPI Summary Bar */
.author-kpi-bar {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  width: 100%;
}

@media (max-width: 768px) {
  .author-kpi-bar {
    grid-template-columns: 1fr;
  }
}

.author-kpi-card {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.9rem 1.15rem;
  background: #ffffff;
  border-radius: 0.95rem;
  border: 1px solid rgba(24, 24, 27, 0.08);
  box-shadow: 0 4px 14px -4px rgba(0, 0, 0, 0.03);
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.25s ease;
}

.author-kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px -6px rgba(0, 0, 0, 0.07);
}

.kpi-icon-pill {
  width: 40px;
  height: 40px;
  border-radius: 0.65rem;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.kpi-info {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  overflow: hidden;
}

.kpi-title {
  font-size: 0.72rem;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.kpi-value {
  font-size: 1.25rem;
  font-weight: 800;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'JetBrains Mono', 'Geist Mono', Satoshi, monospace;
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
  width: 400px;
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
  padding: 0.75rem 1rem 0.75rem 2.8rem;
  border-radius: 99px;
  border: 1px solid var(--bento-border);
  background: rgba(0,0,0,0.02);
  font-family: inherit;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.3s ease;
}

.search-box input:focus {
  background: #ffffff;
  border-color: rgba(14, 165, 233, 0.4);
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.1);
}

/* Intelligent List */
.intelligent-list-container {
  background: #ffffff;
  border-radius: 1.5rem;
  border: 1px solid rgba(24, 24, 27, 0.08);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.list-header {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border-bottom: 1px solid rgba(24, 24, 27, 0.06);
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
}

.list-body {
  display: flex;
  flex-direction: column;
}

.list-row {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid rgba(24, 24, 27, 0.04);
  transition: background-color 0.2s ease;
}

.list-row:last-child {
  border-bottom: none;
}

.list-row:hover {
  background-color: #f8fafc;
}

.flex-1 { flex: 1; }
.flex-2 { flex: 2; }
.flex-3 { flex: 3; }

.id-badge {
  font-family: 'JetBrains Mono', 'Geist Mono', Satoshi, monospace;
  font-weight: 700;
  font-size: 0.82rem;
  color: #64748b;
}

.author-profile {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: rgba(14, 165, 233, 0.1);
}

.bio-text {
  font-size: 0.88rem;
  color: #475569;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.count-badge {
  font-size: 0.78rem;
  font-weight: 700;
  padding: 0.3rem 0.7rem;
  border-radius: 99px;
  background: rgba(14, 165, 233, 0.1);
  color: #0ea5e9;
  font-family: 'JetBrains Mono', 'Geist Mono', Satoshi, monospace;
}

.actions-col {
  display: flex;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.icon-btn {
  background: none;
  border: none;
  padding: 0.45rem;
  border-radius: 0.5rem;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
  display: grid;
  place-items: center;
}

.icon-btn.edit:hover {
  background: rgba(14, 165, 233, 0.1);
  color: #0ea5e9;
}

.icon-btn.danger:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.empty-state {
  padding: 3rem;
  text-align: center;
  color: #64748b;
  font-weight: 500;
}

/* Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 1rem;
}

.author-modal {
  background: #ffffff;
  width: 100%;
  max-width: 520px;
  border-radius: 1.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  animation: modalIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes modalIn {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(24, 24, 27, 0.06);
}

.modal-header h3 {
  font-size: 1.35rem;
  font-weight: 800;
  margin: 0 0 0.25rem 0;
  color: #0f172a;
}

.modal-header p {
  font-size: 0.85rem;
  color: #64748b;
  margin: 0;
}

.author-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 1.5rem;
}

.author-form label {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  font-size: 0.88rem;
  font-weight: 700;
  color: #334155;
}

.author-form input,
.author-form textarea {
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  border: 1px solid rgba(24, 24, 27, 0.12);
  font-family: inherit;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.author-form input:focus,
.author-form textarea:focus {
  border-color: #0ea5e9;
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.1);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.secondary-btn {
  background: #f1f5f9;
  color: #334155;
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: 99px;
  font-weight: 600;
  cursor: pointer;
}

.secondary-btn:hover {
  background: #e2e8f0;
}
</style>
