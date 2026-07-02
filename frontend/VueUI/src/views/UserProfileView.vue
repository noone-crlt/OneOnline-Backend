<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authUser, updateAuthUser } from '../stores/auth'
import { getOrderHistory, getUserLibrary, updateCurrentUserProfile } from '../services/api'
import { toast } from 'vue-sonner'
import TopNavbar from '../components/layout/TopNavbar.vue'
import AppFooter from '../components/layout/AppFooter.vue'

const router = useRouter()

const activeTab = ref('library') // 'library', 'profile', 'orders'

// Profile form state
const fullName = ref(authUser.value?.fullName || 'Khách hàng One Online')
const email = ref(authUser.value?.email || 'user@oneonline.vn')
const phone = ref(authUser.value?.phone || '')
const isSaving = ref(false)

const purchasedBooks = ref([])
const orders = ref([])
const isLibraryLoading = ref(true)
const isOrdersLoading = ref(true)
const libraryError = ref('')
const ordersError = ref('')

function formatCurrency(val) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(val)
}

async function handleSaveProfile() {
  isSaving.value = true
  try {
    const updatedUser = await updateCurrentUserProfile({
      fullName: fullName.value.trim(),
      phone: phone.value.trim(),
    })
    updateAuthUser(updatedUser)
    fullName.value = updatedUser.fullName || ''
    phone.value = updatedUser.phone || ''
    toast.success('Cập nhật thông tin tài khoản thành công')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'Không thể cập nhật thông tin tài khoản.')
  } finally {
    isSaving.value = false
  }
}

function startReading(slug) {
  if (!slug) {
    toast.error('Sách chưa có đường dẫn đọc hợp lệ.')
    return
  }
  router.push(`/read/${slug}`)
}

function formatDate(value) {
  if (!value) return 'Chưa cập nhật'
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium' }).format(new Date(value))
}

function formatOrderStatus(status) {
  const labels = {
    PENDING: 'Đang chờ xử lý',
    CONFIRMED: 'Đã xác nhận',
    SHIPPING: 'Đang giao hàng',
    COMPLETED: 'Hoàn thành',
    CANCELLED: 'Đã hủy',
  }
  return labels[status] || status || 'Chưa xác định'
}

async function loadAccountData() {
  const [libraryResult, ordersResult] = await Promise.allSettled([
    getUserLibrary(),
    getOrderHistory(),
  ])

  if (libraryResult.status === 'fulfilled') purchasedBooks.value = libraryResult.value
  else libraryError.value = libraryResult.reason?.message || 'Không thể tải thư viện cá nhân.'

  if (ordersResult.status === 'fulfilled') orders.value = ordersResult.value?.content || []
  else ordersError.value = ordersResult.reason?.message || 'Không thể tải lịch sử mua hàng.'

  isLibraryLoading.value = false
  isOrdersLoading.value = false
}

onMounted(() => {
  // Sync profile values
  if (authUser.value) {
    fullName.value = authUser.value.fullName || ''
    email.value = authUser.value.email || ''
    phone.value = authUser.value.phone || ''
  }
  loadAccountData()
})
</script>

<template>
  <div class="profile-page-shell">
    <!-- Header -->
    <TopNavbar />

    <!-- Main Workspace -->
    <main class="profile-container">
      <div class="profile-grid">
        <!-- Sidebar Navigation -->
        <aside class="profile-sidebar">
          <div class="user-intro-card">
            <div class="user-avatar-large">
              {{ fullName.trim().charAt(0).toUpperCase() || 'U' }}
            </div>
            <h2 class="user-name-title">{{ fullName }}</h2>
            <p class="user-email-subtitle">{{ email }}</p>
          </div>

          <nav class="profile-menu-links">
            <button 
              :class="['menu-link-btn', { active: activeTab === 'library' }]" 
              @click="activeTab = 'library'"
            >
              <span class="link-icon">
                <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
                </svg>
              </span>
              <span>Thư viện của tôi</span>
            </button>

            <button 
              :class="['menu-link-btn', { active: activeTab === 'profile' }]" 
              @click="activeTab = 'profile'"
            >
              <span class="link-icon">
                <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </span>
              <span>Thông tin cá nhân</span>
            </button>

            <button 
              :class="['menu-link-btn', { active: activeTab === 'orders' }]" 
              @click="activeTab = 'orders'"
            >
              <span class="link-icon">
                <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
                  <rect x="2" y="5" width="20" height="14" rx="2" ry="2" />
                  <line x1="2" y1="10" x2="22" y2="10" />
                </svg>
              </span>
              <span>Lịch sử mua hàng</span>
            </button>
          </nav>
        </aside>

        <!-- Active View Area -->
        <section class="profile-content-area" aria-label="Nội dung chi tiết tài khoản">
          <!-- SUCCESS TOAST -->

          <!-- TAB: MY LIBRARY -->
          <div v-if="activeTab === 'library'" class="pane-library">
            <div class="pane-header">
              <span class="pane-kicker">Digital Assets</span>
              <h3 class="pane-title">Thư viện sách của tôi</h3>
            </div>

            <p v-if="libraryError" class="profile-state profile-state-error" role="alert">{{ libraryError }}</p>
            <p v-else-if="isLibraryLoading" class="profile-state">Đang tải thư viện của bạn...</p>
            <div v-else-if="purchasedBooks.length === 0" class="profile-state">
              <strong>Thư viện đang trống</strong>
              <span>Sách điện tử đã thanh toán sẽ xuất hiện tại đây.</span>
              <RouterLink to="/library">Khám phá sách</RouterLink>
            </div>

            <div v-else class="purchased-books-grid">
              <article 
                v-for="item in purchasedBooks" 
                :key="item.id" 
                class="purchased-book-card"
              >
                <div class="p-book-cover-frame">
                  <div class="p-book-cover">
                    <img v-if="item.coverUrl" :src="item.coverUrl" :alt="`Bìa sách ${item.bookTitle}`" />
                    <span v-else class="cover-fallback">{{ item.bookTitle?.charAt(0) || 'S' }}</span>
                  </div>
                </div>

                <div class="p-book-meta">
                  <div class="format-badge-row">
                    <span :class="['tech-tag', item.format === 'AUDIOBOOK' ? 'tag-minio' : 'tag-blue']">{{ item.format === 'AUDIOBOOK' ? 'AUDIO' : 'E-BOOK' }}</span>
                  </div>
                  <h4 class="p-book-title">{{ item.bookTitle }}</h4>
                  <p class="p-book-author">{{ item.authorName }}</p>
                  
                  <button 
                    class="btn btn-secondary btn-p-read" 
                    @click="startReading(item.slug)"
                  >
                    {{ item.format === 'AUDIOBOOK' ? 'Nghe ngay' : 'Đọc ngay' }}
                  </button>
                </div>
              </article>
            </div>
          </div>

          <!-- TAB: PERSONAL PROFILE -->
          <div v-else-if="activeTab === 'profile'" class="pane-profile">
            <div class="pane-header">
              <span class="pane-kicker">Profile Details</span>
              <h3 class="pane-title">Thông tin cá nhân</h3>
            </div>

            <form class="profile-form-layout" @submit.prevent="handleSaveProfile">
              <div class="form-row-group">
                <label for="p-email">Địa chỉ Email</label>
                <input id="p-email" type="email" v-model="email" disabled class="form-input-disabled" />
                <span class="input-tip">Email đăng nhập không thể thay đổi để bảo mật tài khoản.</span>
              </div>

              <div class="form-row-group">
                <label for="p-name">Họ và Tên</label>
                <input id="p-name" type="text" v-model="fullName" required maxlength="255" placeholder="Nhập họ và tên..." />
              </div>

              <div class="form-row-group">
                <label for="p-phone">Số điện thoại</label>
                <input id="p-phone" type="tel" v-model="phone" maxlength="20" placeholder="Nhập số điện thoại..." />
              </div>

              <button class="btn btn-primary btn-save-profile" type="submit" :disabled="isSaving">
                {{ isSaving ? 'Đang lưu...' : 'Cập nhật thông tin' }}
              </button>
            </form>
          </div>

          <!-- TAB: ORDER HISTORY -->
          <div v-else-if="activeTab === 'orders'" class="pane-orders">
            <div class="pane-header">
              <span class="pane-kicker">Billing History</span>
              <h3 class="pane-title">Lịch sử mua hàng</h3>
            </div>

            <p v-if="ordersError" class="profile-state profile-state-error" role="alert">{{ ordersError }}</p>
            <p v-else-if="isOrdersLoading" class="profile-state">Đang tải lịch sử mua hàng...</p>
            <div v-else-if="orders.length === 0" class="profile-state">
              <strong>Chưa có đơn hàng</strong>
              <span>Các đơn đã đặt sẽ được lưu tại đây.</span>
            </div>
            <div v-else class="orders-table-wrapper">
              <table class="orders-table">
                <thead>
                  <tr>
                    <th>Mã đơn hàng</th>
                    <th>Ngày giao dịch</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="ord in orders" :key="ord.orderCode">
                    <td class="ord-code">{{ ord.orderCode }}</td>
                    <td class="ord-date">{{ formatDate(ord.createdAt) }}</td>
                    <td class="ord-total">{{ formatCurrency(ord.totalAmount) }}</td>
                    <td>
                      <span class="order-status-badge">{{ formatOrderStatus(ord.status) }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.profile-page-shell {
  min-height: 100vh;
  background-color: var(--page-bg);
  display: flex;
  flex-direction: column;
}

.profile-container {
  width: min(100% - clamp(2rem, 5vw, 4rem), var(--content-width));
  margin: 3rem auto;
  flex: 1;
}

.profile-state {
  display: flex;
  min-height: 180px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  padding: 2rem;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  color: var(--text-soft);
  text-align: center;
}

.profile-state strong {
  color: var(--text-strong);
  font-size: 1rem;
}

.profile-state a {
  color: var(--text-strong);
  font-weight: 600;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.profile-state-error {
  min-height: auto;
  background: var(--pastel-red-bg, #fdebec);
  color: var(--pastel-red-text, #9f2f2d);
}

.cover-fallback {
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  background: var(--surface-soft);
  color: var(--text-soft);
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 600;
}

.profile-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 3.5rem;
  align-items: start;
}

/* User sidebar */
.profile-sidebar {
  background-color: var(--surface);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  padding: 2rem 1.5rem;
}

.user-intro-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border-bottom: 1px solid var(--line-soft);
  padding-bottom: 1.5rem;
  margin-bottom: 1.5rem;
}

.user-avatar-large {
  width: 64px;
  height: 64px;
  background-color: var(--text-strong);
  color: var(--surface);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.user-name-title {
  font-family: var(--font-body);
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 0.25rem;
}

.user-email-subtitle {
  font-family: var(--font-mono);
  font-size: 0.8rem;
  color: var(--text-soft);
  margin: 0;
  word-break: break-all;
}

.profile-menu-links {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.menu-link-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--text);
  font-family: var(--font-body);
  font-size: 0.92rem;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  transition: all 180ms ease;
}

.menu-link-btn:hover {
  background-color: var(--surface-soft);
  color: var(--text-strong);
}

.menu-link-btn.active {
  background-color: var(--text-strong);
  color: var(--surface);
}

.link-icon {
  display: inline-grid;
  place-items: center;
  flex-shrink: 0;
}

/* Active content workspace */
.profile-content-area {
  position: relative;
  min-width: 0;
}

.pane-header {
  border-bottom: 1px solid var(--line-soft);
  padding-bottom: 1.2rem;
  margin-bottom: 2.2rem;
}

.pane-kicker {
  font-family: var(--font-mono);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--text-soft);
  display: block;
  margin-bottom: 0.4rem;
}

.pane-title {
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 500;
  color: var(--text-strong);
  margin: 0;
}

/* Library Pane grid */
.purchased-books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.purchased-book-card {
  display: flex;
  background-color: var(--surface);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  overflow: hidden;
  padding: 1rem;
  gap: 1.2rem;
  align-items: center;
}

.p-book-cover-frame {
  flex: 0 0 90px;
}

.p-book-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 2/3;
  background-color: var(--surface-soft);
  border-radius: 2px;
  overflow: hidden;
  border: 1px solid var(--line-soft);
}

.p-book-cover::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 8px;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.12) 0%, rgba(0, 0, 0, 0.03) 30%, transparent 100%);
  pointer-events: none;
}

.p-book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.p-book-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.format-badge-row {
  margin-bottom: 0.4rem;
}

.tech-tag {
  display: inline-flex;
  align-items: center;
  padding: 0.15rem 0.45rem;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 0.65rem;
  font-weight: 700;
  border: 1px solid transparent;
}

.tag-vue {
  background-color: var(--pastel-green-bg);
  color: var(--pastel-green-text);
  border-color: rgba(52, 101, 56, 0.12);
}

.tag-blue {
  background-color: var(--pastel-blue-bg);
  color: var(--pastel-blue-text);
  border-color: rgba(31, 108, 159, 0.12);
}

.tag-minio {
  background-color: var(--pastel-yellow-bg);
  color: var(--pastel-yellow-text);
  border-color: rgba(149, 100, 0, 0.12);
}

.p-book-title {
  font-family: var(--font-body);
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 0.15rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.p-book-author {
  font-size: 0.85rem;
  color: var(--text-soft);
  margin: 0 0 1rem;
}

.btn-p-read {
  width: max-content;
  padding: 0.4rem 0.9rem;
  font-size: 0.8rem;
  font-weight: 600;
  height: auto;
  border-radius: var(--radius-sm);
}

/* Profile Form styling */
.profile-form-layout {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 550px;
}

.form-row-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-row-group label {
  font-family: var(--font-body);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-strong);
}

.form-row-group input[type="text"],
.form-row-group input[type="tel"],
.form-row-group textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
  background-color: var(--surface);
  color: var(--text);
  font-family: var(--font-body);
  font-size: 0.95rem;
  outline: none;
  transition: border-color 150ms ease;
}

.form-row-group input:focus,
.form-row-group textarea:focus {
  border-color: var(--text-strong);
}

.form-input-disabled {
  background-color: var(--surface-soft) !important;
  color: var(--text-soft) !important;
  cursor: not-allowed;
}

.input-tip {
  font-size: 0.78rem;
  color: var(--text-soft);
}

.btn-save-profile {
  width: max-content;
  margin-top: 1rem;
  padding: 0.8rem 1.6rem;
  font-size: 0.9rem;
  font-weight: 600;
}

/* Orders table */
.orders-table-wrapper {
  width: 100%;
  overflow-x: auto;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-sm);
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-family: var(--font-body);
  font-size: 0.92rem;
}

.orders-table th {
  background-color: var(--surface-soft);
  color: var(--text-strong);
  font-weight: 600;
  padding: 0.9rem 1.2rem;
  border-bottom: 1px solid var(--line-soft);
}

.orders-table td {
  padding: 1.1rem 1.2rem;
  border-bottom: 1px solid var(--line-soft);
  color: var(--text);
}

.orders-table tr:last-child td {
  border-bottom: none;
}

.ord-code {
  font-family: var(--font-mono);
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-strong);
}

.ord-date {
  font-family: var(--font-mono);
  font-size: 0.85rem;
}

.ord-total {
  font-weight: 600;
  color: var(--text-strong);
}

.order-status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  font-weight: 600;
}

.status-paid {
  background-color: var(--pastel-green-bg);
  color: var(--pastel-green-text);
  border: 1px solid rgba(52, 101, 56, 0.12);
}

/* TOAST ANIMATION */
.success-toast {
  position: fixed;
  top: 90px;
  right: 2rem;
  background-color: var(--text-strong);
  color: var(--surface);
  padding: 0.85rem 1.5rem;
  border-radius: var(--radius-sm);
  font-family: var(--font-body);
  font-size: 0.9rem;
  font-weight: 600;
  z-index: 1000;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 300ms ease, transform 300ms ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* RESPONSIVE LAYOUT */
@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
    gap: 2.5rem;
  }
}
</style>
