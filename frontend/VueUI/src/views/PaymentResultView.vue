<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getPaymentStatus } from '../services/api'

const route = useRoute()
const payment = ref(null)
const status = ref(String(route.query.status || 'pending').toLowerCase())
const errorMessage = ref('')
const remainingSeconds = ref(0)
let pollTimer
let countdownTimer

const success = computed(() => status.value === 'success')
const pending = computed(() => status.value === 'pending')
const isSePay = computed(() => payment.value?.paymentMethod?.toUpperCase() === 'SEPAY')
const expired = computed(() => pending.value && remainingSeconds.value <= 0 && Boolean(payment.value?.expiresAt))
const title = computed(() => success.value ? 'Thanh toán thành công'
  : expired.value ? 'Mã thanh toán đã hết hạn'
    : pending.value ? 'Quét mã để thanh toán' : 'Thanh toán chưa hoàn tất')
const description = computed(() => success.value
  ? 'Giao dịch đã được xác nhận. Sách điện tử đã mua sẽ xuất hiện trong thư viện cá nhân.'
  : expired.value ? 'Thời gian chờ thanh toán đã kết thúc. Vui lòng liên hệ hỗ trợ nếu bạn đã chuyển khoản.'
    : pending.value ? 'Dùng ứng dụng ngân hàng quét mã QR và giữ nguyên nội dung chuyển khoản.'
      : 'Giao dịch bị hủy, thất bại hoặc dữ liệu trả về không hợp lệ.')
const countdown = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60).toString().padStart(2, '0')
  const seconds = (remainingSeconds.value % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
})

function money(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0)
}

async function loadStatus() {
  const orderCode = String(route.query.orderCode || '')
  if (!orderCode) return
  try {
    payment.value = await getPaymentStatus(orderCode)
    status.value = String(payment.value.status || 'pending').toLowerCase()
    if (payment.value.expiresAt) {
      remainingSeconds.value = Math.max(0, Math.floor((new Date(payment.value.expiresAt).getTime() - Date.now()) / 1000))
    }
    if (!pending.value) stopPolling()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể kiểm tra trạng thái thanh toán.'
  }
}

async function copy(value) {
  if (!value) return
  await navigator.clipboard.writeText(String(value))
}

function stopPolling() {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = undefined
}

onMounted(async () => {
  await loadStatus()
  if (pending.value) pollTimer = setInterval(loadStatus, 3000)
  countdownTimer = setInterval(() => {
    if (remainingSeconds.value > 0) remainingSeconds.value -= 1
  }, 1000)
})

onBeforeUnmount(() => {
  stopPolling()
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<template>
  <main class="result-page">
    <section class="result-panel">
      <span :class="['status-mark', { success, pending }]">{{ success ? '✓' : pending ? '…' : '×' }}</span>
      <p class="order-code">Đơn hàng {{ route.query.orderCode || 'không xác định' }}</p>
      <h1>{{ title }}</h1>
      <p class="description">{{ description }}</p>
      <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>

      <div v-if="isSePay && pending && !expired" class="payment-box">
        <img :src="payment.paymentUrl" alt="Mã QR chuyển khoản SePay" class="payment-qr" />
        <p class="countdown">Thời gian còn lại <strong>{{ countdown }}</strong></p>
        <dl class="payment-details">
          <div><dt>Ngân hàng</dt><dd>{{ payment.bankCode }}</dd></div>
          <div><dt>Chủ tài khoản</dt><dd>{{ payment.accountName }}</dd></div>
          <div><dt>Số tài khoản</dt><dd>{{ payment.accountNumber }} <button @click="copy(payment.accountNumber)">Sao chép</button></dd></div>
          <div><dt>Số tiền</dt><dd>{{ money(payment.amount) }}</dd></div>
          <div><dt>Nội dung</dt><dd>{{ payment.transferContent }} <button @click="copy(payment.transferContent)">Sao chép</button></dd></div>
        </dl>
      </div>

      <div class="result-actions">
        <RouterLink to="/library" class="primary-action">Về thư viện</RouterLink>
        <RouterLink v-if="!success" to="/cart" class="secondary-action">Kiểm tra giỏ hàng</RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.result-page{min-height:100vh;display:grid;place-items:center;padding:2rem;background:#fbfbfa;color:#2f3437;font-family:var(--font-body,'Helvetica Neue',sans-serif)}.result-panel{width:min(620px,100%);padding:clamp(2rem,7vw,4.5rem);text-align:center;background:#fff;border:1px solid #eaeaea;border-radius:8px}.status-mark{display:grid;place-items:center;width:54px;height:54px;margin:0 auto 1.5rem;border-radius:50%;background:#fdebec;color:#9f2f2d;font-size:1.6rem}.status-mark.success{background:#edf3ec;color:#346538}.status-mark.pending{background:#fbf3db;color:#956400}.order-code{font:500 .78rem var(--font-mono,monospace);color:#787774;letter-spacing:.04em}.result-panel h1{font-family:var(--font-display,'Newsreader',serif);font-size:clamp(2rem,5vw,3.5rem);letter-spacing:-.03em;line-height:1.08;margin:.7rem 0 1rem}.description{max-width:52ch;margin:0 auto 2rem;line-height:1.65;color:#787774}.error-message{color:#9f2f2d}.payment-box{margin:0 auto 2rem;padding:1.25rem;border:1px solid #eaeaea;border-radius:8px;background:#fbfbfa}.payment-qr{width:min(260px,100%);aspect-ratio:1;object-fit:contain;background:#fff}.countdown{color:#787774}.payment-details{margin:1rem 0 0;text-align:left}.payment-details div{display:flex;justify-content:space-between;gap:1rem;padding:.7rem 0;border-top:1px solid #eaeaea}.payment-details dt,.payment-details dd{margin:0}.payment-details dd{text-align:right;font-weight:650}.payment-details button{margin-left:.4rem;border:0;background:none;color:#346538;font-weight:700;cursor:pointer}.result-actions{display:flex;justify-content:center;flex-wrap:wrap;gap:.75rem}.primary-action,.secondary-action{padding:.8rem 1.1rem;border-radius:5px;font-weight:700}.primary-action{background:#111;color:#fff}.secondary-action{border:1px solid #eaeaea;color:#2f3437}.primary-action:active,.secondary-action:active{transform:scale(.98)}
@media(max-width:520px){.result-page{padding:1rem}.result-panel{padding:2rem 1rem}.payment-details div{display:block}.payment-details dd{margin-top:.25rem;text-align:left}}
</style>
