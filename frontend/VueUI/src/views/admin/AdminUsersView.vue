<script setup>
import { ref, onMounted, watch } from 'vue'
import {
  PhMagnifyingGlass,
  PhLockKey,
  PhCheckCircle,
  PhCaretLeft,
  PhCaretRight,
  PhUser,
} from '@phosphor-icons/vue'
import { getAdminUsers, toggleBanAdminUser } from '../../services/api'
import notify, { confirmDialog, promptDialog } from '../../services/toast'

const users = ref([])
const totalUsers = ref(0)
const currentPage = ref(0)
const totalPages = ref(1)
const searchQuery = ref('')
const selectedRole = ref('')
const selectedStatus = ref('')
const isLoading = ref(false)

let searchTimer = null

async function loadUsers() {
  isLoading.value = true
  try {
    const res = await getAdminUsers({
      search: searchQuery.value,
      role: selectedRole.value,
      status: selectedStatus.value,
      page: currentPage.value,
      size: 10,
    })
    users.value = res.content || []
    totalPages.value = res.totalPages || 1
    totalUsers.value = res.totalElements || 0
  } catch (error) {
    notify.error(error instanceof Error ? error.message : 'Không thể tải danh sách người dùng.')
  } finally {
    isLoading.value = false
  }
}

async function handleToggleBan(user) {
  const isBanned = user.status === 'BANNED'

  if (isBanned) {
    const confirmed = await confirmDialog(
      'Xác nhận gỡ khóa tài khoản',
      `Bạn có chắc chắn muốn gỡ khóa tài khoản cho "${user.fullName || user.email}" không?`,
      'Gỡ khóa',
      'Hủy'
    )
    if (!confirmed) return

    try {
      await toggleBanAdminUser(user.id, false, '')
      notify.success('Đã gỡ khóa tài khoản thành công.')
      await loadUsers()
    } catch (error) {
      notify.error(error instanceof Error ? error.message : 'Không thể gỡ khóa tài khoản.')
    }
  } else {
    const reasonInput = await promptDialog(
      'Khóa tài khoản người dùng',
      `Nhập lý do khóa tài khoản cho "${user.fullName || user.email}":`,
      'Vi phạm quy định sử dụng hệ thống.',
      'Khóa tài khoản',
      'Hủy'
    )
    if (reasonInput === null) return

    const reason = reasonInput.trim() || 'Vi phạm quy định của hệ thống.'

    try {
      await toggleBanAdminUser(user.id, true, reason)
      notify.success('Đã khóa tài khoản người dùng thành công.')
      await loadUsers()
    } catch (error) {
      notify.error(error instanceof Error ? error.message : 'Không thể khóa tài khoản.')
    }
  }
}

function changePage(page) {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  loadUsers()
}

function formatRole(roles) {
  if (!roles || roles.length === 0) return 'Người dùng'
  const hasAdmin = roles.some((r) => String(r).toUpperCase().includes('ADMIN'))
  return hasAdmin ? 'Quản trị viên' : 'Người dùng'
}

function formatDate(dateStr) {
  if (!dateStr) return 'Chưa cập nhật'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleDateString('vi-VN')
}

function formatDateTime(dateStr) {
  if (!dateStr) return 'Chưa xác định'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

watch([selectedRole, selectedStatus], () => {
  currentPage.value = 0
  loadUsers()
})

watch(searchQuery, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 0
    loadUsers()
  }, 350)
})

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div class="admin-page">
    <!-- Header (Chỉ còn tiêu đề, không có nút thêm) -->
    <header class="page-header bento-item">
      <div>
        <h2>Quản lý Người dùng</h2>
        <p>Quản lý danh sách tài khoản và quyền khóa/mở khóa đăng nhập.</p>
      </div>
    </header>

    <!-- Toolbar -->
    <div class="toolbar bento-item">
      <div class="search-box">
        <PhMagnifyingGlass :size="20" class="search-icon" />
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Tìm kiếm theo tên, email, sđt..."
        />
      </div>
      <div class="filter-actions">
        <select v-model="selectedRole" class="bento-select">
          <option value="">Tất cả vai trò</option>
          <option value="ADMIN">Quản trị viên</option>
          <option value="USER">Người dùng</option>
        </select>
        <select v-model="selectedStatus" class="bento-select">
          <option value="">Trạng thái</option>
          <option value="ACTIVE">Hoạt động</option>
          <option value="BANNED">Bị khóa</option>
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
            <th>SĐT</th>
            <th>Vai trò</th>
            <th>Ngày tham gia</th>
            <th>Trạng thái</th>
            <th class="actions-col">Thao tác (Khóa / Gỡ khóa)</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>
              <div class="user-profile">
                <div class="avatar">{{ (user.fullName || user.email || 'U').charAt(0).toUpperCase() }}</div>
                <strong>{{ user.fullName || 'Chưa đặt tên' }}</strong>
              </div>
            </td>
            <td>{{ user.email }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span class="role-badge" :class="{ 'admin-role': formatRole(user.roles) === 'Quản trị viên' }">
                {{ formatRole(user.roles) }}
              </span>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td>
              <span class="status-badge" :class="user.status === 'BANNED' ? 'danger' : 'success'">
                {{ user.status === 'BANNED' ? 'Bị khóa' : 'Hoạt động' }}
              </span>
              <div v-if="user.status === 'BANNED'" class="ban-details">
                <small class="ban-reason" :title="user.banReason">
                  🚫 <strong>Lý do:</strong> {{ user.banReason || 'Vi phạm quy định' }}
                </small>
                <small class="ban-date">
                  📅 <strong>Ngày khóa:</strong> {{ formatDateTime(user.bannedAt) }}
                </small>
              </div>
            </td>
            <td class="actions-col">
              <div class="action-buttons">
                <!-- Nút duy nhất: Khóa / Gỡ khóa tài khoản -->
                <button
                  class="icon-btn"
                  :class="user.status === 'BANNED' ? 'success-btn' : 'ban-btn'"
                  @click="handleToggleBan(user)"
                  :title="user.status === 'BANNED' ? 'Gỡ khóa tài khoản' : 'Khóa tài khoản'"
                >
                  <PhCheckCircle v-if="user.status === 'BANNED'" :size="18" />
                  <PhLockKey v-else :size="18" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!isLoading && users.length === 0">
            <td colspan="7" class="empty-state">
              <PhUser :size="40" class="empty-icon" />
              <p>Không tìm thấy người dùng phù hợp.</p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="users.length > 0" class="pagination">
      <span class="page-info">
        Trang <strong>{{ currentPage + 1 }}</strong> / {{ totalPages }} (Tổng {{ totalUsers }} người dùng)
      </span>
      <div class="page-buttons">
        <button class="page-btn" :disabled="currentPage === 0 || isLoading" @click="changePage(currentPage - 1)">
          <PhCaretLeft :size="16" />
        </button>
        <button
          v-for="p in totalPages"
          :key="p"
          class="page-btn"
          :class="{ 'page-btn--active': p - 1 === currentPage }"
          :disabled="isLoading"
          @click="changePage(p - 1)"
        >
          {{ p }}
        </button>
        <button class="page-btn" :disabled="currentPage + 1 >= totalPages || isLoading" @click="changePage(currentPage + 1)">
          <PhCaretRight :size="16" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 0.5rem;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  font-size: 1.85rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 0.25rem 0;
  color: var(--text-main);
}

.page-header p {
  color: var(--text-muted);
  margin: 0;
  font-size: 0.95rem;
}

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bento-surface);
  padding: 1rem;
  border-radius: 1.25rem;
  border: 1px solid var(--bento-border);
  box-shadow: 0 10px 30px -15px rgba(0, 0, 0, 0.03);
}

.search-box {
  position: relative;
  width: 320px;
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
  padding: 0.65rem 1rem 0.65rem 2.5rem;
  border: 1px solid var(--bento-border);
  border-radius: 99px;
  background: var(--bento-bg);
  font-family: inherit;
  font-size: 0.9rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
}

.search-box input:focus {
  border-color: #a1a1aa;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.filter-actions {
  display: flex;
  gap: 0.75rem;
}

.bento-select {
  padding: 0.65rem 1.15rem;
  border: 1px solid var(--bento-border);
  border-radius: 99px;
  background: var(--bento-bg);
  font-family: inherit;
  font-size: 0.9rem;
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
  border-radius: 1.25rem;
  border: 1px solid var(--bento-border);
  box-shadow: 0 10px 30px -15px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.bento-table {
  width: 100%;
  border-collapse: collapse;
}

.bento-table th {
  background: #fafafa;
  padding: 0.9rem 1.25rem;
  text-align: left;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  border-bottom: 1px solid var(--bento-border);
}

.bento-table td {
  padding: 0.9rem 1.25rem;
  border-bottom: 1px solid var(--bento-border);
  vertical-align: middle;
  font-size: 0.9rem;
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
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--text-main, #09090b);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
}

.role-badge {
  background: #f4f4f5;
  color: var(--text-muted);
  padding: 0.2rem 0.65rem;
  border-radius: 99px;
  font-size: 0.8rem;
  font-weight: 600;
}

.role-badge.admin-role {
  background: #eff6ff;
  color: #3b82f6;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 99px;
  font-size: 0.78rem;
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

.ban-details {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  margin-top: 0.35rem;
  font-size: 0.75rem;
  color: #71717a;
}

.ban-reason {
  color: #dc2626;
  white-space: nowrap;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ban-date {
  color: #52525b;
}

.actions-col {
  text-align: right;
  width: 140px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.icon-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid var(--bento-border);
  background: var(--bento-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-muted);
}

.ban-btn:hover {
  background: #fffbeb;
  color: #d97706;
  border-color: #fcd34d;
}

.success-btn:hover {
  background: #ecfdf5;
  color: #059669;
  border-color: #6ee7b7;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.empty-icon {
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
}

.page-info {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.page-buttons {
  display: flex;
  gap: 0.35rem;
}

.page-btn {
  width: 32px;
  height: 32px;
  border-radius: 0.5rem;
  border: 1px solid var(--bento-border);
  background: var(--bento-surface);
  font-family: inherit;
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f4f4f5;
}

.page-btn--active {
  background: var(--text-main, #09090b) !important;
  color: white !important;
  border-color: var(--text-main, #09090b) !important;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
