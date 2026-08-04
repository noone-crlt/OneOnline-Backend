<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  PhMagnifyingGlass,
  PhPlus,
  PhPencilSimple,
  PhTrash,
  PhBookmarkSimple,
  PhX,
} from '@phosphor-icons/vue'
import {
  getCategories,
  createAdminCategory,
  updateAdminCategory,
  deleteAdminCategory
} from '../../services/api'
import { toast } from 'vue-sonner'

const categories = ref([])
const isLoading = ref(false)
const isSaving = ref(false)
const searchQuery = ref('')
const isModalOpen = ref(false)
const editingCategoryId = ref(null)
const categoryForm = ref({
  name: ''
})

const filteredCategories = computed(() => {
  if (!searchQuery.value.trim()) return categories.value
  const query = searchQuery.value.toLowerCase().trim()
  return categories.value.filter(c => c.name?.toLowerCase().includes(query))
})

const modalTitle = computed(() => editingCategoryId.value ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới')

function generateSlug(name) {
  if (!name) return ''
  return name.toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[đĐ]/g, 'd')
    .replace(/([^0-9a-z-\s])/g, '')
    .replace(/(\s+)/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-+|-+$/g, '')
}

async function loadCategories() {
  isLoading.value = true
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error(error)
    toast.error('Không thể tải danh sách danh mục.')
  } finally {
    isLoading.value = false
  }
}

function openCreateModal() {
  editingCategoryId.value = null
  categoryForm.value.name = ''
  isModalOpen.value = true
}

function openEditModal(category) {
  editingCategoryId.value = category.id
  categoryForm.value.name = category.name
  isModalOpen.value = true
}

function closeModal() {
  isModalOpen.value = false
  editingCategoryId.value = null
  categoryForm.value.name = ''
}

async function saveCategory() {
  if (!categoryForm.value.name.trim()) return

  isSaving.value = true
  try {
    const payload = { name: categoryForm.value.name.trim() }
    if (editingCategoryId.value) {
      await updateAdminCategory(editingCategoryId.value, payload)
      toast.success('Cập nhật danh mục thành công.')
    } else {
      await createAdminCategory(payload)
      toast.success('Thêm danh mục thành công.')
    }
    closeModal()
    await loadCategories()
  } catch (error) {
    console.error(error)
    toast.error(error instanceof Error ? error.message : 'Không thể lưu danh mục.')
  } finally {
    isSaving.value = false
  }
}

async function handleDeleteCategory(id) {
  if (!window.confirm('Bạn có chắc chắn muốn xóa danh mục này?')) return

  try {
    await deleteAdminCategory(id)
    toast.success('Xóa danh mục thành công.')
    await loadCategories()
  } catch (error) {
    console.error(error)
    toast.error(error instanceof Error ? error.message : 'Không thể xóa danh mục.')
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<template>
  <div class="admin-page">
    <!-- Header -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Danh mục</h2>
        <p>Phân loại và tổ chức các tác phẩm theo thể loại.</p>
      </div>
      <button class="primary-btn" @click="openCreateModal">
        <PhPlus :size="20" weight="bold" />
        Thêm danh mục
      </button>
    </header>

    <!-- Toolbar -->
    <div class="toolbar bento-item">
      <div class="search-box">
        <PhMagnifyingGlass :size="20" class="search-icon" />
        <input type="text" v-model="searchQuery" placeholder="Tìm kiếm danh mục..." />
      </div>
    </div>

    <!-- Intelligent List -->
    <div class="intelligent-list-container bento-item">
      <div class="list-header">
        <div style="width: 80px;">ID</div>
        <div class="flex-2">Tên danh mục</div>
        <div class="flex-2">Đường dẫn (Slug)</div>
        <div class="flex-1">Số lượng sách</div>
        <div class="actions-col" style="width: 100px; text-align: right;">Thao tác</div>
      </div>
      
      <transition-group name="list-stagger" tag="div" class="list-body">
        <div v-if="isLoading" class="empty-state" key="loading">
          <p>Đang tải danh sách danh mục…</p>
        </div>
        
        <div v-else v-for="category in filteredCategories" :key="category.id" class="list-row">
          <div style="width: 80px;">
            <span class="id-badge">#{{ category.id }}</span>
          </div>
          <div class="flex-2">
            <div class="category-name">
              <PhBookmarkSimple :size="20" color="#ec4899" weight="duotone" />
              <strong>{{ category.name }}</strong>
            </div>
          </div>
          <div class="flex-2">
            <code class="slug-code">{{ generateSlug(category.name) }}</code>
          </div>
          <div class="flex-1">
            <span class="count-badge">{{ category.bookCount || 0 }} tác phẩm</span>
          </div>
          <div class="actions-col" style="width: 100px; justify-content: flex-end;">
            <div class="action-buttons">
              <button class="icon-btn edit magnetic-btn" @click="openEditModal(category)" title="Chỉnh sửa">
                <PhPencilSimple :size="18" />
              </button>
              <button class="icon-btn danger magnetic-btn" @click="handleDeleteCategory(category.id)" title="Xóa">
                <PhTrash :size="18" />
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="!isLoading && filteredCategories.length === 0" class="empty-state" key="empty">
          <p>Chưa có danh mục nào.</p>
        </div>
      </transition-group>
    </div>

    <!-- Category Modal -->
    <div v-if="isModalOpen" class="modal-backdrop" @click.self="closeModal">
      <section class="category-modal" role="dialog" aria-modal="true" :aria-label="modalTitle">
        <header class="modal-header">
          <div>
            <h3>{{ modalTitle }}</h3>
            <p>Thông tin có dấu * là bắt buộc.</p>
          </div>
          <button class="icon-btn" type="button" title="Đóng" @click="closeModal"><PhX :size="22" /></button>
        </header>

        <form class="category-form" @submit.prevent="saveCategory">
          <label>
            Tên danh mục *
            <input v-model="categoryForm.name" required placeholder="Ví dụ: Lịch sử, Kinh tế..." maxlength="255" />
          </label>

          <div class="modal-actions">
            <button class="secondary-btn" type="button" :disabled="isSaving" @click="closeModal">Hủy</button>
            <button class="primary-btn" type="submit" :disabled="isSaving">
              {{ isSaving ? 'Đang lưu…' : editingCategoryId ? 'Lưu thay đổi' : 'Tạo danh mục' }}
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

.flex-1 { flex: 1; min-width: 0; }
.flex-2 { flex: 2; min-width: 0; }
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

.id-badge {
  font-family: monospace;
  color: var(--text-muted);
  background: #f4f4f5;
  padding: 0.3rem 0.6rem;
  border-radius: 0.75rem;
  font-weight: 600;
  font-size: 0.9rem;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.category-name strong {
  font-size: 1.05rem;
  color: var(--text-main);
  letter-spacing: -0.01em;
}

.slug-code {
  font-family: monospace;
  color: #6366f1;
  background: #eef2ff;
  padding: 0.3rem 0.6rem;
  border-radius: 0.75rem;
  font-size: 0.9rem;
}

.count-badge {
  font-size: 0.9rem;
  font-weight: 600;
  color: #10b981;
  background: #d1fae5;
  padding: 0.3rem 0.75rem;
  border-radius: 99px;
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

/* Magnetic physics simulation */
.magnetic-btn:active {
  transform: scale(0.92);
}

.empty-state {
  padding: 4rem 2rem;
  text-align: center;
  color: var(--text-muted);
  font-weight: 500;
}

/* Modal Styling */
.modal-backdrop {
  position: fixed;
  z-index: 50;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background: rgba(9, 9, 11, 0.45);
}

.category-modal {
  width: min(480px, 100%);
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

.modal-header h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.4rem;
  color: var(--text-main);
}

.modal-header p {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.85rem;
}

.category-form {
  display: grid;
  gap: 1.25rem;
}

.category-form label {
  display: grid;
  gap: 0.45rem;
  color: var(--text-main);
  font-weight: 600;
  font-size: 0.9rem;
}

.category-form input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--bento-border);
  border-radius: 0.75rem;
  background: #fff;
  padding: 0.7rem 0.8rem;
  color: var(--text-main);
  font: inherit;
  outline: none;
  transition: border-color 0.2s;
}

.category-form input:focus {
  border-color: #a1a1aa;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.secondary-btn {
  background: #f4f4f5;
  color: var(--text-main);
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: 99px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-btn:hover {
  background: #e4e4e7;
}
</style>
