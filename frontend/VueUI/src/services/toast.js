import { Notify, Confirm, Loading } from 'notiflix'

// Cấu hình Notify (Toast thông báo)
Notify.init({
  width: '320px',
  position: 'right-top',
  fontSize: '14px',
  borderRadius: '10px',
  cssAnimation: true,
  cssAnimationDuration: 300,
  cssAnimationStyle: 'slide',
  useIcon: true,
  showOnlyTheLastOne: false,
  timeout: 3500,
  fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
  success: {
    background: '#10b981',
    textColor: '#ffffff',
    childClassName: 'notiflix-notify-success',
    notiflixIconColor: '#ffffff',
    fontAwesomeIconColor: '#ffffff',
  },
  failure: {
    background: '#ef4444',
    textColor: '#ffffff',
    childClassName: 'notiflix-notify-failure',
    notiflixIconColor: '#ffffff',
    fontAwesomeIconColor: '#ffffff',
  },
  warning: {
    background: '#f59e0b',
    textColor: '#ffffff',
    childClassName: 'notiflix-notify-warning',
    notiflixIconColor: '#ffffff',
    fontAwesomeIconColor: '#ffffff',
  },
  info: {
    background: '#3b82f6',
    textColor: '#ffffff',
    childClassName: 'notiflix-notify-info',
    notiflixIconColor: '#ffffff',
    fontAwesomeIconColor: '#ffffff',
  },
})

// Cấu hình Confirm (Hộp thoại xác nhận & Prompt)
Confirm.init({
  width: '380px',
  borderRadius: '14px',
  titleColor: '#09090b',
  messageColor: '#52525b',
  okButtonBackground: '#ef4444',
  okButtonColor: '#ffffff',
  cancelButtonBackground: '#f4f4f5',
  cancelButtonColor: '#27272a',
  cssAnimation: true,
  cssAnimationStyle: 'zoom',
  backOverlay: true,
  backOverlayColor: 'rgba(0, 0, 0, 0.45)',
  fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
})

export const notify = {
  success(message) {
    Notify.success(message)
  },
  error(message) {
    Notify.failure(message)
  },
  warning(message) {
    Notify.warning(message)
  },
  info(message) {
    Notify.info(message)
  },
}

export function confirmDialog(title, message, okText = 'Xác nhận', cancelText = 'Hủy') {
  return new Promise((resolve) => {
    Confirm.show(
      title,
      message,
      okText,
      cancelText,
      () => resolve(true),
      () => resolve(false),
      {}
    )
  })
}

export function promptDialog(title, message, defaultValue = '', okText = 'Xác nhận', cancelText = 'Hủy') {
  return new Promise((resolve) => {
    Confirm.prompt(
      title,
      message,
      defaultValue,
      okText,
      cancelText,
      (clientAnswer) => {
        resolve(clientAnswer)
      },
      () => {
        resolve(null)
      },
      {}
    )
  })
}

export { Notify, Confirm, Loading }
export default notify
