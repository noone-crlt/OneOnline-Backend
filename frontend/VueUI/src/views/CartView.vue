<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder, getCart, getCheckoutOptions, removeCartItem, updateCartItem, getFileUrl } from '../services/api'
import AppFooter from '@/components/layout/AppFooter.vue'
import { fetchCartItemCount, updateCartItemCount } from '../stores/cart'

const router = useRouter()
const cart = ref({ items: [], cartTotalAmount: 0 })
const options = ref({ addresses: [], paymentMethods: [] })
const addressId = ref(null)
const paymentMethodId = ref(null)
const couponCode = ref('')
const loading = ref(true)
const submitting = ref(false)
const errorMessage = ref('')

const shippingFee = computed(() => cart.value.items.some(item => item.format === 'PHYSICAL') ? 30000 : 0)
const requiresShippingAddress = computed(() => cart.value.items.some(item => item.format === 'PHYSICAL'))
const estimatedTotal = computed(() => Number(cart.value.cartTotalAmount || 0) + shippingFee.value)

function money(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0)
}

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [cartData, checkoutData] = await Promise.all([getCart(), getCheckoutOptions()])
    cart.value = cartData
    updateCartItemCount(cart.value?.items?.reduce((sum, item) => sum + (item.quantity || 1), 0) || 0)
    options.value = checkoutData
    addressId.value = checkoutData.addresses.find(item => item.isDefault)?.id ?? checkoutData.addresses[0]?.id ?? null
    paymentMethodId.value = checkoutData.paymentMethods.find(item => item.name.toLowerCase() === 'sepay')?.id
      ?? checkoutData.paymentMethods[0]?.id ?? null
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể tải giỏ hàng.'
  } finally {
    loading.value = false
  }
}

async function changeQuantity(item, newQuantity) {
  const quantity = Math.max(1, Number(newQuantity) || 1)
  try {
    cart.value = await updateCartItem(item.id, quantity)
    updateCartItemCount(cart.value?.items?.reduce((sum, item) => sum + (item.quantity || 1), 0) || 0)
  }
  catch (error) { errorMessage.value = error.message }
}

async function removeItem(itemId) {
  try { 
    cart.value = await removeCartItem(itemId)
    updateCartItemCount(cart.value?.items?.reduce((sum, item) => sum + (item.quantity || 1), 0) || 0)
  }
  catch (error) { errorMessage.value = error.message }
}

async function checkout() {
  if (!paymentMethodId.value) {
    errorMessage.value = 'Vui lòng chọn phương thức thanh toán.'
    return
  }
  if (requiresShippingAddress.value && !addressId.value) {
    errorMessage.value = 'Vui lòng chọn địa chỉ nhận sách.'
    return
  }
  submitting.value = true
  errorMessage.value = ''
  try {
    const result = await createOrder({
      addressId: requiresShippingAddress.value ? addressId.value : null,
      paymentMethod: paymentMethodId.value,
      couponCode: couponCode.value.trim() || null,
    })
    router.push({ name: 'payment-result', query: { status: result.paymentStatus?.toLowerCase() || 'pending', orderCode: result.orderCode } })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Không thể tạo đơn hàng.'
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="checkout-page">
    <header class="checkout-header">
      <RouterLink to="/" class="checkout-brand" aria-label="Về trang chủ One Online">
        <img src="@/assets/logo/vectorized.svg" alt="One Online Logo" class="brand-logo-img" />
      </RouterLink>
      <RouterLink to="/library" class="back-link"><span aria-hidden="true">←</span> Tiếp tục chọn sách</RouterLink>
    </header>

    <main id="main-content" class="checkout-main">
      <div class="page-heading">
        <p>Đơn hàng của bạn</p>
        <h1>Giỏ hàng và thanh toán</h1>
      </div>

      <p v-if="errorMessage" class="message message-error" role="alert">{{ errorMessage }}</p>
      <section v-if="loading" class="loading-state" aria-live="polite" aria-label="Đang tải giỏ hàng">
        <div class="loading-copy">
          <span class="skeleton skeleton-label"></span>
          <span class="skeleton skeleton-title"></span>
          <span class="skeleton skeleton-text"></span>
        </div>
        <span class="skeleton skeleton-panel"></span>
        <span class="sr-only">Đang tải giỏ hàng...</span>
      </section>
      <section v-else-if="cart.items.length === 0" class="empty-state">
        <div class="empty-visual" aria-hidden="true">
          <span class="book book-back"></span>
          <span class="book book-middle"></span>
          <span class="book book-front">
            <span class="book-title">ONE<br />ONLINE</span>
            <span class="book-mark-line"></span>
          </span>
        </div>
        <div class="empty-copy">
          <span class="empty-index">Giỏ sách · 00</span>
          <h2>Giỏ hàng đang trống</h2>
          <p>Chọn một phiên bản sách bạn yêu thích để bắt đầu đơn hàng.</p>
          <RouterLink to="/library" class="primary-link">Khám phá thư viện <span aria-hidden="true">↗</span></RouterLink>
        </div>
      </section>

      <div v-else class="checkout-grid">
        <section class="order-column" aria-label="Sản phẩm trong giỏ hàng">
          <div class="order-column-heading">
            <h2>Sách đã chọn</h2>
            <span>{{ cart.items.length }} sản phẩm</span>
          </div>
          <article v-for="item in cart.items" :key="item.id" class="cart-row">
            <div class="cart-item-image">
              <img v-if="item.coverImageUrl" :src="getFileUrl(item.coverImageUrl)" :alt="item.bookTitle" class="item-cover-img" />
              <div v-else class="book-mark" aria-hidden="true"><span>{{ item.format === 'PHYSICAL' ? 'BẢN IN' : 'BẢN SỐ' }}</span></div>
            </div>
            <div class="item-copy">
              <h2>{{ item.bookTitle }}</h2>
              <p>{{ item.format === 'PHYSICAL' ? 'Sách giấy' : 'Sách điện tử' }}</p>
              <strong>{{ money(item.salePrice) }}</strong>
            </div>
            <div class="item-actions">
              <label>
                <span>Số lượng</span>
                <input
                  type="number"
                  min="1"
                  :max="item.format === 'PHYSICAL' ? 99 : 1"
                  :value="item.quantity"
                  @change="changeQuantity(item, $event.target.value)"
                />
              </label>
              <button type="button" :aria-label="`Xóa ${item.bookTitle} khỏi giỏ hàng`" @click="removeItem(item.id)">Xóa</button>
            </div>
          </article>
        </section>

        <aside class="checkout-panel">
          <div class="panel-heading"><span>Thanh toán</span><strong>{{ money(estimatedTotal) }}</strong></div>
          <div v-if="requiresShippingAddress" class="field-group">
            <label for="address">Địa chỉ nhận hàng</label>
            <select id="address" v-model="addressId">
              <option :value="null" disabled>Chọn địa chỉ</option>
              <option v-for="address in options.addresses" :key="address.id" :value="address.id">
                {{ address.recipientName }} · {{ address.addressLine }}, {{ address.wardName }}
              </option>
            </select>
            <p v-if="options.addresses.length === 0" class="field-note">Tài khoản chưa có địa chỉ giao hàng.</p>
          </div>

          <div class="field-group">
            <span class="field-label">Phương thức thanh toán</span>
            <label v-for="method in options.paymentMethods" :key="method.id" class="payment-option">
              <input v-model="paymentMethodId" type="radio" :value="method.id" />
              <span><strong>{{ method.name }}</strong><small>{{ method.description }}</small></span>
            </label>
          </div>

          <div class="field-group">
            <label for="coupon">Mã giảm giá</label>
            <input id="coupon" v-model="couponCode" type="text" placeholder="Nhập mã nếu có" />
          </div>

          <dl class="totals">
            <div><dt>Tạm tính</dt><dd>{{ money(cart.cartTotalAmount) }}</dd></div>
            <div><dt>Phí giao hàng</dt><dd>{{ shippingFee ? money(shippingFee) : 'Miễn phí' }}</dd></div>
            <div class="grand-total"><dt>Tổng dự kiến</dt><dd>{{ money(estimatedTotal) }}</dd></div>
          </dl>

          <button class="pay-button" :disabled="submitting || (requiresShippingAddress && !addressId) || !paymentMethodId" @click="checkout">
            {{ submitting ? 'Đang tạo giao dịch...' : 'Thanh toán đơn hàng' }}
          </button>
          <p class="security-note">Số tiền cuối cùng được tính lại và xác minh tại máy chủ.</p>
        </aside>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<style scoped>
.checkout-page{min-height:100dvh;background:var(--page-bg,#fbfbfa);color:var(--text,#2f3437);font-family:var(--font-body,'Helvetica Neue',sans-serif)}
.checkout-header{height:76px;display:flex;align-items:center;justify-content:space-between;padding:0 clamp(1.25rem,5vw,5rem);border-bottom:1px solid var(--line-soft,#eaeaea);background:rgba(255,255,255,.92);backdrop-filter:blur(14px)}
.checkout-brand{display:flex;align-items:center}
.brand-logo-img{height:3.6rem;width:auto;display:block}
.back-link{display:flex;align-items:center;gap:.55rem;font-size:.84rem;font-weight:550;color:var(--text-soft,#787774);transition:color .2s ease,transform .2s ease}.back-link span{font-size:1.05rem;transition:transform .2s ease}.back-link:hover{color:var(--text-strong,#111)}.back-link:hover span{transform:translateX(-3px)}
.checkout-main{width:min(1180px,calc(100% - 2rem));margin:0 auto;padding:clamp(3rem,7vw,5.5rem) 0 7rem}.page-heading{margin-bottom:clamp(2.5rem,5vw,4rem)}.page-heading p{margin:0 0 .55rem;color:var(--text-soft,#787774);font-size:.8rem;font-weight:600;letter-spacing:.08em;text-transform:uppercase}.page-heading h1{max-width:850px;margin:0;font-family:var(--font-display,'Newsreader',serif);font-size:clamp(2.8rem,6vw,5rem);font-weight:450;line-height:.96;letter-spacing:-.055em;text-wrap:balance;color:var(--text-strong,#111)}
.message{padding:.95rem 1.1rem;margin-bottom:1.5rem;border-left:3px solid currentColor;border-radius:2px 8px 8px 2px}.message-error{background:var(--pastel-red-bg,#fdebec);color:var(--pastel-red-text,#9f2f2d)}
.empty-state{position:relative;isolation:isolate;min-height:390px;display:grid;grid-template-columns:minmax(260px,.8fr) minmax(320px,1.15fr);align-items:center;gap:clamp(2rem,7vw,7rem);overflow:hidden;padding:clamp(2.25rem,5vw,4.5rem) clamp(2rem,7vw,6rem);border:1px solid var(--line-soft,#eaeaea);border-radius:12px;background:var(--surface,#fff)}
.empty-state::after{content:'00';position:absolute;right:-.02em;bottom:-.32em;z-index:-1;font:300 clamp(12rem,24vw,22rem)/1 var(--font-display,'Newsreader',serif);letter-spacing:-.08em;color:var(--surface-soft,#f7f6f3);pointer-events:none}
.empty-copy{position:relative;z-index:1;max-width:31rem}.empty-index{display:block;margin-bottom:1.1rem;font:600 .72rem/1 var(--font-mono,monospace);letter-spacing:.1em;text-transform:uppercase;color:var(--text-soft,#787774)}.empty-copy h2{margin:0 0 .85rem;font-family:var(--font-display,'Newsreader',serif);font-size:clamp(2.2rem,4vw,3.35rem);font-weight:500;line-height:1;letter-spacing:-.04em;color:var(--text-strong,#111);text-wrap:balance}.empty-copy p{max-width:42ch;margin:0 0 1.75rem;color:var(--text-soft,#787774);line-height:1.65;text-wrap:pretty}
.empty-visual{position:relative;width:210px;height:210px;margin:auto;filter:drop-shadow(0 20px 24px rgba(51,40,28,.1))}.book{position:absolute;display:block;width:116px;height:164px;border-radius:3px 9px 9px 3px;transform-origin:left bottom}.book::after{content:'';position:absolute;inset:0 auto 0 8px;width:1px;background:rgba(255,255,255,.35)}.book-back{left:19px;top:25px;background:#d9cec0;transform:rotate(-12deg)}.book-middle{left:66px;top:34px;background:#969d8d;transform:rotate(11deg)}.book-front{left:44px;top:18px;display:flex;flex-direction:column;justify-content:space-between;padding:19px 14px 16px;background:#242522;color:#f4efe6;transform:rotate(-1deg);box-shadow:inset -7px 0 rgba(255,255,255,.04)}.book-title{font:600 .76rem/1.15 var(--font-mono,monospace);letter-spacing:.08em}.book-mark-line{width:28px;height:2px;background:#c7a978}
.primary-link,.pay-button{display:flex;align-items:center;justify-content:center;gap:.65rem;width:max-content;padding:.85rem 1.15rem;border:1px solid var(--text-strong,#111);border-radius:5px;background:var(--text-strong,#111);color:#fff;font-weight:650;transition:background .2s ease,transform .2s ease,box-shadow .2s ease}.primary-link:hover,.pay-button:hover:not(:disabled){background:#2f302d;box-shadow:0 8px 18px rgba(26,25,22,.12);transform:translateY(-2px)}.primary-link:active,.pay-button:active:not(:disabled){transform:translateY(0) scale(.98)}
.loading-state{min-height:390px;display:grid;grid-template-columns:1fr .8fr;align-items:center;gap:8%;padding:clamp(2.25rem,5vw,4.5rem);border:1px solid var(--line-soft,#eaeaea);border-radius:12px;background:var(--surface,#fff)}.loading-copy{display:grid;gap:1rem}.skeleton{display:block;overflow:hidden;border-radius:4px;background:var(--surface-soft,#f7f6f3)}.skeleton::after{content:'';display:block;width:45%;height:100%;background:linear-gradient(90deg,transparent,rgba(255,255,255,.85),transparent);animation:shimmer 1.3s ease-in-out infinite}.skeleton-label{width:28%;height:10px}.skeleton-title{width:75%;height:54px}.skeleton-text{width:60%;height:16px}.skeleton-panel{height:210px}.sr-only{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0}@keyframes shimmer{from{transform:translateX(-120%)}to{transform:translateX(240%)}}
.checkout-grid{display:grid;grid-template-columns:minmax(0,1.5fr) minmax(330px,.78fr);gap:clamp(2.25rem,5vw,4.5rem);align-items:start}.order-column{border-top:1px solid var(--line-strong,#dcdcdc)}.order-column-heading{display:flex;align-items:center;justify-content:space-between;padding:1rem 0;border-bottom:1px solid var(--line-soft,#eaeaea)}.order-column-heading h2{font:600 .78rem/1 var(--font-body);letter-spacing:.06em;text-transform:uppercase}.order-column-heading span{color:var(--text-soft,#787774);font:500 .75rem/1 var(--font-mono,monospace)}
.cart-row{display:grid;grid-template-columns:76px 1fr auto;gap:1.4rem;align-items:center;padding:1.65rem 0;border-bottom:1px solid var(--line-soft,#eaeaea)}.cart-item-image{width:76px;display:flex;align-items:center;justify-content:center}.item-cover-img{width:100%;height:auto;object-fit:cover;border-radius:4px;box-shadow:0 2px 8px rgba(0,0,0,0.08)}.book-mark{height:102px;width:100%;display:flex;align-items:flex-end;padding:.7rem;background:#e4ddd2;border-radius:3px 8px 8px 3px;box-shadow:inset 6px 0 rgba(255,255,255,.25);color:#4e493f}.book-mark span{font:650 .58rem/1 var(--font-mono,monospace);letter-spacing:.08em}.item-copy h2{margin:0 0 .3rem;font:600 1.05rem/1.3 var(--font-body);color:var(--text-strong,#111)}.item-copy p{margin:0 0 .75rem;color:var(--text-soft,#787774);font-size:.78rem}.item-copy strong{font:600 .88rem/1 var(--font-mono,monospace);font-variant-numeric:tabular-nums}.item-actions{display:flex;align-items:flex-end;gap:1.15rem}.item-actions label{display:grid;gap:.35rem;font-size:.72rem;color:var(--text-soft,#787774)}.item-actions input{width:68px;padding:.55rem;border:1px solid var(--line-strong,#dcdcdc);border-radius:4px;background:var(--surface,#fff);color:var(--text,#2f3437)}.item-actions button{padding:.55rem 0;color:var(--pastel-red-text,#9f2f2d);font-size:.78rem;transition:opacity .2s ease}.item-actions button:hover{text-decoration:underline;text-underline-offset:3px}
.checkout-panel{position:sticky;top:1.5rem;padding:1.5rem;border:1px solid var(--line-soft,#eaeaea);border-radius:10px;background:var(--surface,#fff);box-shadow:0 14px 34px rgba(66,57,44,.05)}.panel-heading{display:flex;align-items:flex-end;justify-content:space-between;padding-bottom:1.35rem;margin-bottom:1.35rem;border-bottom:1px solid var(--line-soft,#eaeaea)}.panel-heading span{font:600 .76rem/1 var(--font-body);letter-spacing:.06em;text-transform:uppercase}.panel-heading strong{font:600 1rem/1 var(--font-mono,monospace)}.field-group{display:grid;gap:.65rem;padding-bottom:1.25rem;margin-bottom:1.25rem;border-bottom:1px solid var(--line-soft,#eaeaea)}.field-group label,.field-label{font-size:.8rem;font-weight:650}.field-group select,.field-group>input{width:100%;padding:.78rem;border:1px solid var(--line-strong,#dcdcdc);border-radius:4px;background:var(--surface,#fff);color:var(--text,#2f3437);transition:border-color .2s ease}.field-group select:hover,.field-group>input:hover{border-color:var(--text-soft,#787774)}.field-note,.security-note{margin:0;color:var(--text-soft,#787774);font-size:.75rem;line-height:1.5}.payment-option{display:flex;align-items:flex-start;gap:.7rem;padding:.8rem;background:var(--page-bg,#fbfbfa);border:1px solid var(--line-soft,#eaeaea);border-radius:6px;cursor:pointer;transition:border-color .2s ease,background .2s ease}.payment-option:has(input:checked){border-color:var(--text-strong,#111);background:var(--surface,#fff)}.payment-option span{display:grid;gap:.2rem}.payment-option small{color:var(--text-soft,#787774);font-weight:400}.totals{display:grid;gap:.7rem;margin:0 0 1.4rem}.totals div{display:flex;justify-content:space-between}.totals dt,.totals dd{margin:0;font-size:.84rem}.totals dd{font-family:var(--font-mono,monospace);font-variant-numeric:tabular-nums}.grand-total{padding-top:1rem;border-top:1px solid var(--line-soft,#eaeaea)}.grand-total dt,.grand-total dd{font-weight:700;font-size:.95rem}.pay-button{width:100%;cursor:pointer}.pay-button:disabled{opacity:.42;cursor:not-allowed}.security-note{text-align:center;margin-top:.8rem}
@media(max-width:820px){.checkout-header{height:68px}.checkout-main{padding-top:2.75rem}.page-heading{margin-bottom:2.25rem}.empty-state{grid-template-columns:1fr;gap:1.5rem;text-align:center}.empty-visual{width:180px;height:180px;transform:scale(.82)}.empty-copy{margin:auto}.empty-copy p{margin-inline:auto}.primary-link{margin-inline:auto}.checkout-grid{grid-template-columns:1fr;gap:2rem}.cart-row{grid-template-columns:62px 1fr}.book-mark{height:86px}.item-actions{grid-column:2}.checkout-panel{position:static;padding:1.2rem}.loading-state{grid-template-columns:1fr}.skeleton-panel{display:none}}
@media(max-width:520px){.checkout-header{padding-inline:1rem}.brand-logo-img{height:2.6rem}.back-link{font-size:.78rem}.checkout-main{width:min(100% - 1.5rem,1180px);padding-bottom:4rem}.page-heading h1{font-size:2.6rem}.empty-state{min-height:0;padding:2.25rem 1.15rem 2.75rem}.empty-state::after{font-size:13rem}.cart-row{gap:1rem}.item-actions{width:100%;justify-content:space-between}.order-column-heading{padding-top:.8rem}}
@media(prefers-reduced-motion:reduce){*{transition:none!important}.skeleton::after{animation:none}}
</style>
