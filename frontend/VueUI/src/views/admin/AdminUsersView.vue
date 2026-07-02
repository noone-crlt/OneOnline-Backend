<script setup>
import { ref } from 'vue'
import {
  PhMagnifyingGlass,
  PhPlus,
  PhPencilSimple,
  PhTrash,
  PhLockKey,
  PhCheckCircle,
} from '@phosphor-icons/vue'

// Mock Data for Users
const users = ref([
  {
    id: 1,
    fullName: 'Nguyễn Văn A',
    email: 'nguyenvana@example.com',
    role: 'Quản trị viên',
    status: 'Hoạt động',
    joinedAt: '12/05/2026'
  },
  {
    id: 2,
    fullName: 'Trần Thị B',
    email: 'tranthib@example.com',
    role: 'Người dùng',
    status: 'Hoạt động',
    joinedAt: '15/06/2026'
  },
  {
    id: 3,
    fullName: 'Lê Hoàng C',
    email: 'lehoangc@example.com',
    role: 'Người dùng',
    status: 'Bị khóa',
    joinedAt: '01/07/2026'
  }
])

const searchQuery = ref('')

function handleAddUser() {
  alert('Chức năng "Thêm người dùng mới" (UI)')
}

function handleEditUser(id) {
  alert(`Chức năng "Sửa thông tin người dùng" (ID: ${id})`)
}

function handleLockUser(id) {
  alert(`Chức năng "Khóa/Mở khóa tài khoản" (ID: ${id})`)
}

function handleDeleteUser(id) {
  alert(`Chức năng "Xóa người dùng" (ID: ${id})`)
}
</script>

<template>
  <div class="admin-page">
    <!-- Header -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Người dùng</h2>
        <p>Quản lý tài khoản, phân quyền và trạng thái người dùng.</p>
      </div>
      <button class="primary-btn" @click="handleAddUser">
        <PhPlus :size="20" weight="bold" />
        Thêm người dùng
      </button>
    </header>

    <!-- Toolbar -->
    <div class="toolbar bento-item">
      <div class="search-box">
        <PhMagnifyingGlass :size="20" class="search-icon" />
        <input type="text" v-model="searchQuery" placeholder="Tìm kiếm theo tên, email..." />
      </div>
      <div class="filter-actions">
        <select class="bento-select">
          <option value="">Tất cả vai trò</option>
          <option value="admin">Quản trị viên</option>
          <option value="user">Người dùng</option>
        </select>
        <select class="bento-select">
          <option value="">Trạng thái</option>
          <option value="active">Hoạt động</option>
          <option value="locked">Bị khóa</option>
        </select>
      </div>
    </div>

    <!-- Data Table -->
    <div class="table-container bento-item">
      <table class="bento-table">
        <thead>
          <tr>
            <th>Người dùng</th>
            <th>Email</th>
            <th>Vai trò</th>
            <th>Ngày tham gia</th>
            <th>Trạng thái</th>
            <th class="actions-col">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>
              <div class="user-profile">
                <div class="avatar">{{ user.fullName.charAt(0) }}</div>
                <strong>{{ user.fullName }}</strong>
              </div>
            </td>
            <td>{{ user.email }}</td>
            <td>
              <span class="role-badge" :class="{ 'admin-role': user.role === 'Quản trị viên' }">
                {{ user.role }}
              </span>
            </td>
            <td>{{ user.joinedAt }}</td>
            <td>
              <span class="status-badge" :class="user.status === 'Hoạt động' ? 'success' : 'danger'">
                {{ user.status }}
              </span>
            </td>
            <td class="actions-col">
              <div class="action-buttons">
                <button class="icon-btn edit" @click="handleEditUser(user.id)" title="Chỉnh sửa">
                  <PhPencilSimple :size="18" />
                </button>
                <button 
                  class="icon-btn" 
                  :class="user.status === 'Hoạt động' ? 'warning' : 'success'" 
                  @click="handleLockUser(user.id)" 
                  :title="user.status === 'Hoạt động' ? 'Khóa tài khoản' : 'Mở khóa tài khoản'"
                >
                  <PhLockKey v-if="user.status === 'Hoạt động'" :size="18" />
                  <PhCheckCircle v-else :size="18" />
                </button>
                <button class="icon-btn danger" @click="handleDeleteUser(user.id)" title="Xóa">
                  <PhTrash :size="18" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="6" class="empty-state">
              <p>Chưa có người dùng nào.</p>
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

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--text-main);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.9rem;
}

.role-badge {
  background: #f4f4f5;
  color: var(--text-muted);
  padding: 0.25rem 0.75rem;
  border-radius: 99px;
  font-size: 0.85rem;
  font-weight: 600;
}

.role-badge.admin-role {
  background: #eff6ff;
  color: #3b82f6;
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

.status-badge.danger {
  background: #fef2f2;
  color: #ef4444;
}

.actions-col {
  text-align: right;
  width: 150px;
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

.icon-btn.warning:hover {
  color: #d97706;
  background: #fffbeb;
}

.icon-btn.success:hover {
  color: #059669;
  background: #ecfdf5;
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
