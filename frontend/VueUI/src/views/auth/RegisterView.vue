<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Mail, 
  Lock, 
  User, 
  Eye, 
  EyeOff, 
  UserPlus, 
  BookOpen, 
  Sparkles, 
  AlertCircle 
} from 'lucide-vue-next'

import { register } from '../../stores/auth'

const router = useRouter()

const form = reactive({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
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
    showError('Vui lòng nhập họ và tên.')
    return
  }

  if (!email) {
    showError('Vui lòng nhập địa chỉ Email.')
    return
  }

  if (password.length < 6) {
    showError('Mật khẩu phải có ít nhất 6 ký tự.')
    return
  }

  if (password !== confirmPassword) {
    showError('Mật khẩu xác nhận không trùng khớp.')
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
    <div class="auth-container auth-container-reverse">
      <!-- Right side: Form -->
      <div class="auth-form-wrapper">
        <div class="auth-form-inner">
          <div class="form-header">
            <h2 class="form-title">Tạo tài khoản mới</h2>
            <p class="form-subtitle">Tham gia cộng đồng đọc sách trực tuyến OneOnline</p>
          </div>

          <form class="auth-form" @submit.prevent="handleSubmit">
            <div class="form-group">
              <label for="fullName">Họ và tên</label>
              <div class="input-wrapper">
                <User :size="18" class="input-icon" />
                <input
                  id="fullName"
                  v-model="form.fullName"
                  type="text"
                  placeholder="Nguyễn Văn A"
                  required
                  :disabled="isSubmitting"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="email">Địa chỉ Email</label>
              <div class="input-wrapper">
                <Mail :size="18" class="input-icon" />
                <input
                  id="email"
                  v-model="form.email"
                  type="email"
                  placeholder="name@example.com"
                  required
                  :disabled="isSubmitting"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="password">Mật khẩu</label>
                <div class="input-wrapper">
                  <Lock :size="18" class="input-icon" />
                  <input
                    id="password"
                    v-model="form.password"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="••••••••"
                    required
                    :disabled="isSubmitting"
                  />
                  <button 
                    type="button" 
                    class="toggle-password-btn" 
                    @click="showPassword = !showPassword"
                    tabindex="-1"
                  >
                    <EyeOff v-if="showPassword" :size="18" />
                    <Eye v-else :size="18" />
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label for="confirmPassword">Xác nhận</label>
                <div class="input-wrapper">
                  <Lock :size="18" class="input-icon" />
                  <input
                    id="confirmPassword"
                    v-model="form.confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    placeholder="••••••••"
                    required
                    :disabled="isSubmitting"
                  />
                  <button 
                    type="button" 
                    class="toggle-password-btn" 
                    @click="showConfirmPassword = !showConfirmPassword"
                    tabindex="-1"
                  >
                    <EyeOff v-if="showConfirmPassword" :size="18" />
                    <Eye v-else :size="18" />
                  </button>
                </div>
              </div>
            </div>

            <div v-if="errorMessage" class="alert alert-error">
              <AlertCircle :size="18" />
              <span>{{ errorMessage }}</span>
            </div>

            <button class="submit-btn" type="submit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner"></span>
              <template v-else>
                <span>Đăng ký ngay</span>
                <UserPlus :size="18" />
              </template>
            </button>
          </form>

          <p class="auth-redirect">
            Đã có tài khoản?
            <RouterLink to="/login" class="redirect-link">Đăng nhập</RouterLink>
          </p>
        </div>
      </div>

      <!-- Left side: Visual Section -->
      <div class="auth-visual">
        <div class="visual-bg-glow"></div>
        <div class="visual-content">
          <div class="visual-top">
            <RouterLink class="back-link" to="/">
              <ArrowLeft :size="18" />
              <span>Về trang chủ</span>
            </RouterLink>
          </div>

          <div class="visual-center">
            <div class="brand-badge">
              <BookOpen :size="20" class="brand-icon" />
              <span>OneOnline</span>
            </div>
            <h1 class="visual-title">Hành trình đọc sách bắt đầu từ đây</h1>
            <p class="visual-desc">Tạo tài khoản hoàn toàn miễn phí để khám phá hàng ngàn tựa sách hấp dẫn và lưu trữ tủ sách cá nhân.</p>
          </div>

          <div class="visual-bottom">
            <div class="quote-glass-card">
              <div class="quote-header">
                <Sparkles :size="16" class="sparkle-icon" />
                <span>Quyền lợi thành viên</span>
              </div>
              <p class="quote-text">Đọc thử miễn phí, tích điểm ưu đãi và đồng bộ thư viện đọc trên mọi thiết bị.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  background-image: 
    radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.05) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(15, 23, 42, 0.04) 0px, transparent 50%);
  padding: 1.5rem;
  box-sizing: border-box;
}

.auth-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 620px;
  background: #ffffff;
  border-radius: 2rem;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 25px 70px -15px rgba(15, 23, 42, 0.08), 0 4px 20px rgba(0, 0, 0, 0.02);
  overflow: hidden;
  transition: all 0.3s ease;
}

.auth-container-reverse {
  flex-direction: row-reverse;
}

/* Visual Section (Left / Right) */
.auth-visual {
  flex: 1;
  position: relative;
  background: #0f172a;
  overflow: hidden;
  display: none;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

@media (min-width: 768px) {
  .auth-visual {
    display: flex;
  }
}

.visual-bg-glow {
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 15% 15%, rgba(99, 102, 241, 0.22) 0%, transparent 45%),
    radial-gradient(circle at 85% 85%, rgba(79, 70, 229, 0.18) 0%, transparent 45%),
    linear-gradient(145deg, #0f172a 0%, #1e293b 60%, #090d16 100%);
  z-index: 1;
}

.visual-content {
  position: relative;
  z-index: 10;
  padding: 3rem 2.5rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 100%;
  box-sizing: border-box;
  color: #ffffff;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #ffffff;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.875rem;
  padding: 0.5rem 1.1rem;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.back-link:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.35);
  transform: translateX(-3px);
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.9rem;
  border-radius: 9999px;
  background: rgba(99, 102, 241, 0.25);
  border: 1px solid rgba(165, 180, 252, 0.35);
  color: #e0e7ff;
  font-weight: 700;
  font-size: 0.8rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 1.25rem;
}

.visual-title {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.02em;
  margin: 0 0 1rem 0;
  color: #ffffff;
  text-shadow: none;
}

.visual-desc {
  font-size: 0.95rem;
  color: rgba(241, 245, 249, 0.85);
  line-height: 1.6;
  margin: 0;
  max-width: 95%;
  font-weight: 400;
}

.quote-glass-card {
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 1.25rem;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}

.quote-header {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: #c7d2fe;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
}

.quote-text {
  font-size: 0.9rem;
  color: #f8fafc;
  line-height: 1.5;
  margin: 0;
  font-weight: 400;
}

/* Form Section (Right) */
.auth-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2.5rem;
  background: #ffffff;
}

.auth-form-inner {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 2rem;
}

.form-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.03em;
}

.form-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.4;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.875rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 0.9rem;
  color: #94a3b8;
  pointer-events: none;
  transition: color 0.2s ease;
}

.input-wrapper input {
  width: 100%;
  box-sizing: border-box;
  padding: 0.75rem 1rem 0.75rem 2.6rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.875rem;
  font-size: 0.95rem;
  color: #0f172a;
  background-color: #f8fafc;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  outline: none;
}

.input-wrapper input:focus {
  border-color: #0f172a;
  background-color: #ffffff;
  box-shadow: 0 0 0 4px rgba(15, 23, 42, 0.06);
}

.input-wrapper input:focus ~ .input-icon,
.input-wrapper:focus-within .input-icon {
  color: #0f172a;
}

.input-wrapper input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toggle-password-btn {
  position: absolute;
  right: 0.75rem;
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.toggle-password-btn:hover {
  color: #0f172a;
}

/* Alerts */
.alert {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.75rem 1rem;
  border-radius: 0.875rem;
  font-size: 0.875rem;
  font-weight: 500;
  animation: slideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.alert-error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

/* Submit Button */
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.85rem 1.25rem;
  background-color: #0f172a;
  color: #ffffff;
  border: none;
  border-radius: 0.875rem;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  margin-top: 0.25rem;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
}

.submit-btn:hover:not(:disabled) {
  background-color: #1e293b;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.2);
}

.submit-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: wait;
}

/* Spinner */
.spinner {
  width: 1.2rem;
  height: 1.2rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Redirect text */
.auth-redirect {
  margin-top: 1.75rem;
  text-align: center;
  font-size: 0.9rem;
  color: #64748b;
}

.redirect-link {
  color: #0f172a;
  font-weight: 700;
  text-decoration: none;
  margin-left: 0.25rem;
  transition: color 0.2s ease;
}

.redirect-link:hover {
  color: #4f46e5;
  text-decoration: underline;
}

/* Animations */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
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
