import axios, { type AxiosError } from 'axios'
import { session } from '@/utils/session'
import type { ApiErrorResponse } from '@/types/auth'

// Business endpoints (prefixed with /api)
export const apiClient = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}/api`,
})

// Auth endpoints (NOT prefixed with /api)
export const authClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

apiClient.interceptors.request.use((config) => {
  const token = session.getToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let onUnauthorized: (() => void) | null = null
export function registerUnauthorizedHandler(fn: () => void) {
  onUnauthorized = fn
}

function handleResponseError(error: AxiosError<ApiErrorResponse>) {
  if (error.response?.status === 401) {
    session.clearAll()
    onUnauthorized?.()
  }
  return Promise.reject(error)
}

apiClient.interceptors.response.use((res) => res, handleResponseError)
authClient.interceptors.response.use((res) => res, handleResponseError)

export function extractErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  const err = error as AxiosError<ApiErrorResponse>
  const data = err?.response?.data
  if (data?.message) return data.message
  if (err?.response?.status === 404) return 'The requested resource was not found.'
  if (err?.response?.status === 409) return 'This record already exists (duplicate entry).'
  if (err?.response?.status === 403) return "You don't have permission to perform this action."
  if (err?.response?.status === 500) return 'Server error. Please try again shortly.'
  if (err?.message === 'Network Error') return 'Cannot reach the server. Check your connection or backend availability.'
  return fallback
}

export function extractFieldErrors(error: unknown): Record<string, string> {
  const err = error as AxiosError<ApiErrorResponse>
  return err?.response?.data?.details ?? {}
}
