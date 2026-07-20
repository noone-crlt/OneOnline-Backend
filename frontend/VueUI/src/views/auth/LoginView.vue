<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { PhArrowLeft, PhSignIn } from '@phosphor-icons/vue'

import { login } from '../../stores/auth'

const route = useRoute()
const router = useRouter()

const form = reactive({
  email: '',
  password: '',
})

const errorMessage = ref('')
const isSubmitting = ref(false)

const successMessage = computed(() =>
  route.query.registered === '1' ? 'Đăng ký thành công, vui lòng đăng nhập.' : '',
)

const redirectTarget = computed(() => {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect.length > 0 ? redirect : ''
})

function resolveTarget(user) {
  const redirect = redirectTarget.value
  const isAdmin = user?.roles?.includes('ADMIN') ?? false

  if (redirect && redirect.startsWith('/') && !redirect.startsWith('/admin')) {
    return redirect
  }

  return isAdmin ? '/admin' : '/library'
}

async function handleSubmit() {
  errorMessage.value = ''
  isSubmitting.value = true

  try {
    const session = await login({
      email: form.email.trim(),
      password: form.password,
    })

    await router.replace(resolveTarget(session.user))
  } catch (error) {
    errorMessage.value =
      error instanceof TypeError
        ? 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại.'
        : error instanceof Error
          ? error.message
          : 'Đăng nhập thất bại. Vui lòng thử lại.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-container">
      <!-- Left side: Image/Branding -->
      <div class="auth-visual">
        <div class="visual-overlay"></div>
        <div class="visual-content">
          <RouterLink class="back-link" to="/">
            <PhArrowLeft :size="20" />
            Về trang chủ
          </RouterLink>
          <div class="brand">
            <h1>One Online</h1>
            <p>Khám phá kho tàng kiến thức và những câu chuyện bất tận.</p>
          </div>
        </div>
      </div>

      <!-- Right side: Form -->
      <div class="auth-form-wrapper">
        <div class="auth-form-inner">
          <div class="form-header">
            <h2>Chào mừng trở lại</h2>
            <p>Vui lòng đăng nhập để tiếp tục đọc sách.</p>
          </div>

          <form class="auth-form" @submit.prevent="handleSubmit">
            <div v-if="successMessage" class="alert alert-success" role="status">
              {{ successMessage }}
            </div>

            <div class="form-group">
              <label for="email">Địa chỉ Email</label>
              <input
                id="email"
                v-model="form.email"
                type="email"
                placeholder="you@example.com"
                required
                :disabled="isSubmitting"
              />
            </div>

            <div class="form-group">
              <label for="password">Mật khẩu</label>
              <input
                id="password"
                v-model="form.password"
                type="password"
                placeholder="••••••••"
                required
                :disabled="isSubmitting"
              />
            </div>

            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" />
                <span>Ghi nhớ đăng nhập</span>
              </label>
              <a href="#" class="forgot-password">Quên mật khẩu?</a>
            </div>

            <div v-if="errorMessage" class="alert alert-error">
              {{ errorMessage }}
            </div>

            <button class="submit-btn" type="submit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner"></span>
              <template v-else>
                Đăng nhập
                <PhSignIn :size="20" weight="bold" />
              </template>
            </button>
          </form>

          <p class="auth-redirect">
            Chưa có tài khoản?
            <RouterLink to="/register">Đăng ký ngay</RouterLink>
          </p>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6; /* Tailwind gray-100 */
  padding: 1rem;
}

.auth-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: #ffffff;
  border-radius: 1.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* Visual Section (Left) */
.auth-visual {
  flex: 1;
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1456953180671-730de08edaa7?q=80&w=1200&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  display: none;
}

@media (min-width: 768px) {
  .auth-visual {
    display: flex;
  }
}

.visual-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(15, 23, 42, 0.4), rgba(15, 23, 42, 0.9));
}

.visual-content {
  position: relative;
  z-index: 10;
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 100%;
  color: white;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-weight: 500;
  font-size: 0.95rem;
  transition: color 0.2s;
  width: fit-content;
}

.back-link:hover {
  color: white;
}

.brand h1 {
  font-size: 3rem;
  font-weight: 800;
  letter-spacing: -0.05em;
  margin: 0 0 1rem 0;
  line-height: 1;
}

.brand p {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  max-width: 90%;
  line-height: 1.5;
}

/* Form Section (Right) */
.auth-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  background: #ffffff;
}

.auth-form-inner {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 2.5rem;
}

.form-header h2 {
  font-size: 1.8rem;
  font-weight: 800;
  color: #111827; /* gray-900 */
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.03em;
}

.form-header p {
  color: #6b7280; /* gray-500 */
  margin: 0;
  font-size: 0.95rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151; /* gray-700 */
}

.form-group input {
  width: 100%;
  box-sizing: border-box;
  padding: 0.875rem 1rem;
  border: 1px solid #d1d5db; /* gray-300 */
  border-radius: 0.75rem;
  font-size: 1rem;
  color: #111827;
  background-color: #f9fafb; /* gray-50 */
  transition: all 0.2s ease;
  outline: none;
}

.form-group input:focus {
  border-color: #6366f1; /* indigo-500 */
  background-color: #ffffff;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.form-group input:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #4b5563; /* gray-600 */
  cursor: pointer;
}

.remember-me input {
  width: 1rem;
  height: 1rem;
  border-radius: 0.25rem;
  cursor: pointer;
}

.forgot-password {
  color: #6366f1;
  font-weight: 600;
  text-decoration: none;
}

.forgot-password:hover {
  text-decoration: underline;
}

/* Alerts */
.alert {
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  font-size: 0.9rem;
  font-weight: 500;
  animation: slideIn 0.3s ease-out;
}

.alert-success {
  background-color: #ecfdf5; /* emerald-50 */
  color: #065f46; /* emerald-800 */
  border: 1px solid #a7f3d0; /* emerald-200 */
}

.alert-error {
  background-color: #fef2f2; /* red-50 */
  color: #991b1b; /* red-800 */
  border: 1px solid #fecaca; /* red-200 */
}

/* Submit Button */
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.875rem 1rem;
  background-color: #111827; /* gray-900 */
  color: white;
  border: none;
  border-radius: 0.75rem;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 0.5rem;
}

.submit-btn:hover:not(:disabled) {
  background-color: #374151; /* gray-700 */
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

/* Spinner */
.spinner {
  width: 1.25rem;
  height: 1.25rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Redirect text */
.auth-redirect {
  margin-top: 2rem;
  text-align: center;
  font-size: 0.95rem;
  color: #6b7280;
}

.auth-redirect a {
  color: #111827;
  font-weight: 700;
  text-decoration: none;
}

.auth-redirect a:hover {
  text-decoration: underline;
}

/* Animations */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
