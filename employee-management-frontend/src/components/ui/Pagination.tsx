import { ChevronLeft, ChevronRight } from 'lucide-react'

interface Props {
  pageNo: number
  totalPages: number
  onChange: (page: number) => void
}

export default function Pagination({ pageNo, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null
  return (
    <div className="flex items-center justify-between mt-4 pt-3 border-t border-line">
      <span className="text-[11.5px] mono text-inksoft">
        Page {pageNo + 1} of {totalPages}
      </span>
      <div className="flex gap-1">
        <button
          className="btn ghost !px-2.5 !py-1.5"
          disabled={pageNo <= 0}
          onClick={() => onChange(pageNo - 1)}
          aria-label="Previous page"
        >
          <ChevronLeft size={14} />
        </button>
        <button
          className="btn ghost !px-2.5 !py-1.5"
          disabled={pageNo >= totalPages - 1}
          onClick={() => onChange(pageNo + 1)}
          aria-label="Next page"
        >
          <ChevronRight size={14} />
        </button>
      </div>
    </div>
  )
}
