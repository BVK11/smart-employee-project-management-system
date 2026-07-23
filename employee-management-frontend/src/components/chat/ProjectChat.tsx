import { useEffect, useLayoutEffect, useState, useRef } from 'react'
import { Send, Users } from 'lucide-react'
import { chatApi, type ChatMessage, type TeamMember } from '@/api/chat'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'
import { extractErrorMessage } from '@/api/client'
import { formatDate } from '@/utils/format'
import Modal from '@/components/ui/Modal'

interface Props {
  projectId: number | string
}

export default function ProjectChat({ projectId }: Props) {
  const { userId } = useAuth()
  const toast = useToast()

  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [team, setTeam] = useState<TeamMember[]>([])
  const [content, setContent] = useState('')
  const [sending, setSending] = useState(false)
  const [loading, setLoading] = useState(true)

  // Profile summary modal state
  const [selectedMember, setSelectedMember] = useState<TeamMember | null>(null)

  // Ref attached directly to the scrollable message container div.
  // We update scrollTop here so the internal container scrolls
  // WITHOUT triggering any layout scroll on the parent page.
  const messagesRef = useRef<HTMLDivElement>(null)

  // Tracks whether the user is near the bottom so we only auto-scroll
  // when they haven't manually scrolled up to read older messages.
  const userScrolledUp = useRef(false)

  // Scroll the chat container to its bottom without touching the page scroll position.
  const scrollToBottom = () => {
    const el = messagesRef.current
    if (!el) return
    // Direct DOM mutation — never touches window / document scroll
    el.scrollTop = el.scrollHeight
  }

  // After every messages render, scroll to bottom only if the user is
  // already near the bottom (i.e. hasn't scrolled up deliberately).
  // useLayoutEffect runs synchronously after DOM mutations but before paint,
  // which prevents a brief flash of the old scroll position.
  useLayoutEffect(() => {
    if (!userScrolledUp.current) {
      scrollToBottom()
    }
  }, [messages])

  // Track whether the user has scrolled away from the bottom
  const onScroll = () => {
    const el = messagesRef.current
    if (!el) return
    // Allow a 50px tolerance so small reflows don't lock auto-scroll
    const isNearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 50
    userScrolledUp.current = !isNearBottom
  }

  const loadHistory = () => {
    chatApi
      .getHistory(projectId)
      .then((data) => {
        setMessages(data)
      })
      .catch(() => { })
  }

  const loadTeam = () => {
    chatApi
      .getTeam(projectId)
      .then(setTeam)
      .catch(() => { })
  }

  useEffect(() => {
    setLoading(true)
    // Reset scroll tracking when switching projects
    userScrolledUp.current = false

    Promise.all([chatApi.getHistory(projectId), chatApi.getTeam(projectId)])
      .then(([history, members]) => {
        setMessages(history)
        setTeam(members)
      })
      .finally(() => setLoading(false))

    // Poll for new messages every 3 seconds
    const interval = setInterval(loadHistory, 3000)

    return () => clearInterval(interval)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId])

  const onSend = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!content.trim() || sending) return

    setSending(true)

    try {
      const msg = await chatApi.sendMessage(projectId, content.trim())

      // Sending a message always re-enables auto-scroll
      userScrolledUp.current = false

      setMessages((prev) => [...prev, msg])
      setContent('')
    } catch (e) {
      toast.error(extractErrorMessage(e, 'Could not send message.'))
    } finally {
      setSending(false)
    }
  }

  const getInitials = (firstName: string, lastName: string) =>
    ((firstName?.[0] ?? '') + (lastName?.[0] ?? '')).toUpperCase()

  return (
    <div
      className="grid grid-cols-1 lg:grid-cols-[1fr_300px] border border-line divide-y lg:divide-y-0 lg:divide-x divide-line bg-card"
      style={{ height: '560px' }}
    >
      {/* Main Chat Panel */}
      <div className="flex flex-col overflow-hidden">
        <div className="p-3 border-b border-line bg-bg flex justify-between items-center shrink-0">
          <span className="text-[13px] font-bold">Team Chat Room</span>
        </div>

        {/* Message History — this is the ONLY element that scrolls */}
        <div
          ref={messagesRef}
          onScroll={onScroll}
          className="flex-1 overflow-y-auto p-4 flex flex-col gap-3"
        >
          {loading ? (
            <div className="text-center text-inksoft text-[12.5px] py-12">
              Loading conversation…
            </div>
          ) : messages.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center py-16 opacity-60">
              <span className="text-[14px] font-semibold text-ink">
                No Messages
              </span>
              <p className="text-[12px] text-inksoft max-w-xs mt-1">
                Start the conversation by typing a message below.
              </p>
            </div>
          ) : (
            messages.map((msg) => {
              const isMe = msg.senderId === userId

              return (
                <div
                  key={msg.id}
                  className={`flex flex-col max-w-[75%] ${isMe
                    ? 'self-end items-end'
                    : 'self-start items-start'
                    }`}
                >
                  <div className="flex items-center gap-1.5 mb-1">
                    <span className="text-[11px] font-semibold text-ink leading-none">
                      {msg.senderName}
                    </span>

                    <span className="text-[9.5px] text-inksoft mono leading-none">
                      {formatDate(msg.timestamp)}
                    </span>
                  </div>

                  <div
                    className={`p-2.5 rounded text-[12.5px] whitespace-pre-wrap leading-relaxed ${isMe
                      ? 'bg-amber text-ink'
                      : 'bg-bg text-ink border border-line'
                      }`}
                  >
                    {msg.content}
                  </div>
                </div>
              )
            })
          )}
        </div>

        {/* Chat Input */}
        <form
          onSubmit={onSend}
          className="p-3 border-t border-line bg-bg flex gap-2 shrink-0"
        >
          <input
            type="text"
            className="field-input flex-1 !py-2"
            placeholder="Type a message to project team…"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            disabled={sending}
          />

          <button
            type="submit"
            className="btn icon-only"
            disabled={sending || !content.trim()}
          >
            <Send size={15} />
          </button>
        </form>
      </div>

      {/* Team Members Sidebar */}
      <div className="flex flex-col overflow-hidden bg-bg">
        <div className="p-3 border-b border-line flex items-center gap-2 shrink-0">
          <Users size={15} />
          <span className="text-[13px] font-bold">Project Team</span>
        </div>

        <div className="overflow-y-auto flex-1 divide-y divide-line">
          {team.length === 0 ? (
            <div className="p-4 text-center text-[12px] text-inksoft">
              No members assigned to this team.
            </div>
          ) : (
            team.map((member) => (
              <button
                key={member.employeeId}
                onClick={() => setSelectedMember(member)}
                className="w-full flex items-center gap-3 p-3 text-left hover:bg-card transition-colors focus:outline-none"
              >
                <div className="w-8 h-8 rounded bg-ink/5 border border-line flex items-center justify-center shrink-0 text-[12px] font-bold text-ink mono">
                  {getInitials(member.firstName, member.lastName)}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="text-[12.5px] font-semibold text-ink truncate">
                    {member.firstName} {member.lastName}
                  </div>

                  <div className="text-[11px] text-inksoft truncate leading-tight">
                    {member.designation || 'Team Member'}
                  </div>
                </div>
              </button>
            ))
          )}
        </div>

        <Modal
          open={selectedMember !== null}
          title="Team Member Profile"
          onClose={() => setSelectedMember(null)}
          width={360}
        >
          {selectedMember && (
            <div className="flex flex-col items-center text-center p-2">
              <div className="w-16 h-16 rounded bg-ink/5 border border-line flex items-center justify-center text-[20px] font-bold text-ink mono mb-3">
                {getInitials(
                  selectedMember.firstName,
                  selectedMember.lastName
                )}
              </div>

              <h4 className="text-[15px] font-bold m-0 text-ink">
                {selectedMember.firstName} {selectedMember.lastName}
              </h4>

              <p className="text-[12px] text-inksoft mt-0.5 mb-4">
                {selectedMember.designation || 'Team Member'}
              </p>

              <div className="w-full text-left space-y-3.5 border-t border-line pt-4 text-[12.5px]">
                <div className="flex justify-between">
                  <span className="text-inksoft">Department</span>
                  <span className="font-semibold text-ink">
                    {selectedMember.department || 'N/A'}
                  </span>
                </div>

                <div className="flex justify-between">
                  <span className="text-inksoft">Email</span>
                  <span className="font-semibold text-ink mono">
                    {selectedMember.email}
                  </span>
                </div>

                <div className="flex justify-between">
                  <span className="text-inksoft">Employee Code</span>
                  <span className="font-semibold text-ink mono">
                    {selectedMember.employeeCode || 'N/A'}
                  </span>
                </div>
              </div>

              <button
                className="btn ghost w-full mt-6"
                onClick={() => setSelectedMember(null)}
              >
                Close
              </button>
            </div>
          )}
        </Modal>
      </div>
    </div>
  )
}