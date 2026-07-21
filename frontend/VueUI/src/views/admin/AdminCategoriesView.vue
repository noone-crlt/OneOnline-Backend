<script setup>
import { ref } from 'vue'
import {
  PhMagnifyingGlass,
  PhPlus,
  PhPencilSimple,
  PhTrash,
  PhBookmarkSimple,
} from '@phosphor-icons/vue'

// Mock Data for Categories
const categories = ref([
  { id: 1, name: 'Tâm lý học', slug: 'tam-ly-hoc', bookCount: 15 },
  { id: 2, name: 'Tiểu thuyết', slug: 'tieu-thuyet', bookCount: 42 },
  { id: 3, name: 'Kỹ năng sống', slug: 'ky-nang-song', bookCount: 28 },
  { id: 4, name: 'Kinh doanh', slug: 'kinh-doanh', bookCount: 10 },
  { id: 5, name: 'Lịch sử', slug: 'lich-su', bookCount: 8 }
])

const searchQuery = ref('')

function handleAddCategory() {
  alert('Chức năng "Thêm danh mục" (UI)')
}

function handleEditCategory(id) {
  alert(`Chức năng "Sửa danh mục" (ID: ${id})`)
}

function handleDeleteCategory(id) {
  alert(`Chức năng "Xóa danh mục" (ID: ${id})`)
}
</script>

<template>
  <div class="admin-page">
    <!-- Header -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Danh mục</h2>
        <p>Phân loại và tổ chức các tác phẩm theo thể loại.</p>
      </div>
      <button class="primary-btn" @click="handleAddCategory">
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
        <div v-for="category in categories" :key="category.id" class="list-row">
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
            <code class="slug-code">{{ category.slug }}</code>
          </div>
          <div class="flex-1">
            <span class="count-badge">{{ category.bookCount }} tác phẩm</span>
          </div>
          <div class="actions-col" style="width: 100px; justify-content: flex-end;">
            <div class="action-buttons">
              <button class="icon-btn edit magnetic-btn" @click="handleEditCategory(category.id)" title="Chỉnh sửa">
                <PhPencilSimple :size="18" />
              </button>
              <button class="icon-btn danger magnetic-btn" @click="handleDeleteCategory(category.id)" title="Xóa">
                <PhTrash :size="18" />
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="categories.length === 0" class="empty-state" key="empty">
          <p>Chưa có danh mục nào.</p>
        </div>
      </transition-group>
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
</style>
