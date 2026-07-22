import { forwardRef, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react'

interface BaseProps {
  label: string
  error?: string
  hint?: string
  required?: boolean
}

export const TextField = forwardRef<HTMLInputElement, BaseProps & InputHTMLAttributes<HTMLInputElement>>(
  function TextField({ label, error, hint, required, ...rest }, ref) {
    return (
      <div>
        <label className="field-label">
          {label} {required && <span style={{ color: 'var(--red)' }}>*</span>}
        </label>
        <input ref={ref} className={`field-input ${error ? 'field-error' : ''}`} {...rest} />
        {hint && !error && <div className="text-[11px] text-inksoft mt-1">{hint}</div>}
        {error && <div className="field-error-msg">{error}</div>}
      </div>
    )
  },
)

export const SelectField = forwardRef<
  HTMLSelectElement,
  BaseProps & SelectHTMLAttributes<HTMLSelectElement> & { children: ReactNode }
>(function SelectField({ label, error, hint, required, children, ...rest }, ref) {
  return (
    <div>
      <label className="field-label">
        {label} {required && <span style={{ color: 'var(--red)' }}>*</span>}
      </label>
      <select ref={ref} className={`field-input ${error ? 'field-error' : ''}`} {...rest}>
        {children}
      </select>
      {hint && !error && <div className="text-[11px] text-inksoft mt-1">{hint}</div>}
      {error && <div className="field-error-msg">{error}</div>}
    </div>
  )
})

export const TextareaField = forwardRef<HTMLTextAreaElement, BaseProps & TextareaHTMLAttributes<HTMLTextAreaElement>>(
  function TextareaField({ label, error, hint, required, ...rest }, ref) {
    return (
      <div>
        <label className="field-label">
          {label} {required && <span style={{ color: 'var(--red)' }}>*</span>}
        </label>
        <textarea ref={ref} className={`field-input ${error ? 'field-error' : ''}`} rows={3} {...rest} />
        {hint && !error && <div className="text-[11px] text-inksoft mt-1">{hint}</div>}
        {error && <div className="field-error-msg">{error}</div>}
      </div>
    )
  },
)
