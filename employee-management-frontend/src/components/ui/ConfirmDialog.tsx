import { AlertTriangle } from 'lucide-react'
import Modal from './Modal'

interface Props {
  open: boolean
  title: string
  description?: string
  confirmLabel?: string
  danger?: boolean
  loading?: boolean
  onConfirm: () => void
  onCancel: () => void
}

export default function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = 'Confirm',
  danger = true,
  loading = false,
  onConfirm,
  onCancel,
}: Props) {
  return (
    <Modal open={open} onClose={onCancel} title={title} width={420}>
      <div className="flex gap-3">
        <AlertTriangle size={20} color={danger ? 'var(--red)' : 'var(--amber)'} className="shrink-0 mt-0.5" />
        <p className="text-[13px] text-inksoft">{description}</p>
      </div>
      <div className="flex justify-end gap-2 mt-6">
        <button className="btn ghost" onClick={onCancel} disabled={loading}>
          Cancel
        </button>
        <button className={`btn ${danger ? 'danger' : ''}`} onClick={onConfirm} disabled={loading}>
          {loading ? 'Please wait…' : confirmLabel}
        </button>
      </div>
    </Modal>
  )
}
