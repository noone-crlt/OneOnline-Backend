import { useToast } from 'vue-toast-notification'

const toast = useToast()

export const notify = {
  success(message, options = {}) {
    return toast.success(message, { position: 'top-right', duration: 3500, ...options })
  },
  error(message, options = {}) {
    return toast.error(message, { position: 'top-right', duration: 4500, ...options })
  },
  warning(message, options = {}) {
    return toast.warning(message, { position: 'top-right', duration: 4000, ...options })
  },
  info(message, options = {}) {
    return toast.info(message, { position: 'top-right', duration: 3500, ...options })
  },
}

export default notify
