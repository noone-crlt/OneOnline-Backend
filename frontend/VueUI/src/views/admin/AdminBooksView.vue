<script setup>
import { ref } from 'vue'
import {
  PhMagnifyingGlass,
  PhPlus,
  PhPencilSimple,
  PhTrash,
  PhImageSquare,
  PhDotsThree,
  PhBookOpenText
} from '@phosphor-icons/vue'

// Mock Data for Books
const books = ref([
  {
    id: 1,
    title: 'Đắc Nhân Tâm',
    author: 'Dale Carnegie',
    category: 'Tâm lý học',
    status: 'Đã xuất bản',
    views: 1250,
    cover: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=100'
  },
  {
    id: 2,
    title: 'Nhà Giả Kim',
    author: 'Paulo Coelho',
    category: 'Tiểu thuyết',
    status: 'Đã xuất bản',
    views: 890,
    cover: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&q=80&w=100'
  },
  {
    id: 3,
    title: 'Tuổi Trẻ Đáng Giá Bao Nhiêu',
    author: 'Rosie Nguyễn',
    category: 'Kỹ năng sống',
    status: 'Nháp',
    views: 0,
    cover: 'https://images.unsplash.com/photo-1554907984-15263bfd63bd?auto=format&fit=crop&q=80&w=100'
  }
])

const searchQuery = ref('')

function handleAddBook() {
  alert('Chức năng "Thêm sách" (UI)')
}

function handleEditBook(id) {
  alert(`Chức năng "Sửa sách" (ID: ${id})`)
}

function handleDeleteBook(id) {
  alert(`Chức năng "Xóa sách" (ID: ${id})`)
}
</script>

<template>
  <div class="admin-page">
    <!-- Header -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Sách</h2>
        <p>Thêm, sửa, xóa và quản lý các tác phẩm trên hệ thống.</p>
      </div>
      <button class="primary-btn" @click="handleAddBook">
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
        <select class="bento-select">
          <option value="">Tất cả thể loại</option>
          <option value="Tâm lý học">Tâm lý học</option>
          <option value="Tiểu thuyết">Tiểu thuyết</option>
          <option value="Kỹ năng sống">Kỹ năng sống</option>
        </select>
        <select class="bento-select">
          <option value="">Trạng thái</option>
          <option value="published">Đã xuất bản</option>
          <option value="draft">Bản nháp</option>
        </select>
      </div>
    </div>

    <!-- Data Table -->
    <div class="table-container bento-item">
      <table class="bento-table">
        <thead>
          <tr>
            <th>Bìa sách</th>
            <th>Tên sách</th>
            <th>Tác giả</th>
            <th>Thể loại</th>
            <th>Trạng thái</th>
            <th>Lượt xem</th>
            <th class="actions-col">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in books" :key="book.id">
            <td>
              <div class="book-cover">
                <img v-if="book.cover" :src="book.cover" alt="Cover" />
                <PhImageSquare v-else :size="24" color="#a1a1aa" />
              </div>
            </td>
            <td>
              <strong>{{ book.title }}</strong>
            </td>
            <td>{{ book.author }}</td>
            <td>
              <span class="badge">{{ book.category }}</span>
            </td>
            <td>
              <span class="status-badge" :class="book.status === 'Đã xuất bản' ? 'success' : 'warning'">
                {{ book.status }}
              </span>
            </td>
            <td>{{ book.views }}</td>
            <td class="actions-col">
              <div class="action-buttons">
                <button class="icon-btn edit" @click="handleEditBook(book.id)" title="Chỉnh sửa">
                  <PhPencilSimple :size="18" />
                </button>
                <button class="icon-btn info" title="Quản lý audio/chương">
                  <PhBookOpenText :size="18" />
                </button>
                <button class="icon-btn danger" @click="handleDeleteBook(book.id)" title="Xóa">
                  <PhTrash :size="18" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="books.length === 0">
            <td colspan="7" class="empty-state">
              <p>Chưa có sách nào.</p>
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

.book-cover {
  width: 48px;
  height: 64px;
  border-radius: 0.5rem;
  overflow: hidden;
  background: #f4f4f5;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge {
  background: #f4f4f5;
  color: var(--text-muted);
  padding: 0.25rem 0.75rem;
  border-radius: 99px;
  font-size: 0.85rem;
  font-weight: 600;
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

.icon-btn.info:hover {
  color: #8b5cf6;
  background: #f5f3ff;
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
