import { computed, reactive } from 'vue'
import { getCart } from '../services/api'
import { authIsAuthenticated } from './auth'

const state = reactive({
  itemCount: 0,
})

export const cartItemCount = computed(() => state.itemCount)

export function updateCartItemCount(count) {
  state.itemCount = count
}

export function incrementCartItemCount(amount = 1) {
  state.itemCount += amount
}

export async function fetchCartItemCount() {
  if (!authIsAuthenticated.value) {
    state.itemCount = 0
    return
  }

  try {
    const cart = await getCart()
    state.itemCount = cart?.items?.reduce((sum, item) => sum + (item.quantity || 1), 0) || 0
  } catch (err) {
    console.error('Failed to fetch cart item count', err)
  }
}
