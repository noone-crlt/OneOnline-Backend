<script setup>
import { computed, onMounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import gsap from 'gsap'
import { PhUsers, PhBooks, PhBookOpenText, PhBookmarkSimple, PhPenNib, PhCurrencyDollar, PhChatCircleDots, PhWarningCircle, PhArrowsClockwise } from '@phosphor-icons/vue'

import DashboardBarChart from '../../components/admin/DashboardBarChart.vue'
import { getDashboardSummary, getMonthlyStats, getTopSellingBooks } from '../../services/adminDashboard'
import { ApiError, getFileUrl } from '../../services/api'
import { logout } from '../../stores/auth'

const router = useRouter()

const summary = ref(null)
const monthlyStats = ref([])
const topBooks = ref([])
const loading = ref(true)
const errorMessage = ref('')
const monthlyStatsLoading = ref(true)
const monthlyStatsError = ref('')
const topBooksLoading = ref(true)
const topBooksError = ref('')
const granularity = ref('monthly')

const compactNumber = new Intl.NumberFormat('vi-VN')
const currencyNumber = {
  format: (value) => new Intl.NumberFormat('vi-VN').format(value) + ' VNĐ'
}

function resolveCover(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return getFileUrl(url)
}

const summaryCards = computed(() => {
  const current = summary.value
  return [
    { label: 'Tổng người dùng', value: compactNumber.format(current?.totalUsers ?? 0), icon: PhUsers, color: '#10b981' },
    { label: 'Tổng tác phẩm', value: compactNumber.format(current?.totalBooks ?? 0), icon: PhBooks, color: '#6366f1' },
    { label: 'Chương audio', value: compactNumber.format(current?.totalChapters ?? 0), icon: PhBookOpenText, color: '#f59e0b' },
    { label: 'Danh mục sách', value: compactNumber.format(current?.totalCategories ?? 0), icon: PhBookmarkSimple, color: '#ec4899' },
    { label: 'Tổng tác giả', value: compactNumber.format(current?.totalAuthors ?? 0), icon: PhPenNib, color: '#0ea5e9' },
    { label: 'Tổng bình luận', value: compactNumber.format(current?.totalComments ?? 0), icon: PhChatCircleDots, color: '#8b5cf6' },
  ]
})

async function handleAuthFailure() {
  logout()
  await router.replace({ name: 'login', query: { redirect: '/admin' } })
}

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await getDashboardSummary()
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
      await handleAuthFailure()
      return
    }
    errorMessage.value = 'Không thể tải dữ liệu tổng quan.'
  } finally {
    loading.value = false
  }
}

async function loadMonthlyStatsData() {
  monthlyStatsLoading.value = true
  monthlyStatsError.value = ''
  try {
    monthlyStats.value = await getMonthlyStats(granularity.value)
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
      await handleAuthFailure()
      return
    }
    monthlyStats.value = []
    monthlyStatsError.value = 'Không thể tải thống kê theo tháng.'
  } finally {
    monthlyStatsLoading.value = false
  }
}

async function loadTopBooksData() {
  topBooksLoading.value = true
  topBooksError.value = ''
  try {
    topBooks.value = await getTopSellingBooks(5)
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
      await handleAuthFailure()
      return
    }
    topBooks.value = []
    topBooksError.value = 'Không thể tải danh sách sách bán chạy.'
  } finally {
    topBooksLoading.value = false
  }
}

async function loadDashboard() {
  await Promise.all([loadSummary(), loadMonthlyStatsData(), loadTopBooksData()])
  
  await nextTick()
  // GSAP Entrance Stagger
  gsap.fromTo('.bento-item', 
    { y: 40, opacity: 0, scale: 0.98 }, 
    { y: 0, opacity: 1, scale: 1, stagger: 0.05, ease: 'back.out(1.5)', duration: 0.8, clearProps: 'all' }
  )
}

onMounted(loadDashboard)
</script>

<template>
  <div class="dashboard-content">
    <header class="admin-header bento-item">
      <div>
        <h2>Bảng điều khiển</h2>
        <p>Theo dõi hoạt động hệ thống và tăng trưởng nội dung.</p>
      </div>
      <button class="action-btn" @click="loadDashboard" :disabled="loading">
        <PhArrowsClockwise :size="20" :class="{ 'spin': loading }" />
        Làm mới
      </button>
    </header>

    <!-- Loading State -->
    <div v-if="loading" class="bento-grid">
       <div v-for="i in 6" :key="i" class="bento-card skeleton-card bento-item"></div>
    </div>
    
    <div v-else-if="summary" class="bento-grid">
      
      <!-- Hero KPI Card -->
      <div class="bento-card hero-kpi-card bento-item">
        <div class="card-surface kpi-surface">
          <div class="kpi-content">
            <span class="kpi-label">Tổng doanh thu hệ thống</span>
            <strong class="kpi-value">{{ currencyNumber.format(summary?.totalRevenue ?? 0) }}</strong>
          </div>
          <div class="kpi-icon-bg">
            <PhCurrencyDollar :size="120" weight="duotone" />
          </div>
        </div>
      </div>

      <!-- Charts Container (2 columns side by side) -->
      <div class="charts-grid">
        <!-- Growth Chart Card -->
        <div class="bento-card chart-card bento-item">
          <div class="card-title-outside" style="display: flex; justify-content: space-between; align-items: flex-end;">
            <div>
              <h3>Tăng trưởng hệ thống</h3>
              <p>Người dùng mới, sách mới và bình luận.</p>
            </div>
            <select v-model="granularity" @change="loadMonthlyStatsData" class="granularity-select">
              <option value="monthly">6 tháng gần đây</option>
              <option value="daily">7 ngày gần đây</option>
            </select>
          </div>
          <div class="card-surface">
            <DashboardBarChart
              :stats="monthlyStats"
              :loading="monthlyStatsLoading"
              :error="monthlyStatsError"
              type="growth"
              @retry="loadMonthlyStatsData"
            />
          </div>
        </div>

        <!-- Revenue Chart Card -->
        <div class="bento-card chart-card bento-item">
          <div class="card-title-outside" style="display: flex; justify-content: space-between; align-items: flex-end;">
            <div>
              <h3>Doanh thu</h3>
              <p>Biến động doanh thu theo thời gian.</p>
            </div>
          </div>
          <div class="card-surface">
            <DashboardBarChart
              :stats="monthlyStats"
              :loading="monthlyStatsLoading"
              :error="monthlyStatsError"
              type="revenue"
              @retry="loadMonthlyStatsData"
            />
          </div>
        </div>
      </div>

      <!-- Compact & Clear Metric Cards Grid -->
      <div class="compact-metrics-grid">
        <div 
          v-for="card in summaryCards" 
          :key="card.label" 
          class="compact-metric-card bento-item"
        >
          <div class="compact-icon-box" :style="{ color: card.color, backgroundColor: `${card.color}15` }">
            <component :is="card.icon" :size="20" weight="duotone" />
          </div>
          <div class="compact-metric-details">
            <span class="compact-metric-label">{{ card.label }}</span>
            <span class="compact-metric-value">{{ card.value }}</span>
          </div>
        </div>
      </div>

      <!-- Top Selling Books Data Table -->
      <div class="bento-card table-card bento-item">
        <div class="card-title-outside table-header-flex">
          <div>
            <h3>Sách bán chạy nhất</h3>
            <p>Danh sách các tác phẩm có số lượng bản bán ra và doanh thu cao nhất.</p>
          </div>
          <RouterLink to="/admin/books" class="view-all-link">
            Quản lý kho sách &rarr;
          </RouterLink>
        </div>

        <div class="card-surface table-surface">
          <div v-if="topBooksLoading" class="table-loading-container">
            <div class="table-spinner"></div>
            <p>Đang tải dữ liệu sách bán chạy...</p>
          </div>

          <div v-else-if="topBooksError" class="table-error-state">
            <span>{{ topBooksError }}</span>
            <button class="action-btn small-btn" @click="loadTopBooksData">Thử lại</button>
          </div>

          <div v-else-if="topBooks.length === 0" class="table-empty-state">
            Chưa có dữ liệu giao dịch bán hàng.
          </div>

          <div v-else class="responsive-table-wrapper">
            <table class="dashboard-table">
              <thead>
                <tr>
                  <th class="col-rank">#</th>
                  <th class="col-book">Tác phẩm</th>
                  <th class="col-author">Tác giả</th>
                  <th class="col-category">Thể loại</th>
                  <th class="col-sold text-center">Đã bán</th>
                  <th class="col-revenue text-right">Tổng doanh thu</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(book, index) in topBooks" :key="book.bookId">
                  <td class="col-rank">
                    <span :class="['rank-badge', `rank-${index + 1}`]">#{{ index + 1 }}</span>
                  </td>
                  <td class="col-book">
                    <div class="book-cell">
                      <div class="book-thumb">
                        <img v-if="resolveCover(book.coverUrl)" :src="resolveCover(book.coverUrl)" :alt="book.title" />
                        <div v-else class="book-thumb-fallback">{{ book.title?.charAt(0) || 'S' }}</div>
                      </div>
                      <div class="book-info">
                        <span class="book-title-text" :title="book.title">{{ book.title }}</span>
                        <span class="book-id-tag">ID: #{{ book.bookId }}</span>
                      </div>
                    </div>
                  </td>
                  <td class="col-author">
                    <span class="text-truncate" :title="book.authorName">{{ book.authorName }}</span>
                  </td>
                  <td class="col-category">
                    <span class="category-chip">{{ book.categoryName }}</span>
                  </td>
                  <td class="col-sold text-center">
                    <span class="sold-badge">{{ compactNumber.format(book.totalSold || 0) }} bản</span>
                  </td>
                  <td class="col-revenue text-right">
                    <strong class="revenue-amount">{{ currencyNumber.format(book.totalRevenue || 0) }}</strong>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
    
    <div v-else class="error-state bento-item">
      <PhWarningCircle :size="48" color="#ef4444" weight="duotone" />
      <h3>Lỗi kết nối</h3>
      <p>{{ errorMessage }}</p>
      <button class="action-btn" @click="loadDashboard">Thử lại</button>
    </div>
  </div>
</template>

<style scoped>
.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0.25rem;
}

.admin-header h2 {
  font-size: 1.85rem;
  font-weight: 800;
  letter-spacing: -0.04em;
  margin: 0 0 0.25rem 0;
  color: var(--text-main);
}

.admin-header p {
  color: var(--text-muted);
  margin: 0;
  font-size: 0.95rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--bento-surface);
  border: 1px solid var(--bento-border);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  padding: 0.6rem 1.1rem;
  border-radius: 99px;
  font-weight: 600;
  font-family: inherit;
  color: var(--text-main);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  font-size: 0.88rem;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px -6px rgba(0, 0, 0, 0.08);
}

.action-btn:active {
  transform: scale(0.97);
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Bento Grid Layout */
.bento-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.card-title-outside {
  margin-bottom: 0.75rem;
  padding: 0 0.25rem;
}

.card-title-outside h3 {
  font-size: 1.15rem;
  font-weight: 800;
  margin: 0 0 0.15rem 0;
  letter-spacing: -0.02em;
  color: var(--text-main);
}

.card-title-outside p {
  color: var(--text-muted);
  margin: 0;
  font-size: 0.88rem;
}

.granularity-select {
  padding: 0.4rem 0.85rem;
  border-radius: 99px;
  border: 1px solid var(--bento-border);
  background: var(--bento-surface);
  color: var(--text-main);
  font-family: inherit;
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
  outline: none;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  transition: all 0.2s;
}

.granularity-select:hover {
  border-color: #d4d4d8;
  box-shadow: 0 4px 8px rgba(0,0,0,0.04);
}

.card-surface {
  background: #ffffff;
  border-radius: 1.25rem;
  border: 1px solid rgba(24, 24, 27, 0.06);
  box-shadow: 0 8px 24px -8px rgba(0,0,0,0.03);
  padding: 1.25rem;
  height: 100%;
}

/* Premium KPI Surface (Dark theme) - Compact */
.kpi-surface {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 1.75rem 2rem;
  background: #09090b;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 1.25rem;
  height: 100%;
  box-shadow: 0 12px 24px -10px rgba(0,0,0,0.3);
}

.kpi-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 0.85rem;
  font-weight: 700;
  color: #71717a;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
}

.kpi-value {
  font-size: clamp(2rem, 3.5vw, 3rem);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.1;
  color: white;
  font-family: Satoshi, monospace;
}

.kpi-icon-bg {
  position: absolute;
  right: 5%;
  bottom: -25%;
  color: rgba(255, 255, 255, 0.03);
  transform: rotate(-15deg);
  pointer-events: none;
  z-index: 1;
}

/* Charts Grid */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1.25rem;
  width: 100%;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  width: 100%;
}

/* Compact & Clear Metric Cards Grid */
.compact-metrics-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 1rem;
  width: 100%;
}

@media (max-width: 1200px) {
  .compact-metrics-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .compact-metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.compact-metric-card {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.85rem 1.1rem;
  background: #ffffff;
  border-radius: 0.95rem;
  border: 1px solid rgba(24, 24, 27, 0.08);
  box-shadow: 0 4px 14px -4px rgba(0, 0, 0, 0.03);
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.25s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.25s ease;
}

.compact-metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px -6px rgba(0, 0, 0, 0.07);
  border-color: rgba(99, 102, 241, 0.25);
}

.compact-icon-box {
  width: 38px;
  height: 38px;
  border-radius: 0.65rem;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.compact-metric-card:hover .compact-icon-box {
  transform: scale(1.08);
}

.compact-metric-details {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  overflow: hidden;
}

.compact-metric-label {
  font-size: 0.78rem;
  font-weight: 700;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.compact-metric-value {
  font-size: 1.45rem;
  font-weight: 800;
  line-height: 1.1;
  color: #0f172a;
  font-family: 'JetBrains Mono', 'Geist Mono', Satoshi, monospace;
  letter-spacing: -0.02em;
}

/* Skeleton Loading */
.skeleton-card {
  height: 120px;
  background: var(--bento-surface);
  border-radius: 1.25rem;
  border: 1px solid var(--bento-border);
  position: relative;
  overflow: hidden;
}

.skeleton-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  background: var(--bento-surface);
  border-radius: 1.25rem;
  border: 1px dashed rgba(239, 68, 68, 0.3);
  padding: 3rem;
  text-align: center;
}

.error-state h3 {
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0;
}

.error-state p {
  color: var(--text-muted);
  margin: 0;
}

/* Table Bento Card & Custom Table Styles */
.table-card {
  grid-column: 1 / -1;
}

.table-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.view-all-link {
  font-size: 0.85rem;
  font-weight: 600;
  color: #6366f1;
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.view-all-link:hover {
  text-decoration: underline;
  opacity: 0.85;
}

.table-surface {
  padding: 0;
  overflow: hidden;
}

.responsive-table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.dashboard-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.9rem;
}

.dashboard-table th {
  padding: 0.9rem 1.25rem;
  background-color: var(--surface-soft, #f8fafc);
  color: var(--text-muted, #64748b);
  font-size: 0.78rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--bento-border, #e2e8f0);
}

.dashboard-table td {
  padding: 0.9rem 1.25rem;
  border-bottom: 1px solid var(--bento-border, #f1f5f9);
  vertical-align: middle;
  color: var(--text-strong, #0f172a);
}

.dashboard-table tbody tr {
  transition: background-color 0.15s ease;
}

.dashboard-table tbody tr:hover {
  background-color: rgba(99, 102, 241, 0.03);
}

.dashboard-table tbody tr:last-child td {
  border-bottom: none;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 0.8rem;
  font-weight: 800;
  background-color: #f1f5f9;
  color: #64748b;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #fef08a 0%, #f59e0b 100%);
  color: #78350f;
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #e2e8f0 0%, #94a3b8 100%);
  color: #1e293b;
  box-shadow: 0 2px 6px rgba(148, 163, 184, 0.3);
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #ffedd5 0%, #d97706 100%);
  color: #7c2d12;
  box-shadow: 0 2px 6px rgba(217, 119, 6, 0.25);
}

.book-cell {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.book-thumb {
  width: 42px;
  height: 58px;
  border-radius: 6px;
  overflow: hidden;
  background-color: #f1f5f9;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.book-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-thumb-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #64748b;
  font-size: 1.1rem;
}

.book-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  max-width: 260px;
}

.book-title-text {
  font-weight: 700;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-id-tag {
  font-size: 0.72rem;
  color: #94a3b8;
  font-family: var(--font-mono, monospace);
}

.category-chip {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  border-radius: 99px;
  background-color: #f1f5f9;
  color: #475569;
  font-size: 0.78rem;
  font-weight: 600;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sold-badge {
  display: inline-block;
  padding: 0.25rem 0.7rem;
  border-radius: 6px;
  background-color: #ecfdf5;
  color: #059669;
  font-weight: 700;
  font-size: 0.85rem;
}

.revenue-amount {
  font-size: 0.95rem;
  font-weight: 800;
  color: #10b981;
}

.table-loading-container,
.table-empty-state,
.table-error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1.5rem;
  color: #64748b;
  gap: 0.75rem;
}

.table-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(99, 102, 241, 0.15);
  border-radius: 50%;
  border-top-color: #6366f1;
  animation: spin 1s linear infinite;
}
</style>
