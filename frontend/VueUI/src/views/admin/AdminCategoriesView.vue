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

    <!-- Data Table -->
    <div class="table-container bento-item">
      <table class="bento-table">
        <thead>
          <tr>
            <th style="width: 60px;">ID</th>
            <th>Tên danh mục</th>
            <th>Đường dẫn (Slug)</th>
            <th>Số lượng sách</th>
            <th class="actions-col">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="category in categories" :key="category.id">
            <td>
              <span class="id-badge">#{{ category.id }}</span>
            </td>
            <td>
              <div class="category-name">
                <PhBookmarkSimple :size="20" color="#ec4899" weight="duotone" />
                <strong>{{ category.name }}</strong>
              </div>
            </td>
            <td>
              <code class="slug-code">{{ category.slug }}</code>
            </td>
            <td>
              <span class="count-badge">{{ category.bookCount }} tác phẩm</span>
            </td>
            <td class="actions-col">
              <div class="action-buttons">
                <button class="icon-btn edit" @click="handleEditCategory(category.id)" title="Chỉnh sửa">
                  <PhPencilSimple :size="18" />
                </button>
                <button class="icon-btn danger" @click="handleDeleteCategory(category.id)" title="Xóa">
                  <PhTrash :size="18" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="categories.length === 0">
            <td colspan="5" class="empty-state">
              <p>Chưa có danh mục nào.</p>
            </td>
          </tr>
        </tbody>
      </table>
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

/* Table */
.table-container {
  background: var(--bento-surface);
  border-radius: 1.5rem;
  border: 1px solid var(--bento-border);
  box-shadow: 0 10px 30px -15px rgba(0,0,0,0.03);
  overflow: hidden;
}

.bento-table {
  width: 100%;
  border-collapse: collapse;
}

.bento-table th {
  background: #fafafa;
  padding: 1rem 1.5rem;
  text-align: left;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--bento-border);
}

.bento-table td {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--bento-border);
  vertical-align: middle;
}

.bento-table tr:last-child td {
  border-bottom: none;
}

.bento-table tr:hover td {
  background: #fdfdfd;
}

.id-badge {
  font-family: monospace;
  color: var(--text-muted);
  background: #f4f4f5;
  padding: 0.2rem 0.5rem;
  border-radius: 0.5rem;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.slug-code {
  background: #f4f4f5;
  padding: 0.25rem 0.5rem;
  border-radius: 0.5rem;
  font-family: monospace;
  font-size: 0.85rem;
  color: #52525b;
}

.count-badge {
  background: #f4f4f5;
  color: var(--text-muted);
  padding: 0.25rem 0.75rem;
  border-radius: 99px;
  font-size: 0.85rem;
  font-weight: 600;
}

.actions-col {
  text-align: right;
  width: 120px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-muted);
}

.icon-btn:hover {
  background: #f4f4f5;
  color: var(--text-main);
}

.icon-btn.edit:hover {
  color: #3b82f6;
  background: #eff6ff;
}

.icon-btn.danger:hover {
  color: #ef4444;
  background: #fef2f2;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}
</style>
