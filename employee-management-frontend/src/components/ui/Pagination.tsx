import { ChevronLeft, ChevronRight } from 'lucide-react'

interface Props {
  pageNo: number
  totalPages: number
  onChange: (page: number) => void
  /** Currently selected rows per page. If provided, shows a rows-per-page selector. */
  pageSize?: number
  /** Called when the user picks a different rows-per-page value. */
  onPageSizeChange?: (size: number) => void
  /** Total number of records (used for display). */
  totalElements?: number
}

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100]

export default function Pagination({
  pageNo,
  totalPages,
  onChange,
  pageSize,
  onPageSizeChange,
  totalElements,
}: Props) {
  const showSizeSelector = pageSize !== undefined && onPageSizeChange !== undefined

  if (totalPages <= 1 && !showSizeSelector) return null

  return (
    <div className="flex items-center justify-between mt-4 pt-3 border-t border-line flex-wrap gap-2">
      {/* Left side: rows-per-page selector + page info */}
      <div className="flex items-center gap-3">
        {showSizeSelector && (
          <div className="flex items-center gap-1.5">
            <span className="text-[11.5px] mono text-inksoft whitespace-nowrap">Rows per page:</span>
            <select
              id="rows-per-page-select"
              className="field-input !py-0.5 !px-2 !h-7 text-[12px] mono w-auto"
              value={pageSize}
              onChange={(e) => {
                onPageSizeChange(Number(e.target.value))
              }}
              aria-label="Rows per page"
            >
              {PAGE_SIZE_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </div>
        )}
        <span className="text-[11.5px] mono text-inksoft">
          {totalElements !== undefined
            ? `Page ${pageNo + 1} of ${totalPages} · ${totalElements} records`
            : `Page ${pageNo + 1} of ${totalPages}`}
        </span>
      </div>

      {/* Right side: prev / next buttons */}
      {totalPages > 1 && (
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
      )}
    </div>
  )
}
