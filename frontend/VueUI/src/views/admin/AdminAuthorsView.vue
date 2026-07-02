<script setup>
import { ref } from 'vue'
import {
  PhMagnifyingGlass,
  PhPlus,
  PhPencilSimple,
  PhTrash,
  PhUserCircle,
} from '@phosphor-icons/vue'

// Mock Data for Authors
const authors = ref([
  {
    id: 1,
    name: 'Dale Carnegie',
    bio: 'Nhà văn và nhà thuyết trình người Mỹ',
    bookCount: 3,
    avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=facearea&facepad=2&w=256&h=256&q=80'
  },
  {
    id: 2,
    name: 'Paulo Coelho',
    bio: 'Tiểu thuyết gia nổi tiếng người Brazil',
    bookCount: 5,
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=facearea&facepad=2&w=256&h=256&q=80'
  },
  {
    id: 3,
    name: 'Rosie Nguyễn',
    bio: 'Tác giả, blogger du lịch',
    bookCount: 2,
    avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=facearea&facepad=2&w=256&h=256&q=80'
  }
])

const searchQuery = ref('')

function handleAddAuthor() {
  alert('Chức năng "Thêm tác giả mới" (UI)')
}

function handleEditAuthor(id) {
  alert(`Chức năng "Sửa thông tin tác giả" (ID: ${id})`)
}

function handleDeleteAuthor(id) {
  alert(`Chức năng "Xóa tác giả" (ID: ${id})`)
}
</script>

<template>
  <div class="admin-page">
    <!-- Header -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Tác giả</h2>
        <p>Thêm và cập nhật thông tin về các tác giả trên hệ thống.</p>
      </div>
      <button class="primary-btn" @click="handleAddAuthor">
        <PhPlus :size="20" weight="bold" />
        Thêm tác giả
      </button>
    </header>

    <!-- Toolbar -->
    <div class="toolbar bento-item">
      <div class="search-box">
        <PhMagnifyingGlass :size="20" class="search-icon" />
        <input type="text" v-model="searchQuery" placeholder="Tìm kiếm tác giả..." />
      </div>
    </div>

    <!-- Data Table -->
    <div class="table-container bento-item">
      <table class="bento-table">
        <thead>
          <tr>
            <th>Tác giả</th>
            <th>Tiểu sử (Bio)</th>
            <th>Số sách</th>
            <th class="actions-col">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="author in authors" :key="author.id">
            <td>
              <div class="author-profile">
                <img v-if="author.avatar" :src="author.avatar" alt="Avatar" class="avatar-img" />
                <PhUserCircle v-else :size="40" weight="light" color="#a1a1aa" />
                <strong>{{ author.name }}</strong>
              </div>
            </td>
            <td>
              <p class="bio-text">{{ author.bio }}</p>
            </td>
            <td>
              <span class="count-badge">{{ author.bookCount }} tác phẩm</span>
            </td>
            <td class="actions-col">
              <div class="action-buttons">
                <button class="icon-btn edit" @click="handleEditAuthor(author.id)" title="Chỉnh sửa">
                  <PhPencilSimple :size="18" />
                </button>
                <button class="icon-btn danger" @click="handleDeleteAuthor(author.id)" title="Xóa">
                  <PhTrash :size="18" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="authors.length === 0">
            <td colspan="4" class="empty-state">
              <p>Chưa có tác giả nào.</p>
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

.author-profile {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.bio-text {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.95rem;
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
