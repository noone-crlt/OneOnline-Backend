<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const status = computed(() => String(route.query.status || 'invalid').toLowerCase())
const success = computed(() => status.value === 'success')
const pending = computed(() => status.value === 'pending')
const title = computed(() => success.value ? 'Thanh toán thành công' : pending.value ? 'Đơn hàng đang chờ xử lý' : 'Thanh toán chưa hoàn tất')
const description = computed(() => success.value
  ? 'Giao dịch đã được xác nhận. Sách điện tử đã mua sẽ xuất hiện trong thư viện cá nhân.'
  : pending.value ? 'Đơn hàng đã được ghi nhận và sẽ được xử lý theo phương thức bạn chọn.'
    : 'Giao dịch bị hủy, thất bại hoặc dữ liệu trả về không hợp lệ. Bạn có thể thử thanh toán lại.')
</script>

<template>
  <main class="result-page">
    <section class="result-panel">
      <span :class="['status-mark', { success, pending }]">{{ success ? '✓' : pending ? '…' : '×' }}</span>
      <p class="order-code">Đơn hàng {{ route.query.orderCode || 'không xác định' }}</p>
      <h1>{{ title }}</h1>
      <p class="description">{{ description }}</p>
      <div class="result-actions">
        <RouterLink to="/library" class="primary-action">Về thư viện</RouterLink>
        <RouterLink v-if="!success" to="/cart" class="secondary-action">Kiểm tra giỏ hàng</RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.result-page{min-height:100vh;display:grid;place-items:center;padding:2rem;background:#fbfbfa;color:#2f3437;font-family:var(--font-body,'Helvetica Neue',sans-serif)}.result-panel{width:min(620px,100%);padding:clamp(2rem,7vw,4.5rem);text-align:center;background:#fff;border:1px solid #eaeaea;border-radius:8px}.status-mark{display:grid;place-items:center;width:54px;height:54px;margin:0 auto 1.5rem;border-radius:50%;background:#fdebec;color:#9f2f2d;font-size:1.6rem}.status-mark.success{background:#edf3ec;color:#346538}.status-mark.pending{background:#fbf3db;color:#956400}.order-code{font:500 .78rem var(--font-mono,monospace);color:#787774;letter-spacing:.04em}.result-panel h1{font-family:var(--font-display,'Newsreader',serif);font-size:clamp(2rem,5vw,3.5rem);letter-spacing:-.03em;line-height:1.08;margin:.7rem 0 1rem}.description{max-width:52ch;margin:0 auto 2rem;line-height:1.65;color:#787774}.result-actions{display:flex;justify-content:center;flex-wrap:wrap;gap:.75rem}.primary-action,.secondary-action{padding:.8rem 1.1rem;border-radius:5px;font-weight:700}.primary-action{background:#111;color:#fff}.secondary-action{border:1px solid #eaeaea;color:#2f3437}.primary-action:active,.secondary-action:active{transform:scale(.98)}
</style>
