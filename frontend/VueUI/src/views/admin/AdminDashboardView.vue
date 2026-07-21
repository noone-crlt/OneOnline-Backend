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
    { label: 'Doanh thu', value: currencyNumber.format(current?.totalRevenue ?? 0), icon: PhCurrencyDollar, color: '#ef4444' },
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
  gap: 2.5rem;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 1rem;
}

.admin-header h2 {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.04em;
  margin: 0 0 0.25rem 0;
}

.admin-header p {
  color: var(--text-muted);
  margin: 0;
  font-size: 1.05rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--bento-surface);
  border: 1px solid var(--bento-border);
  box-shadow: var(--bento-shadow);
  padding: 0.75rem 1.25rem;
  border-radius: 99px;
  font-weight: 600;
  font-family: inherit;
  color: var(--text-main);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 40px -10px rgba(0,0,0,0.08);
}

.action-btn:active {
  transform: scale(0.96);
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Bento Grid */
.bento-grid {
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.card-title-outside {
  margin-bottom: 1rem;
  padding: 0 0.5rem;
}

.card-title-outside h3 {
  font-size: 1.25rem;
  font-weight: 800;
  margin: 0 0 0.25rem 0;
  letter-spacing: -0.02em;
}

.card-title-outside p {
  color: var(--text-muted);
  margin: 0;
  font-size: 0.95rem;
}

.granularity-select {
  padding: 0.5rem 1rem;
  border-radius: 99px;
  border: 1px solid var(--bento-border);
  background: var(--bento-surface);
  color: var(--text-main);
  font-family: inherit;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  outline: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  transition: all 0.2s;
}

.granularity-select:hover {
  border-color: #d4d4d8;
  box-shadow: 0 8px 16px rgba(0,0,0,0.06);
}

.card-surface {
  background: var(--bento-surface);
  border-radius: 2.5rem;
  border: 1px solid var(--bento-border);
  box-shadow: var(--bento-shadow);
  padding: 2rem;
  height: 100%;
}

.kpi-surface {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 3rem 4rem;
  background: linear-gradient(135deg, #18181b, #27272a);
  color: white;
  border: none;
}

.kpi-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 1.15rem;
  font-weight: 600;
  color: #a1a1aa; /* Zinc 400 */
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 0.5rem;
}

.kpi-value {
  font-size: clamp(3rem, 5vw, 5.5rem);
  font-weight: 900;
  letter-spacing: -0.04em;
  line-height: 1;
  color: white;
  text-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

.kpi-icon-bg {
  position: absolute;
  right: -10%;
  bottom: -20%;
  color: rgba(255, 255, 255, 0.05);
  transform: rotate(-15deg);
  pointer-events: none;
  z-index: 1;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1.5rem;
  width: 100%;
}

.chart-card {
  width: 100%;
}

.metrics-bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1.5rem;
  align-items: start;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* Asymmetric Span */
.large-metric {
  grid-column: span 6; /* 2 columns across 12-col grid */
}

.small-metric {
  grid-column: span 4; /* 3 columns across 12-col grid, will wrap nicely */
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
  padding: 2rem;
  border-radius: 2.5rem;
  height: auto;
  min-height: 140px;
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.metric-surface:hover {
  transform: translateY(-4px);
  box-shadow: 0 30px 50px -15px rgba(0,0,0,0.08);
}

.metric-icon-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 1.25rem;
  display: grid;
  place-items: center;
}

.large-metric .metric-icon {
  width: 64px;
  height: 64px;
  border-radius: 1.5rem;
}

.metric-value {
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.04em;
  color: var(--text-main);
}

.large-value {
  font-size: 3rem;
}

.metric-label-outside {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-muted);
  text-align: center;
  padding: 0 1rem;
}

/* Perpetual Micro-animation */
.pulse-animation .metric-icon {
  animation: pulse-soft 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse-soft {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

/* Skeleton Loading */
.skeleton-card {
  height: 160px;
  background: var(--bento-surface);
  border-radius: 2.5rem;
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
  border-radius: 2.5rem;
  border: 1px dashed rgba(239, 68, 68, 0.3);
  padding: 4rem;
  text-align: center;
}

.error-state h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.error-state p {
  color: var(--text-muted);
  margin: 0;
}
</style>
