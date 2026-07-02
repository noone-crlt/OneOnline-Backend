<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { PhArrowLeft, PhUserPlus } from '@phosphor-icons/vue'

import { register } from '../../stores/auth'

const router = useRouter()

const form = reactive({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const errorMessage = ref('')
const isSubmitting = ref(false)

function showError(message) {
  errorMessage.value = message
}

async function handleSubmit() {
  errorMessage.value = ''

  const fullName = form.fullName.trim()
  const email = form.email.trim()
  const password = form.password
  const confirmPassword = form.confirmPassword

  if (!fullName) {
    showError('Họ tên không được để trống.')
    return
  }

  if (!email) {
    showError('Email không được để trống.')
    return
  }

  if (password.length < 6) {
    showError('Mật khẩu phải có ít nhất 6 ký tự.')
    return
  }

  if (password !== confirmPassword) {
    showError('Mật khẩu xác nhận không khớp.')
    return
  }

  isSubmitting.value = true

  try {
    await register({
      fullName,
      email,
      password,
    })

    await router.replace({
      path: '/login',
      query: {
        registered: '1',
      },
    })
  } catch (error) {
    showError(error instanceof Error ? error.message : 'Đăng ký thất bại. Vui lòng thử lại.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-container">
      
      <!-- Right side: Form (Swapped order for variety) -->
      <div class="auth-form-wrapper">
        <div class="auth-form-inner">
          <div class="form-header">
            <h2>Tạo tài khoản mới</h2>
            <p>Tham gia cộng đồng đọc sách trực tuyến lớn nhất.</p>
          </div>

          <form class="auth-form" @submit.prevent="handleSubmit">
            
            <div class="form-group">
              <label for="fullName">Họ và tên</label>
              <input
                id="fullName"
                v-model="form.fullName"
                type="text"
                placeholder="VD: Nguyễn Văn A"
                required
                :disabled="isSubmitting"
              />
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

            <div class="form-row">
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
              <div class="form-group">
                <label for="confirmPassword">Xác nhận</label>
                <input
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  required
                  :disabled="isSubmitting"
                />
              </div>
            </div>

            <div v-if="errorMessage" class="alert alert-error">
              {{ errorMessage }}
            </div>

            <button class="submit-btn" type="submit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner"></span>
              <template v-else>
                Đăng ký ngay
                <PhUserPlus :size="20" weight="bold" />
              </template>
            </button>
          </form>

          <p class="auth-redirect">
            Đã có tài khoản?
            <RouterLink to="/login">Đăng nhập</RouterLink>
          </p>
        </div>
      </div>

      <!-- Left side: Image/Branding (Visually on the right due to flex-direction row-reverse) -->
      <div class="auth-visual">
        <div class="visual-overlay"></div>
        <div class="visual-content">
          <RouterLink class="back-link" to="/">
            <PhArrowLeft :size="20" />
            Về trang chủ
          </RouterLink>
          <div class="brand">
            <h1>Hành trình bắt đầu</h1>
            <p>Hàng ngàn cuốn sách đang chờ đón bạn. Tham gia ngay hôm nay hoàn toàn miễn phí.</p>
          </div>
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
  flex-direction: row-reverse; /* Swap sides for register page */
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: #ffffff;
  border-radius: 1.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* Visual Section */
.auth-visual {
  flex: 1;
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1532012197267-da84d127e765?q=80&w=1200&auto=format&fit=crop');
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
  background: linear-gradient(to bottom, rgba(79, 70, 229, 0.5), rgba(30, 27, 75, 0.9)); /* Indigo gradient */
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

/* Form Section */
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

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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

/* Alerts */
.alert {
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  font-size: 0.9rem;
  font-weight: 500;
  animation: slideIn 0.3s ease-out;
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
  background-color: #4f46e5; /* indigo-600 */
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
  background-color: #4338ca; /* indigo-700 */
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
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
  color: #4f46e5;
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
