import React, { useState, useEffect } from 'react'

const SCORE_COLOR = (s) => s >= 0.65 ? 'text-red-400' : s >= 0.45 ? 'text-yellow-400' : 'text-emerald-400'
const LABEL_BG = { FLAGGED: 'bg-red-900 text-red-300', REVIEW: 'bg-yellow-900 text-yellow-300', CLEAN: 'bg-emerald-900 text-emerald-300', PENDING: 'bg-slate-700 text-slate-300' }

export default function ModerationQueue() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')

  const fetchQueue = async () => {
    setLoading(true)
    try {
      const res = await fetch('/api/moderation/queue')
      const data = await res.json()
      setItems(data)
    } catch (e) {
      setMsg('Could not fetch queue. Is the listing-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchQueue() }, [])

  const action = async (id, type) => {
    await fetch(`/api/moderation/${id}/${type}`, { method: 'POST' })
    setMsg(`Item ${type}d`)
    fetchQueue()
  }

  if (loading) return <div className="text-slate-400 py-8 text-center">Loading moderation queue...</div>

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold">Moderation Queue</h2>
        <button onClick={fetchQueue} className="text-xs bg-slate-700 hover:bg-slate-600 px-3 py-1.5 rounded">Refresh</button>
      </div>
      {msg && <div className="mb-3 text-sm text-emerald-400 bg-emerald-900/30 border border-emerald-800 rounded px-3 py-2">{msg}</div>}
      {items.length === 0 ? (
        <div className="text-center py-16 text-slate-400">
          <div className="text-4xl mb-3">✓</div>
          <p>No pending moderation items</p>
        </div>
      ) : (
        <div className="space-y-3">
          {items.map(item => (
            <div key={item.id} className="bg-slate-800 border border-slate-700 rounded-lg p-4">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className={`text-xs px-2 py-0.5 rounded font-medium ${LABEL_BG['FLAGGED']}`}>FLAGGED</span>
                    <span className={`font-mono font-bold ${SCORE_COLOR(item.fraudScore)}`}>
                      {(item.fraudScore * 100).toFixed(1)}% fraud
                    </span>
                  </div>
                  <p className="text-sm font-medium truncate">{item.listing?.title || 'Listing ' + item.id?.slice(0,8)}</p>
                  <p className="text-xs text-slate-400 mt-1">{item.flaggedReason || 'Automated flag'}</p>
                </div>
                <div className="flex gap-2 shrink-0">
                  <button onClick={() => action(item.id, 'approve')} className="text-xs bg-emerald-800 hover:bg-emerald-700 text-emerald-200 px-3 py-1.5 rounded">Approve</button>
                  <button onClick={() => action(item.id, 'reject')} className="text-xs bg-red-900 hover:bg-red-800 text-red-200 px-3 py-1.5 rounded">Reject</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
