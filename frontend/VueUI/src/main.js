import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'vue-toast-notification/dist/theme-bootstrap.css'
import './assets/main.css'

import { createApp } from 'vue'
import ToastPlugin from 'vue-toast-notification'
import App from './components/App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.use(ToastPlugin, {
  position: 'top-right',
  duration: 3500,
  dismissible: true,
})
app.mount('#app')
