<script setup>
import { computed, onMounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import gsap from 'gsap'
import { PhUsers, PhBooks, PhBookOpenText, PhBookmarkSimple, PhPenNib, PhCurrencyDollar, PhChatCircleDots, PhWarningCircle, PhArrowsClockwise } from '@phosphor-icons/vue'

import DashboardBarChart from '../../components/admin/DashboardBarChart.vue'
import { getDashboardSummary, getMonthlyStats } from '../../services/adminDashboard'
import { ApiError } from '../../services/api'
import { logout } from '../../stores/auth'

const router = useRouter()

const summary = ref(null)
const monthlyStats = ref([])
const loading = ref(true)
const errorMessage = ref('')
const monthlyStatsLoading = ref(true)
const monthlyStatsError = ref('')
const granularity = ref('monthly')

const compactNumber = new Intl.NumberFormat('vi-VN')
const currencyNumber = {
  format: (value) => new Intl.NumberFormat('vi-VN').format(value) + ' VNĐ'
}

const summaryCards = computed(() => {
  const current = summary.value
  return [
    { label: 'Người dùng', value: compactNumber.format(current?.totalUsers ?? 0), icon: PhUsers, color: '#10b981' },
    { label: 'Tác phẩm', value: compactNumber.format(current?.totalBooks ?? 0), icon: PhBooks, color: '#6366f1' },
    { label: 'Chương audio', value: compactNumber.format(current?.totalChapters ?? 0), icon: PhBookOpenText, color: '#f59e0b' },
    { label: 'Danh mục', value: compactNumber.format(current?.totalCategories ?? 0), icon: PhBookmarkSimple, color: '#ec4899' },
    { label: 'Tác giả', value: compactNumber.format(current?.totalAuthors ?? 0), icon: PhPenNib, color: '#0ea5e9' },
    { label: 'Bình luận', value: compactNumber.format(current?.totalComments ?? 0), icon: PhChatCircleDots, color: '#8b5cf6' },
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

async function loadDashboard() {
  await Promise.all([loadSummary(), loadMonthlyStatsData()])
  
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

      <!-- Metric Cards - Bento 2.0 Asymmetric -->
      <div class="metrics-bento-grid">
        <div v-for="(card, index) in summaryCards" :key="card.label" 
             :class="['bento-card', 'metric-item', 'bento-item', index < 2 ? 'large-metric' : 'small-metric']">
          <div class="card-surface metric-surface">
            <!-- Perpetual Micro-interaction wrapper for icons -->
            <div class="metric-icon-wrapper" :class="{ 'pulse-animation': card.label === 'Doanh thu' }">
              <div class="metric-icon" :style="{ color: card.color, backgroundColor: `${card.color}15` }">
                <component :is="card.icon" :size="index < 2 ? 36 : 24" weight="duotone" />
              </div>
            </div>
            <div class="metric-value" :class="{ 'large-value': index < 2 }">
              {{ card.value }}
            </div>
          </div>
          <!-- Title outside and below -->
          <div class="metric-label-outside">
            {{ card.label }}
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

/* Metrics Bento Grid */
.metrics-bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1.25rem;
  align-items: start;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.large-metric {
  grid-column: span 6;
}

.small-metric {
  grid-column: span 3;
}

@media (max-width: 1024px) {
  .large-metric { grid-column: span 6; }
  .small-metric { grid-column: span 6; }
}

@media (max-width: 768px) {
  .large-metric { grid-column: span 12; }
  .small-metric { grid-column: span 12; }
}

.metric-surface {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 1.25rem;
  background: #ffffff;
  border-radius: 1.25rem;
  border: 1px solid rgba(24, 24, 27, 0.06);
  box-shadow: 0 8px 24px -8px rgba(0,0,0,0.03);
  height: auto;
  min-height: 110px;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.metric-surface:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px -12px rgba(0, 0, 0, 0.08);
  border-color: rgba(24, 24, 27, 0.1);
}

.metric-icon-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
}

.metric-icon {
  width: 44px;
  height: 44px;
  border-radius: 0.75rem;
  display: grid;
  place-items: center;
  transition: transform 0.3s;
}

.metric-surface:hover .metric-icon {
  transform: scale(1.05);
}

.large-metric .metric-icon {
  width: 52px;
  height: 52px;
  border-radius: 1rem;
}

.metric-value {
  font-size: 1.85rem;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.03em;
  color: var(--text-main);
  font-family: Satoshi, monospace;
}

.large-value {
  font-size: 2.25rem;
}

.metric-label-outside {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0 0.25rem;
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
</style>
