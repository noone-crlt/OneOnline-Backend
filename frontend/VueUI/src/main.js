import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'remixicon/fonts/remixicon.css'
import './assets/main.css'

import { createApp } from 'vue'
import App from './components/App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
