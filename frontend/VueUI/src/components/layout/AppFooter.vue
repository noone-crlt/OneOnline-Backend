<template>
  <footer class="app-footer">
    <div class="app-footer__inner">
      <section class="app-footer__main" aria-label="Thông tin One Online">
        <div class="app-footer__brand">
          <RouterLink class="app-footer__logo" to="/" aria-label="Về trang chủ One Online">
            <img src="@/assets/logo/vectorized.svg" alt="One Online Logo" class="app-footer__logo-img" />
          </RouterLink>

          <p class="app-footer__slogan">
            Nền tảng phát hành và phân phối sách bản quyền trực tuyến hàng đầu. 
            Đồng hành cùng các nhà xuất bản mang tới những trải nghiệm đọc và nghe tối ưu.
          </p>

          <a class="app-footer__support" href="mailto:support@oneonline.vn">
            <span class="app-footer__support-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-.2-.9-2-2V6c0-1.1.9-2 2-2z" />
                <polyline points="22,6 12,13 2,6" />
              </svg>
            </span>
            <span>support@oneonline.vn</span>
          </a>
        </div>

        <nav class="app-footer__nav" aria-label="Liên kết nhanh">
          <h2>LIÊN KẾT NHANH</h2>
          <RouterLink to="/">Trang chủ</RouterLink>
          <RouterLink to="/library">Thư viện sách</RouterLink>
          <a href="/#/latest-books" @click.prevent="scrollToLatestBooks">Mới nhất</a>
          <RouterLink to="/login">Đăng nhập</RouterLink>
        </nav>

        <nav class="app-footer__nav" aria-label="Hỗ trợ khách hàng">
          <h2>HỖ TRỢ</h2>
          <a href="#">Hướng dẫn mua hàng</a>
          <a href="#">Chính sách đổi trả</a>
          <a href="#">Câu hỏi thường gặp</a>
          <a href="#">Liên hệ hợp tác</a>
        </nav>
      </section>

      <section class="app-footer__bottom" aria-label="Bản quyền">
        <p>© 2026 ONE ONLINE. Tất cả quyền được bảo lưu.</p>
        <div class="app-footer__policies">
          <a href="#">Điều khoản dịch vụ</a>
          <span aria-hidden="true" class="bullet-dot">•</span>
          <a href="#">Chính sách bảo mật</a>
        </div>
      </section>
    </div>
  </footer>
</template>

<script setup>
function scrollToLatestBooks() {
  const latestBooks = document.getElementById('latest-books')
  if (latestBooks) {
    latestBooks.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    })
    return
  }
  window.location.hash = '#/'
}
</script>

<style scoped>
.app-footer {
  width: 100%;
  margin-top: clamp(4rem, 8vw, 6rem);
  background-color: var(--surface-soft);
  color: var(--text);
  border-top: 1px solid var(--line-soft);
  font-family: var(--font-body);
}

.app-footer__inner {
  width: min(100% - clamp(2rem, 5vw, 4rem), var(--content-width));
  margin: 0 auto;
}

.app-footer__main {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(12rem, 0.7fr) minmax(0, 0.9fr);
  gap: clamp(2rem, 5vw, 4.5rem);
  padding: 4.5rem 0 3.5rem;
}

.app-footer__brand,
.app-footer__nav {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.app-footer__logo {
  display: inline-flex;
  align-items: center;
  margin-bottom: 1.2rem;
}

.app-footer__logo-img {
  height: 52px;
  width: auto;
  object-fit: contain;
  display: block;
}

.app-footer__slogan {
  max-width: 34rem;
  color: var(--text);
  font-size: 0.95rem;
  line-height: 1.7;
  margin: 0;
  text-align: justify;
}

.app-footer__support {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  color: var(--text-soft);
  font-size: 0.9rem;
  font-weight: 500;
  transition: color 200ms ease;
  width: max-content;
}

.app-footer__support:hover {
  color: var(--text-strong);
}

.app-footer__support-icon {
  display: inline-grid;
  place-items: center;
  color: var(--text-soft);
  flex: 0 0 auto;
}

.app-footer h2 {
  margin: 0 0 1.25rem;
  color: var(--text-strong);
  font-family: var(--font-body);
  font-size: 0.85rem;
  font-weight: 700;
  line-height: 1.35;
  letter-spacing: 0.1em;
}

.app-footer__nav {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.app-footer__nav a {
  width: max-content;
  max-width: 100%;
  color: var(--text-soft);
  font-size: 0.95rem;
  transition: color 180ms ease-out, transform 180ms ease-out;
}

.app-footer__nav a:hover {
  color: var(--text-strong);
  transform: translateX(4px);
}

.app-footer__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.5rem 0 2rem;
  border-top: 1px solid var(--line-soft);
  color: var(--text-soft);
  font-size: 0.85rem;
}

.app-footer__bottom p {
  margin: 0;
}

.app-footer__policies {
  display: flex;
  align-items: center;
}

.app-footer__policies a {
  color: var(--text-soft);
  text-decoration: none;
  transition: color 150ms ease;
}

.app-footer__policies a:hover {
  color: var(--text-strong);
}

.bullet-dot {
  margin: 0 0.5rem;
  opacity: 0.5;
}

@media (max-width: 900px) {
  .app-footer__main {
    grid-template-columns: 1fr;
    gap: 2.5rem;
    padding: 3rem 0 2.5rem;
  }

  .app-footer__slogan {
    max-width: 100%;
  }
}

@media (max-width: 640px) {
  .app-footer__inner {
    width: min(100% - 2rem, 1200px);
  }

  .app-footer__bottom {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
